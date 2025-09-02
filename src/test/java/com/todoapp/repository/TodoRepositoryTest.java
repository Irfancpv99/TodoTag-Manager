package com.todoapp.repository;

import com.todoapp.model.Todo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoRepositoryTest {

    @Mock
    private TodoRepository todoRepository;

    @Test
    void shouldFindAllTodos() {
        List<Todo> expectedTodos = Arrays.asList(
            new Todo("Task 1"),
            new Todo("Task 2")
        );
        
        when(todoRepository.findAll()).thenReturn(expectedTodos);
        
        List<Todo> actualTodos = todoRepository.findAll();
        
        assertEquals(expectedTodos, actualTodos);
        verify(todoRepository).findAll();
    }
    
    @Test
    void shouldFindTodoById() {
        Todo expectedTodo = new Todo("Test task");
        expectedTodo.setId(1L);
        
        when(todoRepository.findById(1L)).thenReturn(Optional.of(expectedTodo));
        
        Optional<Todo> actualTodo = todoRepository.findById(1L);
        
        assertTrue(actualTodo.isPresent());
        assertEquals(expectedTodo, actualTodo.get());
        verify(todoRepository).findById(1L);
    }
    
    @Test
    void shouldReturnEmptyWhenTodoNotFound() {
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());
        
        Optional<Todo> actualTodo = todoRepository.findById(999L);
        
        assertFalse(actualTodo.isPresent());
        verify(todoRepository).findById(999L);
    }
    
    @Test
    void shouldSaveTodo() {
        Todo todoToSave = new Todo("New task");
        Todo savedTodo = new Todo("New task");
        savedTodo.setId(1L);
        
        when(todoRepository.save(todoToSave)).thenReturn(savedTodo);
        
        Todo result = todoRepository.save(todoToSave);
        
        assertEquals(savedTodo, result);
        verify(todoRepository).save(todoToSave);
    }
    
    @Test
    void shouldDeleteTodoById() {
        Long todoId = 1L;
        
        doNothing().when(todoRepository).deleteById(todoId);
        
        todoRepository.deleteById(todoId);
        
        verify(todoRepository).deleteById(todoId);
    }
    
    @Test
    void shouldDeleteTodo() {
        Todo todo = new Todo("Test task");
        todo.setId(1L);
        
        doNothing().when(todoRepository).delete(todo);
        
        todoRepository.delete(todo);
        
        verify(todoRepository).delete(todo);
    }
    
    @Test
    void shouldFindTodosByDoneStatus() {
        List<Todo> completedTodos = Arrays.asList(
            new Todo("Completed task 1", true),
            new Todo("Completed task 2", true)
        );
        
        when(todoRepository.findByDone(true)).thenReturn(completedTodos);
        
        List<Todo> actualTodos = todoRepository.findByDone(true);
        
        assertEquals(completedTodos, actualTodos);
        verify(todoRepository).findByDone(true);
    }
    
    @Test
    void shouldFindTodosByDescriptionContaining() {
        List<Todo> searchResults = Arrays.asList(
            new Todo("Important task")
        );
        
        when(todoRepository.findByDescriptionContaining("important")).thenReturn(searchResults);
        
        List<Todo> actualTodos = todoRepository.findByDescriptionContaining("important");
        
        assertEquals(searchResults, actualTodos);
        verify(todoRepository).findByDescriptionContaining("important");
    }
}