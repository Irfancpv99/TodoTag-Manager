package com.todoapp.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppConfigTest {

    @Test
    void defaultConstructor_loadsFromFile() {
        AppConfig cfg = new AppConfig();
        assertThat(cfg.getDatabaseType()).isEqualTo(DatabaseType.MONGODB);
        assertThat(cfg.getMongoDbHost()).isEqualTo("localhost");
        assertThat(cfg.getMongoDbPort()).isEqualTo(27017);
        assertThat(cfg.getMongoDbDatabase()).isEqualTo("todoapp");
        assertThat(cfg.getMySqlUrl()).contains("3307");
        assertThat(cfg.getMySqlUsername()).isEqualTo("todouser");
        assertThat(cfg.getMySqlPassword()).isEqualTo("todopassword");
    }

    @Test
    void customPropertiesConstructor_usesGivenProps() {
        Properties p = new Properties();
        p.setProperty("database.type", "mysql");
        p.setProperty("mysql.username", "user");
        
        AppConfig cfg = new AppConfig(p);
        assertThat(cfg.getDatabaseType()).isEqualTo(DatabaseType.MYSQL);
        assertThat(cfg.getMySqlUsername()).isEqualTo("user");
    }

    @Test
    void nullProperties_fallsBackToFile() {
        AppConfig cfg = new AppConfig((Properties) null);
        assertThat(cfg.getMySqlUrl()).contains("3307");
    }

    @Test
    void setDatabaseType_changesType() {
        AppConfig cfg = new AppConfig();
        cfg.setDatabaseType(DatabaseType.MYSQL);
        assertThat(cfg.getDatabaseType()).isEqualTo(DatabaseType.MYSQL);
    }

    @Test
    void loadException_wrapsInUnchecked() {
        ClassLoader cl = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public InputStream getResourceAsStream(String name) {
                if ("application.properties".equals(name)) {
                    return new InputStream() {
                        @Override public int read() throws IOException { throw new IOException("fail"); }
                    };
                }
                return super.getResourceAsStream(name);
            }
        };
        Thread.currentThread().setContextClassLoader(cl);

        assertThatThrownBy(AppConfig::new)
            .isInstanceOf(UncheckedIOException.class)
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void missingResource_usesDefaults() {
        ClassLoader cl = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public InputStream getResourceAsStream(String name) {
                return null; 
                }
        };
        Thread.currentThread().setContextClassLoader(cl);
        
        AppConfig cfg = new AppConfig();
        assertThat(cfg.getDatabaseType()).isEqualTo(DatabaseType.MONGODB);
        assertThat(cfg.getMongoDbHost()).isEqualTo("localhost");
        assertThat(cfg.getMongoDbPort()).isEqualTo(27017);
        assertThat(cfg.getMySqlUrl()).contains("3306"); // default port
    }

    @Test
    void closeException_isIgnored() {
        ClassLoader cl = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public InputStream getResourceAsStream(String name) {
                if ("application.properties".equals(name)) {
                    return new InputStream() {
                        @Override public int read() { return -1; }
                        @Override public void close() throws IOException { throw new IOException("close fail"); }
                    };
                }
                return super.getResourceAsStream(name);
            }
        };
        Thread.currentThread().setContextClassLoader(cl);

        AppConfig cfg = new AppConfig();
        assertThat(cfg.getDatabaseType()).isEqualTo(DatabaseType.MONGODB);
    }

    @Test
    void inputStream_isAlwaysClosed() {
        class TrackingStream extends InputStream {
            boolean closed = false;
            @Override public int read() { return -1; }
            @Override public void close() { closed = true; }
        }
        
        TrackingStream stream = new TrackingStream();
        ClassLoader cl = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public InputStream getResourceAsStream(String name) {
                if ("application.properties".equals(name)) {
                    return stream;
                }
                return super.getResourceAsStream(name);
            }
        };
        Thread.currentThread().setContextClassLoader(cl);

        new AppConfig();
        assertThat(stream.closed).isTrue();
    }

    @Test
    void getProperties_returnsSameInstance() {
        Properties p = new Properties();
        p.setProperty("database.type", "mysql");
        
        AppConfig cfg = new AppConfig(p);
        assertThat(cfg.getProperties()).isSameAs(cfg.getProperties());
        assertThat(cfg.getProperties()).containsEntry("database.type", "mysql");
    }

    @AfterEach
    void resetClassLoader() {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    }
}