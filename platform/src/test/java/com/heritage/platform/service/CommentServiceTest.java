package com.heritage.platform.service;

import com.heritage.platform.dto.CommentDTO;
import com.heritage.platform.dto.CommentRequest;
import com.heritage.platform.model.Comment;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.repository.CommentRepository;
import com.heritage.platform.repository.HeritageResourceRepository;
import com.heritage.platform.repository.HeritageUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private HeritageResourceRepository resourceRepository;
    @Mock private HeritageUserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private HeritageUser testUser;
    private HeritageResource testResource;

    @BeforeEach
    void setUp() {
        testUser = new HeritageUser();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        testUser.setUsername("yuhan_mei");
        testUser.setDisplayName("Yuhan");

        testResource = new HeritageResource();
        ReflectionTestUtils.setField(testResource, "id", 200L);
    }

    // --- 测试场景 1：获取评论列表 (提升 getCommentsByResourceId 覆盖率) ---

    @Test
    void shouldReturnEmptyListWhenNoCommentsFound() {
        when(commentRepository.findByResourceIdOrderByCreatedAtDesc(200L)).thenReturn(Collections.emptyList());

        List<CommentDTO> result = commentService.getCommentsByResourceId(200L);

        assertTrue(result.isEmpty());
        verify(commentRepository).findByResourceIdOrderByCreatedAtDesc(200L);
    }

    @Test
    void shouldMapCommentsToDTOsCorrectly() {
        Comment comment = new Comment();
        ReflectionTestUtils.setField(comment, "id", 10L);
        comment.setResource(testResource);
        comment.setUser(testUser);
        comment.setContent("Great exhibit!");
        comment.setCreatedAt(LocalDateTime.now());

        when(commentRepository.findByResourceIdOrderByCreatedAtDesc(200L)).thenReturn(List.of(comment));

        List<CommentDTO> result = commentService.getCommentsByResourceId(200L);

        assertEquals(1, result.size());
        assertEquals("Great exhibit!", result.get(0).getContent());
        assertEquals("Yuhan", result.get(0).getAuthorName());
    }

    // --- 测试场景 2：添加评论 (提升 addComment 覆盖率) ---

    @Test
    void shouldAddCommentSuccessfully() {
        CommentRequest request = new CommentRequest();
        request.setContent("New Comment");

        when(resourceRepository.findById(200L)).thenReturn(Optional.of(testResource));
        when(userRepository.findByUsername("yuhan_mei")).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> {
            Comment c = i.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 500L);
            return c;
        });

        CommentDTO result = commentService.addComment(200L, "yuhan_mei", request);

        assertNotNull(result);
        assertEquals(500L, result.getId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        CommentRequest request = new CommentRequest();
        when(resourceRepository.findById(200L)).thenReturn(Optional.of(testResource));
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            commentService.addComment(200L, "unknown", request)
        );
    }
}