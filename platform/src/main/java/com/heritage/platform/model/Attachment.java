package com.heritage.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "resource_id")
    private Long resourceId;  // associated resource ID (unused for now, FK will be enabled after integration with team member 4)

    @Column(name = "stored_name")
    private String storedName;   
    
    @Column(name = "display_name")
    private String displayName; 
    
    @Column(name = "file_path")
    private String filePath;  
    
    @Column(name = "file_type")
    private String fileType; 
    
    @Column(name = "file_size")
    private Long fileSize;     
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    
    public Attachment() {}


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    
    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}