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
    void resetSingleton() {
        try {
            Field dmInstance = DatabaseManager.class.getDeclaredField("instance");
            dmInstance.setAccessible(true);
            dmInstance.set(null, null);
            
            Field acInstance = AppConfig.class.getDeclaredField("instance");
            acInstance.setAccessible(true);
            acInstance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldBeSingleton() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class)) {
            AppConfig mockConfig = mock(AppConfig.class);
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);

            DatabaseManager instance1 = DatabaseManager.getInstance();
            DatabaseManager instance2 = DatabaseManager.getInstance();

            assertNotNull(instance1);
            assertSame(instance1, instance2);
        }
    }

    @Test
    void shouldInitializeWithMongoDB() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class)) {
            AppConfig mockConfig = mock(AppConfig.class);
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);

            DatabaseManager manager = DatabaseManager.getInstance();
            
            assertNull(manager.getEntityManager());
            verify(mockConfig).getDatabaseType();
        }
    }

    @Test
    void shouldInitializeWithMySQL() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<Persistence> mockedPersistence = mockStatic(Persistence.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockConfig.getMySqlUrl()).thenReturn("jdbc:mysql://localhost:3306/test");
            when(mockConfig.getMySqlUsername()).thenReturn("user");
            when(mockConfig.getMySqlPassword()).thenReturn("pass");
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            
            EntityManagerFactory mockEMF = mock(EntityManagerFactory.class);
            EntityManager mockEM = mock(EntityManager.class);
            when(mockEMF.createEntityManager()).thenReturn(mockEM);
            mockedPersistence.when(() -> Persistence.createEntityManagerFactory(
                eq("todoapp"), anyMap()
            )).thenReturn(mockEMF);
            
            DatabaseManager manager = DatabaseManager.getInstance();
            
            assertNotNull(manager.getEntityManager());
        }
    }

    @Test
    void shouldThrowExceptionOnMySQLInitializationFailure() {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class);
             MockedStatic<Persistence> mockedPersistence = mockStatic(Persistence.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
            when(mockConfig.getMySqlUrl()).thenReturn("jdbc:mysql://invalid");
            when(mockConfig.getMySqlUsername()).thenReturn("user");
            when(mockConfig.getMySqlPassword()).thenReturn("pass");
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            
            mockedPersistence.when(() -> Persistence.createEntityManagerFactory(
                eq("todoapp"), anyMap()
            )).thenThrow(new PersistenceException("Connection failed"));
            
            IllegalStateException exception = assertThrows(IllegalStateException.class, 
                DatabaseManager::getInstance
            );
            
            assertEquals("MySQL database initialization failed", exception.getMessage());
        }
    }

    @Test
    void shouldHandleTransactionMethods() throws Exception {
        try (MockedStatic<AppConfig> mockedAppConfig = mockStatic(AppConfig.class)) {
            AppConfig mockConfig = mock(AppConfig.class);
            when(mockConfig.getDatabaseType()).thenReturn(DatabaseType.MONGODB);
            mockedAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);

            DatabaseManager manager = DatabaseManager.getInstance();
            
            assertDoesNotThrow(() -> manager.beginTransaction());
            assertDoesNotThrow(() -> manager.commitTransaction());
            assertDoesNotThrow(() -> manager.rollbackTransaction());
            assertDoesNotThrow(() -> manager.close());
            
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
    }
}