package com.heritage.platform.web;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heritage.platform.common.ApiResponse;
import com.heritage.platform.service.ReviewService;

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

    public static class PendingItem {
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
    public ResponseEntity<ApiResponse<List<PendingItem>>> pending() {
        return ResponseEntity.ok(ApiResponse.success(reviewService.listPending()));
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<ApiResponse<ResourceDetail>> detail(@PathVariable Long id) {
        ResourceDetail detail = reviewService.getDetail(id);
        if (detail == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Resource not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PostMapping("/resources/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approve(
            @PathVariable Long id,
            @RequestBody ApproveRequest request,
            Authentication authentication) {
        try {
            String adminUsername = authentication == null ? null : authentication.getName();
            reviewService.approve(id, request.version, adminUsername);
            return ResponseEntity.ok(ApiResponse.success("Approved"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Resource not found"));
        } catch (ReviewService.ConflictException e) {
            return ResponseEntity.status(409)
                    .body(ApiResponse.error(409, "This resource has been processed by another admin, please refresh the list"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping("/resources/{id}/reject")
    public ResponseEntity<ApiResponse<String>> reject(
            @PathVariable Long id,
            @RequestBody RejectRequest request,
            Authentication authentication) {
        try {
            String adminUsername = authentication == null ? null : authentication.getName();
            reviewService.reject(id, request.version, adminUsername, request.rejectionReason);
            return ResponseEntity.ok(ApiResponse.success("Rejected"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Resource not found"));
        } catch (ReviewService.ConflictException e) {
            return ResponseEntity.status(409)
                    .body(ApiResponse.error(409, "This resource has been processed by another admin, please refresh the list"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}