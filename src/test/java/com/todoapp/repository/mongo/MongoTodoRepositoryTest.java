package com.todoapp.repository.mongo;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TDD Test for MongoTodoRepository implementation
 * 
 * RED PHASE: These tests will fail because MongoTodoRepository doesn't exist yet.
 * Following TDD: test the concrete MongoDB implementation before writing it
 */
@ExtendWith(MockitoExtension.class)
class MongoTodoRepositoryTest {

    @Mock
    private MongoClient mongoClient;
    
    @Mock
    private MongoDatabase mongoDatabase;
    
    @Mock
    private MongoCollection<Document> todoCollection;
    
    @Mock
    private MongoTagRepository tagRepository;

    private MongoTodoRepository mongoTodoRepository;

    @BeforeEach
    void setUp() {
        // RED: This will fail - MongoTodoRepository class doesn't exist yet
        when(mongoClient.getDatabase("testdb")).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection("todos")).thenReturn(todoCollection);
        
        mongoTodoRepository = new MongoTodoRepository(mongoClient, "testdb", tagRepository);
    }

    @Test
    void shouldSaveNewTodoWithGeneratedId() {
        // RED: This will fail - MongoTodoRepository.save() doesn't exist yet
        Todo todo = new Todo("Test task");
        
        Todo savedTodo = mongoTodoRepository.save(todo);
        
        assertNotNull(savedTodo.getId());
        assertEquals("Test task", savedTodo.getDescription());
        assertFalse(savedTodo.isDone());
        
        // Verify MongoDB interaction
        verify(todoCollection).replaceOne(any(), any(), any());
    }

    @Test
    void shouldUpdateExistingTodo() {
        // RED: This will fail - methods don't exist yet
        Todo existingTodo = new Todo("Existing task");
        existingTodo.setId(1L);
        existingTodo.setDone(true);
        
        Todo updatedTodo = mongoTodoRepository.save(existingTodo);
        
        assertEquals(1L, updatedTodo.getId());
        assertEquals("Existing task", updatedTodo.getDescription());
        assertTrue(updatedTodo.isDone());
        
        verify(todoCollection).replaceOne(any(), any(), any());
    }

    @Test
    void shouldFindTodoById() {
        // RED: This will fail - findById() doesn't exist yet
        Document todoDoc = new Document()
            .append("_id", 1L)
            .append("description", "Found task")
            .append("done", false)
            .append("tagIds", List.of());

        when(todoCollection.find(any())).thenReturn(mock(com.mongodb.client.FindIterable.class));
        when(todoCollection.find(any()).first()).thenReturn(todoDoc);
        
        Optional<Todo> found = mongoTodoRepository.findById(1L);
        
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
        assertEquals("Found task", found.get().getDescription());
        assertFalse(found.get().isDone());
        
        verify(todoCollection).find(any());
    }

    @Test
    void shouldReturnEmptyWhenTodoNotFound() {
        // RED: This will fail - findById() doesn't exist yet
        when(todoCollection.find(any()).first()).thenReturn(null);
        
        Optional<Todo> found = mongoTodoRepository.findById(999L);
        
        assertFalse(found.isPresent());
        verify(todoCollection).find(any());
    }

    @Test
    void shouldFindAllTodos() {
        // RED: This will fail - findAll() doesn't exist yet
        when(todoCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
        
        List<Todo> todos = mongoTodoRepository.findAll();
        
        assertNotNull(todos);
        verify(todoCollection).find();
    }

    @Test
    void shouldDeleteTodoById() {
        // RED: This will fail - deleteById() doesn't exist yet
        mongoTodoRepository.deleteById(1L);
        
        verify(todoCollection).deleteOne(any());
    }

    @Test
    void shouldFindTodosByDoneStatus() {
        // RED: This will fail - findByDone() doesn't exist yet
        when(todoCollection.find(any())).thenReturn(mock(com.mongodb.client.FindIterable.class));
        
        List<Todo> doneTodos = mongoTodoRepository.findByDone(true);
        
        assertNotNull(doneTodos);
        verify(todoCollection).find(any());
    }

    @Test
    void shouldFindTodosByDescriptionContaining() {
        // RED: This will fail - findByDescriptionContaining() doesn't exist yet
        when(todoCollection.find(any())).thenReturn(mock(com.mongodb.client.FindIterable.class));
        
        List<Todo> searchResults = mongoTodoRepository.findByDescriptionContaining("important");
        
        assertNotNull(searchResults);
        verify(todoCollection).find(any());
    }

    @Test
    void shouldHandleTodoWithTags() {
        // RED: This will fail - tag handling doesn't exist yet
        Todo todo = new Todo("Tagged task");
        Tag tag = new Tag("work");
        tag.setId(1L);
        todo.addTag(tag);
        
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        
        Todo savedTodo = mongoTodoRepository.save(todo);
        
        assertEquals(1, savedTodo.getTags().size());
        assertTrue(savedTodo.getTags().contains(tag));
        
        verify(todoCollection).replaceOne(any(), any(), any());
    }
}