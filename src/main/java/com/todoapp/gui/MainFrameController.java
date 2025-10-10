package com.todoapp.gui;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.service.TodoService;

import java.util.List;
import java.util.Optional;

/**
 * Controller that handles business logic for MainFrame.
 * This class is unit-testable without Swing dependencies.
 */
public class MainFrameController {
    
    private final TodoService todoService;
    
    public MainFrameController(TodoService todoService) {
        this.todoService = todoService;
    }
    
    /**
     * Adds a new todo with the given description.
     * @return the created Todo, or null if description is empty
     */
    public Todo addTodo(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        return todoService.createTodo(description.trim());
    }
    
    /**
     * Creates a new tag with the given name.
     * @return the created Tag, or null if name is empty or tag already exists
     */
    public Tag addTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return null;
        }
        
        Optional<Tag> existing = todoService.findTagByName(tagName.trim());
        if (existing.isPresent()) {
            return null; // Tag already exists
        }
        
        return todoService.createTag(tagName.trim());
    }
    
    /**
     * Deletes a todo by ID.
     * @return true if deleted successfully
     */
    public boolean deleteTodo(Long todoId) {
        if (todoId == null) {
            return false;
        }
        todoService.deleteTodo(todoId);
        return true;
    }
    
    /**
     * Updates a todo's description.
     * @return true if updated successfully
     */
    public boolean updateTodoDescription(Long todoId, String newDescription) {
        if (todoId == null || newDescription == null || newDescription.trim().isEmpty()) {
            return false;
        }
        
        Optional<Todo> todoOpt = todoService.getTodoById(todoId);
        if (todoOpt.isEmpty()) {
            return false;
        }
        
        Todo todo = todoOpt.get();
        todo.setDescription(newDescription.trim());
        todoService.saveTodo(todo);
        return true;
    }
    
    /**
     * Deletes a tag by ID.
     */
    public boolean deleteTag(Long tagId) {
        if (tagId == null) {
            return false;
        }
        todoService.deleteTag(tagId);
        return true;
    }
    
    /**
     * Toggles the done status of a todo.
     * @return the new done status, or null if todo not found
     */
    public Boolean toggleTodoDone(Long todoId) {
        if (todoId == null) {
            return null;
        }
        
        Optional<Todo> todoOpt = todoService.getTodoById(todoId);
        if (todoOpt.isEmpty()) {
            return null;
        }
        
        Todo todo = todoOpt.get();
        if (todo.isDone()) {
            todoService.markTodoIncomplete(todoId);
            return false;
        } else {
            todoService.markTodoComplete(todoId);
            return true;
        }
    }
    
    /**
     * Searches todos by keyword.
     * @return list of matching todos, or all todos if keyword is empty
     */
    public List<Todo> searchTodos(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return todoService.getAllTodos();
        }
        return todoService.searchTodos(keyword.trim());
    }
    
    /**
     * Gets all todos.
     */
    public List<Todo> getAllTodos() {
        return todoService.getAllTodos();
    }
    
    /**
     * Gets all tags.
     */
    public List<Tag> getAllTags() {
        return todoService.getAllTags();
    }
    
    /**
     * Adds a tag to a todo.
     */
    public boolean addTagToTodo(Long todoId, Long tagId) {
        if (todoId == null || tagId == null) {
            return false;
        }
        todoService.addTagToTodo(todoId, tagId);
        return true;
    }
    
    /**
     * Removes a tag from a todo.
     */
    public boolean removeTagFromTodo(Long todoId, Long tagId) {
        if (todoId == null || tagId == null) {
            return false;
        }
        todoService.removeTagFromTodo(todoId, tagId);
        return true;
    }
}