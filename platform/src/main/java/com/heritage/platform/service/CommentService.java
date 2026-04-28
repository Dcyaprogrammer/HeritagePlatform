package com.heritage.platform.service;

import com.heritage.platform.dto.CommentDTO;
import com.heritage.platform.dto.CommentRequest;
import com.heritage.platform.model.Comment;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.repository.CommentRepository;
import com.heritage.platform.repository.HeritageResourceRepository;
import com.heritage.platform.repository.HeritageUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private HeritageResourceRepository resourceRepository;

    @Autowired
    private HeritageUserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByResourceId(Long resourceId) {
        List<Comment> comments = commentRepository.findByResourceIdOrderByCreatedAtDesc(resourceId);
        return comments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public CommentDTO addComment(Long resourceId, String username, CommentRequest request) {
        HeritageResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with id: " + resourceId));
        HeritageUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Comment comment = new Comment();
        comment.setResource(resource);
        comment.setUser(user);
        comment.setContent(request.getContent());
        
        Comment savedComment = commentRepository.save(comment);

        return convertToDTO(savedComment);
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setResourceId(comment.getResource().getId());
        
        // Load user info safely (requires active transaction which is guaranteed by @Transactional)
        HeritageUser user = comment.getUser();
        dto.setUserId(user.getId());
        dto.setAuthorName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
        dto.setAvatar(user.getAvatar());
        
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}
