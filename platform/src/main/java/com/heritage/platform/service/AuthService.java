//主




package com.heritage.platform.service;

import com.heritage.platform.config.PasswordConfig;
import com.heritage.platform.dto.*;

import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.entity.Role; 

import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
public class AuthService {
    @Autowired private HeritageUserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RateLimitService rateLimitService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailService emailService;



    //pbi1
    public void register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        HeritageUser user = new HeritageUser();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRoles(new java.util.HashSet<>(java.util.List.of(Role.VIEWER.name())));
        user.setDisplayName(req.getUsername());
        userRepository.save(user);
    }




    //pbi2
    public String login(LoginRequest req, String clientIp) {
        if (!rateLimitService.isAllowed(clientIp)) {
            throw new RuntimeException("Too many attempts. Please wait for a while.");
        }

        HeritageUser user = userRepository.findByUsername(req.getUsername()).orElse(null);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getLockTime() != null) {
            if (user.getLockTime().isAfter(LocalDateTime.now().minusMinutes(15))) {
                throw new RuntimeException("账号已被锁定，请15分钟后再试");
            }
            user.setLockTime(null);
            user.setFailedAttempts(0);
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() != null ? user.getFailedAttempts() + 1 : 1;
            user.setFailedAttempts(attempts);

            if (attempts >= 3) {
                user.setLockTime(LocalDateTime.now());
            }
            userRepository.save(user);
            throw new RuntimeException("用户名或密码错误");
        }

        user.setFailedAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);

        return jwtUtil.generateToken(user.getUsername(), user.getRoles());
    }

    public Map<String, Object> loginWithDetails(LoginRequest req, String clientIp) {
        String token = login(req, clientIp);
        HeritageUser user = userRepository.findByUsername(req.getUsername()).orElse(null);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("roles", user.getRoles());
        
        return result;
    }






    //pbi4正式版邮件重置密码
    public void forgotPassword(ForgotPasswordRequest req) {
        // 1. 正确处理 Optional
        HeritageUser user = userRepository.findByEmail(req.getEmail()).orElse(null);

        if (user == null) {
            return;
        }

        String token = java.util.UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        //EmailService发送邮件
        try {
            emailService.sendResetPasswordEmail(req.getEmail(), token);
            System.out.println("✅ 密码重置邮件已成功发送至：" + req.getEmail());
        } catch (Exception e) {
            System.err.println("❌ 发送邮件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void resetPassword(ResetPasswordRequest req) {
        HeritageUser user = userRepository.findByResetToken(req.getToken()).orElse(null);
        if (user == null || user.getResetTokenExpiry() == null ||
            user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("重置链接无效或已过期");
        }

        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}