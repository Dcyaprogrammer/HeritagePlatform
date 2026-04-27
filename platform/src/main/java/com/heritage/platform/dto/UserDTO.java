package com.heritage.platform.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User Data Transfer Object / 用户数据传输对象
 * Used for API responses to avoid exposing sensitive data like passwords
 * 用于API响应，避免暴露密码等敏感信息
 */
public class UserDTO {

	private Long id;
	private String username;
	private String displayName;
	private String email;
	private String avatar;
	private String bio;
	private Set<String> roles;
	private String contributorStatus;
	private LocalDateTime createdAt;

	// Default constructor / 默认构造器
	public UserDTO() {
	}

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public String getContributorStatus() {
		return contributorStatus;
	}

	public void setContributorStatus(String contributorStatus) {
		this.contributorStatus = contributorStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
