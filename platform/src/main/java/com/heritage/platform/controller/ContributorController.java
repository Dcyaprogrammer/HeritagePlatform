package com.heritage.platform.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heritage.platform.dto.ApiResponse;
import com.heritage.platform.dto.UserDTO;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.repository.HeritageUserRepository;

/**
 * Contributor Application Controller / 贡献者申请控制器
 * Handles contributor applications and approvals
 * 处理贡献者申请和审批
 */
@RestController
@RequestMapping("/api")
public class ContributorController {

	@Autowired
	private HeritageUserRepository userRepository;

	/**
	 * Get pending contributor applications / 获取待审批的申请列表
	 * GET /api/users/pending
	 */
	@GetMapping("/users/pending")
	public ResponseEntity<ApiResponse<List<UserDTO>>> getPendingApplications() {
		List<HeritageUser> pendingUsers = userRepository.findByContributorStatus("PENDING");

		List<UserDTO> dtoList = pendingUsers.stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.success(dtoList));
	}

	/**
	 * Apply to be a contributor / 申请成为贡献者
	 * POST /api/user/{username}/apply
	 */
	@PostMapping("/user/{username}/apply")
	public ResponseEntity<ApiResponse<UserDTO>> applyForContributor(
			@PathVariable String username,
			@RequestBody Map<String, String> request) {

		Optional<HeritageUser> userOptional = userRepository.findByUsername(username);

		if (!userOptional.isPresent()) {
			return ResponseEntity.status(404)
					.body(ApiResponse.error(404, "User not found: " + username));
		}

		HeritageUser user = userOptional.get();

		// Check if already a contributor / 检查是否已经是贡献者
		if (user.getRoles().contains("CONTRIBUTOR")) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.error(400, "User is already a contributor"));
		}

		// Check if already pending / 检查是否已在申请中
		if ("PENDING".equals(user.getContributorStatus())) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.error(400, "Application already pending"));
		}

		// Update status and reason / 更新状态和申请理由
		user.setContributorStatus("PENDING");
		user.setContributorReason(request.getOrDefault("reason", ""));

		HeritageUser updatedUser = userRepository.save(user);
		return ResponseEntity.ok(ApiResponse.success(convertToDTO(updatedUser)));
	}

	/**
	 * Approve contributor application / 批准贡献者申请
	 * PUT /api/user/{username}/approve
	 */
	@PutMapping("/user/{username}/approve")
	public ResponseEntity<ApiResponse<UserDTO>> approveContributor(@PathVariable String username) {
		Optional<HeritageUser> userOptional = userRepository.findByUsername(username);

		if (!userOptional.isPresent()) {
			return ResponseEntity.status(404)
					.body(ApiResponse.error(404, "User not found: " + username));
		}

		HeritageUser user = userOptional.get();

		// Check if status is pending / 检查状态是否为待审批
		if (!"PENDING".equals(user.getContributorStatus())) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.error(400, "No pending application found for this user"));
		}

		// Update role and status / 更新角色和状态
		user.getRoles().clear();
		user.getRoles().add("VIEWER");
		user.getRoles().add("CONTRIBUTOR");
		user.setContributorStatus("APPROVED");

		HeritageUser updatedUser = userRepository.save(user);
		return ResponseEntity.ok(ApiResponse.success(convertToDTO(updatedUser)));
	}

	/**
	 * Reject contributor application / 拒绝贡献者申请
	 * PUT /api/user/{username}/reject
	 */
	@PutMapping("/user/{username}/reject")
	public ResponseEntity<ApiResponse<UserDTO>> rejectContributor(@PathVariable String username) {
		Optional<HeritageUser> userOptional = userRepository.findByUsername(username);

		if (!userOptional.isPresent()) {
			return ResponseEntity.status(404)
					.body(ApiResponse.error(404, "User not found: " + username));
		}

		HeritageUser user = userOptional.get();

		// Check if status is pending / 检查状态是否为待审批
		if (!"PENDING".equals(user.getContributorStatus())) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.error(400, "No pending application found for this user"));
		}

		// Update status only (role stays as VIEWER) / 只更新状态（角色保持VIEWER）
		user.getRoles().clear();
		user.getRoles().add("VIEWER");
		user.setContributorStatus("REJECTED");

		HeritageUser updatedUser = userRepository.save(user);
		return ResponseEntity.ok(ApiResponse.success(convertToDTO(updatedUser)));
	}

	/**
	 * Convert HeritageUser entity to UserDTO
	 * 将HeritageUser实体转换为UserDTO
	 */
	private UserDTO convertToDTO(HeritageUser user) {
		UserDTO dto = new UserDTO();
		dto.setId(user.getId());
		dto.setUsername(user.getUsername());
		dto.setDisplayName(user.getDisplayName());
		dto.setEmail(user.getEmail());
		dto.setAvatar(user.getAvatar());
		dto.setBio(user.getBio());
		dto.setRoles(new java.util.HashSet<>(user.getRoles()));
		dto.setContributorStatus(user.getContributorStatus());
		dto.setCreatedAt(user.getCreatedAt());
		return dto;
	}
}
