package com.todoapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    @Test
    void shouldCreateTagWithName() {
        Tag tag = new Tag("work");
        
        assertEquals("work", tag.getName());
        assertNull(tag.getId());
    }

    @Test
    void shouldCreateDefaultTag() {
        Tag tag = new Tag();
        
        assertNull(tag.getName());
        assertNull(tag.getId());
    }
    
    @Test
    void shouldRejectNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Tag(null));
        
        Tag tag = new Tag("test");
        assertThrows(IllegalArgumentException.class, () -> tag.setName(null));
    }
    
    @Test
    void shouldSetIdAndName() {
        Tag tag = new Tag("original");
        
        tag.setId(1L);
        tag.setName("updated");
        
        assertEquals(1L, tag.getId());
        assertEquals("updated", tag.getName());
    }
    
    @Test
    void shouldHandleEqualityCorrectly() {
        Tag tag1 = new Tag("work");
        Tag tag2 = new Tag("urgent");
        Tag tag3 = new Tag("personal");
        
        // Same object reference
        assertEquals(tag1, tag1);
        
        // Both null IDs - should be false
        assertNotEquals(tag1, tag2);
        
        // Same IDs - should be equal regardless of name
        tag1.setId(1L);
        tag2.setId(1L);
        assertEquals(tag1, tag2);
        
        // Different IDs
        tag2.setId(2L);
        assertNotEquals(tag1, tag2);
        
        // One null ID, one not null
        assertNotEquals(tag2, tag3);
        assertNotEquals(tag3, tag2);
        
        // Null object
        assertNotEquals(tag1, null);
        
        // Different class
        assertNotEquals(tag1, "string");
    }
    
    @Test
    void shouldCalculateHashCodeBasedOnName() {
        Tag tag1 = new Tag("same name");
        Tag tag2 = new Tag("same name");
        Tag tag3 = new Tag("different name");
        Tag tagNull = new Tag();
        
        // Same name = same hashCode
        assertEquals(tag1.hashCode(), tag2.hashCode());
        
        // Different names = different hashCode
        assertNotEquals(tag1.hashCode(), tag3.hashCode());
        
        // Null name should return 0
        assertEquals(0, tagNull.hashCode());
        
        // HashCode should be consistent regardless of ID
        tag1.setId(1L);
        tag2.setId(2L);
        assertEquals(tag1.hashCode(), tag2.hashCode()); // Still same because same name
    }
    
    @Test
    void shouldProvideDefensiveCopiesAndHandleNulls() {
        Tag tag = new Tag("work");
        Todo todo = new Todo("Task");
        todo.addTag(tag); // This establishes bidirectional relationship
        
        // getTodos returns defensive copy
        tag.getTodos().clear();
        assertEquals(1, tag.getTodos().size());
        
        // setTodos with null creates empty set
        tag.setTodos(null);
        assertNotNull(tag.getTodos());
        assertEquals(0, tag.getTodos().size());
        
        // setTodos with non-null creates defensive copy
        java.util.Set<Todo> originalSet = new java.util.HashSet<>();
        originalSet.add(todo);
        tag.setTodos(originalSet);
        originalSet.clear();
        assertEquals(1, tag.getTodos().size());
    }
    
    @Test
    void shouldGenerateCorrectStringRepresentation() {
        Tag tag = new Tag("work");
        tag.setId(42L);
        
        String result = tag.toString();
        
        assertTrue(result.contains("id=42"));
        assertTrue(result.contains("name='work'"));
    }
}