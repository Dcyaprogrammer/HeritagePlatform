package com.heritage.platform.service;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private HeritageResourceRepository resources;
    @Mock private HeritageUserRepository users;
    @Mock private ReviewLogRepository reviewLogs;

    @InjectMocks
    private ReviewService reviewService;

    private HeritageResource testResource;
    private final Long RESOURCE_ID = 1L;
    private final Long VERSION = 1L;

    @BeforeEach
    void setUp() {
        testResource = new HeritageResource();
        testResource.setId(RESOURCE_ID);
        testResource.setVersion(VERSION);
        testResource.setStatus(ResourceStatus.PENDING_REVIEW);
    }

    @Test
    void shouldApproveResourceSuccessfully() {
        // 必须 Mock 掉 users.findByUsername，因为 service 会用它获取 reviewer 对象
        HeritageUser mockAdmin = new HeritageUser();
        mockAdmin.setUsername("adminUser");
        when(users.findByUsername("adminUser")).thenReturn(Optional.of(mockAdmin));
        when(resources.findById(RESOURCE_ID)).thenReturn(Optional.of(testResource));
        
        reviewService.approve(RESOURCE_ID, VERSION, "adminUser");

        assertEquals(ResourceStatus.APPROVED, testResource.getStatus());
        // 验证确实调用了 save，且参数不为 null
        verify(reviewLogs, times(1)).save(any(ReviewLog.class)); 
        verify(resources).save(testResource);
    }

    @Test
    void shouldThrowConflictWhenVersionMismatch() {
        when(resources.findById(RESOURCE_ID)).thenReturn(Optional.of(testResource));

        // 传入错误的 version
        assertThrows(ReviewService.ConflictException.class, () -> 
            reviewService.approve(RESOURCE_ID, 99L, "adminUser")
        );
    }

    @Test
    void shouldRejectWithReason() {
        String reason = "Information inaccurate";
        when(resources.findById(RESOURCE_ID)).thenReturn(Optional.of(testResource));

        reviewService.reject(RESOURCE_ID, VERSION, "adminUser", reason);

        assertEquals(ResourceStatus.REJECTED, testResource.getStatus());
        assertEquals(reason, testResource.getRejectionReason());
        verify(resources).save(testResource);
    }

    @Test
    void shouldFailRejectWithoutReason() {
        assertThrows(IllegalArgumentException.class, () -> 
            reviewService.reject(RESOURCE_ID, VERSION, "adminUser", "")
        );
    }
}