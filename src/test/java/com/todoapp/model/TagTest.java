package com.todoapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    @Test
    void shouldCreateTagWithName() {
        // RED: This will fail - Tag class don't exist yet
        String name = "work";
        
        Tag tag = new Tag(name);
        
        assertNotNull(tag);
        assertEquals("work", tag.getName());
    }
    
    @Test
    void shouldHaveNullIdWhenCreated() {
        // RED: This will fail - Tag don't exist yet
        Tag tag = new Tag("urgent");
        
        assertNull(tag.getId());
    }
    
    @Test
    void shouldAllowSettingId() {
        // RED: This will fail - Tag don't exist yet
        Tag tag = new Tag("personal");
        
        tag.setId(1L);
        
        assertEquals(1L, tag.getId());
    }
    
    @Test
    void shouldThrowExceptionWhenCreatedWithNullName() {
        // RED: This will fail - Tag don't exist yet
        assertThrows(IllegalArgumentException.class, () -> {
            new Tag(null);
        });
    }
    
    @Test
    void shouldAllowSettingName() {
        // RED: This will fail - Tag don't exist yet
        Tag tag = new Tag("original");
        
        tag.setName("updated");
        
        assertEquals("updated", tag.getName());
    }
}