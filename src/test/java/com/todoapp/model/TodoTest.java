package com.todoapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TodoTest {

    @Test
    void shouldCreateTodoWithDescription() {
        Todo todo = new Todo("Learn TDD");
        
        assertEquals("Learn TDD", todo.getDescription());
        assertFalse(todo.isDone());
        assertNull(todo.getId());
    }
    
    @Test
    void shouldCreateTodoWithDescriptionAndStatus() {
        Todo todoTrue = new Todo("Task", true);
        Todo todoFalse = new Todo("Task", false);
        
        assertTrue(todoTrue.isDone());
        assertFalse(todoFalse.isDone());
    }

    @Test
    void shouldCreateDefaultTodo() {
        Todo todo = new Todo();
        
        assertNull(todo.getDescription());
        assertFalse(todo.isDone()); 
    }
    
    @Test
    void shouldRejectNullDescription() {
        assertThrows(IllegalArgumentException.class, () -> new Todo(null));
        assertThrows(IllegalArgumentException.class, () -> new Todo(null, true));
        
        Todo todo = new Todo("Test");
        assertThrows(IllegalArgumentException.class, () -> todo.setDescription(null));
    }
    
    @Test
    void shouldSetProperties() {
        Todo todo = new Todo("Test");
        
        todo.setId(1L);
        todo.setDescription("Updated");
        todo.setDone(true);
        
        assertEquals(1L, todo.getId());
        assertEquals("Updated", todo.getDescription());
        assertTrue(todo.isDone());
        
        todo.setDone(false);
        assertFalse(todo.isDone());
    }
    
    @Test
    void shouldTestEquality() {
        Todo todo1 = new Todo("Task1");
        Todo todo2 = new Todo("Task2");

        // Same instance
        assertEquals(todo1, todo1);

        // Null and different class
        assertNotEquals(todo1, null);
        assertNotEquals(todo1, new Object());

        // Both ids null - both directions
        assertNotEquals(todo1, todo2);
        assertNotEquals(todo2, todo1);

        todo1.setId(1L);
        assertNotEquals(todo1, todo2);
        assertNotEquals(todo2, todo1);  
        
       todo1.setId(null);
        todo2.setId(2L);
        assertNotEquals(todo1, todo2);
        assertNotEquals(todo2, todo1);

        // Both have different ids
        todo1.setId(1L);
        todo2.setId(2L);
        assertNotEquals(todo1, todo2);

        // Both have same id
        todo2.setId(1L);
        assertEquals(todo1, todo2);
    }
    
    @Test
    void shouldCalculateHashCode() {
        Todo todo1 = new Todo("Same description");
        Todo todo2 = new Todo("Same description");
        Todo todo3 = new Todo("Different description");
        Todo todoNull = new Todo();
        
        assertEquals(todo1.hashCode(), todo2.hashCode());
        assertNotEquals(todo1.hashCode(), todo3.hashCode());
        assertEquals(0, todoNull.hashCode());
    }
    
    @Test
    void shouldHandleTagOperations() {
        Todo todo1 = new Todo("Task 1");
        Todo todo2 = new Todo("Task 2");
        Tag workTag = new Tag("work");
        Tag urgentTag = new Tag("urgent");
        
        todo1.addTag(workTag);
        todo1.addTag(urgentTag);
        todo2.addTag(workTag);
        
        assertEquals(2, todo1.getTags().size());
        assertEquals(1, todo2.getTags().size());
        
        assertTrue(todo1.getTags().contains(workTag));
        assertTrue(todo1.getTags().contains(urgentTag));
        
        todo1.removeTag(urgentTag);
        assertEquals(1, todo1.getTags().size());
        
        // Test null handling
        todo1.addTag(null);
        assertEquals(1, todo1.getTags().size());
        
        todo1.removeTag(null);
        assertEquals(1, todo1.getTags().size());
    }
    
    @Test
    void shouldProvideDefensiveCopies() {
        Todo todo = new Todo("Test");
        Tag tag = new Tag("work");
        todo.addTag(tag);
        
        todo.getTags().clear();
        assertEquals(1, todo.getTags().size());
       
        todo.setTags(null);
        assertNotNull(todo.getTags());
        assertEquals(0, todo.getTags().size());  
      
        java.util.Set<Tag> originalSet = new java.util.HashSet<>();
        originalSet.add(tag);
        todo.setTags(originalSet);
        originalSet.clear();
        assertEquals(1, todo.getTags().size());
    }
    
    @Test
    void shouldGenerateToString() {
        Todo todo = new Todo("Test task");
        todo.setId(42L);
        todo.setDone(true);
        
        String result = todo.toString();
        
        assertTrue(result.contains("id=42"));
        assertTrue(result.contains("description='Test task'"));
        assertTrue(result.contains("done=true"));
    }
}