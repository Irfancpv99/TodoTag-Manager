package com.todoapp.gui;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainFrameControllerTest {

    @Mock
    private TodoService service;
    private MainFrameController controller;

    @BeforeEach
    void setUp() {
        controller = new MainFrameController(service);
    }

    @Test
    void addTodo_validDescription_createsTodo() {
        Todo todo = mockTodo(1L, "Task");
        when(service.createTodo("Task")).thenReturn(todo);

        assertEquals(todo, controller.addTodo("Task"));
        verify(service).createTodo("Task");
    }

    @Test
    void addTodo_emptyOrNull_returnsNull() {
        assertNull(controller.addTodo(""));
        assertNull(controller.addTodo(null));
        assertNull(controller.addTodo("   "));
        verify(service, never()).createTodo(anyString());
    }
    
    @Test
    void addTag_validName_createsTag() {
        Tag tag = mockTag(1L, "urgent");
        when(service.findTagByName("urgent")).thenReturn(Optional.empty());
        when(service.createTag("urgent")).thenReturn(tag);

        assertEquals(tag, controller.addTag("urgent"));
        verify(service).createTag("urgent");
    }

    @Test
    void addTag_emptyNullOrExists_returnsNull() {
        when(service.findTagByName("urgent")).thenReturn(Optional.of(mockTag(1L, "urgent")));

        assertNull(controller.addTag(""));
        assertNull(controller.addTag(null));
        assertNull(controller.addTag("urgent"));
        verify(service, never()).createTag(anyString());
    }

    private Todo mockTodo(Long id, String description) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setDescription(description);
        return todo;
    }

    private Tag mockTag(Long id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }
}