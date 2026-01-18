package com.todoapp.repository.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.todoapp.model.Tag;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoTagRepositoryTest {

    @Mock
    private MongoClient mongoClient;
    
    @Mock
    private MongoCollection<Document> collection;
    
    @Mock
    private FindIterable<Document> findIterable;
    
    @Mock
    private MongoCursor<Document> cursor;
    
    @Mock
    private DeleteResult deleteResult;
    
    private MongoTagRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new MongoTagRepository(collection, mongoClient);
    }
    
    @Test
    void shouldFindAll() {
        Document doc1 = new Document("_id", 1L).append("name", "Work");
        Document doc2 = new Document("_id", 2L).append("name", "Personal");
        
        when(collection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);
        
        List<Tag> result = repository.findAll();
        
        assertEquals(2, result.size());
        assertEquals("Work", result.get(0).getName());
        assertEquals("Personal", result.get(1).getName());
    }
    
    @Test
    void shouldFindAllWhenEmpty() {
        when(collection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        List<Tag> result = repository.findAll();
        
        assertEquals(0, result.size());
    }
    
    @Test
    void shouldFindById() {
        Document doc = new Document("_id", 1L).append("name", "Work");
        
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        Optional<Tag> result = repository.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals("Work", result.get().getName());
        assertEquals(1L, result.get().getId());
    }
    
    @Test
    void shouldReturnEmptyWhenTagNotFoundById() {
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);
        
        Optional<Tag> result = repository.findById(999L);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void shouldSaveNewTag() {
        Tag tag = new Tag("Work");
        
        Tag result = repository.save(tag);
        
        verify(collection).replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class));
        assertNotNull(result.getId());
        assertEquals("Work", result.getName());
    }
    
    @Test
    void shouldSaveExistingTag() {
        Tag tag = new Tag("Work");
        tag.setId(1L);
        
        Tag result = repository.save(tag);
        
        verify(collection).replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class));
        assertEquals(1L, result.getId());
        assertEquals("Work", result.getName());
    }
    
    @Test
    void shouldDeleteTag() {
        Tag tag = new Tag("Work");
        tag.setId(1L);
        
        repository.delete(tag);
        
        verify(collection).deleteOne(any(Bson.class));
    }
    
    @Test
    void shouldNotDeleteTagWithNullId() {
        Tag tag = new Tag("Work");
        
        repository.delete(tag);
        
        verify(collection, never()).deleteOne(any(Bson.class));
    }
    
    @Test
    void shouldDeleteById() {
        repository.deleteById(1L);
        
        verify(collection).deleteOne(any(Bson.class));
    }
    
    @Test
    void shouldFindByName() {
        Document doc = new Document("_id", 1L).append("name", "Work");
        
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        Optional<Tag> result = repository.findByName("Work");
        
        assertTrue(result.isPresent());
        assertEquals("Work", result.get().getName());
    }
    
    @Test
    void shouldReturnEmptyWhenTagNotFoundByName() {
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);
        
        Optional<Tag> result = repository.findByName("NonExistent");
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void shouldFindByNameContaining() {
        Document doc1 = new Document("_id", 1L).append("name", "Work");
        Document doc2 = new Document("_id", 2L).append("name", "Homework");
        
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);
        
        List<Tag> result = repository.findByNameContaining("work");
        
        assertEquals(2, result.size());
    }
    
    @Test
    void shouldFindByNameContainingWhenEmpty() {
        when(collection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        List<Tag> result = repository.findByNameContaining("nonexistent");
        
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
        MongoTagRepository nullClientRepo = new MongoTagRepository(collection, null);
        
        // Should not throw exception
        assertDoesNotThrow(() -> nullClientRepo.close());
    }
}
