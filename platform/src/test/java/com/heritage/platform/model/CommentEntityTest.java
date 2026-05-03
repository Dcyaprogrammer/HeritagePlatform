package com.heritage.platform.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CommentEntityTest {

    @Test
    void testFullEntityLogic() {
        Comment comment = new Comment();
        LocalDateTime now = LocalDateTime.now();
        
        comment.setContent("Sample Content");
        comment.setId(100L);
        
        assertEquals(100L, comment.getId());
        assertEquals("Sample Content", comment.getContent());

        comment.onCreate();
        assertNotNull(comment.getCreatedAt());
        assertNotNull(comment.getUpdatedAt());

        comment.onUpdate();
        assertNotNull(comment.getUpdatedAt());
    }

    @Test
    void testRelationships() {
        Comment comment = new Comment();
        HeritageResource resource = new HeritageResource();
        HeritageUser user = new HeritageUser();
        
        comment.setResource(resource);
        comment.setUser(user);
        
        assertNotNull(comment.getResource());
        assertNotNull(comment.getUser());
    }
}