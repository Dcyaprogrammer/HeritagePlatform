package com.heritage.platform.repository;

import com.heritage.platform.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByResourceIdOrderByCreatedAtDesc(Long resourceId);
    
    List<Comment> findByResourceIdAndParentIsNullOrderByCreatedAtDesc(Long resourceId);
    
}
