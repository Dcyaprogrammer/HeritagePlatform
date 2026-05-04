package com.heritage.platform.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.ReviewAction;
import com.heritage.platform.model.ReviewLog;
import com.heritage.platform.model.ResourceStatus;
import com.heritage.platform.repository.HeritageResourceRepository;
import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.repository.ReviewLogRepository;
import com.heritage.platform.web.ResourceDetail;
import com.heritage.platform.web.ReviewController.PendingItem;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final HeritageResourceRepository resources;
    private final HeritageUserRepository users;
    private final ReviewLogRepository reviewLogs;

    public ReviewService(HeritageResourceRepository resources, HeritageUserRepository users, ReviewLogRepository reviewLogs) {
        this.resources = resources;
        this.users = users;
        this.reviewLogs = reviewLogs;
    }

    @Transactional(readOnly = true)
    public List<PendingItem> listPending() {
        Instant staleThreshold = Instant.now().minus(3, ChronoUnit.DAYS);

        List<PendingItem> stale = new ArrayList<>();
        List<PendingItem> normal = new ArrayList<>();

        List<HeritageResource> pending = resources.findByStatusOrderBySubmittedAtDesc(ResourceStatus.PENDING_REVIEW);

        for(HeritageResource r : pending){
            boolean isStale = r.getSubmittedAt() != null && r.getSubmittedAt().isBefore(staleThreshold);
            PendingItem p = toPendingItem(r, isStale);
            if(isStale){
                stale.add(p);
            } else {
                normal.add(p);
            }
        }

        stale.addAll(normal);
        return stale;
    }
    
    private static PendingItem toPendingItem(HeritageResource resource, boolean stale) {
        PendingItem p = new PendingItem();
        p.id = resource.getId();
        p.title = resource.getTitle();
        if (resource.getSubmitter() != null) {
            p.submitterName = resource.getSubmitter().getUsername(); 
        } else {
            p.submitterName = "Unknown";
        }
        p.submittedAt = resource.getSubmittedAt();
        p.category = resource.getCategory();
        p.version = resource.getVersion();
        p.rejectionReason = resource.getRejectionReason();
        p.stale = stale;
        return p;
    }

    @Transactional(readOnly = true)
    public ResourceDetail getDetail(Long id) {
        HeritageResource r = resources.findById(id).orElse(null);
        if(r == null){
            return null;
        }

        ResourceDetail d = new ResourceDetail();
        d.id = r.getId();
        d.title = r.getTitle();
        d.category = r.getCategory();
        d.locationName = r.getLocationName();
        d.description = r.getDescription();
        d.copyrightDeclaration = r.getCopyrightDeclaration();
        d.submittedAt = r.getSubmittedAt();
        d.version = r.getVersion();
        d.status = r.getStatus().name();
        d.rejectionReason = r.getRejectionReason();
        if (r.getSubmitter() != null) {
            d.submitterName = r.getSubmitter().getDisplayName() != null
                    ? r.getSubmitter().getDisplayName()
                    : r.getSubmitter().getUsername();
        }

        List<Map<String, Object>> tags = new ArrayList<>();
        r.getTags().forEach(tag -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", tag.getId());
            item.put("name", tag.getName());
            tags.add(item);
        });
        d.tags = tags;

        List<Map<String, Object>> attachments = new ArrayList<>();
        r.getAttachments().forEach(attachment -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", attachment.getId());
            item.put("display_name", attachment.getDisplayName());
            item.put("stored_name", attachment.getStoredName());
            item.put("file_path", attachment.getFilePath());
            item.put("file_type", attachment.getFileType());
            item.put("file_size", attachment.getFileSize());
            item.put("displayName", attachment.getDisplayName());
            item.put("storedName", attachment.getStoredName());
            item.put("filePath", attachment.getFilePath());
            item.put("fileType", attachment.getFileType());
            item.put("fileSize", attachment.getFileSize());
            if (attachment.getThumbnailPath() != null) {
                item.put("thumbnailUrl", "/api/attachments/" + attachment.getId() + "/thumbnail");
            }
            attachments.add(item);
        });
        d.attachments = attachments;


        return d;
    }
        
    public static class ConflictException extends RuntimeException {
        public ConflictException(String msg) {super(msg);}
    }

    @Transactional
    public void approve(Long id, Long version, String adminUsername) {
        HeritageResource r = resources.findById(id).orElseThrow(() -> new NoSuchElementException("Resource not found"));
        if(r.getStatus() != ResourceStatus.PENDING_REVIEW){
            throw new IllegalArgumentException("not pending");
        }

        if (version == null || !version.equals(r.getVersion())) {
            throw new ConflictException("Version mismatch");
        }

        HeritageUser reviewer = adminUsername == null ? null : users.findByUsername(adminUsername).orElse(null);

        r.setStatus(ResourceStatus.APPROVED);
        r.setReviewedAt(Instant.now());
        r.setReviewedBy(reviewer);
        r.setRejectionReason(null);

        resources.save(r);
        writeReviewLog(r, reviewer, ReviewAction.APPROVED, null);
    }

    @Transactional
    public void reject(Long id, Long version, String adminUsername, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("reason is required");
        }

        HeritageResource r = resources.findById(id).orElseThrow(() -> new NoSuchElementException("Resource not found"));

        if(r.getStatus() != ResourceStatus.PENDING_REVIEW){
            throw new IllegalArgumentException("not pending");
        }

        if (version == null || !version.equals(r.getVersion())) {
            throw new ConflictException("Version mismatch");
        }
        
        HeritageUser reviewer = adminUsername == null ? null : users.findByUsername(adminUsername).orElse(null);

        r.setStatus(ResourceStatus.REJECTED);
        r.setReviewedAt(Instant.now());
        r.setReviewedBy(reviewer);
        r.setRejectionReason(reason.trim());

        resources.save(r);
        writeReviewLog(r, reviewer, ReviewAction.REJECTED, reason.trim());
    }

    private void writeReviewLog(HeritageResource resource, HeritageUser reviewer, ReviewAction action, String reason) {
        if (reviewer == null) {
            return;
        }

        ReviewLog log = new ReviewLog();
        log.setResource(resource);
        log.setReviewer(reviewer);
        log.setAction(action);
        log.setReason(reason);
        log.setOperatedAt(resource.getReviewedAt() == null ? Instant.now() : resource.getReviewedAt());
        reviewLogs.save(log);
    }
}
