package com.todoapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import com.todoapp.model.Todo;
import com.todoapp.repository.TagRepository;
import com.todoapp.repository.TodoRepository;

class TodoServiceTest {
    private TodoRepository todoRepository;
    private TagRepository tagRepository;
    private TodoService todoService;

    @BeforeEach
    void setUp() {
        todoRepository = mock(TodoRepository.class);
        tagRepository = mock(TagRepository.class);
        todoService = new TodoService(todoRepository, tagRepository);
    }
    
    @Test
    void shouldGetAllTodos() {
        List todos = List.of(new Todo("Task 1"));
        when(todoRepository.findAll()).thenReturn(todos);

        assertEquals(todos, todoService.getAllTodos());
    }
    
    @Test
    void shouldGetTodoById() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        Optional result = todoService.getTodoById(1L);

        assertTrue(result.isPresent());
        assertEquals(todo, result.get());
    }
    @Test
    void shouldSaveTodo() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.save(todo)).thenReturn(todo);

        assertEquals(todo, todoService.saveTodo(todo));
        verify(todoRepository).save(todo);
    }
    @Test
    void shouldThrowExceptionWhenCreatingTodoWithNullDescription() {
        assertThrows(IllegalArgumentException.class, () -> todoService.createTodo(null));
    }
    @Test
    void shouldThrowExceptionWhenCreatingTodoWithEmptyDescription() {
        assertThrows(IllegalArgumentException.class, () -> todoService.createTodo("   "));
    }  
    @Test
    void shouldCreateTodo() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        assertNotNull(todoService.createTodo("  Task 1  "));
        verify(todoRepository).save(any(Todo.class));
    }
    
    @Test
    void shouldDeleteTodo() {
        assertDoesNotThrow(() -> todoService.deleteTodo(1L));
        verify(todoRepository).deleteById(1L);
    }
    
    @Test
    void shouldThrowExceptionWhenMarkingNonExistentTodoComplete() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> todoService.markTodoComplete(1L));
    }
    
    @Test
    void shouldMarkTodoComplete() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        assertTrue(todoService.markTodoComplete(1L).isDone());
        verify(todoRepository).save(todo);
    }
    
    @Test
    void shouldMarkTodoIncomplete() {
        Todo todo = new Todo("Task 1");
        todo.setDone(true);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        assertFalse(todoService.markTodoIncomplete(1L).isDone());
        verify(todoRepository).save(todo);
    }
    
    @Test
    void shouldThrowExceptionWhenMarkingNonExistentTodoIncomplete() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> todoService.markTodoIncomplete(1L));
    }
}

