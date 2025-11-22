package com.todoapp.gui;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainFrameTest {

    private MainFrame frame;
    private MainFrameController controller;

    @BeforeEach
    void setup() {
        controller = mock(MainFrameController.class);
        frame = new MainFrame(controller);
        frame.setVisible(false);
    }


    @Test
    void testAddTodo() {
        // Happy path
        Todo todo = new Todo("task");
        when(controller.addTodo("task")).thenReturn(todo);
        when(controller.getAllTodos()).thenReturn(List.of(todo));
        frame.todoDescriptionField.setText("task");
        frame.addTodo();
        verify(controller).addTodo("task");
        
        // Validation failure (empty)
        frame.todoDescriptionField.setText("   ");
        frame.addTodo();
        verify(controller, times(1)).addTodo(anyString()); // Still only called once
    }

    @Test
    void testAddTag() {
        // Happy path
        Tag tag = new Tag("urgent");
        when(controller.addTag("urgent")).thenReturn(tag);
        when(controller.getAllTags()).thenReturn(List.of(tag));
        frame.tagNameField.setText("urgent");
        frame.addTag();
        verify(controller).addTag("urgent");
        
        // Validation failure (empty)
        frame.tagNameField.setText("");
        frame.addTag();
        verify(controller, times(1)).addTag(anyString());
    }

    @Test
    void testAddTagToTodo() {
        Todo todo = new Todo("task");
        todo.setId(1L);
        Tag tag = new Tag("urgent");
        tag.setId(2L);
        
        // Happy path
        when(controller.addTagToTodo(1L, 2L)).thenReturn(true);
        when(controller.getAllTodos()).thenReturn(List.of(todo));
        when(controller.getAllTags()).thenReturn(List.of(tag));
        frame.todoTableModel.setTodos(List.of(todo));
        frame.todoTable.setRowSelectionInterval(0, 0);
        frame.availableTagsListModel.addElement(tag);
        frame.availableTagsList.setSelectedIndex(0);
        frame.addTagToTodo();
        verify(controller).addTagToTodo(1L, 2L);
        
        // No selection - clear selections
        frame.availableTagsList.clearSelection();
        frame.addTagToTodo();
        verify(controller, times(1)).addTagToTodo(anyLong(), anyLong());
    }

    @Test
    void testRemoveTagFromTodo() {
        Todo todo = new Todo("task");
        todo.setId(1L);
        Tag tag = new Tag("urgent");
        tag.setId(2L);
        
        // Happy path
        when(controller.removeTagFromTodo(1L, 2L)).thenReturn(true);
        when(controller.getAllTodos()).thenReturn(List.of(todo));
        when(controller.getAllTags()).thenReturn(List.of(tag));
        frame.todoTableModel.setTodos(List.of(todo));
        frame.todoTable.setRowSelectionInterval(0, 0);
        frame.tagListModel.addElement(tag);
        frame.tagList.setSelectedIndex(0);
        frame.removeTagFromTodo();
        verify(controller).removeTagFromTodo(1L, 2L);
        
        // No selection
        frame.tagList.clearSelection();
        frame.removeTagFromTodo();
        verify(controller, times(1)).removeTagFromTodo(anyLong(), anyLong());
    }

    @Test
    void testDeleteTag() throws Exception {
        Tag tag = new Tag("urgent");
        tag.setId(1L);
        when(controller.deleteTag(1L)).thenReturn(true);
        when(controller.getAllTags()).thenReturn(new ArrayList<>());
        when(controller.getAllTodos()).thenReturn(new ArrayList<>());

        frame.availableTagsListModel.addElement(tag);
        frame.availableTagsList.setSelectedIndex(0);

        frame.deleteTag();
        SwingUtilities.invokeAndWait(() -> {}); 

        verify(controller, times(1)).deleteTag(1L);

        
        frame.deleteTag();
        SwingUtilities.invokeAndWait(() -> {}); 
        verify(controller, times(1)).deleteTag(anyLong());
    }

    @Test
    void testDeleteTodo() throws Exception {
        Todo todo = new Todo("task");
        todo.setId(1L);
        when(controller.deleteTodo(1L)).thenReturn(true);
        when(controller.getAllTodos()).thenReturn(new ArrayList<>());

        frame.todoTableModel.setTodos(List.of(todo));
        frame.todoTable.setRowSelectionInterval(0, 0);

        frame.deleteTodo();

        SwingUtilities.invokeAndWait(() -> {});

        verify(controller, times(1)).deleteTodo(1L);
    }

    @Test
    void testToggleTodoDone() {
        Todo todo = new Todo("task");
        todo.setId(1L);
        when(controller.toggleTodoDone(1L)).thenReturn(true);
        when(controller.getAllTodos()).thenReturn(List.of(todo));
        
        frame.todoTableModel.setTodos(List.of(todo));
        frame.todoTable.setRowSelectionInterval(0, 0);
        
        clearInvocations(controller);
        
        frame.toggleTodoDone();
        
        verify(controller, times(1)).toggleTodoDone(1L);
        
        frame.todoTable.clearSelection();
        clearInvocations(controller);
        frame.toggleTodoDone();
        
        verify(controller, never()).toggleTodoDone(anyLong());
    }

    @Test
    void testEditTodo() {
        frame.editTodo();
        verify(controller, never()).updateTodoDescription(anyLong(), anyString());
    }


    @Test
    void testRefreshTodos() {
        Todo todo = new Todo("task");
        todo.setId(1L);
        when(controller.getAllTodos()).thenReturn(List.of(todo));
        
        frame.todoTableModel.setTodos(List.of(todo));
        frame.todoTable.setRowSelectionInterval(0, 0);
        frame.refreshTodos();
        verify(controller, atLeastOnce()).getAllTodos();
        assertEquals(0, frame.todoTable.getSelectedRow());
        
        when(controller.getAllTodos()).thenReturn(new ArrayList<>());
        frame.refreshTodos();
        assertEquals(0, frame.tagListModel.size());
    }

    @Test
    void testRefreshTags() {
        Tag tag = new Tag("urgent");
        when(controller.getAllTags()).thenReturn(List.of(tag));
        frame.refreshTags();
        verify(controller).getAllTags();
        assertEquals(1, frame.availableTagsListModel.size());
    }

    @Test
    void testSearchTodos() {
        Todo todo = new Todo("search result");
        when(controller.searchTodos("search")).thenReturn(List.of(todo));
        frame.searchField.setText("search");
        frame.searchTodos();
        verify(controller).searchTodos("search");
    }

    @Test
    void testShowAllTodos() throws Exception {
        when(controller.getAllTodos()).thenReturn(List.of(new Todo("task")));
        frame.showAllTodos();
        SwingUtilities.invokeAndWait(() -> {});
        verify(controller, atLeastOnce()).getAllTodos();
    }

    @Test
    void testGetSelectedTodo() {
        Todo todo = new Todo("task");
        todo.setId(1L);
        
        frame.todoTableModel.setTodos(List.of(todo));
        frame.todoTable.setRowSelectionInterval(0, 0);
        assertNotNull(frame.getSelectedTodo());
        
        frame.todoTable.clearSelection();
        assertNull(frame.getSelectedTodo());
    }

   @Test
    void testTodoTableModel() {
        MainFrame.TodoTableModel model = new MainFrame.TodoTableModel();
        Todo todo = new Todo("task");
        todo.setId(1L);
        todo.setDone(true);
        
        model.setTodos(List.of(todo));
        
        assertEquals(1, model.getRowCount());
        assertEquals(3, model.getColumnCount());
        assertEquals("ID", model.getColumnName(0));
        assertEquals("Description", model.getColumnName(1));
        assertEquals("Done", model.getColumnName(2));
        assertEquals(todo, model.getTodoAt(0));
        
        assertEquals(1L, model.getValueAt(0, 0));
        assertEquals("task", model.getValueAt(0, 1));
        assertEquals(true, model.getValueAt(0, 2));
        
        assertNull(model.getValueAt(0, 99));
    }

 
    @Test
    void testTextFieldListeners() throws Exception {
        Todo todo = new Todo("task");
        when(controller.addTodo("task")).thenReturn(todo);
        when(controller.getAllTodos()).thenReturn(List.of(todo));
        
        frame.todoDescriptionField.setText("task");
        SwingUtilities.invokeAndWait(() -> frame.todoDescriptionField.postActionEvent());
        Thread.sleep(100);
        verify(controller).addTodo("task");
    }

    @Test
    void testTableSelectionListener() throws Exception {
        Todo todo = new Todo("task");
        todo.setId(1L);
        Tag tag = new Tag("urgent");
        todo.addTag(tag);
        
        frame.todoTableModel.setTodos(List.of(todo));
        SwingUtilities.invokeAndWait(() -> frame.todoTable.setRowSelectionInterval(0, 0));
        Thread.sleep(100);
        assertEquals(1, frame.tagListModel.size());
    }

    @Test
    void testDoubleClick() throws Exception {
        Todo todo = new Todo("task");
        todo.setId(1L);
        when(controller.toggleTodoDone(1L)).thenReturn(true);
        when(controller.getAllTodos()).thenReturn(List.of(todo));
        
        frame.todoTableModel.setTodos(List.of(todo));
        frame.todoTable.setRowSelectionInterval(0, 0);
        
        SwingUtilities.invokeAndWait(() -> {
            java.awt.event.MouseEvent evt = new java.awt.event.MouseEvent(
                frame.todoTable, java.awt.event.MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 0, 0, 2, false
            );
            for (java.awt.event.MouseListener listener : frame.todoTable.getMouseListeners()) {
                listener.mouseClicked(evt);
            }
        });
        Thread.sleep(100);
        verify(controller).toggleTodoDone(1L);
    }

   
    @Test
    void testReselectTodoEdgeCases() {
        Todo todo1 = new Todo("task1");
        todo1.setId(1L);
        Todo todo2 = new Todo("task2");
        todo2.setId(2L);
        
        when(controller.addTagToTodo(1L, 1L)).thenReturn(true);
        when(controller.getAllTodos()).thenReturn(List.of(todo1, todo2));
        when(controller.getAllTags()).thenReturn(new ArrayList<>());
        
        Tag tag = new Tag("test");
        tag.setId(1L);
        
        frame.todoTableModel.setTodos(List.of(todo1, todo2));
        frame.todoTable.setRowSelectionInterval(0, 0);
        frame.availableTagsListModel.addElement(tag);
        frame.availableTagsList.setSelectedIndex(0);
        
        frame.addTagToTodo();
        
        try {
            SwingUtilities.invokeAndWait(() -> {});
            Thread.sleep(100);
        } catch (Exception ignored) {}
    }

    @Test
    void testGetTextWithNull() {
        frame.todoDescriptionField.setText(null);
        String result = frame.todoDescriptionField.getText();
        assertNotNull(result); // Swing converts null to ""
    }

    @Test
    void testUpdateTodoTagsWithTags() {
        Todo todo = new Todo("task");
        todo.setId(1L);
        Tag tag1 = new Tag("tag1");
        Tag tag2 = new Tag("tag2");
        todo.addTag(tag1);
        todo.addTag(tag2);
        
        when(controller.getAllTodos()).thenReturn(List.of(todo));
        frame.todoTableModel.setTodos(List.of(todo));
        frame.todoTable.setRowSelectionInterval(0, 0);
        
        frame.refreshTodos();
        
        assertEquals(2, frame.tagListModel.size());
    }
}