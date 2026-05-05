package com.heritage.platform.controller;

import com.heritage.platform.common.ApiResponse;
import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class InteractionController {

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private HeritageUserRepository userRepository;

    @GetMapping("/api/public/resources/{id}/interactions")
    public ApiResponse<Map<String, Object>> getInteractionStatus(
            @PathVariable("id") Long resourceId,
            Authentication authentication) {
        Long userId = null;
        if (authentication != null) {
            String username = authentication.getName();
            userId = userRepository.findByUsername(username)
                    .map(u -> u.getId())
                    .orElse(null);
        }
        Map<String, Object> status = interactionService.getInteractionStatus(resourceId, userId);
        return ApiResponse.success(status);
    }

    @PostMapping("/api/resources/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> toggleLike(
            @PathVariable("id") Long resourceId,
            Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> result = interactionService.toggleLike(resourceId, username);
        return ApiResponse.success(result);
    }

    @PostMapping("/api/resources/{id}/favorite")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> toggleFavorite(
            @PathVariable("id") Long resourceId,
            Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> result = interactionService.toggleFavorite(resourceId, username);
        return ApiResponse.success(result);
    }
}
