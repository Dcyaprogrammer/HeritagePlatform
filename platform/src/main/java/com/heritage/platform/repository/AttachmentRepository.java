package com.heritage.platform.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.heritage.platform.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByResourceIsNullAndCreatedAtBefore(LocalDateTime cutoff);
}