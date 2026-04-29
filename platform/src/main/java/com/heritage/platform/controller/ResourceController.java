package com.heritage.platform.controller;

import com.heritage.platform.common.ApiResponse;
import com.heritage.platform.dto.ResourceDTO;
import com.heritage.platform.dto.ResourceDraftRequest;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.ResourceStatus;
import com.heritage.platform.model.ReviewAction;
import com.heritage.platform.model.ReviewLog;
import com.heritage.platform.repository.HeritageResourceRepository;
import com.heritage.platform.repository.ReviewLogRepository;
import com.heritage.platform.service.ResourceService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

	@Autowired
	private ReviewLogRepository reviewLogRepository;

	@Autowired
	private HeritageResourceRepository heritageResourceRepository;

	@Autowired
	private ResourceService resourceService;

	@PostMapping
	@PreAuthorize("hasRole('CONTRIBUTOR') or hasRole('ADMIN')")
	public ApiResponse<ResourceDTO> createDraft(@RequestBody ResourceDraftRequest request, Authentication authentication) {
		String username = authentication.getName();
		ResourceDTO dto = resourceService.createDraft(request, username);
		return ApiResponse.success(dto);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('CONTRIBUTOR') or hasRole('ADMIN')")
	public ApiResponse<ResourceDTO> getOwnedResource(@PathVariable Long id, Authentication authentication) {
		String username = authentication.getName();
		ResourceDTO dto = resourceService.getOwnedResource(id, username);
		return ApiResponse.success(dto);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('CONTRIBUTOR') or hasRole('ADMIN')")
	public ApiResponse<ResourceDTO> updateDraft(@PathVariable Long id, @RequestBody ResourceDraftRequest request, Authentication authentication) {
		String username = authentication.getName();
		ResourceDTO dto = resourceService.updateDraft(id, request, username);
		return ApiResponse.success(dto);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('CONTRIBUTOR') or hasRole('ADMIN')")
	public ApiResponse<Void> deleteOwnedResource(@PathVariable Long id, Authentication authentication) {
		String username = authentication.getName();
		resourceService.deleteOwnedResource(id, username);
		return ApiResponse.success("Resource deleted successfully", null);
	}

	@PostMapping("/{id}/submit")
	@PreAuthorize("hasRole('CONTRIBUTOR') or hasRole('ADMIN')")
	public ApiResponse<ResourceDTO> submitForReview(@PathVariable Long id, Authentication authentication) {
		String username = authentication.getName();
		ResourceDTO dto = resourceService.submitForReview(id, username);
		return ApiResponse.success(dto);
	}


	@GetMapping("/submissions")
	public ApiResponse<List<Map<String, Object>>> getMySubmissions(Authentication authentication) {
		String username = requireUsername(authentication);
		List<HeritageResource> resources = heritageResourceRepository.findBySubmitterUsernameOrderBySubmittedAtDesc(username);
		List<Map<String, Object>> data = new ArrayList<>();

		for (HeritageResource resource : resources) {
			boolean rejected = resource.getStatus() == ResourceStatus.REJECTED;
			data.add(Map.of(
					"id", resource.getId(),
					"title", resource.getTitle() == null ? "" : resource.getTitle(),
					"category", resource.getCategory() == null ? "" : resource.getCategory(),
					"status", resource.getStatus() == null ? "" : resource.getStatus().toString(),
					"submittedAt", resource.getSubmittedAt() == null ? "" : resource.getSubmittedAt().toString(),
					"canViewFeedback", rejected,
					"canResubmit", rejected));
		}

		return ApiResponse.success(data);
	}

	@GetMapping("/{id}/feedback")
	public ApiResponse<Map<String, String>> getFeedback(@PathVariable Long id, Authentication authentication) {
		HeritageResource resource = requireOwnedResource(id, authentication);
		ReviewLog reviewLog = findLatestRejectedLog(resource.getId()).orElse(null);
		String reason = reviewLog != null ? reviewLog.getReason() : resource.getRejectionReason();
		Instant operatedAt = reviewLog != null ? reviewLog.getOperatedAt() : resource.getReviewedAt();

		if ((reason == null || reason.isBlank()) && operatedAt == null) {
			return ApiResponse.success(null);
		}

		return ApiResponse.success(Map.of(
				"reason", reason == null ? "" : reason,
				"operatedAt", operatedAt == null ? "" : operatedAt.toString()));
	}

	@PostMapping("/{id}/resubmit")
	@Transactional
	public ApiResponse<Map<String, String>> resubmit(@PathVariable Long id, Authentication authentication) {
		HeritageResource resource = requireOwnedResource(id, authentication);
		String username = requireUsername(authentication);

		if (resource.getStatus() != ResourceStatus.REJECTED) {
			throw new RuntimeException("Only rejected resources can be resubmitted");
		}

		Instant submittedAt = Instant.now();
		int updated = heritageResourceRepository.resubmitRejectedResource(
				resource.getId(),
				username,
				ResourceStatus.REJECTED,
				ResourceStatus.PENDING_REVIEW,
				submittedAt);

		if (updated == 0) {
			throw new RuntimeException("Resubmit failed because the resource state changed");
		}

		return ApiResponse.success(Map.of(
				"newStatus", ResourceStatus.PENDING_REVIEW.toString(),
				"submittedAt", submittedAt.toString()));
	}

	@GetMapping("/{id}/history")
	public ApiResponse<List<Map<String, String>>> getHistory(@PathVariable Long id, Authentication authentication) {
		HeritageResource resource = requireOwnedResource(id, authentication);
		List<ReviewLog> logs = reviewLogRepository.findByResourceIdOrderByOperatedAtDesc(resource.getId());
		List<Map<String, String>> data = new ArrayList<>();

		for (ReviewLog log : logs) {
			data.add(Map.of(
					"action", log.getAction().toDisplayValue(),
					"reason", log.getReason() == null ? "" : log.getReason(),
					"operatedAt", log.getOperatedAt().toString()));
		}

		if (data.isEmpty() && (resource.getReviewedAt() != null || resource.getRejectionReason() != null)) {
			data.add(Map.of(
					"action", resource.getStatus() == ResourceStatus.APPROVED ? "APPROVED" : "REJECTED",
					"reason", resource.getRejectionReason() == null ? "" : resource.getRejectionReason(),
					"operatedAt", resource.getReviewedAt() == null ? "" : resource.getReviewedAt().toString()));
		}

		return ApiResponse.success(data);
	}

	private Optional<ReviewLog> findLatestRejectedLog(Long resourceId) {
		return reviewLogRepository.findByResourceIdOrderByOperatedAtDesc(resourceId).stream()
				.filter(log -> log.getAction() != null && log.getAction().isRejected())
				.findFirst();
	}

	private HeritageResource requireOwnedResource(Long id, Authentication authentication) {
		String username = requireUsername(authentication);
		return heritageResourceRepository.findByIdAndSubmitterUsername(id, username)
				.orElseThrow(() -> new RuntimeException("Resource not found"));
	}

	private String requireUsername(Authentication authentication) {
		if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
			throw new RuntimeException("User is not authenticated");
		}
		return authentication.getName();
	}
}
