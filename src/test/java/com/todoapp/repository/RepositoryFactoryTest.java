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
import org.mockito.MockedStatic;

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
            
            Field acInstance = AppConfig.class.getDeclaredField("instance");
            acInstance.setAccessible(true);
            acInstance.set(null, null);
            
            Field dmInstance = DatabaseManager.class.getDeclaredField("instance");
            dmInstance.setAccessible(true);
            dmInstance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSingletonBehavior() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory instance1 = RepositoryFactory.getInstance();
            RepositoryFactory instance2 = RepositoryFactory.getInstance();
            
            assertNotNull(instance1);
            assertSame(instance1, instance2);
        }
    }

    @Test
    void testCreateTodoRepository_MySQL() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            EntityManager mockEM = mock(EntityManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockDbMgr.getEntityManager()).thenReturn(mockEM);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            TodoRepository result = factory.createTodoRepository();
            
            assertNotNull(result);
            assertInstanceOf(MySqlTodoRepository.class, result);
        }
    }

    @Test
    void testCreateTodoRepository_MySQLWithNullEntityManager() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockDbMgr.getEntityManager()).thenReturn(null);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> factory.createTodoRepository()
            );
            
            assertEquals("EntityManager not initialized for MySQL", exception.getMessage());
        }
    }

    @Test
    void testCreateTodoRepository_MongoDB() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
            when(mockConfig.getMongoDbHost()).thenReturn("localhost");
            when(mockConfig.getMongoDbPort()).thenReturn(27017);
            when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            try (MockedConstruction<MongoTagRepository> mockedTagRepo = mockConstruction(MongoTagRepository.class);
                 MockedConstruction<MongoTodoRepository> mockedTodoRepo = mockConstruction(MongoTodoRepository.class)) {
                
                RepositoryFactory factory = RepositoryFactory.getInstance();
                TodoRepository result = factory.createTodoRepository();
                
                assertNotNull(result);
                assertInstanceOf(MongoTodoRepository.class, result);
                
                assertEquals(1, mockedTagRepo.constructed().size());
                assertEquals(1, mockedTodoRepo.constructed().size());
            }
        }
    }

    @Test
    void testCreateTodoRepository_UnsupportedType() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(null);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> factory.createTodoRepository()
            );
            assertEquals("Unsupported database type: null", exception.getMessage());
        }
    }

    @Test
    void testCreateTagRepository_MySQL() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            EntityManager mockEM = mock(EntityManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockDbMgr.getEntityManager()).thenReturn(mockEM);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            TagRepository result = factory.createTagRepository();
            
            assertNotNull(result);
            assertInstanceOf(MySqlTagRepository.class, result);
        }
    }

    @Test
    void testCreateTagRepository_MySQLWithNullEntityManager() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockDbMgr.getEntityManager()).thenReturn(null);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> factory.createTagRepository()
            );
            
            assertEquals("EntityManager not initialized for MySQL", exception.getMessage());
        }
    }

    @Test
    void testCreateTagRepository_MongoDB() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
            when(mockConfig.getMongoDbHost()).thenReturn("localhost");
            when(mockConfig.getMongoDbPort()).thenReturn(27017);
            when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            try (MockedConstruction<MongoTagRepository> mockedTagRepo = mockConstruction(MongoTagRepository.class)) {
                
                RepositoryFactory factory = RepositoryFactory.getInstance();
                TagRepository result = factory.createTagRepository();
                
                assertNotNull(result);
                assertInstanceOf(MongoTagRepository.class, result);
                
                assertEquals(1, mockedTagRepo.constructed().size());
            }
        }
    }

    @Test
    void testCreateTagRepository_UnsupportedType() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(null);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, 
                () -> factory.createTagRepository()
            );
            assertEquals("Unsupported database type: null", exception.getMessage());
        }
    }

    @Test
    void testGetEntityManager() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            EntityManager mockEM = mock(EntityManager.class);
            
            when(mockDbMgr.getEntityManager()).thenReturn(mockEM);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            EntityManager result = factory.getEntityManager();
            
            assertSame(mockEM, result);
            assertNotNull(result);
        }
    }

    @Test
    void testBeginTransaction() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            factory.beginTransaction();
            
            verify(mockDbMgr, times(1)).beginTransaction();
        }
    }

    @Test
    void testCommitTransaction() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            factory.commitTransaction();
            
            verify(mockDbMgr, times(1)).commitTransaction();
        }
    }

    @Test
    void testRollbackTransaction() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            factory.rollbackTransaction();
            
            verify(mockDbMgr, times(1)).rollbackTransaction();
        }
    }

    @Test
    void testClose() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            factory.close();
            
            verify(mockDbMgr, times(1)).close();
        }
    }

    @Test
    void testTransactionMethodsWithTestConstructor() {
        // Test the test constructor that sets databaseManager to null
        AppConfig mockConfig = mock(AppConfig.class);
        EntityManager mockEM = mock(EntityManager.class);
        
        RepositoryFactory factory = new RepositoryFactory(mockConfig, mockEM);
        
        // Should not throw when databaseManager is null
        assertDoesNotThrow(() -> factory.beginTransaction());
        assertDoesNotThrow(() -> factory.commitTransaction());
        assertDoesNotThrow(() -> factory.rollbackTransaction());
        assertDoesNotThrow(() -> factory.close());
    }

    @Test
    void testGetEntityManagerWithTestConstructor() {
        // Test getEntityManager when databaseManager is null (test constructor)
        AppConfig mockConfig = mock(AppConfig.class);
        EntityManager mockEM = mock(EntityManager.class);
        
        RepositoryFactory factory = new RepositoryFactory(mockConfig, mockEM);
        EntityManager result = factory.getEntityManager();
        
        assertNull(result); // Returns null because databaseManager is null
    }

    @Test
    void testConditionalBranchesMySQL() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            EntityManager mockEM = mock(EntityManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockDbMgr.getEntityManager()).thenReturn(mockEM);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            
            TodoRepository todoRepo = factory.createTodoRepository();
            TagRepository tagRepo = factory.createTagRepository();
            
            assertInstanceOf(MySqlTodoRepository.class, todoRepo);
            assertInstanceOf(MySqlTagRepository.class, tagRepo);
        }
    }

    @Test
    void testConditionalBranchesMongoDB() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
            when(mockConfig.getMongoDbHost()).thenReturn("localhost");
            when(mockConfig.getMongoDbPort()).thenReturn(27017);
            when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            try (MockedConstruction<MongoTagRepository> mockedTagRepo = mockConstruction(MongoTagRepository.class);
                 MockedConstruction<MongoTodoRepository> mockedTodoRepo = mockConstruction(MongoTodoRepository.class)) {
                
                RepositoryFactory factory = RepositoryFactory.getInstance();
                
                TodoRepository todoRepo = factory.createTodoRepository();
                TagRepository tagRepo = factory.createTagRepository();
                
                assertInstanceOf(MongoTodoRepository.class, todoRepo);
                assertInstanceOf(MongoTagRepository.class, tagRepo);
                assertNotNull(todoRepo);
                assertNotNull(tagRepo);
            }
        }
    }

    @Test
    void testNullReturnMutations() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            
            // Kill NULL_RETURNS mutant on getInstance
            assertNotNull(factory);
        }
    }

    @Test
    void testGetInstanceConditional() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            // First call creates instance
            RepositoryFactory factory1 = RepositoryFactory.getInstance();
            assertNotNull(factory1);
            
            // Second call returns same instance (kills NEGATE_CONDITIONALS mutant)
            RepositoryFactory factory2 = RepositoryFactory.getInstance();
            assertSame(factory1, factory2);
        }
    }

    @Test
    void testTernaryOperatorInGetEntityManager() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            EntityManager mockEM = mock(EntityManager.class);
            
            when(mockDbMgr.getEntityManager()).thenReturn(mockEM);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            
            // databaseManager != null, should return databaseManager.getEntityManager()
            EntityManager result = factory.getEntityManager();
            assertNotNull(result);
            assertSame(mockEM, result);
        }
    }
}