package com.heritage.platform.repository;

import com.heritage.platform.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndResourceId(Long userId, Long resourceId);

    boolean existsByUserIdAndResourceId(Long userId, Long resourceId);

    long countByResourceId(Long resourceId);

    List<Like> findByResourceId(Long resourceId);

    @Query("SELECT l.resource.id FROM Like l WHERE l.user.id = :userId")
    List<Long> findResourceIdsByUserId(@Param("userId") Long userId);
}
