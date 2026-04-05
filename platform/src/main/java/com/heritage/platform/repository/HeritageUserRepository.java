package com.heritage.platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.heritage.platform.model.HeritageUser;

public interface HeritageUserRepository extends JpaRepository<HeritageUser, Long> {

	Optional<HeritageUser> findByUsername(String username);
}
