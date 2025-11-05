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
    
    @Test
    void deleteTodo_validId_deletesAndReturnsTrue() {
        assertTrue(controller.deleteTodo(1L));
        verify(service).deleteTodo(1L);
    }

    @Test
    void deleteTodo_nullId_returnsFalse() {
        assertFalse(controller.deleteTodo(null));
        verify(service, never()).deleteTodo(anyLong());
    }
    
    @Test
    void updateTodoDescription_validData_updatesAndReturnsTrue() {
        Todo todo = mockTodo(1L, "Old");
        when(service.getTodoById(1L)).thenReturn(Optional.of(todo));

        assertTrue(controller.updateTodoDescription(1L, "New"));
        assertEquals("New", todo.getDescription());
        verify(service).saveTodo(todo);
    }

    @Test
    void updateTodoDescription_invalidInputs_returnsFalse() {
        when(service.getTodoById(1L)).thenReturn(Optional.empty());

        assertFalse(controller.updateTodoDescription(null, "New"));
        assertFalse(controller.updateTodoDescription(1L, ""));
        assertFalse(controller.updateTodoDescription(1L, null));
        assertFalse(controller.updateTodoDescription(1L, "New"));
        verify(service, never()).saveTodo(any());
    }
    
    @Test
    void toggleTodoDone_incompleteTodo_marksComplete() {
        Todo todo = mockTodo(1L, "Task");
        when(service.getTodoById(1L)).thenReturn(Optional.of(todo));

        assertTrue(controller.toggleTodoDone(1L));
        verify(service).markTodoComplete(1L);
    }

    @Test
    void toggleTodoDone_completeTodo_marksIncomplete() {
        Todo todo = mockTodo(1L, "Task");
        todo.setDone(true);
        when(service.getTodoById(1L)).thenReturn(Optional.of(todo));

        assertFalse(controller.toggleTodoDone(1L));
        verify(service).markTodoIncomplete(1L);
    }

    @Test
    void toggleTodoDone_nullId_returnsFalse() {
        assertFalse(controller.toggleTodoDone(null));
        verify(service, never()).getTodoById(anyLong());
        verify(service, never()).markTodoComplete(anyLong());
        verify(service, never()).markTodoIncomplete(anyLong());
    }

    @Test
    void toggleTodoDone_todoNotFound_returnsFalse() {
        when(service.getTodoById(1L)).thenReturn(Optional.empty());

        assertFalse(controller.toggleTodoDone(1L));
        verify(service).getTodoById(1L);
        verify(service, never()).markTodoComplete(anyLong());
        verify(service, never()).markTodoIncomplete(anyLong());
    }
    
    @Test
    void searchTodos_withKeyword_returnsFiltered() {
        List<Todo> results = List.of(mockTodo(1L, "Test"));
        when(service.searchTodos("test")).thenReturn(results);

        assertEquals(results, controller.searchTodos("test"));
        verify(service).searchTodos("test");
    }

    @Test
    void searchTodos_emptyOrNull_returnsAll() {
        List<Todo> all = List.of(mockTodo(1L, "Task"));
        when(service.getAllTodos()).thenReturn(all);

        assertEquals(all, controller.searchTodos(""));
        assertEquals(all, controller.searchTodos(null));
        assertEquals(all, controller.searchTodos("   "));
        verify(service, times(3)).getAllTodos();
    }
    
    @Test
    void getAllTodos_returnsAllTodos() {
        List<Todo> todos = List.of(mockTodo(1L, "Task"));
        when(service.getAllTodos()).thenReturn(todos);

        assertEquals(todos, controller.getAllTodos());
    }

    @Test
    void getAllTags_returnsAllTags() {
        List<Tag> tags = List.of(mockTag(1L, "urgent"));
        when(service.getAllTags()).thenReturn(tags);

        assertEquals(tags, controller.getAllTags());
    }
    
    @Test
    void addTagToTodo_validIds_addsAndReturnsTrue() {
        assertTrue(controller.addTagToTodo(1L, 2L));
        verify(service).addTagToTodo(1L, 2L);
    }

    @Test
    void addTagToTodo_nullIds_returnsFalse() {
        assertFalse(controller.addTagToTodo(null, 1L));
        assertFalse(controller.addTagToTodo(1L, null));
        assertFalse(controller.addTagToTodo(null, null));
        verify(service, never()).addTagToTodo(anyLong(), anyLong());
    }
    
    @Test
    void removeTagFromTodo_validIds_removesAndReturnsTrue() {
        assertTrue(controller.removeTagFromTodo(1L, 2L));
        verify(service).removeTagFromTodo(1L, 2L);
    }

    @Test
    void removeTagFromTodo_nullIds_returnsFalse() {
        assertFalse(controller.removeTagFromTodo(null, 1L));
        assertFalse(controller.removeTagFromTodo(1L, null));
        assertFalse(controller.removeTagFromTodo(null, null));
        verify(service, never()).removeTagFromTodo(anyLong(), anyLong());
    }
    
    @Test
    void deleteTag_validId_deletesAndReturnsTrue() {
        assertTrue(controller.deleteTag(1L));
        verify(service).deleteTag(1L);
    }

    @Test
    void deleteTag_nullId_returnsFalse() {
        assertFalse(controller.deleteTag(null));
        verify(service, never()).deleteTag(anyLong());
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