package com.heritage.platform.service;

import com.heritage.platform.model.Attachment;
import com.heritage.platform.repository.AttachmentRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttachmentCleanupJob {
    private static final Logger log = LoggerFactory.getLogger(AttachmentCleanupJob.class);
    private static final int ORPHAN_TTL_HOURS = 24;

    private final AttachmentRepository attachmentRepository;

    public AttachmentCleanupJob(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000L)
    @Transactional
    public void deleteExpiredOrphanAttachments() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(ORPHAN_TTL_HOURS);
        List<Attachment> expiredAttachments = attachmentRepository.findByResourceIsNullAndCreatedAtBefore(cutoff);

        for (Attachment attachment : expiredAttachments) {
            deletePhysicalFile(attachment);
            attachmentRepository.delete(attachment);
        }

        if (!expiredAttachments.isEmpty()) {
            log.info("Deleted {} orphan attachment(s)", expiredAttachments.size());
        }
    }

    private void deletePhysicalFile(Attachment attachment) {
        String storedName = attachment.getStoredName();
        if (storedName == null || storedName.isBlank()) {
            return;
        }

        Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", storedName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete orphan attachment file: {}", filePath, e);
        }
    }
}
