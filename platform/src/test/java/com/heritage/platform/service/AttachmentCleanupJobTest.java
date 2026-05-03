package com.heritage.platform.service;

import com.heritage.platform.model.Attachment;
import com.heritage.platform.repository.AttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentCleanupJobTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentCleanupJob attachmentCleanupJob;

    @BeforeEach
    void setup() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @Test
    @DisplayName("清理过期孤儿附件：成功删除数据库记录并注册文件清理任务")
    void testDeleteExpiredOrphanAttachments_Success() {
        Attachment orphan = new Attachment();
        orphan.setId(1L);
        orphan.setStoredName("test-file.jpg");
        List<Attachment> expiredList = List.of(orphan);

        when(attachmentRepository.findByResourceIsNullAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(expiredList);

        attachmentCleanupJob.deleteExpiredOrphanAttachments();

        verify(attachmentRepository, times(1)).delete(orphan);

        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assert !synchronizations.isEmpty();
        
        synchronizations.get(0).afterCommit();
        
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    @DisplayName("清理任务：当没有过期附件时应直接返回")
    void testDeleteExpiredOrphanAttachments_Empty() {
        when(attachmentRepository.findByResourceIsNullAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        attachmentCleanupJob.deleteExpiredOrphanAttachments();

        verify(attachmentRepository, never()).delete(any());
        assert TransactionSynchronizationManager.getSynchronizations().isEmpty();
    }
}