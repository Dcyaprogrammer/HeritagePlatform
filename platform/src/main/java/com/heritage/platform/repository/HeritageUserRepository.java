package com.heritage.platform.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.heritage.platform.model.HeritageUser;

public interface HeritageUserRepository extends JpaRepository<HeritageUser, Long> {

	Optional<HeritageUser> findByUsername(String username);

	/**
	 * Find users by username containing keyword (case insensitive)
	 * 根据用户名包含的关键词查询用户（不区分大小写）
	 */
	Page<HeritageUser> findByUsernameContainingIgnoreCase(String keyword, Pageable pageable);

	Page<HeritageUser> findByRolesContaining(String role, Pageable pageable);

	Page<HeritageUser> findByUsernameContainingIgnoreCaseAndRolesContaining(
			String keyword,
			String role,
			Pageable pageable);

	List<HeritageUser> findByContributorStatus(String contributorStatus);
}
