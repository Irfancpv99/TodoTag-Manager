package com.todoapp.repository;

import com.todoapp.config.AppConfig;
import com.todoapp.config.DatabaseType;
import com.todoapp.repository.mongo.MongoTagRepository;
import com.todoapp.repository.mongo.MongoTodoRepository;
import com.todoapp.repository.mysql.MySqlTagRepository;
import com.todoapp.repository.mysql.MySqlTodoRepository;
import jakarta.persistence.EntityManager;

public class RepositoryFactory {
    
    private final AppConfig config;
    private final EntityManager entityManager;

    public RepositoryFactory(AppConfig config, EntityManager entityManager) {
        this.config = config;
        this.entityManager = entityManager;
    }

    public TodoRepository createTodoRepository() {
        DatabaseType dbType = config.getDatabaseType();
        
        if (dbType == DatabaseType.MYSQL) {
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
            return new MySqlTagRepository(entityManager);
        } else if (dbType == DatabaseType.MONGODB) {
            String connectionString = String.format("mongodb://%s:%d", 
                config.getMongoDbHost(), config.getMongoDbPort());
            return new MongoTagRepository(connectionString, config.getMongoDbDatabase());
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
}