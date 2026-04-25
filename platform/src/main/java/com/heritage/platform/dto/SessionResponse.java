package com.heritage.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SessionResponse {
    private String jti;
    private String ipAddress;
    private String deviceInfo;
    private LocalDateTime loginTime;
    private boolean isCurrent; // 标识是否为当前正在操作的设备
}