package com.todoapp.service;
import java.util.List;
import java.util.Optional;

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
}