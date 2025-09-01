package com.todoapp.repository;

import com.todoapp.model.Todo;

import java.util.List;
import java.util.Optional;

public interface TodoRepository {
    
    List<Todo> findAll();
    
    Optional<Todo> findById(Long id);
    
    Todo save(Todo todo);
    
    void delete(Todo todo);
    
    void deleteById(Long id);
    
    List<Todo> findByDone(boolean done);
    
    List<Todo> findByDescriptionContaining(String keyword);
}