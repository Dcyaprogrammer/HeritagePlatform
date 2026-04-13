package com.heritage.platform.controller;

import com.heritage.platform.common.ApiResponse;
import com.heritage.platform.dto.*;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.service.AuthService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")



public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private HeritageUserRepository userRepository;

    // ==================== PBI 1: Register ====================
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest req) {
        authService.register(req);
        return ApiResponse.success("注册成功111", null);
    }

    // ==================== PBI 2 + PBI 3: Login ====================
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest req,
                                                  jakarta.servlet.http.HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        Map<String, Object> result = authService.loginWithDetails(req, clientIp);
        return ApiResponse.success("登录成功", result);
    }

    // ==================== 测试接口 ====================
    @GetMapping("/test")
    public ApiResponse<String> test() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success("当前登录用户: " + username, null);
    }

    // ==================== 组员2需要的 /me 接口 ====================
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        HeritageUser user = userRepository.findByUsername(username);

        if (user == null) {
            return ApiResponse.error(404, "User not found");
        }

        Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole().name());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("bio", user.getBio());
        userInfo.put("contributorStatus", user.getContributorStatus());

        return ApiResponse.success(userInfo);
    }

    // ==================== PBI 4: Forgot & Reset Password ====================
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