package com.todoapp.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private static volatile DatabaseManager instance;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private final DatabaseType databaseType;

    private DatabaseManager() {
        AppConfig config = AppConfig.getInstance();
        this.databaseType = config.getDatabaseType();
        
        if (databaseType == DatabaseType.MYSQL) {
            initializeMySQL(config);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
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
        } catch (PersistenceException e) {
            throw new IllegalStateException("MySQL database initialization failed", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid database configuration", e);
        }
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