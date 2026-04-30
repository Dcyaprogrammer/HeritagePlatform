//pbi5multisession


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

    private Long userId;
    private String tokenJti;
    private String ipAddress;
    private String deviceInfo;
    private LocalDateTime loginTime;
}