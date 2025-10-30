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
        
        assertEquals(tag1, tag1);  
        assertTrue(tag1.equals(tag1));
        
        assertNotEquals(tag1, tag2);
        
        tag1.setId(1L);
        tag2.setId(1L);
        assertEquals(tag1, tag2);
        
        tag2.setId(2L);
        assertNotEquals(tag1, tag2);
        
        assertNotEquals(tag2, tag3);
        assertNotEquals(tag3, tag2);
        
        assertNotEquals(null, tag1);  
        assertFalse(tag1.equals(null));  
        
        assertNotEquals("string", tag1);  
        assertFalse(tag1.equals("string")); 
    }
    
    @Test
    void shouldCalculateHashCodeBasedOnName() {
        Tag tag1 = new Tag("same name");
        Tag tag2 = new Tag("same name");
        Tag tag3 = new Tag("different name");
        Tag tagNull = new Tag();
        
         assertEquals(tag1.hashCode(), tag2.hashCode());
        
        assertNotEquals(tag1.hashCode(), tag3.hashCode());
        
        assertEquals(0, tagNull.hashCode());
    
        tag1.setId(1L);
        tag2.setId(2L);
        assertEquals(tag1.hashCode(), tag2.hashCode()); // Still same because same name
    }
    
    @Test
    void shouldProvideDefensiveCopiesAndHandleNulls() {
        Tag tag = new Tag("work");
        Todo todo = new Todo("Task");
        todo.addTag(tag);
        
        tag.getTodos().clear();
        assertEquals(1, tag.getTodos().size());
        
        tag.setTodos(null);
        assertNotNull(tag.getTodos());
        assertEquals(0, tag.getTodos().size());
        
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