package com.todoapp.repository.mongo;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MongoTodoRepositoryTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = 
        new MongoDBContainer("mongo:6.0.5");

    private MongoTodoRepository todoRepository;
    private MongoTagRepository tagRepository;

    @BeforeEach
    void setUp() {
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        tagRepository = new MongoTagRepository(connectionString, "testdb");
        todoRepository = new MongoTodoRepository(connectionString, "testdb", tagRepository);
        
        tagRepository.deleteAll();
        todoRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        if (todoRepository != null) {
            todoRepository.deleteAll();
            todoRepository.close();
        }
        if (tagRepository != null) {
            tagRepository.deleteAll();
            tagRepository.close();
        }
    }
    
    @Test
    void shouldPerformBasicCRUDOperations() {
        Todo todo1 = new Todo("Task 1");
        Todo todo2 = new Todo("Task 2");
        
        Todo saved1 = todoRepository.save(todo1);
        Todo saved2 = todoRepository.save(todo2);
        
        assertEquals(1L, saved1.getId());
        assertEquals(2L, saved2.getId());
        assertEquals(saved1.getId(), todo1.getId());
        
        // Find by ID
        Optional<Todo> found = todoRepository.findById(saved1.getId());
        assertTrue(found.isPresent());
        assertEquals("Task 1", found.get().getDescription());
        assertFalse(found.get().isDone());
        
        // Find non-existent
        assertFalse(todoRepository.findById(999L).isPresent());
        
        // Update existing
        todo1.setDescription("Updated");
        todo1.setDone(true);
        todoRepository.save(todo1);
        
        Todo updated = todoRepository.findById(todo1.getId()).get();
        assertEquals("Updated", updated.getDescription());
        assertTrue(updated.isDone());
        
        // Find all
        assertEquals(2, todoRepository.findAll().size());
        
        // Delete by ID
        todoRepository.deleteById(todo1.getId());
        assertFalse(todoRepository.findById(todo1.getId()).isPresent());
        
        // Delete by entity
        todoRepository.delete(todo2);
        assertFalse(todoRepository.findById(todo2.getId()).isPresent());
        
        // Delete with null ID (should not crash)
        todoRepository.delete(new Todo("no-id"));
        
        // Delete all and verify ID reset
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
        
        // Find by done status
        List<Todo> done = todoRepository.findByDone(true);
        List<Todo> pending = todoRepository.findByDone(false);
        
        assertEquals(1, done.size());
        assertEquals(3, pending.size());
        assertTrue(done.get(0).isDone());
        assertFalse(pending.get(0).isDone());
        
        // Find by description containing (case insensitive)
        List<Todo> important = todoRepository.findByDescriptionContaining("important");
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
        todoRepository.save(new Todo("First"));
        todoRepository.save(new Todo("Second"));
        
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        MongoTodoRepository newRepo = new MongoTodoRepository(connectionString, "testdb", tagRepository);
        
        Todo newTodo = newRepo.save(new Todo("Third"));
        assertEquals(3L, newTodo.getId());
        
        newRepo.close();
    }
    
    @Test
    void shouldHandleCloseOperations() {
        todoRepository.save(new Todo("test"));
        
        assertDoesNotThrow(() -> todoRepository.close());
        
        assertThrows(Exception.class, () -> todoRepository.save(new Todo("after-close")));
        
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        MongoTodoRepository testRepo = new MongoTodoRepository(connectionString, "temp_db", tagRepository);
        testRepo.close();
        
        assertDoesNotThrow(() -> testRepo.close());
    }
    
    @Test
    void todoIdIsMappedCorrectly() {
        Todo todo = new Todo("Test task");
        todo.setId(123L);

        todoRepository.save(todo);

        Optional<Todo> fetched = todoRepository.findById(123L);

        assertTrue(fetched.isPresent(), "Todo should be found");
        assertEquals(123L, fetched.get().getId(), "Todo id should be set correctly");
    }
}