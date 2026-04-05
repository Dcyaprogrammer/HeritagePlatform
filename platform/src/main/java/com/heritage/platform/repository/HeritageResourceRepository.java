package com.heritage.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.ResourceStatus;

public interface HeritageResourceRepository extends JpaRepository<HeritageResource, Long> {

	List<HeritageResource> findByStatusOrderBySubmittedAtDesc(ResourceStatus status);

	List<HeritageResource> findByStatusAndSubmitterIdOrderBySubmittedAtDesc(ResourceStatus status, Long submitterId);
}
