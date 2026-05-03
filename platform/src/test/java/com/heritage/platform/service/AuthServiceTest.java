package com.heritage.platform.service;

import com.heritage.platform.dto.LoginRequest;
import com.heritage.platform.dto.RegisterRequest;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.repository.UserSessionRepository;
import com.heritage.platform.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private HeritageUserRepository userRepository;

    @Mock
    private UserSessionRepository sessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest req = new RegisterRequest("newuser", "newuser@example.com", "password123");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(HeritageUser.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> authService.register(req));

        verify(userRepository, times(1)).save(any(HeritageUser.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        RegisterRequest req = new RegisterRequest("existing", "test@example.com", "123");
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(req));
        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("correctpass");

        HeritageUser user = new HeritageUser();
        user.setUsername("testuser");
        user.setPasswordHash("encodedPass");

        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anySet(), anyString())).thenReturn("mock.jwt.token");

        String token = authService.login(req, "127.0.0.1", "Mozilla/5.0");

        assertNotNull(token);
        verify(sessionRepository, atLeastOnce()).save(any()); 
    }

    @Test
    void shouldThrowExceptionWhenLoginWithWrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("wrongpass");

        HeritageUser user = new HeritageUser();
        user.setUsername("testuser");
        user.setPasswordHash("encodedPass");

        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(req, "127.0.0.1", "Mozilla"));

        assertEquals("用户名或密码错误", exception.getMessage());
    }
}