package com.todoapp.repository;

import com.todoapp.config.AppConfig;
import com.todoapp.config.DatabaseManager;
import com.todoapp.config.DatabaseType;
import com.todoapp.repository.mongo.MongoTagRepository;
import com.todoapp.repository.mongo.MongoTodoRepository;
import com.todoapp.repository.mysql.MySqlTagRepository;
import com.todoapp.repository.mysql.MySqlTodoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepositoryFactoryTest {

    @BeforeEach
    void setUp() {
        resetSingleton();
    }

    @AfterEach
    void tearDown() {
        resetSingleton();
    }

    private void resetSingleton() {
        try {
            Field instance = RepositoryFactory.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSingletonAndMySQL() {
        try (MockedConstruction<DatabaseManager> mockedDbManager = mockConstruction(DatabaseManager.class,
            (mock, context) -> {
                EntityManager mockEM = mock(EntityManager.class);
                when(mock.getEntityManager()).thenReturn(mockEM);
            })) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            
            RepositoryFactory instance1 = RepositoryFactory.getInstance(mockConfig);
            RepositoryFactory instance2 = RepositoryFactory.getInstance(mockConfig);
            
            assertSame(instance1, instance2);
            assertNotNull(instance1);
            
            TodoRepository todoRepo = instance1.createTodoRepository();
            TagRepository tagRepo = instance1.createTagRepository();
            
            assertInstanceOf(MySqlTodoRepository.class, todoRepo);
            assertInstanceOf(MySqlTagRepository.class, tagRepo);
            
            EntityManager em = instance1.getEntityManager();
            assertNotNull(em);
            
            instance1.beginTransaction();
            instance1.commitTransaction();
            instance1.rollbackTransaction();
            instance1.close();
            
            DatabaseManager dbManager = mockedDbManager.constructed().get(0);
            verify(dbManager, times(1)).beginTransaction();
            verify(dbManager, times(1)).commitTransaction();
            verify(dbManager, times(1)).rollbackTransaction();
            verify(dbManager, times(1)).close();
        }
    }

    @Test
    void testMySQLWithNullEntityManager() {
        try (MockedConstruction<DatabaseManager> mockedDbManager = mockConstruction(DatabaseManager.class,
            (mock, context) -> {
                when(mock.getEntityManager()).thenReturn(null);
            })) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            
            RepositoryFactory factory = RepositoryFactory.getInstance(mockConfig);
            
            assertThrows(IllegalStateException.class, factory::createTodoRepository);
            assertThrows(IllegalStateException.class, factory::createTagRepository);
        }
    }

    @Test
    void testMongoDB() {
        try (MockedConstruction<DatabaseManager> mockedDbManagerConstruction = mockConstruction(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
            when(mockConfig.getMongoDbHost()).thenReturn("localhost");
            when(mockConfig.getMongoDbPort()).thenReturn(27017);
            when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
            
            try (MockedConstruction<MongoTagRepository> mockedTagRepo = mockConstruction(MongoTagRepository.class);
                 MockedConstruction<MongoTodoRepository> mockedTodoRepo = mockConstruction(MongoTodoRepository.class)) {
                
                RepositoryFactory factory = RepositoryFactory.getInstance(mockConfig);
                
                TodoRepository todoRepo = factory.createTodoRepository();
                TagRepository tagRepo = factory.createTagRepository();
                
                assertInstanceOf(MongoTodoRepository.class, todoRepo);
                assertInstanceOf(MongoTagRepository.class, tagRepo);
                
                assertEquals(2, mockedTagRepo.constructed().size());
                assertEquals(1, mockedTodoRepo.constructed().size());
            }
        }
    }

    @Test
    void testUnsupportedDatabaseType() {
        try (MockedConstruction<DatabaseManager> mockedDbManager = mockConstruction(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(null);
            
            RepositoryFactory factory = RepositoryFactory.getInstance(mockConfig);
            
            assertThrows(IllegalArgumentException.class, factory::createTodoRepository);
            assertThrows(IllegalArgumentException.class, factory::createTagRepository);
        }
    }

    @Test
    void testConstructorWithDatabaseManager() {
        AppConfig mockConfig = mock(AppConfig.class);
        DatabaseManager mockDbManager = mock(DatabaseManager.class);
        
        RepositoryFactory factory = new RepositoryFactory(mockConfig, mockDbManager);
        
        when(mockDbManager.getEntityManager()).thenReturn(null);
        
        assertNull(factory.getEntityManager());
        assertDoesNotThrow(() -> {
            factory.beginTransaction();
            factory.commitTransaction();
            factory.rollbackTransaction();
            factory.close();
        });
        
        verify(mockDbManager).getEntityManager();
        verify(mockDbManager).beginTransaction();
        verify(mockDbManager).commitTransaction();
        verify(mockDbManager).rollbackTransaction();
        verify(mockDbManager).close();
    }

    @Test
    void testConstructorWithNullDatabaseManager() {
        AppConfig mockConfig = mock(AppConfig.class);
        
        RepositoryFactory factory = new RepositoryFactory(mockConfig, null);
        
        assertNull(factory.getEntityManager());
        assertDoesNotThrow(() -> {
            factory.beginTransaction();
            factory.commitTransaction();
            factory.rollbackTransaction();
            factory.close();
        });
    }

    @Test
    void testMySQLRepositoryCreation() {
        AppConfig mockConfig = mock(AppConfig.class);
        DatabaseManager mockDbManager = mock(DatabaseManager.class);
        EntityManager mockEM = mock(EntityManager.class);
        
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        when(mockDbManager.getEntityManager()).thenReturn(mockEM);
        
        RepositoryFactory factory = new RepositoryFactory(mockConfig, mockDbManager);
        
        TodoRepository todoRepo = factory.createTodoRepository();
        TagRepository tagRepo = factory.createTagRepository();
        
        assertInstanceOf(MySqlTodoRepository.class, todoRepo);
        assertInstanceOf(MySqlTagRepository.class, tagRepo);
    }

    @Test
    void testMongoDBRepositoryCreation() {
        AppConfig mockConfig = mock(AppConfig.class);
        DatabaseManager mockDbManager = mock(DatabaseManager.class);
        
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        when(mockConfig.getMongoDbHost()).thenReturn("localhost");
        when(mockConfig.getMongoDbPort()).thenReturn(27017);
        when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
        
        try (MockedConstruction<MongoTagRepository> mockedTagRepo = mockConstruction(MongoTagRepository.class);
             MockedConstruction<MongoTodoRepository> mockedTodoRepo = mockConstruction(MongoTodoRepository.class)) {
            
            RepositoryFactory factory = new RepositoryFactory(mockConfig, mockDbManager);
            
            TodoRepository todoRepo = factory.createTodoRepository();
            TagRepository tagRepo = factory.createTagRepository();
            
            assertInstanceOf(MongoTodoRepository.class, todoRepo);
            assertInstanceOf(MongoTagRepository.class, tagRepo);
            
            assertEquals(2, mockedTagRepo.constructed().size());
            assertEquals(1, mockedTodoRepo.constructed().size());
        }
    }

    @Test
    void testTransactionManagementWithValidDatabaseManager() {
        AppConfig mockConfig = mock(AppConfig.class);
        DatabaseManager mockDbManager = mock(DatabaseManager.class);
        
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        
        RepositoryFactory factory = new RepositoryFactory(mockConfig, mockDbManager);
        
        factory.beginTransaction();
        factory.commitTransaction();
        factory.rollbackTransaction();
        
        verify(mockDbManager).beginTransaction();
        verify(mockDbManager).commitTransaction();
        verify(mockDbManager).rollbackTransaction();
    }

    @Test
    void testCloseWithValidDatabaseManager() {
        AppConfig mockConfig = mock(AppConfig.class);
        DatabaseManager mockDbManager = mock(DatabaseManager.class);
        
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        
        RepositoryFactory factory = new RepositoryFactory(mockConfig, mockDbManager);
        
        factory.close();
        
        verify(mockDbManager).close();
    }
}