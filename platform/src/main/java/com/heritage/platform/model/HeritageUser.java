package com.heritage.platform.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "heritage_users")
public class HeritageUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64)
	private String username;

	@Column(nullable = false, length = 120)
	private String passwordHash;

	@Column(nullable = false, length = 120)
	private String displayName;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "heritage_user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "role", length = 32)
	private Set<String> roles = new HashSet<>();

	// Extended fields for user management / 用户管理扩展字段

	@Column(length = 100)
	private String email;

	@Column(length = 500)
	private String avatar;

	@Column(columnDefinition = "TEXT")
	private String bio;

	@Column(name = "contributor_status", length = 20)
	private String contributorStatus = "NONE";

	@Column(name = "contributor_reason", columnDefinition = "TEXT")
	private String contributorReason;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	// Getters and Setters for extended fields

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

	public String getContributorStatus() {
		return contributorStatus;
	}

	public void setContributorStatus(String contributorStatus) {
		this.contributorStatus = contributorStatus;
	}

	public String getContributorReason() {
		return contributorReason;
	}

	public void setContributorReason(String contributorReason) {
		this.contributorReason = contributorReason;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
}
