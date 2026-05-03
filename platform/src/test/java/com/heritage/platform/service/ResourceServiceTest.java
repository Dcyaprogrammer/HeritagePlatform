package com.heritage.platform.service;

import com.heritage.platform.dto.*;
import com.heritage.platform.model.*;
import com.heritage.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock private HeritageResourceRepository resourceRepository;
    @Mock private HeritageUserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private ResourceService resourceService;

    private HeritageUser testUser;
    private HeritageResource testResource;

    @BeforeEach
    void setUp() {
        testUser = new HeritageUser();
        testUser.setUsername("contributor_yuhan");

        testResource = new HeritageResource();
        testResource.setId(101L);
        testResource.setSubmitter(testUser);
        testResource.setStatus(ResourceStatus.DRAFT);
        testResource.setVersion(1L);
    }

    @Test
    void shouldSubmitForReviewSuccessfully() {
        when(resourceRepository.findById(101L)).thenReturn(Optional.of(testResource));
        when(resourceRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ResourceDTO result = resourceService.submitForReview(101L, "contributor_yuhan");

        assertEquals("PENDING_REVIEW", result.getStatus());
        assertNotNull(testResource.getSubmittedAt());
    }

    @Test
    void shouldThrowExceptionWhenSubmittingOtherUsersResource() {
        when(resourceRepository.findById(101L)).thenReturn(Optional.of(testResource));

        // 尝试使用不同的用户名提交
        assertThrows(RuntimeException.class, () -> 
            resourceService.submitForReview(101L, "other_user")
        );
    }

    @Test
    void shouldThrowConflictDuringUpdateIfVersionMismatch() {
        ResourceDraftRequest request = new ResourceDraftRequest();
        request.setVersion(2L); // 传入错误的 version

        when(resourceRepository.findById(101L)).thenReturn(Optional.of(testResource));

        assertThrows(RuntimeException.class, () -> 
            resourceService.updateDraft(101L, request, "contributor_yuhan")
        );
    }
}