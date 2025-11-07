package com.todoapp.repository.mongo;

import com.todoapp.model.Tag;

import com.todoapp.model.Todo;
import org.bson.Document;
import java.lang.reflect.Field;
import com.mongodb.client.MongoCollection;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MongoTodoRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

    private MongoTodoRepository todoRepository;
    private MongoTagRepository tagRepository;
    private String testDb;
    
    @BeforeEach
    void setUp() {
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        testDb = "test_db_" + System.currentTimeMillis();
        tagRepository = new MongoTagRepository(connectionString, testDb);
        todoRepository = new MongoTodoRepository(connectionString, testDb, tagRepository);
        todoRepository.deleteAll();
        tagRepository.deleteAll();
    }
    
    @AfterEach
    void tearDown() {
        if (todoRepository != null) {
            todoRepository.close();
        }
        if (tagRepository != null) {
            tagRepository.close();
        }
    }
    
    @Test
    void shouldPerformBasicCRUDOperations() {
        Todo todo1 = new Todo("Task 1");
        Todo todo2 = new Todo("Task 2");
        
        Todo saved1 = todoRepository.save(todo1);
        Todo saved2 = todoRepository.save(todo2);
        
        assertNotNull(saved1);
        assertNotNull(saved2);
        assertEquals(1L, saved1.getId());
        assertEquals(2L, saved2.getId());
        assertEquals(saved1.getId(), todo1.getId());
        
        Optional<Todo> found = todoRepository.findById(saved1.getId());
        assertTrue(found.isPresent());
        assertEquals("Task 1", found.get().getDescription());
        assertFalse(found.get().isDone());
        
        assertFalse(todoRepository.findById(999L).isPresent());
        
        todo1.setDescription("Updated");
        todo1.setDone(true);
        todoRepository.save(todo1);
        
        Todo updated = todoRepository.findById(todo1.getId()).get();
        assertEquals("Updated", updated.getDescription());
        assertTrue(updated.isDone());
        
        List<Todo> all = todoRepository.findAll();
        assertNotNull(all);
        assertEquals(2, all.size());
        
        todoRepository.deleteById(todo1.getId());
        assertFalse(todoRepository.findById(todo1.getId()).isPresent());
        
        todoRepository.delete(todo2);
        assertFalse(todoRepository.findById(todo2.getId()).isPresent());
        
        todoRepository.delete(new Todo("no-id"));
        
        todoRepository.save(new Todo("test"));
        todoRepository.deleteAll();
        assertTrue(todoRepository.findAll().isEmpty());
        
        Todo afterReset = todoRepository.save(new Todo("new"));
        assertEquals(1L, afterReset.getId());
    }
    
    @Test
    void shouldFindByStatusAndDescription() {
        todoRepository.save(new Todo("Done task", true));
        todoRepository.save(new Todo("Pending task", false));
        todoRepository.save(new Todo("Important meeting", false));
        todoRepository.save(new Todo("Another important item", false));
        
        List<Todo> done = todoRepository.findByDone(true);
        List<Todo> pending = todoRepository.findByDone(false);
        
        assertNotNull(done);
        assertNotNull(pending);
        assertEquals(1, done.size());
        assertEquals(3, pending.size());
        assertTrue(done.get(0).isDone());
        assertFalse(pending.get(0).isDone());
        
        List<Todo> important = todoRepository.findByDescriptionContaining("important");
        assertNotNull(important);
        assertEquals(2, important.size());
    }
    
    @Test
    void shouldHandleTodoWithTags() {
        Tag workTag = tagRepository.save(new Tag("work"));
        Tag urgentTag = tagRepository.save(new Tag("urgent"));
        
        Todo todo = new Todo("Tagged task");
        todo.addTag(workTag);
        todo.addTag(urgentTag);
        
        Todo saved = todoRepository.save(todo);
        Optional<Todo> found = todoRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getTags().size());
        assertTrue(found.get().getTags().stream().anyMatch(t -> t.getName().equals("work")));
        assertTrue(found.get().getTags().stream().anyMatch(t -> t.getName().equals("urgent")));
    }
    
    @Test
    void shouldInitializeNextIdCorrectly() {
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        
        todoRepository.save(new Todo("First"));
        todoRepository.save(new Todo("Second"));
        
        todoRepository.close();
        MongoTodoRepository newRepo = new MongoTodoRepository(connectionString, testDb, tagRepository);
        
        Todo newTodo = newRepo.save(new Todo("Third"));
        assertEquals(3L, newTodo.getId());
        
        newRepo.close();
    }
    
    @Test
    void shouldHandleCloseOperations() {
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        
        todoRepository.save(new Todo("test"));
        
        assertDoesNotThrow(todoRepository::close);
        
        assertThrows(Exception.class, () -> todoRepository.save(new Todo("after-close")));
        
        MongoTodoRepository testRepo = new MongoTodoRepository(connectionString, "temp_db", tagRepository);
        testRepo.close();
        
        assertDoesNotThrow(testRepo::close);
    }
    
    @Test
    void todoIdIsMappedCorrectly() {
        Todo todo = new Todo("Test task");
        todo.setId(123L);

        todoRepository.save(todo);

        Optional<Todo> fetched = todoRepository.findById(123L);

        assertTrue(fetched.isPresent());
        assertEquals(123L, fetched.get().getId());
    }
    
    @Test
    void testFindTodoWithoutTagIdsField() throws Exception {
        // Covers: documentToTodo() when tagIds field is missing (returns null)
        Field collectionField = MongoTodoRepository.class.getDeclaredField("collection");
        collectionField.setAccessible(true);
        MongoCollection<Document> collection = (MongoCollection<Document>) collectionField.get(todoRepository);
        
        // Insert document directly without tagIds field
        Document doc = new Document("_id", 999L)
            .append("description", "Task without tags")
            .append("done", false);
        collection.insertOne(doc);
        
        Optional<Todo> found = todoRepository.findById(999L);
        assertTrue(found.isPresent());
        assertTrue(found.get().getTags().isEmpty()); // Should handle null gracefully
        
        todoRepository.deleteById(999L); // Cleanup
    }

    @Test
    void testCloseWithNullMongoClient() throws Exception {
        // Covers: close() when mongoClient is null
        String tempDb = "temp_close_test_" + System.currentTimeMillis();
        MongoTagRepository tempTagRepo = new MongoTagRepository(
            mongoDBContainer.getReplicaSetUrl(), tempDb
        );
        MongoTodoRepository testRepo = new MongoTodoRepository(
            mongoDBContainer.getReplicaSetUrl(), tempDb, tempTagRepo
        );
        
        Field field = MongoTodoRepository.class.getDeclaredField("mongoClient");
        field.setAccessible(true);
        field.set(testRepo, null);
        
        assertDoesNotThrow(testRepo::close);
        tempTagRepo.close();
    }
    
}