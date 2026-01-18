package com.todoapp.service;

import com.todoapp.config.AppConfig;
import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.repository.RepositoryFactory;
import com.todoapp.repository.TagRepository;
import com.todoapp.repository.TodoRepository;

import java.util.List;
import java.util.Optional;

public class TodoService {
    
    private final TodoRepository todoRepository;
    private final TagRepository tagRepository;
    private final RepositoryFactory repositoryFactory;

    public TodoService(AppConfig config) {
       
    	this.repositoryFactory = new RepositoryFactory(config);
        this.todoRepository = repositoryFactory.createTodoRepository();
        this.tagRepository = repositoryFactory.createTagRepository();
    }

    public TodoService(TodoRepository todoRepository, TagRepository tagRepository) {
        this.todoRepository = todoRepository;
        this.tagRepository = tagRepository;
        this.repositoryFactory = null; 
    }

    // 				 Section
    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Optional<Todo> getTodoById(Long id) {
        return todoRepository.findById(id);
    }

    public Todo saveTodo(Todo todo) {
        return executeWithTransaction(() -> todoRepository.save(todo));
    }

    public Todo createTodo(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Todo description cannot be null");
        }
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("Todo description cannot be empty");
        }
        
        Todo todo = new Todo(description.trim());
        return saveTodo(todo);
    }

    public void deleteTodo(Long id) {
        executeWithTransaction(() -> {
            todoRepository.deleteById(id);
            return null;
        });
    }

    public Todo markTodoComplete(Long id) {
        return executeWithTransaction(() -> {
            Optional<Todo> todoOpt = todoRepository.findById(id);
            if (todoOpt.isPresent()) {
                Todo todo = todoOpt.get();
                todo.setDone(true);
                return todoRepository.save(todo);
            }
            throw new IllegalArgumentException("Todo not found with id: " + id);
        });
    }

    public Todo markTodoIncomplete(Long id) {
        return executeWithTransaction(() -> {
            Optional<Todo> todoOpt = todoRepository.findById(id);
            if (todoOpt.isPresent()) {
                Todo todo = todoOpt.get();
                todo.setDone(false);
                return todoRepository.save(todo);
            }
            throw new IllegalArgumentException("Todo not found with id: " + id);
        });
    }

    public List<Todo> getCompletedTodos() {
        return todoRepository.findByDone(true);
    }

    public List<Todo> getIncompleteTodos() {
        return todoRepository.findByDone(false);
    }

    public List<Todo> searchTodos(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException("Search keyword cannot be null");
        }
        return todoRepository.findByDescriptionContaining(keyword);
    }

    // 				TAG Section 

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }

    public Tag saveTag(Tag tag) {
        return executeWithTransaction(() -> tagRepository.save(tag));
    }

    public Tag createTag(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Tag name cannot be null");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }
        
        // Check for duplicate tag name
        Optional<Tag> existingTag = tagRepository.findByName(name.trim());
        if (existingTag.isPresent()) {
            throw new IllegalArgumentException("Tag with name '" + name.trim() + "' already exists");
        }
        
        Tag tag = new Tag(name.trim());
        return saveTag(tag);
    }

    public void deleteTag(Long id) {
        executeWithTransaction(() -> {
            tagRepository.deleteById(id);
            return null;
        });
    }

    public Optional<Tag> findTagByName(String name) {
        return tagRepository.findByName(name);
    }

    public List<Tag> searchTags(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException("Search keyword cannot be null");
        }
        return tagRepository.findByNameContaining(keyword);
    }

    // 					Model Relationship 

    public Todo addTagToTodo(Long todoId, Long tagId) {
        return executeWithTransaction(() -> {
            Optional<Todo> todoOpt = todoRepository.findById(todoId);
            Optional<Tag> tagOpt = tagRepository.findById(tagId);
            
            if (todoOpt.isPresent() && tagOpt.isPresent()) {
                Todo todo = todoOpt.get();
                Tag tag = tagOpt.get();
                todo.addTag(tag);
                return todoRepository.save(todo);
            }
            throw new IllegalArgumentException("Todo or Tag not found");
        });
    }

    public Todo removeTagFromTodo(Long todoId, Long tagId) {
        return executeWithTransaction(() -> {
            Optional<Todo> todoOpt = todoRepository.findById(todoId);
            Optional<Tag> tagOpt = tagRepository.findById(tagId);
            
            if (todoOpt.isPresent() && tagOpt.isPresent()) {
                Todo todo = todoOpt.get();
                Tag tag = tagOpt.get();
                todo.removeTag(tag);
                return todoRepository.save(todo);
            }
            throw new IllegalArgumentException("Todo or Tag not found");
        });
    }
    
    public List<Todo> findTodosByTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        return todoRepository.findByTag(tag);
    }
    
    public List<Todo> findTodosByTagId(Long tagId) {
        Optional<Tag> tagOpt = tagRepository.findById(tagId);
        if (tagOpt.isEmpty()) {
            throw new IllegalArgumentException("Tag not found with id: " + tagId);
        }
        return todoRepository.findByTag(tagOpt.get());
    }
    
    /**
     * Execute an operation within a transaction (for MySQL) or directly (for MongoDB)
     */
    private <T> T executeWithTransaction(TransactionOperation<T> operation) {
        if (repositoryFactory != null) {
            try {
                repositoryFactory.beginTransaction();
                T result = operation.execute();
                repositoryFactory.commitTransaction();
                return result;
            } catch (Exception e) {
                repositoryFactory.rollbackTransaction();
                throw e;
            }
        } else {
            return operation.execute();
        }
    }
    @FunctionalInterface
    private interface TransactionOperation<T> {
        T execute();
    }
}