package com.todoapp.gui;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for MainFrame GUI.
 * Note: This is excluded from PIT mutation testing.
 * Use MainFrameControllerTest for mutation coverage.
 */
@ExtendWith(MockitoExtension.class)
class MainFrameTest {

    @Mock
    private TodoService mockTodoService;

    private MainFrame mainFrame;
    private JTextField todoDescriptionField;
    private JTextField tagNameField;
    private JTextField searchField;
    private JTable todoTable;

    @BeforeEach
    void setUp() throws Exception {
        mainFrame = new MainFrame();
        injectMockService();
        extractFields();
    }

    private void injectMockService() throws Exception {
        Field controllerField = MainFrame.class.getDeclaredField("controller");
        controllerField.setAccessible(true);
        MainFrameController controller = new MainFrameController(mockTodoService);
        controllerField.set(mainFrame, controller);
    }

    private void extractFields() throws Exception {
        todoDescriptionField = getPrivateField("todoDescriptionField");
        tagNameField = getPrivateField("tagNameField");
        searchField = getPrivateField("searchField");
        todoTable = getPrivateField("todoTable");
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(String fieldName) throws Exception {
        Field field = MainFrame.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(mainFrame);
    }

    @Test
    void testAddTodo_WithValidDescription_CreatesTodo() throws Exception {
        String description = "Test Todo";
        todoDescriptionField.setText(description);
        when(mockTodoService.createTodo(description)).thenReturn(createMockTodo(1L, description));

        mainFrame.addTodo();
        waitForSwingThread();

        verify(mockTodoService).createTodo(description);
        assertEquals("", todoDescriptionField.getText());
    }

    @Test
    void testAddTodo_WithEmptyDescription_DoesNothing() {
        todoDescriptionField.setText("");

        mainFrame.addTodo();

        verify(mockTodoService, never()).createTodo(anyString());
    }

    @Test
    void testAddTag_WithValidName_CreatesTag() throws Exception {
        String tagName = "urgent";
        tagNameField.setText(tagName);
        when(mockTodoService.findTagByName(tagName)).thenReturn(Optional.empty());
        when(mockTodoService.createTag(tagName)).thenReturn(createMockTag(1L, tagName));

        mainFrame.addTag();
        waitForSwingThread();

        verify(mockTodoService).createTag(tagName);
        assertEquals("", tagNameField.getText());
    }

    @Test
    void testAddTag_WithEmptyName_DoesNothing() {
        tagNameField.setText("");

        mainFrame.addTag();

        verify(mockTodoService, never()).createTag(anyString());
    }

    @Test
    void testAddTag_WhenTagExists_DoesNotCreateDuplicate() {
        String tagName = "urgent";
        tagNameField.setText(tagName);
        when(mockTodoService.findTagByName(tagName)).thenReturn(Optional.of(createMockTag(1L, tagName)));

        mainFrame.addTag();

        verify(mockTodoService, never()).createTag(anyString());
    }

    @Test
    void testDeleteTodo_WithSelectedTodo_DeletesTodo() throws Exception {
        Todo todo = createMockTodo(1L, "Test");
        selectTodoInTable(todo);

        mainFrame.deleteTodo();
        waitForSwingThread();

        verify(mockTodoService).deleteTodo(1L);
    }

    @Test
    void testDeleteTodo_WithNoSelection_DoesNothing() {
        mainFrame.deleteTodo();

        verify(mockTodoService, never()).deleteTodo(anyLong());
    }

    @Test
    void testToggleTodoDone_MarksIncompleteAsComplete() throws Exception {
        Todo todo = createMockTodo(1L, "Test");
        todo.setDone(false);
        when(mockTodoService.getTodoById(1L)).thenReturn(Optional.of(todo));
        selectTodoInTable(todo);

        mainFrame.toggleTodoDone();
        waitForSwingThread();

        verify(mockTodoService).markTodoComplete(1L);
    }

    @Test
    void testToggleTodoDone_MarksCompleteAsIncomplete() throws Exception {
        Todo todo = createMockTodo(1L, "Test");
        todo.setDone(true);
        when(mockTodoService.getTodoById(1L)).thenReturn(Optional.of(todo));
        selectTodoInTable(todo);

        mainFrame.toggleTodoDone();
        waitForSwingThread();

        verify(mockTodoService).markTodoIncomplete(1L);
    }

    @Test
    void testSearchTodos_WithKeyword_ReturnsFilteredResults() {
        String keyword = "test";
        searchField.setText(keyword);
        List<Todo> results = Arrays.asList(createMockTodo(1L, "Test Todo"));
        when(mockTodoService.searchTodos(keyword)).thenReturn(results);

        mainFrame.searchTodos();

        verify(mockTodoService).searchTodos(keyword);
    }

    @Test
    void testSearchTodos_WithEmptyKeyword_ShowsAllTodos() throws Exception {
        searchField.setText("");
        when(mockTodoService.getAllTodos()).thenReturn(Arrays.asList());

        mainFrame.searchTodos();
        waitForSwingThread();

        verify(mockTodoService).getAllTodos();
    }

    @Test
    void testShowAllTodos_ClearsSearchAndRefreshes() throws Exception {
        searchField.setText("test");
        when(mockTodoService.getAllTodos()).thenReturn(Arrays.asList());

        mainFrame.showAllTodos();
        waitForSwingThread();

        verify(mockTodoService).getAllTodos();
        assertEquals("", searchField.getText());
    }

    @Test
    void testAddTagToTodo_WithBothSelected_AddsTag() throws Exception {
        Todo todo = createMockTodo(1L, "Test");
        Tag tag = createMockTag(1L, "urgent");
        selectTodoInTable(todo);
        selectTagInAvailableList(tag);

        mainFrame.addTagToTodo();
        waitForSwingThread();

        verify(mockTodoService).addTagToTodo(1L, 1L);
    }

    @Test
    void testRemoveTagFromTodo_WithBothSelected_RemovesTag() throws Exception {
        Todo todo = createMockTodo(1L, "Test");
        Tag tag = createMockTag(1L, "urgent");
        selectTodoInTable(todo);
        selectTagInTodoList(tag);

        mainFrame.removeTagFromTodo();
        waitForSwingThread();

        verify(mockTodoService).removeTagFromTodo(1L, 1L);
    }

    @Test
    void testDeleteTag_WithSelectedTag_DeletesTag() throws Exception {
        Tag tag = createMockTag(1L, "urgent");
        selectTagInAvailableList(tag);

        mainFrame.deleteTag();
        waitForSwingThread();

        verify(mockTodoService).deleteTag(1L);
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

    private void selectTodoInTable(Todo todo) throws Exception {
        Object tableModel = getPrivateField("todoTableModel");
        Field todosField = tableModel.getClass().getDeclaredField("todos");
        todosField.setAccessible(true);
        List<Todo> todos = Arrays.asList(todo);
        todosField.set(tableModel, todos);
        
        SwingUtilities.invokeAndWait(() -> {
            todoTable.setRowSelectionInterval(0, 0);
        });
    }

    private void selectTagInAvailableList(Tag tag) throws Exception {
        JList<Tag> availableTagsList = getPrivateField("availableTagsList");
        DefaultListModel<Tag> model = (DefaultListModel<Tag>) availableTagsList.getModel();
        SwingUtilities.invokeAndWait(() -> {
            model.addElement(tag);
            availableTagsList.setSelectedIndex(0);
        });
    }

    private void selectTagInTodoList(Tag tag) throws Exception {
        JList<Tag> tagList = getPrivateField("tagList");
        DefaultListModel<Tag> model = (DefaultListModel<Tag>) tagList.getModel();
        SwingUtilities.invokeAndWait(() -> {
            model.addElement(tag);
            tagList.setSelectedIndex(0);
        });
    }

    private void waitForSwingThread() throws Exception {
        SwingUtilities.invokeAndWait(() -> {});
        Thread.sleep(50);
    }
}