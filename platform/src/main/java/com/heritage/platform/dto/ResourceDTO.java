package com.heritage.platform.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ResourceDTO {

    private Long id;
    private String title;
    private String description;
    private String locationName;
    private String heritageTypeCode;
    private String category;
    private Integer categoryId;
    private String copyrightDeclaration;
    
    private String status;
    private Long submitterId;
    private String submitterName;
    
    private LocalDateTime submittedAt;
    private String rejectionReason;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // We can return a simple list of Map or a dedicated TagDTO/AttachmentDTO
    private List<Map<String, Object>> tags;
    private List<Map<String, Object>> attachments;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getHeritageTypeCode() { return heritageTypeCode; }
    public void setHeritageTypeCode(String heritageTypeCode) { this.heritageTypeCode = heritageTypeCode; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCopyrightDeclaration() { return copyrightDeclaration; }
    public void setCopyrightDeclaration(String copyrightDeclaration) { this.copyrightDeclaration = copyrightDeclaration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getSubmitterId() { return submitterId; }
    public void setSubmitterId(Long submitterId) { this.submitterId = submitterId; }

    public String getSubmitterName() { return submitterName; }
    public void setSubmitterName(String submitterName) { this.submitterName = submitterName; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Map<String, Object>> getTags() { return tags; }
    public void setTags(List<Map<String, Object>> tags) { this.tags = tags; }

    public List<Map<String, Object>> getAttachments() { return attachments; }
    public void setAttachments(List<Map<String, Object>> attachments) { this.attachments = attachments; }
}