package com.todoapp.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Database manager to handle EntityManager lifecycle for MySQL
 * and provide centralized database initialization
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private final AppConfig config;
    
    private DatabaseManager() {
        this.config = AppConfig.getInstance();
        initialize();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initialize() {
        if (config.getDatabaseType() == DatabaseType.MYSQL) {
            initializeMySql();
        }
        // MongoDB doesn't need EntityManager initialization
    }
    
    private void initializeMySql() {
        try {
            Map<String, String> properties = new HashMap<>();
            properties.put("jakarta.persistence.jdbc.url", config.getMySqlUrl());
            properties.put("jakarta.persistence.jdbc.user", config.getMySqlUsername());
            properties.put("jakarta.persistence.jdbc.password", config.getMySqlPassword());
            properties.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
            properties.put("hibernate.hbm2ddl.auto", "update");
            properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            properties.put("hibernate.show_sql", "false");
            
            entityManagerFactory = Persistence.createEntityManagerFactory("todoapp", properties);
            entityManager = entityManagerFactory.createEntityManager();
            
            System.out.println("MySQL EntityManager initialized successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize MySQL EntityManager: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    public EntityManager getEntityManager() {
        return entityManager;
    }
    
    public void close() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
    
    /**
     * Begin a transaction if using MySQL
     */
    public void beginTransaction() {
        if (entityManager != null && !entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
    }
    
    /**
     * Commit a transaction if using MySQL
     */
    public void commitTransaction() {
        if (entityManager != null && entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().commit();
        }
    }
    
    /**
     * Rollback a transaction if using MySQL
     */
    public void rollbackTransaction() {
        if (entityManager != null && entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
    }
}