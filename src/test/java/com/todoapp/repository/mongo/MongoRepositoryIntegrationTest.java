package com.todoapp.repository.mongo;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MongoRepositoryIntegrationTest {

    @SuppressWarnings("resource")
	@Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);

    private MongoTodoRepository todoRepository;
    private MongoTagRepository tagRepository;

    @BeforeEach
    void setUp() {
        String connectionString = mongoDBContainer.getConnectionString();
        tagRepository = new MongoTagRepository(connectionString, "testdb");
        todoRepository = new MongoTodoRepository(connectionString, "testdb", tagRepository);
        
        todoRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    void shouldPerformTodoOperations() {
        Todo todo = new Todo("Test todo");
        Todo saved = todoRepository.save(todo);
        
        assertNotNull(saved.getId());
        assertEquals("Test todo", saved.getDescription());
        assertFalse(saved.isDone());
        
        Optional<Todo> found = todoRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test todo", found.get().getDescription());
        
        // Update
        todo.setDescription("Updated");
        todo.setDone(true);
        todoRepository.save(todo);
        
        found = todoRepository.findById(todo.getId());
        assertEquals("Updated", found.get().getDescription());
        assertTrue(found.get().isDone());
        
        // Find all
        todoRepository.save(new Todo("Another"));
        assertEquals(2, todoRepository.findAll().size());
        
        // Delete
        todoRepository.deleteById(todo.getId());
        assertFalse(todoRepository.findById(todo.getId()).isPresent());
    }

    @Test
    void shouldFindTodosByStatusAndDescription() {
        todoRepository.save(new Todo("Incomplete todo", false));
        todoRepository.save(new Todo("Complete todo", true));
        todoRepository.save(new Todo("Important task", false));
        todoRepository.save(new Todo("Another important item", false));
        
        // Find by done status
        List<Todo> incomplete = todoRepository.findByDone(false);
        List<Todo> complete = todoRepository.findByDone(true);
        
        assertEquals(3, incomplete.size());
        assertEquals(1, complete.size());
        
        // Find by description containing
        List<Todo> important = todoRepository.findByDescriptionContaining("important");
        assertEquals(2, important.size());
        assertTrue(important.stream().allMatch(t -> 
            t.getDescription().toLowerCase().contains("important")));
    }

    @Test
    void shouldPerformTagOperations() {
        // Save and find
        Tag tag = new Tag("work");
        Tag saved = tagRepository.save(tag);
        
        assertNotNull(saved.getId());
        assertEquals("work", saved.getName());
        
        Optional<Tag> found = tagRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("work", found.get().getName());
        
        // Find by name
        found = tagRepository.findByName("work");
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void shouldHandleTodoWithTags() {
        Tag workTag = tagRepository.save(new Tag("work"));
        Tag urgentTag = tagRepository.save(new Tag("urgent"));
        
        Todo todo = new Todo("Important work task");
        todo.addTag(workTag);
        todo.addTag(urgentTag);
        
        Todo saved = todoRepository.save(todo);
        
        Optional<Todo> found = todoRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        
        Todo retrieved = found.get();
        assertEquals(2, retrieved.getTags().size());
        assertTrue(retrieved.getTags().stream().anyMatch(t -> t.getName().equals("work")));
        assertTrue(retrieved.getTags().stream().anyMatch(t -> t.getName().equals("urgent")));
    }
}