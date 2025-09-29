package com.todoapp.repository.mongo;

import com.todoapp.model.Tag;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MongoTagRepositoryTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = 
        new MongoDBContainer("mongo:6.0.5");

    private MongoTagRepository repository;

    @BeforeEach
    void setUp() {
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        repository = new MongoTagRepository(connectionString, "testdb");
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        if (repository != null) {
            try {
                repository.deleteAll();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        // Don't close - let Testcontainers handle the lifecycle
    }

    @Test
    void testSaveAndFindById() {
        Tag tag = new Tag("work");
        Tag saved = repository.save(tag);
        
        assertNotNull(saved.getId());
        Optional<Tag> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("work", found.get().getName());
    }

    @Test
    void testFindAll() {
        repository.save(new Tag("work"));
        repository.save(new Tag("personal"));
        
        List<Tag> tags = repository.findAll();
        assertEquals(2, tags.size());
    }

    @Test
    void testUpdate() {
        Tag tag = new Tag("work");
        Tag saved = repository.save(tag);
        
        saved.setName("urgent-work");
        repository.save(saved);
        
        Optional<Tag> updated = repository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("urgent-work", updated.get().getName());
    }

    @Test
    void testDelete() {
        Tag tag = repository.save(new Tag("temporary"));
        Long id = tag.getId();
        
        repository.delete(tag);
        
        Optional<Tag> found = repository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteById() {
        Tag tag = repository.save(new Tag("temporary"));
        Long id = tag.getId();
        
        repository.deleteById(id);
        
        Optional<Tag> found = repository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByName() {
        repository.save(new Tag("urgent"));
        
        Optional<Tag> found = repository.findByName("urgent");
        assertTrue(found.isPresent());
        assertEquals("urgent", found.get().getName());
        
        Optional<Tag> notFound = repository.findByName("nonexistent");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByNameContaining() {
        repository.save(new Tag("important-work"));
        repository.save(new Tag("important-personal"));
        repository.save(new Tag("unrelated"));
        
        List<Tag> results = repository.findByNameContaining("important");
        assertEquals(2, results.size());
        
        List<Tag> noResults = repository.findByNameContaining("xyz");
        assertTrue(noResults.isEmpty());
    }

    @Test
    void testAutoIncrementId() {
        Tag tag1 = repository.save(new Tag("first"));
        Tag tag2 = repository.save(new Tag("second"));
        
        assertNotNull(tag1.getId());
        assertNotNull(tag2.getId());
        assertNotEquals(tag1.getId(), tag2.getId());
    }

    @Test
    void testDeleteAll() {
        repository.save(new Tag("tag1"));
        repository.save(new Tag("tag2"));
        repository.save(new Tag("tag3"));
        
        assertEquals(3, repository.findAll().size());
        
        repository.deleteAll();
        
        assertEquals(0, repository.findAll().size());
    }
}