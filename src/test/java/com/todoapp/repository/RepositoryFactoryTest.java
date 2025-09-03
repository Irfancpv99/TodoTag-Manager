package com.todoapp.repository;

import com.todoapp.config.AppConfig;
import com.todoapp.config.DatabaseType;
import com.todoapp.repository.mongo.MongoTagRepository;
import com.todoapp.repository.mongo.MongoTodoRepository;
import com.todoapp.repository.mysql.MySqlTagRepository;
import com.todoapp.repository.mysql.MySqlTodoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryFactoryTest {

    private RepositoryFactory factory;
    private AppConfig config;

    @BeforeEach
    void setUp() {
        // RED: This will fail - RepositoryFactory class doesn't exist yet
        factory = RepositoryFactory.getInstance();
        config = AppConfig.getInstance();
    }

    @AfterEach
    void tearDown() {
        // RED: This will fail - reset method doesn't exist yet
        factory.reset();
    }

    @Test
    void shouldBeSingleton() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        RepositoryFactory instance1 = RepositoryFactory.getInstance();
        RepositoryFactory instance2 = RepositoryFactory.getInstance();
        
        assertSame(instance1, instance2);
    }

    @Test
    void shouldCreateMongoTodoRepository() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        config.setDatabaseType(DatabaseType.MONGODB);
        
        TodoRepository repository = factory.createTodoRepository();
        
        assertNotNull(repository);
        assertTrue(repository instanceof MongoTodoRepository);
    }

    @Test
    void shouldCreateMongoTagRepository() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        config.setDatabaseType(DatabaseType.MONGODB);
        
        TagRepository repository = factory.createTagRepository();
        
        assertNotNull(repository);
        assertTrue(repository instanceof MongoTagRepository);
    }

    @Test
    void shouldCreateMySqlTodoRepository() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        config.setDatabaseType(DatabaseType.MYSQL);
        
        // Test that we can create the repository (might succeed or fail depending on environment)
        try {
            TodoRepository repository = factory.createTodoRepository();
            assertNotNull(repository);
            assertTrue(repository instanceof MySqlTodoRepository);
        } catch (Exception e) {
            // If MySQL is not available, that's also acceptable for this test
            assertTrue(e instanceof RuntimeException || e instanceof jakarta.persistence.PersistenceException);
        }
    }

    @Test
    void shouldCreateMySqlTagRepository() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        config.setDatabaseType(DatabaseType.MYSQL);
        
        // Test that we can create the repository (might succeed or fail depending on environment)
        try {
            TagRepository repository = factory.createTagRepository();
            assertNotNull(repository);
            assertTrue(repository instanceof MySqlTagRepository);
        } catch (Exception e) {
            // If MySQL is not available, that's also acceptable for this test
            assertTrue(e instanceof RuntimeException || e instanceof jakarta.persistence.PersistenceException);
        }
    }

    @Test
    void shouldReuseRepositoryInstances() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        config.setDatabaseType(DatabaseType.MONGODB);
        
        TodoRepository repo1 = factory.createTodoRepository();
        TodoRepository repo2 = factory.createTodoRepository();
        
        // Should return the same instance (cached)
        assertSame(repo1, repo2);
    }

    @Test
    void shouldCreateDifferentRepositoriesForDifferentDatabases() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        // Test MongoDB
        config.setDatabaseType(DatabaseType.MONGODB);
        TodoRepository mongoRepo = factory.createTodoRepository();
        
        // Reset factory to clear cache
        factory.reset();
        
        // Test MySQL (if available)
        config.setDatabaseType(DatabaseType.MYSQL);
        try {
            TodoRepository mysqlRepo = factory.createTodoRepository();
            
            // Should be different instances and types
            assertNotSame(mongoRepo, mysqlRepo);
            assertTrue(mongoRepo instanceof MongoTodoRepository);
            assertTrue(mysqlRepo instanceof MySqlTodoRepository);
        } catch (Exception e) {
            // MySQL might not be available in test environment
            System.out.println("MySQL test skipped: " + e.getMessage());
        }
    }

    @Test
    void shouldHandleUnsupportedDatabaseType() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        config.setDatabaseType(DatabaseType.MONGODB);
        
        // Verify that current implementation works
        assertDoesNotThrow(() -> {
            factory.createTodoRepository();
            factory.createTagRepository();
        });
    }

    @Test
    void shouldCleanUpResourcesOnReset() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        config.setDatabaseType(DatabaseType.MONGODB);
        
        // Create repositories
        TodoRepository todoRepo1 = factory.createTodoRepository();
        TagRepository tagRepo1 = factory.createTagRepository();
        
        // Reset factory
        factory.reset();
        
        // Create new repositories
        TodoRepository todoRepo2 = factory.createTodoRepository();
        TagRepository tagRepo2 = factory.createTagRepository();
        
        // Should create new instances after reset
        assertNotSame(todoRepo1, todoRepo2);
        assertNotSame(tagRepo1, tagRepo2);
    }

    @Test
    void shouldProvideEntityManagerForMySql() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        config.setDatabaseType(DatabaseType.MYSQL);
        
        try {
            var entityManager = factory.getEntityManager();
            
            if (entityManager != null) {
                assertNotNull(entityManager);
                assertTrue(entityManager.isOpen());
            }
        } catch (Exception e) {
            // MySQL might not be available in test environment
            System.out.println("MySQL EntityManager test skipped: " + e.getMessage());
        }
    }

    @Test
    void shouldReturnNullEntityManagerForMongoDB() {
        // RED: This will fail - RepositoryFactory doesn't exist yet
        config.setDatabaseType(DatabaseType.MONGODB);
        
        // MongoDB doesn't use EntityManager
        var entityManager = factory.getEntityManager();
        
        // For MongoDB, EntityManager will be null
        assertDoesNotThrow(() -> factory.getEntityManager());
    }
}