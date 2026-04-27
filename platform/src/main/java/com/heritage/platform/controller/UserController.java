package com.heritage.platform.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heritage.platform.dto.ApiResponse;
import com.heritage.platform.dto.ChangePasswordRequest;
import com.heritage.platform.dto.UserDTO;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.repository.HeritageUserRepository;

/**
 * User Management Controller / 用户管理控制器
 * Provides endpoints for user management and role assignment
 * 提供用户管理和角色分配的接口
 */
@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	private HeritageUserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Get user by username / 根据用户名查询用户
	 * GET /api/user/{username}
	 * Security: Only the user themselves or ADMIN can view user details
	 */
	@GetMapping("/user/{username}")
	@PreAuthorize("#username == authentication.name or hasRole('ADMIN') or hasRole('VIEWER')")
	public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
		Optional<HeritageUser> userOptional = userRepository.findByUsername(username);

		if (userOptional.isPresent()) {
			UserDTO dto = convertToDTO(userOptional.get());
			return ResponseEntity.ok(ApiResponse.success(dto));
		} else {
			return ResponseEntity.status(404)
					.body(ApiResponse.error(404, "User not found: " + username));
		}
	}

	/**
	 * Get user by ID / 根据ID查询用户
	 * GET /api/users/{id}
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/users/{id}")
	public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
		Optional<HeritageUser> userOptional = userRepository.findById(id);

		if (userOptional.isPresent()) {
			UserDTO dto = convertToDTO(userOptional.get());
			return ResponseEntity.ok(ApiResponse.success(dto));
		} else {
			return ResponseEntity.status(404)
					.body(ApiResponse.error(404, "User not found with id: " + id));
		}
	}

	/**
	 * Get all users / 查询所有用户
	 * GET /api/users
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/users")
	public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
		List<HeritageUser> users = userRepository.findAll();
		List<UserDTO> dtoList = users.stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
		return ResponseEntity.ok(ApiResponse.success(dtoList));
	}

	/**
	 * Get paginated user list / 分页查询用户列表
	 * GET /api/users/page?page=0&size=10&role=VIEWER&keyword=zhang
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/users/page")
	public ResponseEntity<ApiResponse<Page<UserDTO>>> getUserPage(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String role,
			@RequestParam(required = false) String keyword) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<HeritageUser> userPage;
		boolean hasKeyword = keyword != null && !keyword.isEmpty();
		boolean hasRole = role != null && !role.isEmpty();

		if (hasKeyword && hasRole) {
			userPage = userRepository.findByUsernameContainingIgnoreCaseAndRolesContaining(keyword, role, pageable);
		} else if (hasKeyword) {
			userPage = userRepository.findByUsernameContainingIgnoreCase(keyword, pageable);
		} else if (hasRole) {
			userPage = userRepository.findByRolesContaining(role, pageable);
		} else {
			userPage = userRepository.findAll(pageable);
		}

		Page<UserDTO> dtoPage = userPage.map(this::convertToDTO);
		return ResponseEntity.ok(ApiResponse.success(dtoPage));
	}

	/**
	 * Update user profile / 更新用户资料
	 * PUT /api/user/{username}
	 * Security: Only the user themselves can update their profile
	 */
	@PutMapping("/user/{username}")
	@PreAuthorize("#username == authentication.name")
	public ResponseEntity<ApiResponse<UserDTO>> updateUser(
			@PathVariable String username,
			@RequestBody Map<String, String> updates) {

		Optional<HeritageUser> userOptional = userRepository.findByUsername(username);

		if (userOptional.isPresent()) {
			HeritageUser user = userOptional.get();

			if (updates.containsKey("displayName")) {
				user.setDisplayName(updates.get("displayName"));
			}
			if (updates.containsKey("email")) {
				user.setEmail(updates.get("email"));
			}
			if (updates.containsKey("avatar")) {
				user.setAvatar(updates.get("avatar"));
			}
			if (updates.containsKey("bio")) {
				user.setBio(updates.get("bio"));
			}

			HeritageUser updatedUser = userRepository.save(user);
			return ResponseEntity.ok(ApiResponse.success(convertToDTO(updatedUser)));
		} else {
			return ResponseEntity.status(404)
					.body(ApiResponse.error(404, "User not found: " + username));
		}
	}

	/**
	 * Update user role (Admin only) / 更新用户角色（仅管理员）
	 * PUT /api/admin/users/{userId}/role?role=CONTRIBUTOR
	 * Security: Only ADMIN can update user roles
	 */
	@PutMapping("/admin/users/{userId}/role")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserDTO>> updateUserRole(
			@PathVariable Long userId,
			@RequestParam String role) {

		Optional<HeritageUser> userOptional = userRepository.findById(userId);

		if (!userOptional.isPresent()) {
			return ResponseEntity.status(404)
					.body(ApiResponse.error(404, "User not found with id: " + userId));
		}

		HeritageUser user = userOptional.get();
		Set<String> roles = user.getRoles();

		switch (role.toUpperCase()) {
		case "ADMIN":
			roles.clear();
			roles.add("ADMIN");
			break;
		case "CONTRIBUTOR":
			roles.clear();
			roles.add("VIEWER");
			roles.add("CONTRIBUTOR");
			user.setContributorStatus("APPROVED");
			break;
		case "VIEWER":
			roles.clear();
			roles.add("VIEWER");
			user.setContributorStatus("NONE");
			break;
		default:
			return ResponseEntity.badRequest()
					.body(ApiResponse.error(400, "Invalid role. Must be: ADMIN, CONTRIBUTOR, or VIEWER"));
		}

		user.setRoles(roles);
		HeritageUser updatedUser = userRepository.save(user);
		return ResponseEntity.ok(ApiResponse.success(convertToDTO(updatedUser)));
	}

	private UserDTO convertToDTO(HeritageUser user) {
		UserDTO dto = new UserDTO();
		dto.setId(user.getId());
		dto.setUsername(user.getUsername());
		dto.setDisplayName(user.getDisplayName());
		dto.setEmail(user.getEmail());
		dto.setAvatar(user.getAvatar());
		dto.setBio(user.getBio());
		dto.setRoles(new HashSet<>(user.getRoles()));
		dto.setContributorStatus(user.getContributorStatus());
		dto.setCreatedAt(user.getCreatedAt());
		return dto;
	}

	@PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
	@PutMapping("/user/{username}/password")
	public ResponseEntity<ApiResponse<?>> changePassword(
			@PathVariable String username,
			@RequestBody ChangePasswordRequest request) {

		Optional<HeritageUser> userOptional = userRepository.findByUsername(username);

		if (!userOptional.isPresent()) {
			return ResponseEntity.status(404)
					.body(ApiResponse.error(404, "User not found: " + username));
		}

		HeritageUser user = userOptional.get();

		if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.error(400, "Old password is incorrect"));
		}

		user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);

		return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
	}
}
