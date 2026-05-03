package com.heritage.platform.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils; // 关键工具类[cite: 1]
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String TEST_SECRET = "thisIsASecureSecretKeyForTestingPurpose1234567890";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET);
        // 解决 @Value 无法在普通单元测试中注入的问题[cite: 1]
        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET);
        // 如果你的 JwtUtil 内部在构造函数中初始化了 key，可能需要额外设置 key 字段
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String username = "testuser";
        Set<String> roles = Set.of("VIEWER");

        String token = jwtUtil.generateToken(username, roles, "test-jti");

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(username, jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.extractRoles(token).contains("VIEWER"));
    }
}