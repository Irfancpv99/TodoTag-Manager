package com.todoapp.gui;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainFrameControllerTest {

    @Mock
    private TodoService mockTodoService;

    private MainFrameController controller;

    @BeforeEach
    void setUp() {
        controller = new MainFrameController(mockTodoService);
    }

    @Test
    void testAddTodo_WithValidDescription_CreatesTodo() {
        String description = "Test Todo";
        Todo mockTodo = createMockTodo(1L, description);
        when(mockTodoService.createTodo(description)).thenReturn(mockTodo);

        Todo result = controller.addTodo(description);

        assertNotNull(result);
        assertEquals(mockTodo, result);
        verify(mockTodoService).createTodo(description);
    }

    @Test
    void testAddTodo_WithEmptyDescription_ReturnsNull() {
        Todo result = controller.addTodo("");

        assertNull(result);
        verify(mockTodoService, never()).createTodo(anyString());
    }

    @Test
    void testAddTodo_WithNullDescription_ReturnsNull() {
        Todo result = controller.addTodo(null);

        assertNull(result);
        verify(mockTodoService, never()).createTodo(anyString());
    }

    @Test
    void testAddTodo_TrimsWhitespace() {
        String description = "  Test Todo  ";
        Todo mockTodo = createMockTodo(1L, "Test Todo");
        when(mockTodoService.createTodo("Test Todo")).thenReturn(mockTodo);

        Todo result = controller.addTodo(description);

        assertNotNull(result);
        verify(mockTodoService).createTodo("Test Todo");
    }

    @Test
    void testAddTag_WithValidName_CreatesTag() {
        String tagName = "urgent";
        Tag mockTag = createMockTag(1L, tagName);
        when(mockTodoService.findTagByName(tagName)).thenReturn(Optional.empty());
        when(mockTodoService.createTag(tagName)).thenReturn(mockTag);

        Tag result = controller.addTag(tagName);

        assertNotNull(result);
        assertEquals(mockTag, result);
        verify(mockTodoService).findTagByName(tagName);
        verify(mockTodoService).createTag(tagName);
    }

    @Test
    void testAddTag_WithEmptyName_ReturnsNull() {
        Tag result = controller.addTag("");

        assertNull(result);
        verify(mockTodoService, never()).createTag(anyString());
    }

    @Test
    void testAddTag_WithNullName_ReturnsNull() {
        Tag result = controller.addTag(null);

        assertNull(result);
        verify(mockTodoService, never()).createTag(anyString());
    }

    @Test
    void testAddTag_WhenTagExists_ReturnsNull() {
        String tagName = "urgent";
        when(mockTodoService.findTagByName(tagName)).thenReturn(Optional.of(createMockTag(1L, tagName)));

        Tag result = controller.addTag(tagName);

        assertNull(result);
        verify(mockTodoService).findTagByName(tagName);
        verify(mockTodoService, never()).createTag(anyString());
    }

    @Test
    void testAddTag_TrimsWhitespace() {
        String tagName = "  urgent  ";
        Tag mockTag = createMockTag(1L, "urgent");
        when(mockTodoService.findTagByName("urgent")).thenReturn(Optional.empty());
        when(mockTodoService.createTag("urgent")).thenReturn(mockTag);

        Tag result = controller.addTag(tagName);

        assertNotNull(result);
        verify(mockTodoService).findTagByName("urgent");
        verify(mockTodoService).createTag("urgent");
    }

    @Test
    void testDeleteTodo_WithValidId_DeletesTodo() {
        Long todoId = 1L;

        boolean result = controller.deleteTodo(todoId);

        assertTrue(result);
        verify(mockTodoService).deleteTodo(todoId);
    }

    @Test
    void testDeleteTodo_WithNullId_ReturnsFalse() {
        boolean result = controller.deleteTodo(null);

        assertFalse(result);
        verify(mockTodoService, never()).deleteTodo(anyLong());
    }

    @Test
    void testUpdateTodoDescription_WithValidData_UpdatesTodo() {
        Long todoId = 1L;
        String newDescription = "Updated Description";
        Todo todo = createMockTodo(todoId, "Old Description");
        when(mockTodoService.getTodoById(todoId)).thenReturn(Optional.of(todo));

        boolean result = controller.updateTodoDescription(todoId, newDescription);

        assertTrue(result);
        assertEquals(newDescription, todo.getDescription());
        verify(mockTodoService).getTodoById(todoId);
        verify(mockTodoService).saveTodo(todo);
    }

    @Test
    void testUpdateTodoDescription_WithNullId_ReturnsFalse() {
        boolean result = controller.updateTodoDescription(null, "New Description");

        assertFalse(result);
        verify(mockTodoService, never()).saveTodo(any());
    }

    @Test
    void testUpdateTodoDescription_WithEmptyDescription_ReturnsFalse() {
        boolean result = controller.updateTodoDescription(1L, "");

        assertFalse(result);
        verify(mockTodoService, never()).saveTodo(any());
    }

    @Test
    void testUpdateTodoDescription_WithNullDescription_ReturnsFalse() {
        boolean result = controller.updateTodoDescription(1L, null);

        assertFalse(result);
        verify(mockTodoService, never()).saveTodo(any());
    }

    @Test
    void testUpdateTodoDescription_WhenTodoNotFound_ReturnsFalse() {
        Long todoId = 1L;
        when(mockTodoService.getTodoById(todoId)).thenReturn(Optional.empty());

        boolean result = controller.updateTodoDescription(todoId, "New Description");

        assertFalse(result);
        verify(mockTodoService, never()).saveTodo(any());
    }

    @Test
    void testUpdateTodoDescription_TrimsWhitespace() {
        Long todoId = 1L;
        String newDescription = "  Updated Description  ";
        Todo todo = createMockTodo(todoId, "Old Description");
        when(mockTodoService.getTodoById(todoId)).thenReturn(Optional.of(todo));

        boolean result = controller.updateTodoDescription(todoId, newDescription);

        assertTrue(result);
        assertEquals("Updated Description", todo.getDescription());
        verify(mockTodoService).saveTodo(todo);
    }

    @Test
    void testDeleteTag_WithValidId_DeletesTag() {
        Long tagId = 1L;

        boolean result = controller.deleteTag(tagId);

        assertTrue(result);
        verify(mockTodoService).deleteTag(tagId);
    }

    @Test
    void testDeleteTag_WithNullId_ReturnsFalse() {
        boolean result = controller.deleteTag(null);

        assertFalse(result);
        verify(mockTodoService, never()).deleteTag(anyLong());
    }

    @Test
    void testToggleTodoDone_MarksIncompleteAsComplete() {
        Long todoId = 1L;
        Todo todo = createMockTodo(todoId, "Test");
        todo.setDone(false);
        when(mockTodoService.getTodoById(todoId)).thenReturn(Optional.of(todo));

        Boolean result = controller.toggleTodoDone(todoId);

        assertTrue(result);
        verify(mockTodoService).getTodoById(todoId);
        verify(mockTodoService).markTodoComplete(todoId);
        verify(mockTodoService, never()).markTodoIncomplete(anyLong());
    }

    @Test
    void testToggleTodoDone_MarksCompleteAsIncomplete() {
        Long todoId = 1L;
        Todo todo = createMockTodo(todoId, "Test");
        todo.setDone(true);
        when(mockTodoService.getTodoById(todoId)).thenReturn(Optional.of(todo));

        Boolean result = controller.toggleTodoDone(todoId);

        assertFalse(result);
        verify(mockTodoService).getTodoById(todoId);
        verify(mockTodoService).markTodoIncomplete(todoId);
        verify(mockTodoService, never()).markTodoComplete(anyLong());
    }

    @Test
    void testToggleTodoDone_WithNullId_ReturnsNull() {
        Boolean result = controller.toggleTodoDone(null);

        assertNull(result);
        verify(mockTodoService, never()).getTodoById(anyLong());
    }

    @Test
    void testToggleTodoDone_WhenTodoNotFound_ReturnsNull() {
        Long todoId = 1L;
        when(mockTodoService.getTodoById(todoId)).thenReturn(Optional.empty());

        Boolean result = controller.toggleTodoDone(todoId);

        assertNull(result);
        verify(mockTodoService, never()).markTodoComplete(anyLong());
        verify(mockTodoService, never()).markTodoIncomplete(anyLong());
    }

    @Test
    void testSearchTodos_WithKeyword_ReturnsFilteredResults() {
        String keyword = "test";
        List<Todo> results = Arrays.asList(createMockTodo(1L, "Test Todo"));
        when(mockTodoService.searchTodos(keyword)).thenReturn(results);

        List<Todo> result = controller.searchTodos(keyword);

        assertEquals(results, result);
        verify(mockTodoService).searchTodos(keyword);
        verify(mockTodoService, never()).getAllTodos();
    }

    @Test
    void testSearchTodos_WithEmptyKeyword_ReturnsAllTodos() {
        List<Todo> allTodos = Arrays.asList(
            createMockTodo(1L, "Todo 1"),
            createMockTodo(2L, "Todo 2")
        );
        when(mockTodoService.getAllTodos()).thenReturn(allTodos);

        List<Todo> result = controller.searchTodos("");

        assertEquals(allTodos, result);
        verify(mockTodoService).getAllTodos();
        verify(mockTodoService, never()).searchTodos(anyString());
    }

    @Test
    void testSearchTodos_WithNullKeyword_ReturnsAllTodos() {
        List<Todo> allTodos = Arrays.asList(createMockTodo(1L, "Todo 1"));
        when(mockTodoService.getAllTodos()).thenReturn(allTodos);

        List<Todo> result = controller.searchTodos(null);

        assertEquals(allTodos, result);
        verify(mockTodoService).getAllTodos();
    }

    @Test
    void testSearchTodos_TrimsWhitespace() {
        String keyword = "  test  ";
        List<Todo> results = Arrays.asList(createMockTodo(1L, "Test Todo"));
        when(mockTodoService.searchTodos("test")).thenReturn(results);

        List<Todo> result = controller.searchTodos(keyword);

        assertEquals(results, result);
        verify(mockTodoService).searchTodos("test");
    }

    @Test
    void testSearchTodos_WithWhitespaceOnly_ReturnsAllTodos() {
        List<Todo> allTodos = Arrays.asList(createMockTodo(1L, "Todo 1"));
        when(mockTodoService.getAllTodos()).thenReturn(allTodos);

        List<Todo> result = controller.searchTodos("   ");

        assertEquals(allTodos, result);
        verify(mockTodoService).getAllTodos();
        verify(mockTodoService, never()).searchTodos(anyString());
    }

    @Test
    void testGetAllTodos_ReturnsAllTodos() {
        List<Todo> allTodos = Arrays.asList(
            createMockTodo(1L, "Todo 1"),
            createMockTodo(2L, "Todo 2")
        );
        when(mockTodoService.getAllTodos()).thenReturn(allTodos);

        List<Todo> result = controller.getAllTodos();

        assertEquals(allTodos, result);
        verify(mockTodoService).getAllTodos();
    }

    @Test
    void testGetAllTags_ReturnsAllTags() {
        List<Tag> allTags = Arrays.asList(
            createMockTag(1L, "urgent"),
            createMockTag(2L, "important")
        );
        when(mockTodoService.getAllTags()).thenReturn(allTags);

        List<Tag> result = controller.getAllTags();

        assertEquals(allTags, result);
        verify(mockTodoService).getAllTags();
    }

    @Test
    void testAddTagToTodo_WithValidIds_AddsTag() {
        Long todoId = 1L;
        Long tagId = 2L;

        boolean result = controller.addTagToTodo(todoId, tagId);

        assertTrue(result);
        verify(mockTodoService).addTagToTodo(todoId, tagId);
    }

    @Test
    void testAddTagToTodo_WithNullTodoId_ReturnsFalse() {
        boolean result = controller.addTagToTodo(null, 1L);

        assertFalse(result);
        verify(mockTodoService, never()).addTagToTodo(anyLong(), anyLong());
    }

    @Test
    void testAddTagToTodo_WithNullTagId_ReturnsFalse() {
        boolean result = controller.addTagToTodo(1L, null);

        assertFalse(result);
        verify(mockTodoService, never()).addTagToTodo(anyLong(), anyLong());
    }

    @Test
    void testAddTagToTodo_WithBothNullIds_ReturnsFalse() {
        boolean result = controller.addTagToTodo(null, null);

        assertFalse(result);
        verify(mockTodoService, never()).addTagToTodo(anyLong(), anyLong());
    }

    @Test
    void testRemoveTagFromTodo_WithValidIds_RemovesTag() {
        Long todoId = 1L;
        Long tagId = 2L;

        boolean result = controller.removeTagFromTodo(todoId, tagId);

        assertTrue(result);
        verify(mockTodoService).removeTagFromTodo(todoId, tagId);
    }

    @Test
    void testRemoveTagFromTodo_WithNullTodoId_ReturnsFalse() {
        boolean result = controller.removeTagFromTodo(null, 1L);

        assertFalse(result);
        verify(mockTodoService, never()).removeTagFromTodo(anyLong(), anyLong());
    }

    @Test
    void testRemoveTagFromTodo_WithNullTagId_ReturnsFalse() {
        boolean result = controller.removeTagFromTodo(1L, null);

        assertFalse(result);
        verify(mockTodoService, never()).removeTagFromTodo(anyLong(), anyLong());
    }

    @Test
    void testRemoveTagFromTodo_WithBothNullIds_ReturnsFalse() {
        boolean result = controller.removeTagFromTodo(null, null);

        assertFalse(result);
        verify(mockTodoService, never()).removeTagFromTodo(anyLong(), anyLong());
    }

    // Helper methods

    private Todo createMockTodo(Long id, String description) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setDescription(description);
        todo.setDone(false);
        return todo;
    }

    private Tag createMockTag(Long id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }
}