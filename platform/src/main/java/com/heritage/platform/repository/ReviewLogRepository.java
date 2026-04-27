package com.heritage.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.heritage.platform.model.ReviewAction;
import com.heritage.platform.model.ReviewLog;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

	List<ReviewLog> findByResourceIdOrderByOperatedAtAsc(Long resourceId);

	List<ReviewLog> findByResourceIdOrderByOperatedAtDesc(Long resourceId);

	Optional<ReviewLog> findFirstByResourceIdAndActionOrderByOperatedAtDesc(Long resourceId, ReviewAction action);
}
