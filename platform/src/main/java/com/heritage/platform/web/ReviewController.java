package com.heritage.platform.web;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heritage.platform.service.ReviewService;
import java.util.NoSuchElementException;



@RestController
@RequestMapping("/api/review")
public class ReviewController {

    public final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    public static class ApproveRequest {
        public Long version;
    }

    public static class RejectRequest {
        public Long version;
        public String rejectionReason;
    }

    public static class PendingItem{
        public Long id;
        public String title;
        public String submitterName;
        public Instant submittedAt;
        public String category;
        public Long version;
        public String rejectionReason;
        public boolean stale;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PendingItem>> pending() {
        return ResponseEntity.ok(reviewService.listPending()); // 下一步实现
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<ResourceDetail> detail(@PathVariable Long id) {
        ResourceDetail d = reviewService.getDetail(id); // 下一步实现
        if(d == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(d);
    }

    @PostMapping("/resources/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id,
                                        @RequestBody ApproveRequest request) {
        try {
            reviewService.approve(id, request.version, null);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (ReviewService.ConflictException e) {
            return ResponseEntity.status(409).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/resources/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @RequestBody RejectRequest request) {
        try {
            reviewService.reject(id, request.version, null, request.rejectionReason);            
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (ReviewService.ConflictException e) {
            return ResponseEntity.status(409).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}