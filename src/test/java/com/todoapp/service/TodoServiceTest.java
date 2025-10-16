package com.todoapp.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

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
}
