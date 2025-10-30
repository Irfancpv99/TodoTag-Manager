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
    void testSingletonAndMySQL() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            EntityManager mockEM = mock(EntityManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockDbMgr.getEntityManager()).thenReturn(mockEM);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory instance1 = RepositoryFactory.getInstance();
            RepositoryFactory instance2 = RepositoryFactory.getInstance();
            
            assertSame(instance1, instance2);
            assertNotNull(instance1);
            
            TodoRepository todoRepo = instance1.createTodoRepository();
            TagRepository tagRepo = instance1.createTagRepository();
            
            assertInstanceOf(MySqlTodoRepository.class, todoRepo);
            assertInstanceOf(MySqlTagRepository.class, tagRepo);
            
            EntityManager em = instance1.getEntityManager();
            assertSame(mockEM, em);
            
            instance1.beginTransaction();
            instance1.commitTransaction();
            instance1.rollbackTransaction();
            instance1.close();
            
            verify(mockDbMgr, times(1)).beginTransaction();
            verify(mockDbMgr, times(1)).commitTransaction();
            verify(mockDbMgr, times(1)).rollbackTransaction();
            verify(mockDbMgr, times(1)).close();
        }
    }

    @Test
    void testMySQLWithNullEntityManager() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockDbMgr.getEntityManager()).thenReturn(null);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            
            assertThrows(IllegalStateException.class, factory::createTodoRepository);
            assertThrows(IllegalStateException.class, factory::createTagRepository);
        }
    }

    @Test
    void testMongoDB() {
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
                
                assertEquals(2, mockedTagRepo.constructed().size());
                assertEquals(1, mockedTodoRepo.constructed().size());
            }
        }
    }

    @Test
    void testUnsupportedDatabaseType() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<DatabaseManager> mockedDbManager = mockStatic(DatabaseManager.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            DatabaseManager mockDbMgr = mock(DatabaseManager.class);
            
            when(mockConfig.getDatabaseType()).thenReturn(null);
            
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            mockedDbManager.when(DatabaseManager::getInstance).thenReturn(mockDbMgr);
            
            RepositoryFactory factory = RepositoryFactory.getInstance();
            
            assertThrows(IllegalArgumentException.class, factory::createTodoRepository);
            assertThrows(IllegalArgumentException.class, factory::createTagRepository);
        }
    }

    @Test
    void testConstructorWithNullDatabaseManager() {
        AppConfig mockConfig = mock(AppConfig.class);
        
        RepositoryFactory factory = new RepositoryFactory(mockConfig);
        
        assertNull(factory.getEntityManager());
        assertDoesNotThrow(() -> {
            factory.beginTransaction();
            factory.commitTransaction();
            factory.rollbackTransaction();
            factory.close();
        });
    }
}