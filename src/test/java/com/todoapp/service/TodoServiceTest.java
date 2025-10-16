package com.todoapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}
