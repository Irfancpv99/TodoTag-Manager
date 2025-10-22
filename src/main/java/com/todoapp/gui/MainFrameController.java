package com.todoapp.gui;

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
}