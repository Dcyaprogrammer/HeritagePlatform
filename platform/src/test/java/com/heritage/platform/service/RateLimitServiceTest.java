package com.heritage.platform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;
    private final String TEST_IP = "192.168.1.1";

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    void shouldAllowRequestsUnderLimit() {
        // 连续请求19次，由于上限是20次，应该全部通过
        for (int i = 0; i < 19; i++) {
            assertTrue(rateLimitService.isAllowed(TEST_IP), "Request " + i + " should be allowed");
        }
    }

    @Test
    void shouldBlockRequestsExceedingLimit() {
        // 消耗掉前20次额度
        for (int i = 0; i < 20; i++) {
            rateLimitService.isAllowed(TEST_IP);
        }
        // 第21次请求应该被拒绝
        assertFalse(rateLimitService.isAllowed(TEST_IP), "21st request should be blocked");
    }

    @Test
    void shouldIndependentLimitForDifferentIps() {
        // IP A 达到上限
        for (int i = 0; i < 20; i++) {
            rateLimitService.isAllowed("1.1.1.1");
        }
        assertFalse(rateLimitService.isAllowed("1.1.1.1"));
        
        // IP B 应该是通畅的
        assertTrue(rateLimitService.isAllowed("2.2.2.2"), "Different IP should not be affected");
    }
}