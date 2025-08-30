package com.todoapp.repository.mongo;

import com.todoapp.model.Tag;
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

@ExtendWith(MockitoExtension.class)
class MongoTagRepositoryTest {

    @Mock
    private MongoClient mongoClient;
    
    @Mock
    private MongoDatabase mongoDatabase;
    
    @Mock
    private MongoCollection<Document> tagCollection;

    private MongoTagRepository mongoTagRepository;

    @BeforeEach
    void setUp() {
        // RED: This will fail - MongoTagRepository class doesn't exist yet
        when(mongoClient.getDatabase("testdb")).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection("tags")).thenReturn(tagCollection);
        
        mongoTagRepository = new MongoTagRepository(mongoClient, "testdb");
    }

    @Test
    void shouldSaveNewTagWithGeneratedId() {
        // RED: This will fail - MongoTagRepository.save() doesn't exist yet
        Tag tag = new Tag("work");
        
        Tag savedTag = mongoTagRepository.save(tag);
        
        assertNotNull(savedTag.getId());
        assertEquals("work", savedTag.getName());
        
        // Verify MongoDB interaction
        verify(tagCollection).replaceOne(any(), any(), any());
    }

    @Test
    void shouldUpdateExistingTag() {
        // RED: This will fail - methods don't exist yet
        Tag existingTag = new Tag("old name");
        existingTag.setId(1L);
        existingTag.setName("updated name");
        
        Tag updatedTag = mongoTagRepository.save(existingTag);
        
        assertEquals(1L, updatedTag.getId());
        assertEquals("updated name", updatedTag.getName());
        
        verify(tagCollection).replaceOne(any(), any(), any());
    }

    @Test
    void shouldFindTagById() {
        // RED: This will fail - findById() doesn't exist yet
        Document tagDoc = new Document()
            .append("_id", 1L)
            .append("name", "urgent");

        when(tagCollection.find(any())).thenReturn(mock(com.mongodb.client.FindIterable.class));
        when(tagCollection.find(any()).first()).thenReturn(tagDoc);
        
        Optional<Tag> found = mongoTagRepository.findById(1L);
        
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
        assertEquals("urgent", found.get().getName());
        
        verify(tagCollection).find(any());
    }

    @Test
    void shouldReturnEmptyWhenTagNotFound() {
        // RED: This will fail - findById() doesn't exist yet
        when(tagCollection.find(any()).first()).thenReturn(null);
        
        Optional<Tag> found = mongoTagRepository.findById(999L);
        
        assertFalse(found.isPresent());
        verify(tagCollection).find(any());
    }

    @Test
    void shouldFindAllTags() {
        // RED: This will fail - findAll() doesn't exist yet
        when(tagCollection.find()).thenReturn(mock(com.mongodb.client.FindIterable.class));
        
        List<Tag> tags = mongoTagRepository.findAll();
        
        assertNotNull(tags);
        verify(tagCollection).find();
    }

    @Test
    void shouldDeleteTagById() {
        // RED: This will fail - deleteById() doesn't exist yet
        mongoTagRepository.deleteById(1L);
        
        verify(tagCollection).deleteOne(any());
    }

    @Test
    void shouldFindTagByName() {
        // RED: This will fail - findByName() doesn't exist yet
        Document tagDoc = new Document()
            .append("_id", 1L)
            .append("name", "personal");

        when(tagCollection.find(any()).first()).thenReturn(tagDoc);
        
        Optional<Tag> found = mongoTagRepository.findByName("personal");
        
        assertTrue(found.isPresent());
        assertEquals("personal", found.get().getName());
        
        verify(tagCollection).find(any());
    }

    @Test
    void shouldFindTagsByNameContaining() {
        // RED: This will fail - findByNameContaining() doesn't exist yet
        when(tagCollection.find(any())).thenReturn(mock(com.mongodb.client.FindIterable.class));
        
        List<Tag> searchResults = mongoTagRepository.findByNameContaining("work");
        
        assertNotNull(searchResults);
        verify(tagCollection).find(any());
    }
}