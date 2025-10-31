package com.todoapp.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppConfigTest {

//     helpers 

    private Properties minimalProps(String dbType) {
        Properties p = new Properties();
        p.setProperty("database.type", dbType);
        p.setProperty("mongodb.host", "mh");
        p.setProperty("mongodb.port", "1234");
        p.setProperty("mongodb.database", "mdb");
        p.setProperty("mysql.url", "jdbc:mysql://localhost:3306/todoapp");
        p.setProperty("mysql.username", "u");
        p.setProperty("mysql.password", "p");
        return p;
    }

    @Test
    void defaultConstructor_usesRealFile() {
    	AppConfig cut = new AppConfig();
        assertThat(cut.getDatabaseType()).isEqualTo(DatabaseType.MONGODB);
        assertThat(cut.getMongoDbHost()).isEqualTo("localhost");
        assertThat(cut.getMongoDbPort()).isEqualTo(27017);
        assertThat(cut.getMongoDbDatabase()).isEqualTo("todoapp");
        assertThat(cut.getMySqlUrl())
                .isEqualTo("jdbc:mysql://localhost:3307/todoapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        assertThat(cut.getMySqlUsername()).isEqualTo("todouser");
        assertThat(cut.getMySqlPassword()).isEqualTo("todopassword");
    }

    @Test
    void customPropertiesConstructor_usesGivenProps() {
        Properties p = minimalProps("mysql");
        AppConfig cut = new AppConfig(p);
        assertThat(cut.getDatabaseType()).isEqualTo(DatabaseType.MYSQL);
        assertThat(cut.getMySqlUsername()).isEqualTo("u");
    }

    @Test
    void setDatabaseType_mutatesInternalState() {
        AppConfig cut = new AppConfig();
        cut.setDatabaseType(DatabaseType.MYSQL);
        assertThat(cut.getDatabaseType()).isEqualTo(DatabaseType.MYSQL);
    }

    @Test
    void constructor_withNullProps_fallsBackToFile() {
        AppConfig cut = new AppConfig((Properties) null);
        assertThat(cut.getMySqlUrl()).contains("3307");
    }

    @Test
    void loadProperties_ioException_wrapsInUnchecked() {
    	ClassLoader brokenCl = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public InputStream getResourceAsStream(String name) {
                if ("application.properties".equals(name)) {
                    return new InputStream() {
                        @Override
                        public int read() throws IOException {
                            throw new IOException("boom");
                        }
                    };
                }
                return super.getResourceAsStream(name);
            }
        };

        Thread.currentThread().setContextClassLoader(brokenCl);
        assertThatThrownBy(AppConfig::new)
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseInstanceOf(IOException.class);
    }
    
    @Test
    void whenResourceIsMissing_defaultsAreLoaded() {
        ClassLoader noResourceCl = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public InputStream getResourceAsStream(String name) {
                return null;    
                }
        };

        Thread.currentThread().setContextClassLoader(noResourceCl);
        try {
            AppConfig cfg = new AppConfig();          // must use defaults
            assertThat(cfg.getDatabaseType()).isEqualTo(DatabaseType.MONGODB);
            assertThat(cfg.getMongoDbHost()).isEqualTo("localhost");
            assertThat(cfg.getMongoDbPort()).isEqualTo(27017);
            assertThat(cfg.getMongoDbDatabase()).isEqualTo("todoapp");
            assertThat(cfg.getMySqlUrl()).contains("3306");   // default MySQL port
            assertThat(cfg.getMySqlUsername()).isEqualTo("todouser");
            assertThat(cfg.getMySqlPassword()).isEqualTo("todopassword");
        } finally {
            resetContextLoader();   
            }
    }

    @Test
    void getProperties_exposesInternalPropertiesInstance() {
        Properties p = minimalProps("mysql");
        AppConfig cfg = new AppConfig(p);
       assertThat(cfg.getProperties()).isSameAs(cfg.getProperties()); // same instance
        assertThat(cfg.getProperties()).containsEntry("database.type", "mysql");
    }

    @AfterEach
    void resetContextLoader() {
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    }
}