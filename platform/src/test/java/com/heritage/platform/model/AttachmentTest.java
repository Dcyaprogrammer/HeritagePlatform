package com.heritage.platform.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AttachmentTest {

    @Test
    @DisplayName("验证 Attachment 实体类的 Getter 和 Setter 逻辑")
    void testGetterAndSetter() {
        Attachment attachment = new Attachment();
        LocalDateTime now = LocalDateTime.now();
        HeritageResource resource = new HeritageResource();
        HeritageUser user = new HeritageUser();

        attachment.setId(100L);
        attachment.setStoredName("uuid-name.png");
        attachment.setDisplayName("my-photo.png");
        attachment.setFileSize(1024L);
        attachment.setFileType("image/png");
        attachment.setCreatedAt(now);
        attachment.setResource(resource);
        attachment.setUploader(user);

        assertAll("字段校验",
            () -> assertEquals(100L, attachment.getId()),
            () -> assertEquals("uuid-name.png", attachment.getStoredName()),
            () -> assertEquals("my-photo.png", attachment.getDisplayName()),
            () -> assertEquals(1024L, attachment.getFileSize()),
            () -> assertEquals("image/png", attachment.getFileType()),
            () -> assertEquals(now, attachment.getCreatedAt()),
            () -> assertEquals(resource, attachment.getResource()),
            () -> assertEquals(user, attachment.getUploader())
        );
    }
}