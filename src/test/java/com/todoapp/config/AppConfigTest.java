package com.todoapp.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @BeforeEach
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
    void shouldLoadDefaultProperties() {
        AppConfig config = AppConfig.getInstance();
        
        assertEquals(DatabaseType.MONGODB, config.getDatabaseType());
        assertEquals("localhost", config.getMongoDbHost());
        assertEquals(27017, config.getMongoDbPort());
        assertEquals("todoapp", config.getMongoDbDatabase());
        assertNotNull(config.getMySqlUrl());
        assertNotNull(config.getMySqlUsername());
        assertNotNull(config.getMySqlPassword());
    }

    @Test
    void shouldSetDatabaseType() {
        AppConfig config = AppConfig.getInstance();
        
        config.setDatabaseType(DatabaseType.MYSQL);
        
        assertEquals(DatabaseType.MYSQL, config.getDatabaseType());
    }

    @Test
    void shouldLoadPropertiesFromFile() throws Exception {
        // Create a temporary application.properties file
        URL resourceUrl = getClass().getClassLoader().getResource(".");
        if (resourceUrl != null) {
            File propsFile = new File(resourceUrl.getFile(), "application.properties");
            try (FileWriter writer = new FileWriter(propsFile)) {
                writer.write("database.type=MYSQL\n");
                writer.write("mongodb.host=testhost\n");
                writer.write("mongodb.port=27018\n");
                writer.write("mongodb.database=testdb\n");
                writer.write("mysql.url=jdbc:mysql://test:3306/test\n");
                writer.write("mysql.username=testuser\n");
                writer.write("mysql.password=testpass\n");
            }

            try {
                AppConfig config = AppConfig.getInstance();

                assertEquals(DatabaseType.MYSQL, config.getDatabaseType());
                assertEquals("testhost", config.getMongoDbHost());
                assertEquals(27018, config.getMongoDbPort());
                assertEquals("testdb", config.getMongoDbDatabase());
                assertEquals("jdbc:mysql://test:3306/test", config.getMySqlUrl());
                assertEquals("testuser", config.getMySqlUsername());
                assertEquals("testpass", config.getMySqlPassword());
            } finally {
                propsFile.delete();
            }
        }
    }
}