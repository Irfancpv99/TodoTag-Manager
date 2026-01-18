package com.todoapp.repository.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoTodoRepositoryTest {

    @Mock
    private MongoClient mongoClient;
    
    @Mock
    private MongoCollection<Document> collection;
    
    @Mock
    private FindIterable<Document> findIterable;
    
    @Mock
    private MongoCursor<Document> cursor;
    
    @Mock
    private MongoTagRepository tagRepository;
    
    @Mock
    private DeleteResult deleteResult;
    
    private MongoTodoRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new MongoTodoRepository(collection, mongoClient, tagRepository);
    }
    
    @Test
    void shouldFindAll() {
        Document doc1 = new Document("_id", 1L)
            .append("description", "Task 1")
            .append("done", false)
            .append("tagIds", Arrays.asList());
        Document doc2 = new Document("_id", 2L)
            .append("description", "Task 2")
            .append("done", true)
            .append("tagIds", Arrays.asList());
        
        when(collection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);
        
        List<Todo> result = repository.findAll();
        
        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getDescription());
        assertEquals("Task 2", result.get(1).getDescription());
    }
    
    @Test
    void shouldFindAllWhenEmpty() {
        when(collection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        List<Todo> result = repository.findAll();
        
        assertEquals(0, result.size());
    }
    
    @Test
    void shouldFindById() {
        Document doc = new Document("_id", 1L)
            .append("description", "Task 1")
            .append("done", false)
            .append("tagIds", Arrays.asList());
        
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        Optional<Todo> result = repository.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals("Task 1", result.get().getDescription());
        assertEquals(1L, result.get().getId());
    }
    
    @Test
    void shouldReturnEmptyWhenTodoNotFoundById() {
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);
        
        Optional<Todo> result = repository.findById(999L);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void shouldSaveNewTodo() {
        Todo todo = new Todo("New Task");
        
        Todo result = repository.save(todo);
        
        verify(collection).replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class));
        assertNotNull(result.getId());
        assertEquals("New Task", result.getDescription());
    }
    
    @Test
    void shouldSaveExistingTodo() {
        Todo todo = new Todo("Existing Task");
        todo.setId(1L);
        
        Todo result = repository.save(todo);
        
        verify(collection).replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class));
        assertEquals(1L, result.getId());
        assertEquals("Existing Task", result.getDescription());
    }
    
    @Test
    void shouldDeleteTodo() {
        Todo todo = new Todo("Task");
        todo.setId(1L);
        
        repository.delete(todo);
        
        verify(collection).deleteOne(any(Bson.class));
    }
    
    @Test
    void shouldNotDeleteTodoWithNullId() {
        Todo todo = new Todo("Task");
        
        repository.delete(todo);
        
        verify(collection, never()).deleteOne(any(Bson.class));
    }
    
    @Test
    void shouldDeleteById() {
        repository.deleteById(1L);
        
        verify(collection).deleteOne(any(Bson.class));
    }
    
    @Test
    void shouldFindByDone() {
        Document doc = new Document("_id", 1L)
            .append("description", "Done Task")
            .append("done", true)
            .append("tagIds", Arrays.asList());
        
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(doc);
        
        List<Todo> result = repository.findByDone(true);
        
        assertEquals(1, result.size());
        assertEquals("Done Task", result.get(0).getDescription());
        assertTrue(result.get(0).isDone());
    }
    
    @Test
    void shouldFindByDoneWhenEmpty() {
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        List<Todo> result = repository.findByDone(false);
        
        assertEquals(0, result.size());
    }
    
    @Test
    void shouldFindByDescriptionContaining() {
        Document doc = new Document("_id", 1L)
            .append("description", "Important Meeting")
            .append("done", false)
            .append("tagIds", Arrays.asList());
        
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(doc);
        
        List<Todo> result = repository.findByDescriptionContaining("important");
        
        assertEquals(1, result.size());
        assertEquals("Important Meeting", result.get(0).getDescription());
    }
    
    @Test
    void shouldFindByDescriptionContainingWhenEmpty() {
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        List<Todo> result = repository.findByDescriptionContaining("nonexistent");
        
        assertEquals(0, result.size());
    }
    
    @Test
    void shouldDeleteAll() {
        repository.deleteAll();
        
        verify(collection).deleteMany(any(Document.class));
    }
    
    @Test
    void shouldCloseMongoClient() {
        repository.close();
        
        verify(mongoClient).close();
    }
    
    @Test
    void shouldHandleCloseWhenMongoClientIsNull() {
        MongoTodoRepository nullClientRepo = new MongoTodoRepository(collection, null, tagRepository);
        
        // Should not throw exception
        assertDoesNotThrow(() -> nullClientRepo.close());
    }
    
    @Test
    void shouldSaveTodoWithTags() {
        Tag tag1 = new Tag("Work");
        tag1.setId(1L);
        Tag tag2 = new Tag("Urgent");
        tag2.setId(2L);
        
        Todo todo = new Todo("Task with tags");
        todo.addTag(tag1);
        todo.addTag(tag2);
        
        Todo result = repository.save(todo);
        
        verify(collection).replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class));
        assertEquals(2, result.getTags().size());
    }
    
    @Test
    void shouldSaveTodoWithEmptyTags() {
        Todo todo = new Todo("Task without tags");
        
        Todo result = repository.save(todo);
        
        verify(collection).replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class));
        assertEquals(0, result.getTags().size());
    }
    
    @Test
    void shouldLoadTodoWithTags() {
        Tag tag1 = new Tag("Work");
        tag1.setId(1L);
        Tag tag2 = new Tag("Urgent");
        tag2.setId(2L);
        
        Document doc = new Document("_id", 1L)
            .append("description", "Task with tags")
            .append("done", false)
            .append("tagIds", Arrays.asList(1L, 2L));
        
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag1));
        when(tagRepository.findById(2L)).thenReturn(Optional.of(tag2));
        
        Optional<Todo> result = repository.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getTags().size());
    }
    
    @Test
    void shouldHandleNullTagIds() {
        Document doc = new Document("_id", 1L)
            .append("description", "Task")
            .append("done", false)
            .append("tagIds", null);
        
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        Optional<Todo> result = repository.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(0, result.get().getTags().size());
    }
    
    @Test
    void shouldHandleMissingTagsGracefully() {
        Document doc = new Document("_id", 1L)
            .append("description", "Task")
            .append("done", false)
            .append("tagIds", Arrays.asList(999L));
        
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());
        
        Optional<Todo> result = repository.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(0, result.get().getTags().size());
    }
    
    @Test
    void shouldHandleDefaultDoneValue() {
        Document doc = new Document("_id", 1L)
            .append("description", "Task");
        // No done field, should default to false
        
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        Optional<Todo> result = repository.findById(1L);
        
        assertTrue(result.isPresent());
        assertFalse(result.get().isDone());
    }
}
