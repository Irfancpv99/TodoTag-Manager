package com.todoapp.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @AfterEach
    void resetSingleton() throws Exception {
        Field instance = AppConfig.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void shouldReturnSingletonInstance() {
        AppConfig instance1 = AppConfig.getInstance();
        AppConfig instance2 = AppConfig.getInstance();
        
        assertSame(instance1, instance2);
    }

    @Test
    void shouldLoadPropertiesFromExistingFile() {
        AppConfig config = AppConfig.getInstance();
        
        assertEquals(DatabaseType.MONGODB, config.getDatabaseType());
        assertEquals("localhost", config.getMongoDbHost());
        assertEquals(27017, config.getMongoDbPort());
        assertEquals("todoapp", config.getMongoDbDatabase());
        assertEquals("jdbc:mysql://localhost:3307/todoapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", config.getMySqlUrl());
        assertEquals("todouser", config.getMySqlUsername());
        assertEquals("todopassword", config.getMySqlPassword());
    }

    @Test
    void shouldSetDatabaseType() {
        AppConfig config = AppConfig.getInstance();
        
        config.setDatabaseType(DatabaseType.MYSQL);
        
        assertEquals(DatabaseType.MYSQL, config.getDatabaseType());
    }

    @Test
    void shouldLoadDefaultPropertiesWhenFileNotFound() throws Exception {
        URL resource = getClass().getClassLoader().getResource("application.properties");
        assertNotNull(resource, "application.properties must exist");
        
        File originalFile = new File(resource.toURI());
        File backupFile = new File(originalFile.getParent(), "application.properties.backup." + System.currentTimeMillis());
        
        assertTrue(originalFile.renameTo(backupFile), "Failed to rename file");
        
        try {
            resetSingleton();
            AppConfig config = AppConfig.getInstance();
            
            assertEquals(DatabaseType.MONGODB, config.getDatabaseType());
            assertEquals("localhost", config.getMongoDbHost());
            assertEquals(27017, config.getMongoDbPort());
            assertEquals("todoapp", config.getMongoDbDatabase());
            assertEquals("jdbc:mysql://localhost:3306/todoapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", config.getMySqlUrl());
            assertEquals("todouser", config.getMySqlUsername());
            assertEquals("todopassword", config.getMySqlPassword());
        } finally {
            if (backupFile.exists()) {
                backupFile.renameTo(originalFile);
            }
        }
    }

    @Test
    void shouldCallSetDefaultPropertiesCorrectly() throws Exception {
        AppConfig config = new AppConfig();
        
        Method setDefaultProps = AppConfig.class.getDeclaredMethod("setDefaultProperties");
        setDefaultProps.setAccessible(true);
        
        Field propsField = AppConfig.class.getDeclaredField("properties");
        propsField.setAccessible(true);
        java.util.Properties props = (java.util.Properties) propsField.get(config);
        
        props.clear();
        setDefaultProps.invoke(config);

        assertEquals("MONGODB", props.getProperty("database.type"));
        assertEquals("localhost", props.getProperty("mongodb.host"));
        assertEquals("27017", props.getProperty("mongodb.port"));
        assertEquals("todoapp", props.getProperty("mongodb.database"));
        assertEquals("jdbc:mysql://localhost:3306/todoapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", 
                     props.getProperty("mysql.url"));
        assertEquals("todouser", props.getProperty("mysql.username"));
        assertEquals("todopassword", props.getProperty("mysql.password"));
    }
}