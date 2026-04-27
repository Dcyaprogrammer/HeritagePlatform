package com.heritage.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.ResourceStatus;

public interface HeritageResourceRepository extends JpaRepository<HeritageResource, Long> {

	List<HeritageResource> findByStatusOrderBySubmittedAtDesc(ResourceStatus status);

	List<HeritageResource> findByStatusAndSubmitterIdOrderBySubmittedAtDesc(ResourceStatus status, Long submitterId);

	Optional<HeritageResource> findById(Long id);

	List<HeritageResource> findBySubmitterIdOrderBySubmittedAtDesc(Long submitterId);

	List<HeritageResource> findBySubmitterUsernameOrderBySubmittedAtDesc(String username);

	Optional<HeritageResource> findByIdAndSubmitterUsername(Long id, String username);

	@Modifying
	@Query("""
		update HeritageResource resource
		set resource.status = :status,
		    resource.submittedAt = :submittedAt,
		    resource.reviewedBy = null,
		    resource.reviewedAt = null,
		    resource.rejectionReason = null
		where resource.id = :id
		  and resource.submitter.username = :username
		  and resource.status = :currentStatus
		""")
	int resubmitRejectedResource(
			@Param("id") Long id,
			@Param("username") String username,
			@Param("currentStatus") ResourceStatus currentStatus,
			@Param("status") ResourceStatus status,
			@Param("submittedAt") java.time.Instant submittedAt);
}
