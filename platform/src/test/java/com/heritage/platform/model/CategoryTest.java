package com.heritage.platform.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    @DisplayName("验证 Category 实体 Getter/Setter 及默认时间戳")
    void testCategoryEntity() {
        Category category = new Category();
        category.setId(1);
        category.setName("玉器");
        category.setDescription("各类古代玉石制品");
        
        assertAll("字段检查",
            () -> assertEquals(1, category.getId()),
            () -> assertEquals("玉器", category.getName()),
            () -> assertEquals("各类古代玉石制品", category.getDescription()),
            () -> assertNotNull(category.getCreatedAt())
        );
    }
}