package com.todoapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    
    private static AppConfig instance;
    private final Properties properties;

    protected AppConfig() {
        properties = new Properties();
        loadProperties();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    protected void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                setDefaultProperties();
            }
        } catch (IOException e) {
            // Intentionally using default properties if file cannot be loaded
            setDefaultProperties();
        }
    }

    protected void setDefaultProperties() {
        properties.setProperty("database.type", "MONGODB");
        properties.setProperty("mongodb.host", "localhost");
        properties.setProperty("mongodb.port", "27017");
        properties.setProperty("mongodb.database", "todoapp");
        properties.setProperty("mysql.url", "jdbc:mysql://localhost:3306/todoapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        properties.setProperty("mysql.username", "todouser");
        properties.setProperty("mysql.password", "todopassword");
    }

    public DatabaseType getDatabaseType() {
        String type = properties.getProperty("database.type", "MONGODB");
        return DatabaseType.valueOf(type.toUpperCase());
    }

    public void setDatabaseType(DatabaseType type) {
        properties.setProperty("database.type", type.name());
    }

    public String getMongoDbHost() {
        return properties.getProperty("mongodb.host", "localhost");
    }

    public int getMongoDbPort() {
        return Integer.parseInt(properties.getProperty("mongodb.port", "27017"));
    }

    public String getMongoDbDatabase() {
        return properties.getProperty("mongodb.database", "todoapp");
    }

    public String getMySqlUrl() {
        return properties.getProperty("mysql.url");
    }

    public String getMySqlUsername() {
        return properties.getProperty("mysql.username");
    }

    public String getMySqlPassword() {
        return properties.getProperty("mysql.password");
    }
}