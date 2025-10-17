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
    
//    			TODO SECTION
    
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
    public Tag createTag(String name) {
        validateNotEmpty(name, "Tag name");
        return saveTag(new Tag(name.trim()));
    }
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }
    public Optional findTagByName(String name) {
        return tagRepository.findByName(name);
    }
    
    public List searchTags(String keyword) {
        validateNotEmpty(keyword, "Search keyword");
        return tagRepository.findByNameContaining(keyword);
    }
    
//    			TODO-TAG SECTION
    
    public Todo addTagToTodo(Long todoId, Long tagId) {
        return modifyTodoTag(todoId, tagId, (todo, tag) -> todo.addTag(tag));
    }

    private Todo modifyTodoTag(Long todoId, Long tagId, TagModifier modifier) {
        Todo todo = todoRepository.findById(todoId)
            .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + todoId));
        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + tagId));
        modifier.modify(todo, tag);
        return todoRepository.save(todo);
    }
    @FunctionalInterface
    private interface TagModifier {
        void modify(Todo todo, Tag tag);
    }
    
    public Todo removeTagFromTodo(Long todoId, Long tagId) {
        return modifyTodoTag(todoId, tagId, (todo, tag) -> todo.removeTag(tag));
    }
}