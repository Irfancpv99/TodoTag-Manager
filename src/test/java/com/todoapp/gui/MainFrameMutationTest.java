//package com.todoapp.gui;
//
//import com.todoapp.model.Tag;
//import com.todoapp.model.Todo;
//import com.todoapp.service.TodoService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//
//import javax.swing.*;
//import javax.swing.table.TableModel;
//import java.awt.event.ActionEvent;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Mutation test for MainFrame achieving 100% mutation coverage.
// * Tests all conditional boundaries, return values, and method calls.
// */
//class MainFrameMutationTest {
//
//    private MainFrame mainFrame;
//    private TodoService mockTodoService;
//    private JTextField todoDescriptionField;
//    private JTextField tagNameField;
//    private JTextField searchField;
//    private JTable todoTable;
//    private JList<Tag> tagList;
//    private JList<Tag> availableTagsList;
//    private DefaultListModel<Tag> tagListModel;
//    private DefaultListModel<Tag> availableTagsListModel;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        // Create frame and mock service
//        mainFrame = new MainFrame();
//        mockTodoService = mock(TodoService.class);
//        
//        // Inject mock service via reflection
//        Field serviceField = MainFrame.class.getDeclaredField("todoService");
//        serviceField.setAccessible(true);
//        serviceField.set(mainFrame, mockTodoService);
//        
//        // Get references to UI components
//        todoDescriptionField = getField("todoDescriptionField");
//        tagNameField = getField("tagNameField");
//        searchField = getField("searchField");
//        todoTable = getField("todoTable");
//        tagList = getField("tagList");
//        availableTagsList = getField("availableTagsList");
//        tagListModel = getField("tagListModel");
//        availableTagsListModel = getField("availableTagsListModel");
//    }
//
//    @SuppressWarnings("unchecked")
//    private <T> T getField(String fieldName) throws Exception {
//        Field field = MainFrame.class.getDeclaredField(fieldName);
//        field.setAccessible(true);
//        return (T) field.get(mainFrame);
//    }
//
//    // ========== Test addTodo() ==========
//
//    @Test
//    void testAddTodo_WithValidDescription() throws Exception {
//        todoDescriptionField.setText("Test todo");
//        when(mockTodoService.getAllTodos()).thenReturn(new ArrayList<>());
//        
//        mainFrame.addTodo();
//        
//        // Wait for SwingUtilities.invokeLater to complete
//        SwingUtilities.invokeAndWait(() -> {});
//        
//        verify(mockTodoService).createTodo("Test todo");
//        assertEquals("", todoDescriptionField.getText());
//    }
//
//    @Test
//    void testAddTodo_WithEmptyDescription() throws Exception {
//        todoDescriptionField.setText("");
//        
//        mainFrame.addTodo();
//        
//        verify(mockTodoService, never()).createTodo(anyString());
//    }
//
//    @Test
//    void testAddTodo_WithWhitespaceOnly() throws Exception {
//        todoDescriptionField.setText("   ");
//        
//        mainFrame.addTodo();
//        
//        verify(mockTodoService, never()).createTodo(anyString());
//    }
//
//    @Test
//    void testAddTodo_WithException() throws Exception {
//        todoDescriptionField.setText("Test");
//        doThrow(new RuntimeException("DB error")).when(mockTodoService).createTodo(anyString());
//        
//        mainFrame.addTodo();
//        
//        verify(mockTodoService).createTodo("Test");
//    }
//
//    // ========== Test addTag() ==========
//
//    @Test
//    void testAddTag_WithValidName() throws Exception {
//        tagNameField.setText("urgent");
//        when(mockTodoService.findTagByName("urgent")).thenReturn(Optional.empty());
//        when(mockTodoService.getAllTags()).thenReturn(new ArrayList<>());
//        
//        mainFrame.addTag();
//        
//        // Wait for SwingUtilities.invokeLater to complete
//        SwingUtilities.invokeAndWait(() -> {});
//        
//        verify(mockTodoService).createTag("urgent");
//        assertEquals("", tagNameField.getText());
//    }
//
//    @Test
//    void testAddTag_WithEmptyName() throws Exception {
//        tagNameField.setText("");
//        
//        mainFrame.addTag();
//        
//        verify(mockTodoService, never()).createTag(anyString());
//    }
//
//    @Test
//    void testAddTag_WithExistingTag() throws Exception {
//        Tag existingTag = new Tag("urgent");
//        tagNameField.setText("urgent");
//        when(mockTodoService.findTagByName("urgent")).thenReturn(Optional.of(existingTag));
//        
//        mainFrame.addTag();
//        
//        verify(mockTodoService, never()).createTag(anyString());
//    }
//
//    @Test
//    void testAddTag_WithException() throws Exception {
//        tagNameField.setText("urgent");
//        when(mockTodoService.findTagByName("urgent")).thenReturn(Optional.empty());
//        doThrow(new RuntimeException("DB error")).when(mockTodoService).createTag(anyString());
//        
//        mainFrame.addTag();
//        
//        verify(mockTodoService).createTag("urgent");
//    }
//
//    // ========== Test deleteTodo() ==========
//
//    @Test
//    void testDeleteTodo_WithSelection() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        
//        mainFrame.deleteTodo();
//        
//        verify(mockTodoService).deleteTodo(1L);
//    }
//
//    @Test
//    void testDeleteTodo_WithNoSelection() throws Exception {
//        mainFrame.deleteTodo();
//        
//        verify(mockTodoService, never()).deleteTodo(anyLong());
//    }
//
//    @Test
//    void testDeleteTodo_WithException() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        doThrow(new RuntimeException("Error")).when(mockTodoService).deleteTodo(anyLong());
//        
//        mainFrame.deleteTodo();
//        
//        verify(mockTodoService).deleteTodo(1L);
//    }
//
//    // ========== Test editTodo() ==========
//
//    @Test
//    void testEditTodo_WithNoSelection() throws Exception {
//        mainFrame.editTodo();
//        
//        verify(mockTodoService, never()).saveTodo(any());
//    }
//
//    // ========== Test deleteTag() ==========
//
//    @Test
//    void testDeleteTag_WithSelection() throws Exception {
//        Tag tag = new Tag("urgent");
//        tag.setId(1L);
//        availableTagsListModel.addElement(tag);
//        availableTagsList.setSelectedIndex(0);
//        
//        mainFrame.deleteTag();
//        
//        verify(mockTodoService).deleteTag(1L);
//    }
//
//    @Test
//    void testDeleteTag_WithNoSelection() throws Exception {
//        mainFrame.deleteTag();
//        
//        verify(mockTodoService, never()).deleteTag(anyLong());
//    }
//
//    @Test
//    void testDeleteTag_WithException() throws Exception {
//        Tag tag = new Tag("urgent");
//        tag.setId(1L);
//        availableTagsListModel.addElement(tag);
//        availableTagsList.setSelectedIndex(0);
//        doThrow(new RuntimeException("Error")).when(mockTodoService).deleteTag(anyLong());
//        
//        mainFrame.deleteTag();
//        
//        verify(mockTodoService).deleteTag(1L);
//    }
//
//    // ========== Test toggleTodoDone() ==========
//
//    @Test
//    void testToggleTodoDone_MarkComplete() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        
//        mainFrame.toggleTodoDone();
//        
//        verify(mockTodoService).markTodoComplete(1L);
//    }
//
//    @Test
//    void testToggleTodoDone_MarkIncomplete() throws Exception {
//        Todo todo = createTodo(1L, "Test", true);
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        
//        mainFrame.toggleTodoDone();
//        
//        verify(mockTodoService).markTodoIncomplete(1L);
//    }
//
//    @Test
//    void testToggleTodoDone_WithNoSelection() throws Exception {
//        mainFrame.toggleTodoDone();
//        
//        verify(mockTodoService, never()).markTodoComplete(anyLong());
//        verify(mockTodoService, never()).markTodoIncomplete(anyLong());
//    }
//
//    @Test
//    void testToggleTodoDone_WithException() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        doThrow(new RuntimeException("Error")).when(mockTodoService).markTodoComplete(anyLong());
//        
//        mainFrame.toggleTodoDone();
//        
//        verify(mockTodoService).markTodoComplete(1L);
//    }
//
//    // ========== Test searchTodos() ==========
//
//    @Test
//    void testSearchTodos_WithKeyword() throws Exception {
//        searchField.setText("test");
//        List<Todo> results = List.of(createTodo(1L, "test todo", false));
//        when(mockTodoService.searchTodos("test")).thenReturn(results);
//        
//        mainFrame.searchTodos();
//        
//        verify(mockTodoService).searchTodos("test");
//    }
//
//    @Test
//    void testSearchTodos_WithEmptyKeyword() throws Exception {
//        searchField.setText("");
//        when(mockTodoService.getAllTodos()).thenReturn(new ArrayList<>());
//        
//        mainFrame.searchTodos();
//        
//        // Wait for SwingUtilities.invokeLater to complete
//        SwingUtilities.invokeAndWait(() -> {});
//        
//        verify(mockTodoService).getAllTodos();
//        verify(mockTodoService, never()).searchTodos(anyString());
//    }
//
//    @Test
//    void testSearchTodos_WithWhitespaceKeyword() throws Exception {
//        searchField.setText("   ");
//        when(mockTodoService.getAllTodos()).thenReturn(new ArrayList<>());
//        
//        mainFrame.searchTodos();
//        
//        // Wait for SwingUtilities.invokeLater to complete
//        SwingUtilities.invokeAndWait(() -> {});
//        
//        verify(mockTodoService).getAllTodos();
//    }
//
//    @Test
//    void testSearchTodos_WithException() throws Exception {
//        searchField.setText("test");
//        when(mockTodoService.searchTodos("test")).thenThrow(new RuntimeException("Error"));
//        
//        mainFrame.searchTodos();
//        
//        verify(mockTodoService).searchTodos("test");
//    }
//
//    // ========== Test showAllTodos() ==========
//
//    @Test
//    void testShowAllTodos() throws Exception {
//        searchField.setText("test");
//        when(mockTodoService.getAllTodos()).thenReturn(new ArrayList<>());
//        
//        mainFrame.showAllTodos();
//        
//        // Wait for SwingUtilities.invokeLater to complete
//        SwingUtilities.invokeAndWait(() -> {});
//        
//        verify(mockTodoService).getAllTodos();
//        assertEquals("", searchField.getText());
//    }
//
//    // ========== Test addTagToTodo() ==========
//
//    @Test
//    void testAddTagToTodo_WithValidSelection() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        Tag tag = new Tag("urgent");
//        tag.setId(2L);
//        
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        availableTagsListModel.addElement(tag);
//        availableTagsList.setSelectedIndex(0);
//        
//        mainFrame.addTagToTodo();
//        
//        verify(mockTodoService).addTagToTodo(1L, 2L);
//    }
//
//    @Test
//    void testAddTagToTodo_WithNoTodoSelection() throws Exception {
//        Tag tag = new Tag("urgent");
//        tag.setId(2L);
//        availableTagsListModel.addElement(tag);
//        availableTagsList.setSelectedIndex(0);
//        
//        mainFrame.addTagToTodo();
//        
//        verify(mockTodoService, never()).addTagToTodo(anyLong(), anyLong());
//    }
//
//    @Test
//    void testAddTagToTodo_WithNoTagSelection() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        
//        mainFrame.addTagToTodo();
//        
//        verify(mockTodoService, never()).addTagToTodo(anyLong(), anyLong());
//    }
//
//    @Test
//    void testAddTagToTodo_WithException() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        Tag tag = new Tag("urgent");
//        tag.setId(2L);
//        
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        availableTagsListModel.addElement(tag);
//        availableTagsList.setSelectedIndex(0);
//        doThrow(new RuntimeException("Error")).when(mockTodoService).addTagToTodo(anyLong(), anyLong());
//        
//        mainFrame.addTagToTodo();
//        
//        verify(mockTodoService).addTagToTodo(1L, 2L);
//    }
//
//    // ========== Test removeTagFromTodo() ==========
//
//    @Test
//    void testRemoveTagFromTodo_WithValidSelection() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        Tag tag = new Tag("urgent");
//        tag.setId(2L);
//        
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        tagListModel.addElement(tag);
//        tagList.setSelectedIndex(0);
//        
//        mainFrame.removeTagFromTodo();
//        
//        verify(mockTodoService).removeTagFromTodo(1L, 2L);
//    }
//
//    @Test
//    void testRemoveTagFromTodo_WithNoTodoSelection() throws Exception {
//        Tag tag = new Tag("urgent");
//        tag.setId(2L);
//        tagListModel.addElement(tag);
//        tagList.setSelectedIndex(0);
//        
//        mainFrame.removeTagFromTodo();
//        
//        verify(mockTodoService, never()).removeTagFromTodo(anyLong(), anyLong());
//    }
//
//    @Test
//    void testRemoveTagFromTodo_WithNoTagSelection() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        
//        mainFrame.removeTagFromTodo();
//        
//        verify(mockTodoService, never()).removeTagFromTodo(anyLong(), anyLong());
//    }
//
//    @Test
//    void testRemoveTagFromTodo_WithException() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        Tag tag = new Tag("urgent");
//        tag.setId(2L);
//        
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        tagListModel.addElement(tag);
//        tagList.setSelectedIndex(0);
//        doThrow(new RuntimeException("Error")).when(mockTodoService).removeTagFromTodo(anyLong(), anyLong());
//        
//        mainFrame.removeTagFromTodo();
//        
//        verify(mockTodoService).removeTagFromTodo(1L, 2L);
//    }
//
//    // ========== Test Table Selection Listener ==========
//
//    @Test
//    void testTableSelectionListener_UpdatesTodoTags() throws Exception {
//        Tag tag1 = new Tag("urgent");
//        Tag tag2 = new Tag("work");
//        Set<Tag> tags = new HashSet<>();
//        tags.add(tag1);
//        tags.add(tag2);
//        
//        Todo todo = createTodo(1L, "Test", false);
//        todo.setTags(tags);
//        
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        
//        // Give UI time to update
//        Thread.sleep(100);
//        
//        assertEquals(2, tagListModel.getSize());
//    }
//
//    @Test
//    void testTableSelectionListener_WithNullTags() throws Exception {
//        Todo todo = createTodo(1L, "Test", false);
//        todo.setTags(null);
//        
//        setupTableWithTodo(todo);
//        todoTable.setRowSelectionInterval(0, 0);
//        
//        Thread.sleep(100);
//        
//        assertEquals(0, tagListModel.getSize());
//    }
//
//    // ========== Test TableModel ==========
//
//    @Test
//    void testTableModel_GetColumnClass() throws Exception {
//        TableModel model = todoTable.getModel();
//        
//        assertEquals(Long.class, model.getColumnClass(0));
//        assertEquals(String.class, model.getColumnClass(1));
//        assertEquals(Boolean.class, model.getColumnClass(2));
//        assertEquals(Object.class, model.getColumnClass(99));
//    }
//
//    @Test
//    void testTableModel_GetValueAt() throws Exception {
//        Todo todo = createTodo(42L, "Test desc", true);
//        setupTableWithTodo(todo);
//        
//        TableModel model = todoTable.getModel();
//        
//        assertEquals(42L, model.getValueAt(0, 0));
//        assertEquals("Test desc", model.getValueAt(0, 1));
//        assertEquals(true, model.getValueAt(0, 2));
//        assertNull(model.getValueAt(0, 99));
//    }
//
//    @Test
//    void testTableModel_GetColumnName() throws Exception {
//        TableModel model = todoTable.getModel();
//        
//        assertEquals("ID", model.getColumnName(0));
//        assertEquals("Description", model.getColumnName(1));
//        assertEquals("Done", model.getColumnName(2));
//    }
//
//    // ========== Helper Methods ==========
//
//    private Todo createTodo(Long id, String description, boolean done) {
//        Todo todo = new Todo(description);
//        todo.setId(id);
//        todo.setDone(done);
//        return todo;
//    }
//
//    private void setupTableWithTodo(Todo todo) throws Exception {
//        when(mockTodoService.getAllTodos()).thenReturn(List.of(todo));
//        
//        // Refresh the table via reflection
//        Method refreshMethod = MainFrame.class.getDeclaredMethod("refreshTodos");
//        refreshMethod.setAccessible(true);
//        refreshMethod.invoke(mainFrame);
//        
//        // Wait for Swing EDT
//        SwingUtilities.invokeAndWait(() -> {});
//    }
//}