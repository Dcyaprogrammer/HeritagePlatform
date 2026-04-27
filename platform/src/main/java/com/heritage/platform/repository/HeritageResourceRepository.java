package com.heritage.platform.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.ResourceStatus;

@Repository
public interface HeritageResourceRepository extends JpaRepository<HeritageResource, Long> {

    List<HeritageResource> findBySubmitterUsernameOrderBySubmittedAtDesc(String username);

    Optional<HeritageResource> findByIdAndSubmitterUsername(Long id, String username);

    @Modifying
    @Query("UPDATE HeritageResource r SET r.status = :newStatus, r.submittedAt = :submittedAt " +
           "WHERE r.id = :id AND r.submitter.username = :username AND r.status = :oldStatus")
    int resubmitRejectedResource(
            @Param("id") Long id, 
            @Param("username") String username, 
            @Param("oldStatus") ResourceStatus oldStatus, 
            @Param("newStatus") ResourceStatus newStatus, 
            @Param("submittedAt") Instant submittedAt);
}