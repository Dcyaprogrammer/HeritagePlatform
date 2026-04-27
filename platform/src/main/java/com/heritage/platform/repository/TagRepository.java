package com.heritage.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.heritage.platform.model.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
}