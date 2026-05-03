package com.heritage.platform.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResourceStatusTest {

    @Test
    @DisplayName("验证资源生命周期状态定义")
    void testResourceStatusEnum() {
        ResourceStatus[] statuses = ResourceStatus.values();
        
        assertAll("Status existence",
            () -> assertNotNull(ResourceStatus.valueOf("PENDING_REVIEW")),
            () -> assertNotNull(ResourceStatus.valueOf("APPROVED")),
            () -> assertNotNull(ResourceStatus.valueOf("REJECTED")),
            () -> assertEquals(5, statuses.length)
        );
    }
}