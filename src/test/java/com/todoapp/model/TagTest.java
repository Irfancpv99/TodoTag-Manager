package com.todoapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
        Todo todo1 = new Todo("Task1");
        Todo todo2 = new Todo("Task2");
        Todo todo3 = new Todo("Task3");

        assertEquals(todo1, todo1);
        assertEquals(todo1, todo1);        

        assertNotEquals(todo1, todo2);

        todo1.setId(1L);
        todo2.setId(1L);
        assertEquals(todo1, todo2);

        todo2.setId(2L);
        assertNotEquals(todo1, todo2);

        assertNotEquals(todo2, todo3);
        assertNotEquals(todo3, todo2);

        assertNotEquals(null, todo1);        
        assertNotEquals(null, todo1);       

        assertNotEquals("string", todo1);   
        assertNotEquals("string", todo1);    
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
    @Test
    void shouldKillRemainingEqualsMutants() {
        Tag tag = new Tag("a");
        Tag other = new Tag("b");

        assertEquals(tag, tag);

        assertNotEquals(null, tag);
        assertNotEquals(tag, new Object());

        tag.setId(1L);
        other.setId(2L);
        assertNotEquals(tag, other);

        
        other.setId(1L);
        assertEquals(tag, other);
    }


}