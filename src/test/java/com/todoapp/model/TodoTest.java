package com.todoapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Test for Todo entity - Extended tests
 * 
 * Adding new failing tests for ID and done status
 */
class TodoTest {

    @Test
    void shouldCreateTodoWithDescription() {
        
        String description = "Learn TDD methodology";
        
        Todo todo = new Todo(description);
        
        assertNotNull(todo);
        assertEquals("Learn TDD methodology", todo.getDescription());
    }
    
    
    @Test
    // RED: This will fail - Todo don't have getId() method yet
    void shouldHaveNullIdWhenCreated() {
        Todo todo = new Todo("Test task");
        assertNull(todo.getId());
    }
    
    @Test
    // RED: This will fail - Todo don't have isDone() method yet
    
    void shouldBeNotDoneByDefault() {
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