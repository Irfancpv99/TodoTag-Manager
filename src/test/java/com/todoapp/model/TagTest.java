package com.todoapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    @Test
    void shouldCreateTag() {
        Tag tag = new Tag("work");
        assertEquals("work", tag.getName());
        assertNull(tag.getId());
        
        Tag emptyTag = new Tag();
        assertNull(emptyTag.getName());
        assertNull(emptyTag.getId());
    }
    
    @Test
    void shouldRejectNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Tag(null));
        
        Tag tag = new Tag("test");
        assertThrows(IllegalArgumentException.class, () -> tag.setName(null));
    }
    
    @Test
    void shouldSetProperties() {
        Tag tag = new Tag("original");
        tag.setId(1L);
        tag.setName("updated");
        
        assertEquals(1L, tag.getId());
        assertEquals("updated", tag.getName());
    }
    
    @Test
    void shouldTestEquality() {
        Tag tag1 = new Tag("a");
        Tag tag2 = new Tag("b");

        // Same instance
        assertTrue(tag1.equals(tag1));
        
        // Null check
        assertFalse(tag1.equals(null));
        
        // Different class
        assertFalse(tag1.equals(new Object()));
        
        // Both ID's null 
        assertFalse(tag1.equals(tag2));
        assertFalse(tag2.equals(tag1));
        
        // One id null, one not 
        tag1.setId(1L);
        assertFalse(tag1.equals(tag2));
        assertFalse(tag2.equals(tag1));
        
        tag1.setId(null);
        tag2.setId(2L);
        assertFalse(tag1.equals(tag2));
        assertFalse(tag2.equals(tag1));
        
        // Different ID's 
        tag1.setId(1L);
        tag2.setId(2L);
        assertFalse(tag1.equals(tag2));
        assertFalse(tag2.equals(tag1));
        
        // Same id 
        tag2.setId(1L);
        assertTrue(tag1.equals(tag2));
        assertTrue(tag2.equals(tag1));
    }
        
    @Test
    void shouldCalculateHashCode() {
        Tag tag1 = new Tag("name");
        Tag tag2 = new Tag("name");
        Tag tag3 = new Tag("other");
        
        assertEquals(tag1.hashCode(), tag2.hashCode());
        assertNotEquals(tag1.hashCode(), tag3.hashCode());
        
        Tag nullTag = new Tag();
        assertEquals(0, nullTag.hashCode());
    }
    
    @Test
    void shouldProvideDefensiveCopy() {
        Tag tag = new Tag("work");
        
       tag.getTodos().clear();
         assertNotNull(tag.getTodos());
        
        assertNotNull(tag.getTodosInternal());
        assertEquals(0, tag.getTodosInternal().size());
        
        tag.setTodos(null);
        assertNotNull(tag.getTodos());
        assertEquals(0, tag.getTodos().size());
        
       java.util.Set<Todo> set = new java.util.HashSet<>();
        set.add(new Todo("task"));
        tag.setTodos(set);
        set.clear();
        assertEquals(1, tag.getTodos().size());
        assertEquals(1, tag.getTodosInternal().size());
    }
    
    @Test
    void shouldGenerateToString() {
        Tag tag = new Tag("test");
        tag.setId(42L);
        
        String result = tag.toString();
        assertTrue(result.contains("id=42"));
        assertTrue(result.contains("name='test'"));
    }
}