//register测试




package com.heritage.platform.controller;

import com.heritage.platform.common.ApiResponse;
import com.heritage.platform.dto.*;
import com.heritage.platform.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")




public class AuthController {
    @Autowired private AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest req) {
        authService.register(req);
        return ApiResponse.success("注册成功111", null);
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest req,
                                    jakarta.servlet.http.HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String token = authService.login(req, clientIp);
        return ApiResponse.success("登录成功111", token);
    }

    @GetMapping("/test")
    public ApiResponse<String> test() {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return ApiResponse.success("当前登录用户: " + username, null);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ApiResponse.success("If that email is registered, you will receive a password reset link shortly.", null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ApiResponse.success("密码重置成功，请使用新密码登录", null);
    }
}