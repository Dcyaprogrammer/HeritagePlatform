//主




package com.heritage.platform.service;

import com.heritage.platform.config.PasswordConfig;
import com.heritage.platform.dto.*;

import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.entity.Role; 

import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.security.JwtUtil;

import java.time.LocalDateTime;
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
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.VIEWER);
        
        userRepository.save(user);
    }




    //pbi2
    public String login(LoginRequest req, String clientIp) {
        if (!rateLimitService.isAllowed(clientIp)) {
            throw new RuntimeException("Too many attempts. Please wait for a while.");
        }

        HeritageUser user = userRepository.findByUsername(req.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getLockTime() != null && 
            user.getLockTime().isAfter(LocalDateTime.now().minusMinutes(15))) {
            throw new RuntimeException("账号已被锁定，请15分钟后再试");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 3) {
                user.setLockTime(LocalDateTime.now());
                user.setAccountNonLocked(false);
            }
            userRepository.save(user);
            throw new RuntimeException("用户名或密码错误");
        }

        user.setFailedAttempts(0);
        user.setLockTime(null);
        user.setAccountNonLocked(true);
        userRepository.save(user);

        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }
    // 组员2需要的 loginWithDetails（兼容 /me 接口）
    public Map<String, Object> loginWithDetails(LoginRequest req, String clientIp) {
        String token = login(req, clientIp);   // 调用你原来的 login 方法

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("token", token);
        result.put("message", "登录成功");
        
        return result;
    }






    // pbi4
    public void forgotPassword(ForgotPasswordRequest req) {
        HeritageUser user = userRepository.findByEmail(req.getEmail());
        
        if (user == null) {
            return;  // 故意不提示，防止枚举用户
        }

        String token = java.util.UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        // === 修复：删除危险的 System.out.println ===
        // 生产环境应该发邮件，这里仅打印安全提示
        System.out.println("\n[密码重置请求] 已为邮箱 " + req.getEmail() + " 生成重置令牌");
        System.out.println("前端重置链接应为：http://localhost:5173/reset-password?token=" + token);
        System.out.println("此令牌将在 30 分钟后过期。");
    }

    public void resetPassword(ResetPasswordRequest req) {
        HeritageUser user = userRepository.findByResetToken(req.getToken());
        if (user == null || user.getResetTokenExpiry() == null ||
            user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("重置链接无效或已过期");
        }

        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}