package com.todoapp.service;

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

    /**
     * Constructor for production use
     */
    public TodoService() {
        RepositoryFactory factory = RepositoryFactory.getInstance();
        this.todoRepository = factory.createTodoRepository();
        this.tagRepository = factory.createTagRepository();
    }

    /**
     * Constructor for testing with specific repositories
     */
    public TodoService(TodoRepository todoRepository, TagRepository tagRepository) {
        this.todoRepository = todoRepository;
        this.tagRepository = tagRepository;
    }

    // Todo CRUD Operations

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Optional<Todo> getTodoById(Long id) {
        return todoRepository.findById(id);
    }

    public Todo saveTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    public Todo createTodo(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Todo description cannot be null");
        }
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("Todo description cannot be empty");
        }
        
        Todo todo = new Todo(description);
        return saveTodo(todo);
    }

    public void deleteTodo(Long id) {
        todoRepository.deleteById(id);
    }

    public Todo markTodoComplete(Long id) {
        Optional<Todo> todoOpt = todoRepository.findById(id);
        if (todoOpt.isPresent()) {
            Todo todo = todoOpt.get();
            todo.setDone(true);
            return todoRepository.save(todo);
        }
        throw new IllegalArgumentException("Todo not found with id: " + id);
    }

    public Todo markTodoIncomplete(Long id) {
        Optional<Todo> todoOpt = todoRepository.findById(id);
        if (todoOpt.isPresent()) {
            Todo todo = todoOpt.get();
            todo.setDone(false);
            return todoRepository.save(todo);
        }
        throw new IllegalArgumentException("Todo not found with id: " + id);
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

    // Tag CRUD Operations

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }

    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    public Tag createTag(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Tag name cannot be null");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }
        
        Tag tag = new Tag(name);
        return saveTag(tag);
    }

    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
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

    // Todo-Tag Relationship Management

    public Todo addTagToTodo(Long todoId, Long tagId) {
        Optional<Todo> todoOpt = todoRepository.findById(todoId);
        Optional<Tag> tagOpt = tagRepository.findById(tagId);
        
        if (todoOpt.isPresent() && tagOpt.isPresent()) {
            Todo todo = todoOpt.get();
            Tag tag = tagOpt.get();
            todo.addTag(tag);
            return todoRepository.save(todo);
        }
        throw new IllegalArgumentException("Todo or Tag not found");
    }

    public Todo removeTagFromTodo(Long todoId, Long tagId) {
        Optional<Todo> todoOpt = todoRepository.findById(todoId);
        Optional<Tag> tagOpt = tagRepository.findById(tagId);
        
        if (todoOpt.isPresent() && tagOpt.isPresent()) {
            Todo todo = todoOpt.get();
            Tag tag = tagOpt.get();
            todo.removeTag(tag);
            return todoRepository.save(todo);
        }
        throw new IllegalArgumentException("Todo or Tag not found");
    }
}