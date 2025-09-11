package com.todoapp.service;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.repository.mongo.MongoTagRepository;
import com.todoapp.repository.mongo.MongoTodoRepository;
import com.todoapp.repository.mysql.MySqlTagRepository;
import com.todoapp.repository.mysql.MySqlTodoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class TodoServiceIntegrationTest {

    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:6.0");

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // MongoDB repositories
    private TodoService mongoTodoService;
    private MongoTodoRepository mongoTodoRepository;
    private MongoTagRepository mongoTagRepository;

    // MySQL repositories
    private TodoService mysqlTodoService;
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        setupMongoDB();
        setupMySQL();
    }

    void setupMongoDB() {
    	String databaseName = "test_" + System.nanoTime();
        
        mongoTagRepository = new MongoTagRepository(mongoContainer.getConnectionString(), databaseName);
        mongoTodoRepository = new MongoTodoRepository(mongoContainer.getConnectionString(), databaseName, mongoTagRepository);
        
        mongoTodoRepository.deleteAll();
        mongoTagRepository.deleteAll();
        
        mongoTodoService = new TodoService(mongoTodoRepository, mongoTagRepository);
    }

    void setupMySQL() {
       Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", mysqlContainer.getJdbcUrl());
        properties.put("jakarta.persistence.jdbc.user", mysqlContainer.getUsername());
        properties.put("jakarta.persistence.jdbc.password", mysqlContainer.getPassword());
        properties.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.show_sql", "false");
        
        entityManagerFactory = Persistence.createEntityManagerFactory("todoapp", properties);
        entityManager = entityManagerFactory.createEntityManager();
        
        MySqlTagRepository mysqlTagRepository = new MySqlTagRepository(entityManager);
        MySqlTodoRepository mysqlTodoRepository = new MySqlTodoRepository(entityManager);
        mysqlTodoService = new TodoService(mysqlTodoRepository, mysqlTagRepository);
    }

    @AfterEach
    void tearDown() {
        if (mongoTodoRepository != null) {
            mongoTodoRepository.deleteAll();
            mongoTodoRepository.close();
        }
        if (mongoTagRepository != null) {
            mongoTagRepository.deleteAll();
            mongoTagRepository.close();
        }
        
        if (entityManager != null) entityManager.close();
        if (entityManagerFactory != null) entityManagerFactory.close();
    }

    // MongoDB Tests
    
    @Test
    void shouldCreateAndRetrieveTodos_MongoDB() {
        Todo todo = mongoTodoService.createTodo("Learn MongoDB");
        
        assertNotNull(todo.getId());
        assertEquals("Learn MongoDB", todo.getDescription());
        assertFalse(todo.isDone());
        assertEquals(1, mongoTodoService.getAllTodos().size());
    }

    @Test
    void shouldCreateAndRetrieveTags_MongoDB() {
        Tag tag = mongoTodoService.createTag("database");
        
        assertNotNull(tag.getId());
        assertEquals("database", tag.getName());
        assertEquals(1, mongoTodoService.getAllTags().size());
    }

    @Test
    void shouldAddTagsToTodos_MongoDB() {
        Todo todo = mongoTodoService.createTodo("Learn MongoDB");
        Tag tag = mongoTodoService.createTag("database");
        
        mongoTodoService.addTagToTodo(todo.getId(), tag.getId());
        
        Todo todoWithTag = mongoTodoService.getTodoById(todo.getId()).get();
        assertEquals(1, todoWithTag.getTags().size());
        assertTrue(todoWithTag.getTags().contains(tag));
    }

    @Test
    void shouldHandleValidationErrors_MongoDB() {
        assertThrows(IllegalArgumentException.class, () -> mongoTodoService.createTodo(null));
        assertThrows(IllegalArgumentException.class, () -> mongoTodoService.createTodo(""));
        assertThrows(IllegalArgumentException.class, () -> mongoTodoService.createTag(null));
        assertThrows(IllegalArgumentException.class, () -> mongoTodoService.createTag(""));
        
        assertEquals(0, mongoTodoService.getAllTodos().size());
        assertEquals(0, mongoTodoService.getAllTags().size());
    }

    // MySQL Tests

    @Test
    void shouldCreateAndRetrieveTodos_MySQL() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        
        try {
            Todo todo = mysqlTodoService.createTodo("Learn MySQL");
            
            assertNotNull(todo.getId());
            assertEquals("Learn MySQL", todo.getDescription());
            assertFalse(todo.isDone());
            assertEquals(1, mysqlTodoService.getAllTodos().size());
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    @Test
    void shouldCreateAndRetrieveTags_MySQL() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        
        try {
            Tag tag = mysqlTodoService.createTag("relational");
            
            assertNotNull(tag.getId());
            assertEquals("relational", tag.getName());
            assertEquals(1, mysqlTodoService.getAllTags().size());
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    @Test
    void shouldAddTagsToTodos_MySQL() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        
        try {
            Todo todo = mysqlTodoService.createTodo("Learn MySQL");
            Tag tag = mysqlTodoService.createTag("relational");
            
            mysqlTodoService.addTagToTodo(todo.getId(), tag.getId());
            
            Todo todoWithTag = mysqlTodoService.getTodoById(todo.getId()).get();
            assertEquals(1, todoWithTag.getTags().size());
            assertTrue(todoWithTag.getTags().contains(tag));
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    @Test
    void shouldHandleValidationErrors_MySQL() {
        assertThrows(IllegalArgumentException.class, () -> mysqlTodoService.createTodo(null));
        assertThrows(IllegalArgumentException.class, () -> mysqlTodoService.createTodo(""));
        assertThrows(IllegalArgumentException.class, () -> mysqlTodoService.createTag(null));
        assertThrows(IllegalArgumentException.class, () -> mysqlTodoService.createTag(""));
        
        assertEquals(0, mysqlTodoService.getAllTodos().size());
        assertEquals(0, mysqlTodoService.getAllTags().size());
    }

    
    @Test
    void shouldHandleComplexWorkflow_MongoDB() {
        Todo work1 = mongoTodoService.createTodo("Fix bug in service layer");
        Tag workTag = mongoTodoService.createTag("work");
        Tag urgentTag = mongoTodoService.createTag("urgent");
        
        mongoTodoService.addTagToTodo(work1.getId(), workTag.getId());
        mongoTodoService.addTagToTodo(work1.getId(), urgentTag.getId());
        
        Todo retrieved = mongoTodoService.getTodoById(work1.getId()).get();
        assertEquals(2, retrieved.getTags().size());
        
        mongoTodoService.removeTagFromTodo(work1.getId(), urgentTag.getId());
        Todo updated = mongoTodoService.getTodoById(work1.getId()).get();
        assertEquals(1, updated.getTags().size());
        assertTrue(updated.getTags().stream().anyMatch(t -> t.getName().equals("work")));
        assertFalse(updated.getTags().stream().anyMatch(t -> t.getName().equals("urgent")));
    }

    @Test
    void shouldHandleComplexWorkflow_MySQL() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        
        try {
            Todo work1 = mysqlTodoService.createTodo("Fix bug in service layer");
            Tag workTag = mysqlTodoService.createTag("work");
            Tag urgentTag = mysqlTodoService.createTag("urgent");
            
            mysqlTodoService.addTagToTodo(work1.getId(), workTag.getId());
            mysqlTodoService.addTagToTodo(work1.getId(), urgentTag.getId());
            
            Todo retrieved = mysqlTodoService.getTodoById(work1.getId()).get();
            assertEquals(2, retrieved.getTags().size());
            
            mysqlTodoService.removeTagFromTodo(work1.getId(), urgentTag.getId());
            Todo updated = mysqlTodoService.getTodoById(work1.getId()).get();
            assertEquals(1, updated.getTags().size());
            assertTrue(updated.getTags().stream().anyMatch(t -> t.getName().equals("work")));
            assertFalse(updated.getTags().stream().anyMatch(t -> t.getName().equals("urgent")));
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}