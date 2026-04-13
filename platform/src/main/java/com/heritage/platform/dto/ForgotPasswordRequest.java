package com.heritage.platform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class ForgotPasswordRequest {
    private String email;
}