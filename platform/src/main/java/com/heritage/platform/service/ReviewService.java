package com.heritage.platform.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.repository.HeritageResourceRepository;
import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.web.ResourceDetail;
import com.heritage.platform.web.ReviewController.PendingItem;
import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.model.ResourceStatus;
import jakarta.transaction.Transactional;

@Service
public class ReviewService {
    private final HeritageResourceRepository resources;
    private final HeritageUserRepository users;

    public ReviewService(HeritageResourceRepository resources, HeritageUserRepository users) {
        this.resources = resources;
        this.users = users;
    }

    public List<PendingItem> listPending() {
        Instant staleThreshold = Instant.now().minus(3, ChronoUnit.DAYS);

        List<PendingItem> stale = new ArrayList<>();
        List<PendingItem> normal = new ArrayList<>();

        List<HeritageResource> pending = resources.findByStatusOrderBySubmittedAtDesc(ResourceStatus.Pending);

        for(HeritageResource r : pending){
            boolean isStale = r.getSubmittedAt().isBefore(staleThreshold);
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

    public ResourceDetail getDetail(Long id) {
        HeritageResource r = resources.findById(id).orElse(null);
        if(r == null){
            return null;
        }

        ResourceDetail d = new ResourceDetail();
        d.id = r.getId();
        d.title = r.getTitle();
        d.category = r.getCategory();
        d.submittedAt = r.getSubmittedAt();
        d.version = r.getVersion();
        d.status = r.getStatus().name();
        d.rejectionReason = r.getRejectionReason();


        return d;
    }
        
    public static class ConflictException extends RuntimeException {
        public ConflictException(String msg) {super(msg);}
    }

    @Transactional
    public void approve(Long id, Long version,String reviewerName) {
        HeritageResource r = resources.findById(id).orElseThrow(() -> new ConflictException("Resource not found"));
        if(r.getStatus() != ResourceStatus.Pending){
            throw new IllegalArgumentException("not pending");
        }

        if(version == null || version != r.getVersion()){
            throw new ConflictException("Version mismatch");
        }

        HeritageUser reviewer = users.findByUsername(reviewerName).orElse(null);

        r.setStatus(ResourceStatus.Approved);
        r.setReviewedAt(Instant.now());
        r.setReviewedBy(reviewer);
        r.setRejectionReason(null);

        resources.save(r);
    }

    @Transactional
    public void reject(Long id, Long version,String reviewerName,String reason) {
        if(reason == null || reason.isEmpty()){
            throw new IllegalArgumentException("reason is required");
        }

        HeritageResource r = resources.findById(id).orElseThrow(() -> new ConflictException("Resource not found"));

        if(r.getStatus() != ResourceStatus.Pending){
            throw new IllegalArgumentException("not pending");
        }

        if(version == null || version != r.getVersion()){
            throw new ConflictException("Version mismatch");
        }
        
        HeritageUser reviewer = users.findByUsername(reviewerName).orElse(null);

        r.setStatus(ResourceStatus.Rejected);
        r.setReviewedAt(Instant.now());
        r.setReviewedBy(reviewer);
        r.setRejectionReason(reason);

        resources.save(r);
    }
}