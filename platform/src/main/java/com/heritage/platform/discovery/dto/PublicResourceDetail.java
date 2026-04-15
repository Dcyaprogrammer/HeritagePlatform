package com.heritage.platform.discovery.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PublicResourceDetail {

	private Long id;
	private String title;
	private String description;
	private String locationName;
	private Integer categoryId;
	private String categoryName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<NamedRow> tags;

	private String dynastyCode;
	private String dynastyName;
	private LocalDate eraStart;
	private LocalDate eraEnd;
	private String provinceCode;
	private String provinceName;
	private String heritageTypeCode;
	private String heritageTypeLabel;

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

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<NamedRow> getTags() {
		return tags;
	}

	public void setTags(List<NamedRow> tags) {
		this.tags = tags;
	}

	public String getDynastyCode() {
		return dynastyCode;
	}

	public void setDynastyCode(String dynastyCode) {
		this.dynastyCode = dynastyCode;
	}

	public String getDynastyName() {
		return dynastyName;
	}

	public void setDynastyName(String dynastyName) {
		this.dynastyName = dynastyName;
	}

	public LocalDate getEraStart() {
		return eraStart;
	}

	public void setEraStart(LocalDate eraStart) {
		this.eraStart = eraStart;
	}

	public LocalDate getEraEnd() {
		return eraEnd;
	}

	public void setEraEnd(LocalDate eraEnd) {
		this.eraEnd = eraEnd;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getHeritageTypeCode() {
		return heritageTypeCode;
	}

	public void setHeritageTypeCode(String heritageTypeCode) {
		this.heritageTypeCode = heritageTypeCode;
	}

	public String getHeritageTypeLabel() {
		return heritageTypeLabel;
	}

	public void setHeritageTypeLabel(String heritageTypeLabel) {
		this.heritageTypeLabel = heritageTypeLabel;
	}
}
