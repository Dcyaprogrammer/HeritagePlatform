package com.heritage.platform.repository;

import com.heritage.platform.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUserId(Long userId);
    
    Optional<UserSession> findByTokenJti(String jti);

    @Transactional
    void deleteByTokenJti(String jti);

    @Transactional
    void deleteByUserIdAndTokenJtiNot(Long userId, String jti); // 用于“退出其他设备”
}