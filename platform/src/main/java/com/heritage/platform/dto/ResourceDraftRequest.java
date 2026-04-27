package com.heritage.platform.dto;
import java.util.List;

public class ResourceDraftRequest {

    private String title;

    private String description;

    private String locationName;

    private String heritageTypeCode;

    private String category;

    private Integer categoryId;

    private String copyrightDeclaration;
    // record modification version and prevent concurrency issue
    private Long version;

    private List<Long> tagIds;

    private List<Long> attachmentIds;

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

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }

    public List<Long> getAttachmentIds() { return attachmentIds; }
    public void setAttachmentIds(List<Long> attachmentIds) { this.attachmentIds = attachmentIds; }
}