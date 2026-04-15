package com.heritage.platform.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    // 临时内存存储（重启后消失，仅供测试）
    private static Map<String, Map<String, String>> users = new HashMap<>();
    
    static {
        // 预置一个测试管理员，方便前端登录
        Map<String, String> admin = new HashMap<>();
        admin.put("password", "admin123");
        admin.put("role", "ADMIN");
        users.put("admin", admin);
    }
    
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");
        
        if (users.containsKey(username)) {
            return Map.of("code", 400, "message", "Username already exists");
        }
        
        Map<String, String> user = new HashMap<>();
        user.put("password", password);
        user.put("email", email);
        user.put("role", "VIEWER");
        users.put(username, user);
        
        return Map.of("code", 200, "message", "Registration successful", 
                     "data", Map.of("username", username));
    }
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        
        if (users.containsKey(username) && users.get(username).get("password").equals(password)) {
            // 生成简易 JWT 格式（实际应该是组员1实现）
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + 
                          Base64.getEncoder().encodeToString(username.getBytes()) + 
                          ".fake_signature";
            
            return Map.of("code", 200, "message", "Login successful", 
                         "data", Map.of("token", token, "username", username, 
                                       "role", users.get(username).get("role")));
        }
        
        return Map.of("code", 401, "message", "Invalid username or password");
    }
    
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@RequestHeader("Authorization") String auth) {
        // 简易解析 token
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            // 这里简化处理，实际应该解析 JWT
            return Map.of("code", 200, "message", "success", 
                         "data", Map.of("username", "admin", "role", "ADMIN"));
        }
        return Map.of("code", 401, "message", "Unauthorized");
    }
}