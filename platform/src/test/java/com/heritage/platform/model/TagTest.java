package com.heritage.platform.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    @Test
    @DisplayName("验证 Tag 实体及其与资源的关联集合")
    void testTagEntityAndResources() {
        Tag tag = new Tag();
        tag.setName("乾隆时期");
        
        assertNotNull(tag.getResources());
        assertTrue(tag.getResources() instanceof Set);

        HeritageResource res = new HeritageResource();
        Set<HeritageResource> resources = new HashSet<>();
        resources.add(res);
        tag.setResources(resources);

        assertEquals(1, tag.getResources().size());
        assertEquals("乾隆时期", tag.getName());
    }
}