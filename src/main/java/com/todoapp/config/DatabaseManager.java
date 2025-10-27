package com.todoapp.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages database connections and EntityManager lifecycle.
 * 
 * Uses Singleton pattern because:
 * - Manages expensive database connection resources that should be shared application-wide
 * - Ensures single EntityManagerFactory per application lifecycle
 * - Thread-safe initialization with synchronized getInstance()
 * 
 * The Singleton pattern is appropriate here despite SonarCloud warnings because
 * this class manages stateful, expensive resources (database connections) that
 * must be shared across the application and properly cleaned up on shutdown.
 */
@SuppressWarnings("java:S6548") // Singleton pattern is intentional and necessary for resource management
public class DatabaseManager {
    private static DatabaseManager instance;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private final DatabaseType databaseType;

    private DatabaseManager() {
        AppConfig config = AppConfig.getInstance();
        this.databaseType = config.getDatabaseType();
        
        if (databaseType == DatabaseType.MYSQL) {
            initializeMySQL(config);
        }
        // MongoDB initialization is handled elsewhere (no JPA setup needed)
    }

    private void initializeMySQL(AppConfig config) {
        try {
            Map<String, String> properties = new HashMap<>();
            properties.put("jakarta.persistence.jdbc.url", config.getMySqlUrl());
            properties.put("jakarta.persistence.jdbc.user", config.getMySqlUsername());
            properties.put("jakarta.persistence.jdbc.password", config.getMySqlPassword());
            properties.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
            properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            properties.put("hibernate.hbm2ddl.auto", "update");
            properties.put("hibernate.show_sql", "true");
            
            this.entityManagerFactory = Persistence.createEntityManagerFactory("todoapp", properties);
            this.entityManager = entityManagerFactory.createEntityManager();
        } catch (Exception e) {
            throw new IllegalStateException("Database initialization failed", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void beginTransaction() {
        if (entityManager != null) {
            EntityTransaction transaction = entityManager.getTransaction();
            if (!transaction.isActive()) {
                transaction.begin();
            }
        }
    }

    public void commitTransaction() {
        if (entityManager != null) {
            EntityTransaction transaction = entityManager.getTransaction();
            if (transaction.isActive()) {
                transaction.commit();
            }
        }
    }

    public void rollbackTransaction() {
        if (entityManager != null) {
            EntityTransaction transaction = entityManager.getTransaction();
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    public void close() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}