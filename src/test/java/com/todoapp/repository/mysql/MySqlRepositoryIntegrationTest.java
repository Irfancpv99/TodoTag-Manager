package com.todoapp.repository.mysql;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MySqlRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private MySqlTodoRepository todoRepository;
    private MySqlTagRepository tagRepository;

    @BeforeEach
    void setUp() {
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", mysql.getJdbcUrl());
        properties.put("jakarta.persistence.jdbc.user", mysql.getUsername());
        properties.put("jakarta.persistence.jdbc.password", mysql.getPassword());
        properties.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        properties.put("hibernate.hbm2ddl.auto", "create");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", "false");

        entityManagerFactory = Persistence.createEntityManagerFactory("todoapp", properties);
        entityManager = entityManagerFactory.createEntityManager();
        
        todoRepository = new MySqlTodoRepository(entityManager);
        tagRepository = new MySqlTagRepository(entityManager);
        
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        if (entityManager != null && entityManager.isOpen()) {
            cleanDatabase();
            entityManager.close();
        }
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    private void cleanDatabase() {
        try {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM Todo").executeUpdate();
            entityManager.createQuery("DELETE FROM Tag").executeUpdate();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
        }
    }

    @Test
    void shouldPerformTodoOperations() {
        entityManager.getTransaction().begin();
        
        Todo todo = new Todo("Test todo");
        Todo saved = todoRepository.save(todo);
        
        entityManager.getTransaction().commit();
        
        assertNotNull(saved.getId());
        assertEquals("Test todo", saved.getDescription());
        assertFalse(saved.isDone());
        
        Optional<Todo> found = todoRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test todo", found.get().getDescription());
        
        // Update
        entityManager.getTransaction().begin();
        todo.setDescription("Updated");
        todo.setDone(true);
        todoRepository.save(todo);
        entityManager.getTransaction().commit();
        
        found = todoRepository.findById(todo.getId());
        assertEquals("Updated", found.get().getDescription());
        assertTrue(found.get().isDone());
        
        // Find all and search operations
        entityManager.getTransaction().begin();
        todoRepository.save(new Todo("Another todo"));
        todoRepository.save(new Todo("Important task"));
        entityManager.getTransaction().commit();
        
        assertEquals(3, todoRepository.findAll().size());
        
        List<Todo> done = todoRepository.findByDone(true);
        List<Todo> pending = todoRepository.findByDone(false);
        List<Todo> important = todoRepository.findByDescriptionContaining("important");
        
        assertEquals(1, done.size());
        assertEquals(2, pending.size());
        assertEquals(1, important.size());
        
        // Delete
        entityManager.getTransaction().begin();
        todoRepository.deleteById(todo.getId());
        entityManager.getTransaction().commit();
        
        assertFalse(todoRepository.findById(todo.getId()).isPresent());
    }

    @Test
    void shouldPerformTagOperations() {
        entityManager.getTransaction().begin();
        
        Tag tag = new Tag("work");
        Tag saved = tagRepository.save(tag);
        
        entityManager.getTransaction().commit();
        
        assertNotNull(saved.getId());
        assertEquals("work", saved.getName());
        
        Optional<Tag> found = tagRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("work", found.get().getName());
        
        // Find by name and name containing
        found = tagRepository.findByName("work");
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        
        entityManager.getTransaction().begin();
        tagRepository.save(new Tag("important-work"));
        tagRepository.save(new Tag("important-meeting"));
        entityManager.getTransaction().commit();
        
        List<Tag> containing = tagRepository.findByNameContaining("important");
        assertEquals(2, containing.size());
        
        // Delete
        entityManager.getTransaction().begin();
        tagRepository.delete(tag);
        entityManager.getTransaction().commit();
        
        assertFalse(tagRepository.findById(tag.getId()).isPresent());
    }
}