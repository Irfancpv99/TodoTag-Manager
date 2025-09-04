package com.todoapp.repository;

import com.todoapp.config.AppConfig;
import com.todoapp.config.DatabaseType;
import com.todoapp.repository.mongo.MongoTagRepository;
import com.todoapp.repository.mongo.MongoTodoRepository;
import com.todoapp.repository.mysql.MySqlTagRepository;
import com.todoapp.repository.mysql.MySqlTodoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Testcontainers
class RepositoryFactoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);

    @Mock
    private AppConfig mockConfig;
    
    @Mock
    private EntityManager mockEntityManager;
    
    private RepositoryFactory repositoryFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repositoryFactory = new RepositoryFactory(mockConfig, mockEntityManager);
    }

    @Test
    void testCreateTodoRepository_MySQL() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        
        TodoRepository result = repositoryFactory.createTodoRepository();
        
        assertNotNull(result);
        assertInstanceOf(MySqlTodoRepository.class, result);
    }

    @Test
    void testCreateTagRepository_MySQL() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        
        TagRepository result = repositoryFactory.createTagRepository();
        
        assertNotNull(result);
        assertInstanceOf(MySqlTagRepository.class, result);
    }

    @Test
    void testCreateTodoRepository_MongoDB() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        when(mockConfig.getMongoDbHost()).thenReturn(mongoDBContainer.getHost());
        when(mockConfig.getMongoDbPort()).thenReturn(mongoDBContainer.getMappedPort(27017));
        when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
        
        TodoRepository result = repositoryFactory.createTodoRepository();
        
        assertNotNull(result);
        assertInstanceOf(MongoTodoRepository.class, result);
    }

    @Test
    void testCreateTagRepository_MongoDB() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        when(mockConfig.getMongoDbHost()).thenReturn(mongoDBContainer.getHost());
        when(mockConfig.getMongoDbPort()).thenReturn(mongoDBContainer.getMappedPort(27017));
        when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
        
        TagRepository result = repositoryFactory.createTagRepository();
        
        assertNotNull(result);
        assertInstanceOf(MongoTagRepository.class, result);
    }

    @Test
    void testCreateTodoRepository_UnsupportedType() {
        when(mockConfig.getDatabaseType()).thenReturn(null);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repositoryFactory.createTodoRepository()
        );
        assertEquals("Unsupported database type: null", exception.getMessage());
    }

    @Test
    void testCreateTagRepository_UnsupportedType() {
        when(mockConfig.getDatabaseType()).thenReturn(null);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> repositoryFactory.createTagRepository()
        );
        assertEquals("Unsupported database type: null", exception.getMessage());
    }

    @Test
    void testDatabaseTypeConditionals() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        TodoRepository mysqlTodo = repositoryFactory.createTodoRepository();
        TagRepository mysqlTag = repositoryFactory.createTagRepository();
        
        assertTrue(mysqlTodo instanceof MySqlTodoRepository);
        assertTrue(mysqlTag instanceof MySqlTagRepository);
        
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        when(mockConfig.getMongoDbHost()).thenReturn(mongoDBContainer.getHost());
        when(mockConfig.getMongoDbPort()).thenReturn(mongoDBContainer.getMappedPort(27017));
        when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
        
        TodoRepository mongoTodo = repositoryFactory.createTodoRepository();
        TagRepository mongoTag = repositoryFactory.createTagRepository();
        
        assertTrue(mongoTodo instanceof MongoTodoRepository);
        assertTrue(mongoTag instanceof MongoTagRepository);
        
        assertFalse(mongoTodo instanceof MySqlTodoRepository);
        assertFalse(mongoTag instanceof MySqlTagRepository);
    }
}