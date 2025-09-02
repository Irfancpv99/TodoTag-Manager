package com.todoapp.repository.mongo;

import com.todoapp.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MongoTagRepositoryTest {

    @SuppressWarnings("resource")
	@Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);

    private MongoTagRepository repository;
    private final String TEST_DB = "test_db_" + System.currentTimeMillis();
    
    @BeforeEach
    void setUp() {
        String connectionString = mongoDBContainer.getConnectionString();
        repository = new MongoTagRepository(connectionString, TEST_DB);
        repository.deleteAll();
    }
    
    @Test
    void shouldPerformBasicCRUDOperations() {
        // Save and verify ID generation
        Tag tag1 = repository.save(new Tag("work"));
        Tag tag2 = repository.save(new Tag("urgent"));
        
        assertEquals(1L, tag1.getId());
        assertEquals(2L, tag2.getId());
        
        // Find by ID
        Optional<Tag> found = repository.findById(tag1.getId());
        assertTrue(found.isPresent());
        assertEquals("work", found.get().getName());
        
        // Find non-existent
        assertFalse(repository.findById(999L).isPresent());
        
        // Update existing
        tag1.setName("updated");
        repository.save(tag1);
        assertEquals("updated", repository.findById(tag1.getId()).get().getName());
        
        // Find all
        assertEquals(2, repository.findAll().size());
        
        // Delete by ID
        repository.deleteById(tag1.getId());
        assertFalse(repository.findById(tag1.getId()).isPresent());
        
        // Delete by entity
        repository.delete(tag2);
        assertFalse(repository.findById(tag2.getId()).isPresent());
        
        // Delete with null ID 
        repository.delete(new Tag("no-id"));
        
        // Delete all and verify ID reset
        repository.save(new Tag("test"));
        repository.deleteAll();
        assertTrue(repository.findAll().isEmpty());
        
        Tag afterReset = repository.save(new Tag("new"));
        assertEquals(1L, afterReset.getId());
    }
    
    @Test
    void shouldFindByNameOperations() {
        repository.save(new Tag("important-work"));
        repository.save(new Tag("URGENT-task"));
        repository.save(new Tag("important-meeting"));
        
        // Find by exact name
        Optional<Tag> found = repository.findByName("important-work");
        assertTrue(found.isPresent());
        
        // Find by non-existent name
        assertFalse(repository.findByName("nonexistent").isPresent());
        
        // Find by name containing 
        List<Tag> important = repository.findByNameContaining("important");
        assertEquals(2, important.size());
        
        List<Tag> urgent = repository.findByNameContaining("urgent");
        assertEquals(1, urgent.size());
    }
    
    @Test
    void shouldInitializeNextIdFromExistingData() {
        repository.save(new Tag("first"));
        repository.save(new Tag("second"));
        
        String connectionString = mongoDBContainer.getConnectionString();
        MongoTagRepository newRepo = new MongoTagRepository(connectionString, TEST_DB);
       
        Tag newTag = newRepo.save(new Tag("third"));
        assertEquals(3L, newTag.getId());
        
        newRepo.close();
    }
    
    @Test
    void shouldHandleCloseOperations() {
        repository.save(new Tag("test"));
        
        assertDoesNotThrow(() -> repository.close());
        
        assertThrows(Exception.class, () -> repository.save(new Tag("after-close")));
        
        String connectionString = mongoDBContainer.getConnectionString();
        MongoTagRepository testRepo = new MongoTagRepository(connectionString, "temp_db");
        testRepo.close();
        
        assertDoesNotThrow(() -> testRepo.close());
    }
}