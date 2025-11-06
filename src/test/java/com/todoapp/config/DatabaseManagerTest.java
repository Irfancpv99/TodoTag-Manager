package com.todoapp.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseManagerTest {

    @BeforeEach
    @AfterEach

    @Test
    void shouldInitializeWithMongoDB() {
        assertMongoDBInitializationWorks();
    }

    @Test
    void shouldInitializeWithMongoDBUsingConstructor() {
        assertMongoDBInitializationWorks(); 
    }

    @Test
    void shouldInitializeWithMySQL() {
        try (MockedStatic<Persistence> mockedPersistence = mockStatic(Persistence.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockConfig.getMySqlUrl()).thenReturn("jdbc:mysql://localhost:3306/test");
            when(mockConfig.getMySqlUsername()).thenReturn("user");
            when(mockConfig.getMySqlPassword()).thenReturn("pass");
            
            EntityManagerFactory mockEMF = mock(EntityManagerFactory.class);
            EntityManager mockEM = mock(EntityManager.class);
            when(mockEMF.createEntityManager()).thenReturn(mockEM);
            mockedPersistence.when(() -> Persistence.createEntityManagerFactory(
                eq("todoapp"), anyMap()
            )).thenReturn(mockEMF);
            
            DatabaseManager manager = new DatabaseManager(mockConfig);
            
            assertNotNull(manager.getEntityManager());
        }
    }

    @Test
    void shouldInitializeWithMySQLUsingConstructor() {
        try (MockedStatic<Persistence> mockedPersistence = mockStatic(Persistence.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockConfig.getMySqlUrl()).thenReturn("jdbc:mysql://localhost:3306/test");
            when(mockConfig.getMySqlUsername()).thenReturn("user");
            when(mockConfig.getMySqlPassword()).thenReturn("pass");
            
            EntityManagerFactory mockEMF = mock(EntityManagerFactory.class);
            EntityManager mockEM = mock(EntityManager.class);
            when(mockEMF.createEntityManager()).thenReturn(mockEM);
            mockedPersistence.when(() -> Persistence.createEntityManagerFactory(
                eq("todoapp"), anyMap()
            )).thenReturn(mockEMF);
            
            DatabaseManager manager = new DatabaseManager(mockConfig);
            
            assertNotNull(manager.getEntityManager());
        }
    }

    @Test
    void shouldThrowExceptionOnMySQLInitializationFailure() {
        try (MockedStatic<Persistence> mockedPersistence = mockStatic(Persistence.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockConfig.getMySqlUrl()).thenReturn("jdbc:mysql://invalid");
            when(mockConfig.getMySqlUsername()).thenReturn("user");
            when(mockConfig.getMySqlPassword()).thenReturn("pass");
            
            mockedPersistence.when(() -> Persistence.createEntityManagerFactory(
                eq("todoapp"), anyMap()
            )).thenThrow(new PersistenceException("Connection failed"));
            
            IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> new DatabaseManager(mockConfig)
            );
            
            assertEquals("MySQL database initialization failed", exception.getMessage());
        }
    }

    @Test
    void shouldThrowExceptionOnMySQLInitializationFailureWithConstructor() {
        try (MockedStatic<Persistence> mockedPersistence = mockStatic(Persistence.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockConfig.getMySqlUrl()).thenReturn("jdbc:mysql://invalid");
            when(mockConfig.getMySqlUsername()).thenReturn("user");
            when(mockConfig.getMySqlPassword()).thenReturn("pass");
            
            mockedPersistence.when(() -> Persistence.createEntityManagerFactory(
                eq("todoapp"), anyMap()
            )).thenThrow(new PersistenceException("Connection failed"));
            
            IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> new DatabaseManager(mockConfig)
            );
            
            assertEquals("MySQL database initialization failed", exception.getMessage());
        }
    }

    @Test
    void shouldHandleTransactionMethods() throws Exception {
        AppConfig mockConfig = mock(AppConfig.class);
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);

        DatabaseManager manager = new DatabaseManager(mockConfig);
        
        assertDoesNotThrow(manager::beginTransaction);
        assertDoesNotThrow(manager::commitTransaction);
        assertDoesNotThrow(manager::rollbackTransaction);
        assertDoesNotThrow(manager::close);
        
        EntityManager mockEM = mock(EntityManager.class);
        EntityTransaction mockTx = mock(EntityTransaction.class);
        EntityManagerFactory mockEMF = mock(EntityManagerFactory.class);
        
        when(mockEM.getTransaction()).thenReturn(mockTx);
        when(mockEM.isOpen()).thenReturn(true, false);
        when(mockEMF.isOpen()).thenReturn(true, false);
        when(mockTx.isActive()).thenReturn(false, true, true, false);
        
        Field emField = DatabaseManager.class.getDeclaredField("entityManager");
        emField.setAccessible(true);
        emField.set(manager, mockEM);
        
        Field emfField = DatabaseManager.class.getDeclaredField("entityManagerFactory");
        emfField.setAccessible(true);
        emfField.set(manager, mockEMF);
        
        manager.beginTransaction(); 
        verify(mockTx).begin();
        
        manager.commitTransaction();
        verify(mockTx).commit();
        
        manager.rollbackTransaction(); 
        verify(mockTx).rollback();
        
        manager.beginTransaction(); 
        verify(mockTx, times(2)).begin();
        
        manager.close();
        verify(mockEM).close();
        verify(mockEMF).close();
        
        manager.close();
        verify(mockEM, times(1)).close();
        verify(mockEMF, times(1)).close();
    }

    @Test
    void shouldCreateMultipleInstances() {
        AppConfig mockConfig = mock(AppConfig.class);
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);

        DatabaseManager instance1 = new DatabaseManager(mockConfig);
        DatabaseManager instance2 = new DatabaseManager(mockConfig);

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertNotSame(instance1, instance2, "Should create different instances");
    }
    
//    		helper
    
    private void assertMongoDBInitializationWorks() {
        AppConfig mockConfig = mock(AppConfig.class);
        when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);

        DatabaseManager manager = new DatabaseManager(mockConfig);

        assertNull(manager.getEntityManager());
        verify(mockConfig).getDatabaseType();
    }
}