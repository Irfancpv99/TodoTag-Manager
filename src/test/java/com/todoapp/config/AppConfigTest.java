package com.todoapp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    private AppConfig config;

    @BeforeEach
    void setUp() {
        config = AppConfig.getInstance();
    }

    @Test
    void shouldBeSingleton() {
        AppConfig instance1 = AppConfig.getInstance();
        AppConfig instance2 = AppConfig.getInstance();
        
        assertSame(instance1, instance2);
    }

    @Test
    void shouldHaveDefaultDatabaseType() {
        DatabaseType dbType = config.getDatabaseType();
        
        assertNotNull(dbType);
        // Default should be MongoDB
        assertEquals(DatabaseType.MONGODB, dbType);
    }

    @Test
    void shouldAllowChangingDatabaseType() {
        config.setDatabaseType(DatabaseType.MYSQL);
        assertEquals(DatabaseType.MYSQL, config.getDatabaseType());
        
        config.setDatabaseType(DatabaseType.MONGODB);
        assertEquals(DatabaseType.MONGODB, config.getDatabaseType());
    }

    @Test
    void shouldProvideMongoDbConfiguration() {
        assertEquals("localhost", config.getMongoDbHost());
        assertEquals(27017, config.getMongoDbPort());
        assertEquals("todoapp", config.getMongoDbDatabase());
    }

    @Test
    void shouldProvideMySqlConfiguration() {
        assertNotNull(config.getMySqlUrl());
        assertNotNull(config.getMySqlUsername());
        assertNotNull(config.getMySqlPassword());
        
        assertTrue(config.getMySqlUrl().contains("jdbc:mysql"));
        assertEquals("todouser", config.getMySqlUsername());
        assertEquals("todopassword", config.getMySqlPassword());
    }
}