package com.todoapp.repository;

import com.todoapp.model.Todo;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Todo entity
 * 
 * GREEN PHASE: Interface implementation to make tests pass
 * Following TDD: minimal interface to satisfy test requirements
 */
public interface TodoRepository {
    
    /**
     * Retrieves all todos from the repository.
     * 
     * @return list of all todos
     */
    List<Todo> findAll();
    
    /**
     * Finds a todo by its ID.
     * 
     * @param id the todo ID
     * @return Optional containing the todo if found, empty otherwise
     */
    Optional<Todo> findById(Long id);
    
    /**
     * Saves a todo to the repository.
     * 
     * @param todo the todo to save
     * @return the saved todo (with generated ID if new)
     */
    Todo save(Todo todo);
    
    /**
     * Deletes a todo from the repository.
     * 
     * @param todo the todo to delete
     */
    void delete(Todo todo);
    
    /**
     * Deletes a todo by its ID.
     * 
     * @param id the ID of the todo to delete
     */
    void deleteById(Long id);
    
    /**
     * Finds todos by their completion status.
     * 
     * @param done the completion status to search for
     * @return list of todos with the specified completion status
     */
    List<Todo> findByDone(boolean done);
    
    /**
     * Finds todos whose description contains the specified keyword.
     * 
     * @param keyword the keyword to search for in descriptions
     * @return list of todos containing the keyword
     */
    List<Todo> findByDescriptionContaining(String keyword);
}