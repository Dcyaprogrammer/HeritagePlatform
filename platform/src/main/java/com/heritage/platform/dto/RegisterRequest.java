//










package com.heritage.platform.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor

public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}