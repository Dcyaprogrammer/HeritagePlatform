package com.heritage.platform.repository;

import com.heritage.platform.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndResourceId(Long userId, Long resourceId);

    boolean existsByUserIdAndResourceId(Long userId, Long resourceId);

    long countByResourceId(Long resourceId);

    List<Favorite> findByResourceId(Long resourceId);

    @Query("SELECT f.resource.id FROM Favorite f WHERE f.user.id = :userId")
    List<Long> findResourceIdsByUserId(@Param("userId") Long userId);

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
}
