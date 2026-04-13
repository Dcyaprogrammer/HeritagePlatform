package com.heritage.platform.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}