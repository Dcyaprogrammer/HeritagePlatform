package com.heritage.platform.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class HeritageUserTest {

    @Test
    @DisplayName("验证实体生命周期钩子：自动填充时间戳")
    void testAuditTimestamps() throws Exception {
        HeritageUser user = new HeritageUser();
        
        Method onCreate = HeritageUser.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(user);

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("验证用户锁定逻辑字段")
    void testLockingFields() {
        HeritageUser user = new HeritageUser();
        user.setFailedAttempts(3);
        user.setLockTime(LocalDateTime.now());
        
        assertEquals(3, user.getFailedAttempts());
        assertNotNull(user.getLockTime());
    }
}