package com.todoapp.repository;

import com.todoapp.config.AppConfig;
import com.todoapp.config.DatabaseManager;
import com.todoapp.config.DatabaseType;
import com.todoapp.repository.mongo.MongoTagRepository;
import com.todoapp.repository.mongo.MongoTodoRepository;
import com.todoapp.repository.mysql.MySqlTagRepository;
import com.todoapp.repository.mysql.MySqlTodoRepository;
import jakarta.persistence.EntityManager;

@SuppressWarnings("java:S6548") 
public class RepositoryFactory {
    
    private static RepositoryFactory instance;
    private final AppConfig config;
    private final DatabaseManager databaseManager;

    private RepositoryFactory() {
        this.config = AppConfig.getInstance();
        this.databaseManager = DatabaseManager.getInstance();
    }

    /**
     * Constructor for testing with specific config and entity manager
     */
    public RepositoryFactory(AppConfig config, EntityManager entityManager) {
        this.config = config;
        this.databaseManager = null; // For testing
    }

    public static synchronized RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }

    public TodoRepository createTodoRepository() {
        DatabaseType dbType = config.getDatabaseType();
        
        if (dbType == DatabaseType.MYSQL) {
            EntityManager entityManager = getEntityManager();
            if (entityManager == null) {
                throw new IllegalStateException("EntityManager not initialized for MySQL");
            }
            return new MySqlTodoRepository(entityManager);
        } else if (dbType == DatabaseType.MONGODB) {
            String connectionString = String.format("mongodb://%s:%d", 
                config.getMongoDbHost(), config.getMongoDbPort());
            MongoTagRepository tagRepo = new MongoTagRepository(
                connectionString, config.getMongoDbDatabase());
            return new MongoTodoRepository(
                connectionString, config.getMongoDbDatabase(), tagRepo);
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    public TagRepository createTagRepository() {
        DatabaseType dbType = config.getDatabaseType();
        
        if (dbType == DatabaseType.MYSQL) {
            EntityManager entityManager = getEntityManager();
            if (entityManager == null) {
                throw new IllegalStateException("EntityManager not initialized for MySQL");
            }
            return new MySqlTagRepository(entityManager);
        } else if (dbType == DatabaseType.MONGODB) {
            String connectionString = String.format("mongodb://%s:%d", 
                config.getMongoDbHost(), config.getMongoDbPort());
            return new MongoTagRepository(connectionString, config.getMongoDbDatabase());
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    public EntityManager getEntityManager() {
        return databaseManager != null ? databaseManager.getEntityManager() : null;
    }

    public void beginTransaction() {
        if (databaseManager != null) {
            databaseManager.beginTransaction();
        }
    }

    public void commitTransaction() {
        if (databaseManager != null) {
            databaseManager.commitTransaction();
        }
    }

    public void rollbackTransaction() {
        if (databaseManager != null) {
            databaseManager.rollbackTransaction();
        }
    }

    public void close() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}