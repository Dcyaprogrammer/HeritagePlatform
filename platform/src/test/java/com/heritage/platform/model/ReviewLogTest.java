package com.heritage.platform.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ReviewLogTest {

    @Test
    @DisplayName("验证实体字段存取及 @PrePersist 审计逻辑")
    void testReviewLogAudit() {
        ReviewLog log = new ReviewLog();
        log.setAction(ReviewAction.REJECT);
        log.setReason("需要补充更多背景描述");

        ReflectionTestUtils.invokeMethod(log, "initOperatedAt");

        assertAll("日志字段检查",
            () -> assertEquals(ReviewAction.REJECT, log.getAction()),
            () -> assertEquals("需要补充更多背景描述", log.getReason()),
            () -> assertNotNull(log.getOperatedAt(), "operatedAt 应该由 @PrePersist 自动填充"),
            () -> assertTrue(log.getOperatedAt().isBefore(Instant.now().plusSeconds(1)))
        );
    }

    @Test
    @DisplayName("验证手动设置时间戳不会被 @PrePersist 覆盖")
    void testManualOperatedAt() {
        ReviewLog log = new ReviewLog();
        Instant manualTime = Instant.now().minusSeconds(3600);
        log.setOperatedAt(manualTime);

        ReflectionTestUtils.invokeMethod(log, "initOperatedAt");

        assertEquals(manualTime, log.getOperatedAt(), "如果已存在时间戳，initOperatedAt 不应覆盖它");
    }
}