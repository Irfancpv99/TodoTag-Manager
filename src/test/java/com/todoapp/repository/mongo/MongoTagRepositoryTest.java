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
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
            .withExposedPorts(27017);

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
            repository.deleteAll();
            repository.close();
        }
    }

    @Test
    void testSaveAndFindById() {
        Tag tag = new Tag("Work");
        Tag saved = repository.save(tag);

        assertNotNull(saved.getId());
        
        Optional<Tag> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Work", found.get().getName());
    }

    @Test
    void testFindAll() {
        repository.save(new Tag("Work"));
        repository.save(new Tag("Personal"));

        List<Tag> tags = repository.findAll();
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
}