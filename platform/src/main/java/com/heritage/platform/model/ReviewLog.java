//id, resource_id, reviewer_id, action, reason, operated_at
package com.heritage.platform.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "review_logs")
public class ReviewLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "resource_id", nullable = false)
	private HeritageResource resource;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reviewer_id", nullable = false)
	private HeritageUser reviewer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private ReviewAction action;

	@Column(length = 4000)
	private String reason;

	@Column(nullable = false)
	private Instant operatedAt;

	public Long getId() {
		return id;
	}

	public HeritageResource getResource() {
		return resource;
	}

	public void setResource(HeritageResource resource) {
		this.resource = resource;
	}

	public HeritageUser getReviewer() {
		return reviewer;
	}

	public void setReviewer(HeritageUser reviewer) {
		this.reviewer = reviewer;
	}

	public ReviewAction getAction() {
		return action;
	}

	public void setAction(ReviewAction action) {
		this.action = action;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Instant getOperatedAt() {
		return operatedAt;
	}

	public void setOperatedAt(Instant operatedAt) {
		this.operatedAt = operatedAt;
	}
}
