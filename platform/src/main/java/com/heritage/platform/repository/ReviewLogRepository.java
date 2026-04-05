package com.heritage.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.heritage.platform.model.ReviewLog;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

	List<ReviewLog> findByResourceIdOrderByOperatedAtAsc(Long resourceId);
}
