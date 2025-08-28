package com.todoapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TodoTest {

    @Test
    void shouldCreateTodoWithDescription() {
        // RED: This will fail - Todo class doesn't exist yet
        String description = "Learn TDD methodology";
        
        Todo todo = new Todo(description);
        
        assertNotNull(todo);
        assertEquals("Learn TDD methodology", todo.getDescription());
    }
}