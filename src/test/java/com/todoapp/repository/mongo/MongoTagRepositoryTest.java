package com.todoapp.repository.mongo;

import com.todoapp.model.Tag;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MongoTagRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

    private MongoTagRepository repository;
    private String testDb;

    @BeforeEach
    void setUp() {
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        testDb = "testdb_" + System.currentTimeMillis();
        repository = new MongoTagRepository(connectionString, testDb);
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        if (repository != null) {
            repository.deleteAll();
            repository.close();
        }
    }

    @Test
    void testSaveAndFindById() {
        Tag tag = new Tag("Work");
        Tag saved = repository.save(tag);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        
        Optional<Tag> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
        assertEquals("Work", found.get().getName());
    }

    @Test
    void testFindAll() {
        repository.save(new Tag("Work"));
        repository.save(new Tag("Personal"));

        List<Tag> tags = repository.findAll();
        assertNotNull(tags);
        assertEquals(2, tags.size());
    }

    @Test
    void testFindByName() {
        repository.save(new Tag("Work"));

        Optional<Tag> found = repository.findByName("Work");
        assertTrue(found.isPresent());
        assertEquals("Work", found.get().getName());
    }

    @Test
    void testFindByNameNotFound() {
        Optional<Tag> found = repository.findByName("NonExistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByNameContaining() {
        repository.save(new Tag("Work Project"));
        repository.save(new Tag("Personal Work"));
        repository.save(new Tag("Home"));

        List<Tag> found = repository.findByNameContaining("Work");
        assertNotNull(found);
        assertEquals(2, found.size());
    }

    @Test
    void testDelete() {
        Tag tag = repository.save(new Tag("ToDelete"));
        assertNotNull(tag.getId());

        repository.delete(tag);

        Optional<Tag> found = repository.findById(tag.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteById() {
        Tag tag = repository.save(new Tag("ToDelete"));
        Long id = tag.getId();

        repository.deleteById(id);

        Optional<Tag> found = repository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdate() {
        Tag tag = repository.save(new Tag("Original"));
        Long id = tag.getId();

        tag.setName("Updated");
        repository.save(tag);

        Optional<Tag> found = repository.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getName());
    }

    @Test
    void testMultipleSavesIncrementId() {
        Tag tag1 = repository.save(new Tag("First"));
        Tag tag2 = repository.save(new Tag("Second"));
        Tag tag3 = repository.save(new Tag("Third"));

        assertEquals(1L, tag1.getId());
        assertEquals(2L, tag2.getId());
        assertEquals(3L, tag3.getId());
    }

    @Test
    void testInitializeNextIdFromExistingData() {
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        
        repository.save(new Tag("First"));
        repository.save(new Tag("Second"));
        
        Long firstId = repository.findByName("First").get().getId();
        Long secondId = repository.findByName("Second").get().getId();
        assertEquals(1L, firstId);
        assertEquals(2L, secondId);
        
        repository.close();
        repository = null;
        
        MongoTagRepository newRepo = new MongoTagRepository(connectionString, testDb);
        Tag newTag = newRepo.save(new Tag("Third"));
        
        assertEquals(3L, newTag.getId());
        
        newRepo.deleteAll();
        newRepo.close();
    }

    @Test
    void testCloseOperations() {
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        String tempDb = "temp_close_test_" + System.currentTimeMillis();
        
        MongoTagRepository testRepo = new MongoTagRepository(connectionString, tempDb);
        testRepo.save(new Tag("test"));
        
        assertDoesNotThrow(testRepo::close);
        
        assertThrows(Exception.class, () -> testRepo.save(new Tag("after-close")));
        
        assertDoesNotThrow(testRepo::close);
    }
}