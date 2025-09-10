package com.todoapp.repository;

import com.todoapp.config.AppConfig;
import com.todoapp.config.DatabaseType;
import com.todoapp.repository.mongo.MongoTagRepository;
import com.todoapp.repository.mongo.MongoTodoRepository;
import com.todoapp.repository.mysql.MySqlTagRepository;
import com.todoapp.repository.mysql.MySqlTodoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepositoryFactoryTest {

    @Mock
    private AppConfig mockConfig;
    
    @Mock
    private EntityManager mockEntityManager;
    
    private RepositoryFactory repositoryFactory;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        repositoryFactory = new RepositoryFactory(mockConfig, mockEntityManager);
        resetSingletonInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
        resetSingletonInstance();
    }

    private void resetSingletonInstance() {
        try {
            Field instanceField = RepositoryFactory.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            // Ignore reflection errors in tests
        }
    }

    @Test
    void testSingletonBehavior() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class)) {
            AppConfig mockAppConfig = mock(AppConfig.class);
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockAppConfig);
            
            RepositoryFactory instance1 = RepositoryFactory.getInstance();
            RepositoryFactory instance2 = RepositoryFactory.getInstance();
            
            assertNotNull(instance1);
            assertSame(instance1, instance2);
        }
    }

    @Test
    void testCreateTodoRepository_MySQL() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        
        TodoRepository result = repositoryFactory.createTodoRepository();
        
        assertNotNull(result);
        assertInstanceOf(MySqlTodoRepository.class, result);
    }

    @Test
    void testCreateTodoRepository_MySQLWithNullEntityManager() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        RepositoryFactory factoryWithNullEM = new RepositoryFactory(mockConfig, null);
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> factoryWithNullEM.createTodoRepository()
        );
        
        assertEquals("EntityManager not initialized for MySQL", exception.getMessage());
    }

    @Test
    void testCreateTodoRepository_MongoDB() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        when(mockConfig.getMongoDbHost()).thenReturn("localhost");
        when(mockConfig.getMongoDbPort()).thenReturn(27017);
        when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
        
        TodoRepository result = repositoryFactory.createTodoRepository();
        
        assertNotNull(result);
        assertInstanceOf(MongoTodoRepository.class, result);
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
    void testCreateTagRepository_MySQL() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        
        TagRepository result = repositoryFactory.createTagRepository();
        
        assertNotNull(result);
        assertInstanceOf(MySqlTagRepository.class, result);
    }

    @Test
    void testCreateTagRepository_MySQLWithNullEntityManager() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        RepositoryFactory factoryWithNullEM = new RepositoryFactory(mockConfig, null);
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> factoryWithNullEM.createTagRepository()
        );
        
        assertEquals("EntityManager not initialized for MySQL", exception.getMessage());
    }

    @Test
    void testCreateTagRepository_MongoDB() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        when(mockConfig.getMongoDbHost()).thenReturn("localhost");
        when(mockConfig.getMongoDbPort()).thenReturn(27017);
        when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
        
        TagRepository result = repositoryFactory.createTagRepository();
        
        assertNotNull(result);
        assertInstanceOf(MongoTagRepository.class, result);
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
    void testGetEntityManager() {
        EntityManager result = repositoryFactory.getEntityManager();
        
        assertSame(mockEntityManager, result);
    }

    @Test 
    void testGetEntityManager_ReturnsNull() {
        RepositoryFactory factoryWithNullEM = new RepositoryFactory(mockConfig, null);
        
        EntityManager result = factoryWithNullEM.getEntityManager();
        
        assertNull(result);
    }

    @Test
    void testClose() {
        
        assertDoesNotThrow(() -> repositoryFactory.close());
    }

    @Test
    void testConditionalBranches() {
        // Test MySQL branch
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        TodoRepository mysqlTodo = repositoryFactory.createTodoRepository();
        TagRepository mysqlTag = repositoryFactory.createTagRepository();
        
        assertTrue(mysqlTodo instanceof MySqlTodoRepository);
        assertTrue(mysqlTag instanceof MySqlTagRepository);
        assertFalse(mysqlTodo instanceof MongoTodoRepository);
        assertFalse(mysqlTag instanceof MongoTagRepository);
        
        // Test MongoDB branch  
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        when(mockConfig.getMongoDbHost()).thenReturn("localhost");
        when(mockConfig.getMongoDbPort()).thenReturn(27017);
        when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
        
        TodoRepository mongoTodo = repositoryFactory.createTodoRepository();
        TagRepository mongoTag = repositoryFactory.createTagRepository();
        
        assertTrue(mongoTodo instanceof MongoTodoRepository);
        assertTrue(mongoTag instanceof MongoTagRepository);
        assertFalse(mongoTodo instanceof MySqlTodoRepository);
        assertFalse(mongoTag instanceof MySqlTagRepository);
    }
}