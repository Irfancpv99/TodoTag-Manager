package com.todoapp.service;
import com.todoapp.repository.TagRepository;
import com.todoapp.repository.TodoRepository;

public class TodoService {
    private final TodoRepository todoRepository;
    private final TagRepository tagRepository;

    public TodoService(TodoRepository todoRepository, TagRepository tagRepository) {
        this.todoRepository = todoRepository;
        this.tagRepository = tagRepository;
    }
}