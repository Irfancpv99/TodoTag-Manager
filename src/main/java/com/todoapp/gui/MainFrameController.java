package com.todoapp.gui;

import java.util.Optional;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.service.TodoService;

public class MainFrameController {
    
    private final TodoService todoService;
    
    public MainFrameController(TodoService todoService) {
        this.todoService = todoService;
    }
    
    public Todo addTodo(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        return todoService.createTodo(description.trim());
    }
    public Tag addTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return null;
        }
        
        Optional<Tag> existing = todoService.findTagByName(tagName.trim());
        if (existing.isPresent()) {
            return null;
        }
        
        return todoService.createTag(tagName.trim());
    }
    public boolean deleteTodo(Long todoId) {
        if (todoId == null) {
            return false;
        }
        todoService.deleteTodo(todoId);
        return true;
    }
}