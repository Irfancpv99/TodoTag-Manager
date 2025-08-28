package com.todoapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TodoTest {

    @Test
    void shouldCreateTodoWithDescription() {
        
        String description = "Learn TDD methodology";
        
        Todo todo = new Todo(description);
        
        assertNotNull(todo);
        assertEquals("Learn TDD methodology", todo.getDescription());
    }
    
    @Test
    void shouldHaveNullIdWhenCreated() {
        // RED: This will fail - Todo don't have getId() method yet
        Todo todo = new Todo("Test task");
        
        assertNull(todo.getId());
    }
    
    @Test
    void shouldBeNotDoneByDefault() {
        // RED: This will fail - Todo don't have isDone() method yet
        Todo todo = new Todo("Test task");
        
        assertFalse(todo.isDone());
    }
    
    @Test
    void shouldAllowSettingId() {
        // RED: This will fail - Todo don't have setId() method yet
        Todo todo = new Todo("Test task");
        
        todo.setId(1L);
        
        assertEquals(1L, todo.getId());
    }
    
    @Test
    void shouldAllowMarkingAsDone() {
        // RED: This will fail - Todo don't have setDone() method yet
        Todo todo = new Todo("Test task");
        
        todo.setDone(true);
        
        assertTrue(todo.isDone());
    }
}