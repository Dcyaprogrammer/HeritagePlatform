package com.heritage.platform.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;          // 关联用户ID
    private String tokenJti;      // JWT的唯一标识 (JTI)
    private String ipAddress;     // 登录IP
    private String deviceInfo;    // 设备信息 (从User-Agent解析)
    private LocalDateTime loginTime;
}