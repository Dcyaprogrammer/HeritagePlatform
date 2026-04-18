package com.heritage.platform.controller;

import com.heritage.platform.common.ApiResponse;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.ResourceStatus;
import com.heritage.platform.model.ReviewAction;
import com.heritage.platform.model.ReviewLog;
import com.heritage.platform.repository.HeritageResourceRepository;
import com.heritage.platform.repository.ReviewLogRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

	@Autowired
	private ReviewLogRepository reviewLogRepository;

	@Autowired
	private HeritageResourceRepository heritageResourceRepository;

	@GetMapping("/submissions")
	public ApiResponse<List<Map<String, Object>>> getMySubmissions(Authentication authentication) {
		String username = requireUsername(authentication);
		List<HeritageResource> resources = heritageResourceRepository.findBySubmitterUsernameOrderBySubmittedAtDesc(username);
		List<Map<String, Object>> data = new ArrayList<>();

		for (HeritageResource resource : resources) {
			boolean rejected = resource.getStatus() == ResourceStatus.Rejected;
			data.add(Map.of(
					"id", resource.getId(),
					"title", resource.getTitle(),
					"category", resource.getCategory(),
					"status", resource.getStatus().toString(),
					"submittedAt", resource.getSubmittedAt().toString(),
					"canViewFeedback", rejected,
					"canResubmit", rejected));
		}

		return ApiResponse.success(data);
	}

	@GetMapping("/{id}/feedback")
	public ApiResponse<Map<String, String>> getFeedback(@PathVariable Long id, Authentication authentication) {
		HeritageResource resource = requireOwnedResource(id, authentication);
		Optional<ReviewLog> log = reviewLogRepository.findFirstByResourceIdAndActionOrderByOperatedAtDesc(
				resource.getId(),
				ReviewAction.REJECTED);

		if (log.isEmpty()) {
			throw new RuntimeException("No rejection feedback found");
		}

		ReviewLog reviewLog = log.get();
		return ApiResponse.success(Map.of(
				"reason", reviewLog.getReason() == null ? "" : reviewLog.getReason(),
				"operatedAt", reviewLog.getOperatedAt().toString()));
	}

	@PostMapping("/{id}/resubmit")
	@Transactional
	public ApiResponse<Map<String, String>> resubmit(@PathVariable Long id, Authentication authentication) {
		HeritageResource resource = requireOwnedResource(id, authentication);
		String username = requireUsername(authentication);

		if (resource.getStatus() != ResourceStatus.Rejected) {
			throw new RuntimeException("Only rejected resources can be resubmitted");
		}

		Instant submittedAt = Instant.now();
		int updated = heritageResourceRepository.resubmitRejectedResource(
				resource.getId(),
				username,
				ResourceStatus.Rejected,
				ResourceStatus.Pending,
				submittedAt);

		if (updated == 0) {
			throw new RuntimeException("Resubmit failed because the resource state changed");
		}

		return ApiResponse.success(Map.of(
				"newStatus", ResourceStatus.Pending.toString(),
				"submittedAt", submittedAt.toString()));
	}

	@GetMapping("/{id}/history")
	public ApiResponse<List<Map<String, String>>> getHistory(@PathVariable Long id, Authentication authentication) {
		HeritageResource resource = requireOwnedResource(id, authentication);
		List<ReviewLog> logs = reviewLogRepository.findByResourceIdOrderByOperatedAtDesc(resource.getId());
		List<Map<String, String>> data = new ArrayList<>();

		for (ReviewLog log : logs) {
			data.add(Map.of(
					"action", log.getAction().toString(),
					"reason", log.getReason() == null ? "" : log.getReason(),
					"operatedAt", log.getOperatedAt().toString()));
		}

		return ApiResponse.success(data);
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
