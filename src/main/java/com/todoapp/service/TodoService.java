package com.todoapp.service;
import java.util.List;
import java.util.Optional;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.repository.TagRepository;
import com.todoapp.repository.TodoRepository;

public class TodoService {
    private final TodoRepository todoRepository;
    private final TagRepository tagRepository;

    public TodoService(TodoRepository todoRepository, TagRepository tagRepository) {
        this.todoRepository = todoRepository;
        this.tagRepository = tagRepository;
    }
    public List getAllTodos() {
        return todoRepository.findAll();
    }
    public Optional getTodoById(Long id) {
        return todoRepository.findById(id);
    }
    public Todo saveTodo(Todo todo) {
        return todoRepository.save(todo);
    }
    
    
    public Todo createTodo(String description) {
        validateNotEmpty(description, "Todo description");
        return saveTodo(new Todo(description.trim()));
    }

    private void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    
    public void deleteTodo(Long id) {
        todoRepository.deleteById(id);
    }
    public Todo markTodoComplete(Long id) {
        return updateTodoStatus(id, true);
    }

    private Todo updateTodoStatus(Long id, boolean done) {
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + id));
        todo.setDone(done);
        return todoRepository.save(todo);
    }
    
    public Todo markTodoIncomplete(Long id) {
        return updateTodoStatus(id, false);
    }
    public List getCompletedTodos() {
        return todoRepository.findByDone(true);
    }
    public List getIncompleteTodos() {
        return todoRepository.findByDone(false);
    }
    public List searchTodos(String keyword) {
        validateNotEmpty(keyword, "Search keyword");
        return todoRepository.findByDescriptionContaining(keyword);
    }
    
    
//    		TAG SECTION

    public List getAllTags() {
        return tagRepository.findAll();
    }	
    public Optional getTagById(Long id) {
        return tagRepository.findById(id);
    }
    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }
}