package com.heritage.platform.controller;

import com.heritage.platform.dto.ApiResponse;
import com.heritage.platform.dto.SessionResponse;
import com.heritage.platform.security.JwtUtil;
import com.heritage.platform.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired private AuthService authService;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<List<SessionResponse>> listSessions(Authentication auth, 
                                                         @RequestHeader("Authorization") String token) {
        String currentJti = jwtUtil.extractJti(token.substring(7));
        List<SessionResponse> sessions = authService.getActiveSessions(auth.getName(), currentJti);
        return ApiResponse.success(sessions);
    }

    @DeleteMapping("/{jti}")
    public ApiResponse<String> terminateSession(@PathVariable String jti) {
        authService.logoutSession(jti);
        return ApiResponse.success("Session terminated successfully");
    }
}