//id, title, category, submitted_at, status, version, submitter_id, reviewed_by_id, reviewed_at, rejection_reason
package com.heritage.platform.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "resources")
public class HeritageResource {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 500)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "location_name", length = 255)
	private String locationName;

	@Column(name = "heritage_type_code", length = 64)
	private String heritageTypeCode;

	@Column(length = 120)
	private String category;

	@Column(name = "category_id")
	private Integer categoryId;

	@Column(name = "copyright_declaration", length = 255)
	private String copyrightDeclaration;

	@Column(name = "submitted_at")
	private Instant submittedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ResourceStatus status = ResourceStatus.DRAFT;

	@Version
	private Long version;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contributor_id")
	private HeritageUser submitter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "submitter_id", insertable = false, updatable = false)
	private HeritageUser legacySubmitter; // For backward compatibility with schema

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewed_by_id")
	private HeritageUser reviewedBy;

	@Column(name = "reviewed_at")
	private Instant reviewedAt;

	@Column(name = "rejection_reason", length = 4000)
	private String rejectionReason;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(
		name = "resource_tags",
		joinColumns = @JoinColumn(name = "resource_id"),
		inverseJoinColumns = @JoinColumn(name = "tag_id")
	)
	private Set<Tag> tags = new HashSet<>();

	@OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Attachment> attachments = new HashSet<>();

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now();
		updatedAt = Instant.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getHeritageTypeCode() {
		return heritageTypeCode;
	}

	public void setHeritageTypeCode(String heritageTypeCode) {
		this.heritageTypeCode = heritageTypeCode;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getCopyrightDeclaration() {
		return copyrightDeclaration;
	}

	public void setCopyrightDeclaration(String copyrightDeclaration) {
		this.copyrightDeclaration = copyrightDeclaration;
	}

	public Instant getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(Instant submittedAt) {
		this.submittedAt = submittedAt;
	}

	public ResourceStatus getStatus() {
		return status;
	}

	public void setStatus(ResourceStatus status) {
		this.status = status;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public HeritageUser getSubmitter() {
		return submitter;
	}

	public void setSubmitter(HeritageUser submitter) {
		this.submitter = submitter;
	}

	public HeritageUser getReviewedBy() {
		return reviewedBy;
	}

	public void setReviewedBy(HeritageUser reviewedBy) {
		this.reviewedBy = reviewedBy;
	}

	public Instant getReviewedAt() {
		return reviewedAt;
	}

	public void setReviewedAt(Instant reviewedAt) {
		this.reviewedAt = reviewedAt;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Set<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(Set<Attachment> attachments) {
		this.attachments = attachments;
	}

	public void addTag(Tag tag) {
		this.tags.add(tag);
		tag.getResources().add(this);
	}

	public void removeTag(Tag tag) {
		this.tags.remove(tag);
		tag.getResources().remove(this);
	}
}
