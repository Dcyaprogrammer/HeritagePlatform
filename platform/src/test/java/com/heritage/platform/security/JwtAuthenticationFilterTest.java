package com.heritage.platform.security;

import com.heritage.platform.repository.UserSessionRepository;
import com.heritage.platform.model.UserSession;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach; // 1. 导入这个注解
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder; // 2. 导入上下文持有者

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserSessionRepository sessionRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    // --- 关键修复代码开始 ---
    @BeforeEach
    void setUp() {
        // 在每个测试运行前，强制清空 Security 上下文
        SecurityContextHolder.clearContext(); 
    }
    // --- 关键修复代码结束 ---

    @Test
    void shouldAuthenticateSuccessfully() throws ServletException, IOException {
        String token = "valid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractJti(token)).thenReturn("jti-123");
        when(jwtUtil.extractUsername(token)).thenReturn("Yuhan");
        when(jwtUtil.extractRoles(token)).thenReturn(Set.of("USER"));
        when(sessionRepository.findByTokenJti("jti-123")).thenReturn(Optional.of(new UserSession()));

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldFailWhenTokenIsInvalid() throws ServletException, IOException {
        String token = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(false);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldSkipFilterWhenNoAuthHeader() throws ServletException, IOException {
        // 现在这里会成功，因为 setUp() 帮我们清空了之前的 "Yuhan" 信息
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}