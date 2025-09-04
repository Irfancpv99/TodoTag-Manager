package com.todoapp.repository;

import com.todoapp.config.AppConfig;
import com.todoapp.config.DatabaseType;
import com.todoapp.repository.mysql.MySqlTagRepository;
import com.todoapp.repository.mysql.MySqlTodoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RepositoryFactoryTest {

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
        
        assertInstanceOf(MySqlTodoRepository.class, result);
    }

    @Test
    void testCreateTagRepository_MySQL() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
        
        TagRepository result = repositoryFactory.createTagRepository();
        
        assertInstanceOf(MySqlTagRepository.class, result);
    }

    @Test
    void testCreateTodoRepository_MongoDB_ThrowsException() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        when(mockConfig.getMongoDbHost()).thenReturn("nonexistent-host");
        when(mockConfig.getMongoDbPort()).thenReturn(27017);
        when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
        
       assertThrows(Exception.class, () -> repositoryFactory.createTodoRepository());
    }

    @Test
    void testCreateTagRepository_MongoDB_ThrowsException() {
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
        when(mockConfig.getMongoDbHost()).thenReturn("nonexistent-host");
        when(mockConfig.getMongoDbPort()).thenReturn(27017);
        when(mockConfig.getMongoDbDatabase()).thenReturn("testdb");
        
       assertThrows(Exception.class, () -> repositoryFactory.createTagRepository());
    }

    @Test
    void testCreateTodoRepository_UnsupportedType() {
        when(mockConfig.getDatabaseType()).thenReturn(null);
        
        assertThrows(IllegalArgumentException.class, 
            () -> repositoryFactory.createTodoRepository());
    }

    @Test
    void testCreateTagRepository_UnsupportedType() {
        when(mockConfig.getDatabaseType()).thenReturn(null);
        
        assertThrows(IllegalArgumentException.class, 
            () -> repositoryFactory.createTagRepository());
    }
}