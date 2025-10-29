package com.todoapp.gui;

import java.util.List;
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
    
    public boolean toggleTodoDone(Long todoId) {
        if (todoId == null) {
            return false;
        }
        
        Optional<Todo> todoOpt = todoService.getTodoById(todoId);
        if (todoOpt.isEmpty()) {
            return false;
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
    
    public List<Todo> searchTodos(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return todoService.getAllTodos();
        }
        return todoService.searchTodos(keyword.trim());
    }
    
    public List<Todo> getAllTodos() {
        return todoService.getAllTodos();
    }

    public List<Tag> getAllTags() {
        return todoService.getAllTags();
    }
    
    public boolean addTagToTodo(Long todoId, Long tagId) {
        if (todoId == null || tagId == null) {
            return false;
        }
        todoService.addTagToTodo(todoId, tagId);
        return true;
    }
    
    public boolean removeTagFromTodo(Long todoId, Long tagId) {
        if (todoId == null || tagId == null) {
            return false;
        }
        todoService.removeTagFromTodo(todoId, tagId);
        return true;
    }
    
    public boolean deleteTag(Long tagId) {
        if (tagId == null) {
            return false;
        }
        todoService.deleteTag(tagId);
        return true;
    }
}