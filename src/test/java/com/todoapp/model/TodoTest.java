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
        Todo todo = new Todo("Test task");
        assertNull(todo.getId());
    }
    
    @Test
    void shouldBeNotDoneByDefault() {
        Todo todo = new Todo("Test task");
        
        assertFalse(todo.isDone());
    }
    
    @Test
    void shouldAllowSettingId() {
        Todo todo = new Todo("Test task");
        
        todo.setId(1L);
        
        assertEquals(1L, todo.getId());
    }
    
    @Test
    void shouldAllowMarkingAsDone() {
        Todo todo = new Todo("Test task");
        
        todo.setDone(true);
        
        assertTrue(todo.isDone());
    }
    
    // REFACTOR PHASE
    @Test
    void shouldThrowExceptionWhenCreatedWithNullDescription() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Todo(null);
        });
    }
    
    @Test
    void shouldAllowSettingDescription() {
        Todo todo = new Todo("Original description");
        
        todo.setDescription("Updated description");
        
        assertEquals("Updated description", todo.getDescription());
    }
    
    @Test
    void shouldThrowExceptionWhenSettingNullDescription() {
        Todo todo = new Todo("Test task");
        
        assertThrows(IllegalArgumentException.class, () -> {
            todo.setDescription(null);
        });
    }
    
    @Test
    void shouldBeEqualWhenSameId() {
        Todo todo1 = new Todo("Task 1");
        Todo todo2 = new Todo("Task 2");
        
        todo1.setId(1L);
        todo2.setId(1L);
        
        assertEquals(todo1, todo2);
        assertEquals(todo1.hashCode(), todo2.hashCode());
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Todo todo1 = new Todo("Task 1");
        Todo todo2 = new Todo("Task 2");
        
        todo1.setId(1L);
        todo2.setId(2L);
        
        assertNotEquals(todo1, todo2);
    }
    
    @Test
    void shouldNotBeEqualWhenNoId() {
        Todo todo1 = new Todo("Task 1");
        Todo todo2 = new Todo("Task 1");
        
        assertNotEquals(todo1, todo2);
    }
    
    @Test
    void shouldNotBeEqualToNull() {
        Todo todo = new Todo("Test task");
        
        assertNotEquals(todo, null);
    }
    
    @Test
    void shouldNotBeEqualToDifferentClass() {
        Todo todo = new Todo("Test task");
        
        assertNotEquals(todo, "not a todo");
    }
    
    @Test
    void shouldHaveStringRepresentation() {
        Todo todo = new Todo("Test task");
        todo.setId(1L);
        todo.setDone(true);
        
        String result = todo.toString();
        
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("description='Test task'"));
        assertTrue(result.contains("done=true"));
    }
}