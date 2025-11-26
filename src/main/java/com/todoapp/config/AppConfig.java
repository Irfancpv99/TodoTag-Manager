package com.todoapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class AppConfig {

    /* ---------- constants for property key ---------- */
    private static final String KEY_DB_TYPE        = "database.type";
    private static final String KEY_MONGO_HOST     = "mongodb.host";
    private static final String KEY_MONGO_PORT     = "mongodb.port";
    private static final String KEY_MONGO_DATABASE = "mongodb.database";
    private static final String KEY_MYSQL_URL      = "mysql.url";
    private static final String KEY_MYSQL_USER     = "mysql.username";
    private static final String KEY_MYSQL_PASS     = "mysql.password";

    /* ---------- default values ---------- */
    private static final String DEFAULT_DB_TYPE        = "MONGODB";
    private static final String DEFAULT_MONGO_HOST     = "localhost";
    private static final String DEFAULT_MONGO_PORT     = "27017";
    private static final String DEFAULT_MONGO_DATABASE = "todoapp";

    private final Properties properties;

    public AppConfig() {
        properties = new Properties();
        loadProperties();
    }

    public AppConfig(Properties customProperties) {
        this.properties = new Properties();
        if (customProperties != null) {
            this.properties.putAll(customProperties);
        } else {
            loadProperties();
        }
    }

    /* ---------- loading ---------- */

    private void loadProperties() {
        InputStream in = Thread.currentThread()
                                .getContextClassLoader()
                                .getResourceAsStream("application.properties");

        if (in == null) {
            setDefaultProperties();
            return;
        }

        try (in) {
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setDefaultProperties() {
        properties.setProperty(KEY_DB_TYPE,        DEFAULT_DB_TYPE);
        properties.setProperty(KEY_MONGO_HOST,     DEFAULT_MONGO_HOST);
        properties.setProperty(KEY_MONGO_PORT,     DEFAULT_MONGO_PORT);
        properties.setProperty(KEY_MONGO_DATABASE, DEFAULT_MONGO_DATABASE);
        properties.setProperty(KEY_MYSQL_URL,
                "jdbc:mysql://localhost:3306/todoapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        properties.setProperty(KEY_MYSQL_USER, "todouser");
        properties.setProperty(KEY_MYSQL_PASS, "todopassword");
    }

    /* ---------- public helpers ---------- */
    public DatabaseType getDatabaseType() {
        String type = properties.getProperty(KEY_DB_TYPE, DEFAULT_DB_TYPE);
        return DatabaseType.valueOf(type.toUpperCase());
    }

    public void setDatabaseType(DatabaseType type) {
        properties.setProperty(KEY_DB_TYPE, type.name());
    }

    public String getMongoDbHost() {
        return properties.getProperty(KEY_MONGO_HOST, DEFAULT_MONGO_HOST);
    }

    public int getMongoDbPort() {
        return Integer.parseInt(properties.getProperty(KEY_MONGO_PORT, DEFAULT_MONGO_PORT));
    }

    public String getMongoDbDatabase() {
        return properties.getProperty(KEY_MONGO_DATABASE, DEFAULT_MONGO_DATABASE);
    }

    public String getMySqlUrl()      { return properties.getProperty(KEY_MYSQL_URL);  }
    public String getMySqlUsername() { return properties.getProperty(KEY_MYSQL_USER); }
    public String getMySqlPassword() { return properties.getProperty(KEY_MYSQL_PASS); }

  
    Properties getProperties() {
        return properties;
    }
}