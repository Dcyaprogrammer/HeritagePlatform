package com.heritage.platform.controller;

import com.heritage.platform.common.ApiResponse;
import com.heritage.platform.dto.CommentDTO;
import com.heritage.platform.dto.CommentRequest;
import com.heritage.platform.service.CommentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/api/public/resources/{id}/comments")
    public ApiResponse<List<CommentDTO>> getComments(@PathVariable("id") Long id) {
        List<CommentDTO> comments = commentService.getCommentsByResourceId(id);
        return ApiResponse.success(comments);
    }

    @PostMapping("/api/resources/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CommentDTO> addComment(
            @PathVariable("id") Long id,
            @RequestBody CommentRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        CommentDTO newComment = commentService.addComment(id, username, request);
        return ApiResponse.success(newComment);
    }
}
