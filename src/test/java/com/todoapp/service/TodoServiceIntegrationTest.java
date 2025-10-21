package com.todoapp.service;

import com.todoapp.config.AppConfig;
import com.todoapp.config.DatabaseType;
import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoServiceIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private TodoService todoService;

    @BeforeEach
    void setUp() throws Exception {
        resetSingletons();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (todoService != null) {
            cleanupDatabase();
        }
        resetSingletons();
    }

    private void cleanupDatabase() {
        try {
            List<Todo> allTodos = todoService.getAllTodos();
            for (Todo todo : allTodos) {
                todoService.deleteTodo(todo.getId());
            }
            
            List<Tag> allTags = todoService.getAllTags();
            for (Tag tag : allTags) {
                todoService.deleteTag(tag.getId());
            }
        } catch (Exception e) {
       }
    }

    private void resetSingletons() throws Exception {
        Field acInstance = AppConfig.class.getDeclaredField("instance");
        acInstance.setAccessible(true);
        acInstance.set(null, null);

        try {
            Field rfInstance = Class.forName("com.todoapp.repository.RepositoryFactory")
                    .getDeclaredField("instance");
            rfInstance.setAccessible(true);
            rfInstance.set(null, null);
        } catch (Exception e) {
          
        }

        try {
            Field dmInstance = Class.forName("com.todoapp.config.DatabaseManager")
                    .getDeclaredField("instance");
            dmInstance.setAccessible(true);
            dmInstance.set(null, null);
        } catch (Exception e) {
          }
    }

    
    // Basic CRUD - Create and Retrieve (MongoDB)
   
    @Test
    @Order(1)
    void shouldCreateAndRetrieveTodoWithMongoDB() {

    	
    	 AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MONGODB);
        setMongoDBProperties(config);
        todoService = new TodoService();

        Todo created = todoService.createTodo("Test MongoDB Task");

        assertNotNull(created, "Created todo should not be null");
        assertNotNull(created.getId(), "Created todo should have an ID");
        assertEquals("Test MongoDB Task", created.getDescription(), "Description should match");
        assertFalse(created.isDone(), "New todo should not be done");

        Optional<Todo> retrieved = todoService.getTodoById(created.getId());
        assertTrue(retrieved.isPresent(), "Todo should be retrievable by ID");
        assertEquals("Test MongoDB Task", retrieved.get().getDescription(), "Retrieved description should match");
    }

    //     Basic CRUD - Create and Retrieve (MySQL)
    
    @Test
    @Order(2)
    void shouldCreateAndRetrieveTodoWithMySQL() {
    	AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MYSQL);
        setMySQLProperties(config);
        todoService = new TodoService();

        Todo created = todoService.createTodo("Test MySQL Task");

        assertNotNull(created, "Created todo should not be null");
        assertNotNull(created.getId(), "Created todo should have an ID");
        assertEquals("Test MySQL Task", created.getDescription(), "Description should match");
        assertFalse(created.isDone(), "New todo should not be done");

        Optional<Todo> retrieved = todoService.getTodoById(created.getId());
        assertTrue(retrieved.isPresent(), "Todo should be retrievable by ID");
        assertEquals("Test MySQL Task", retrieved.get().getDescription(), "Retrieved description should match");
    }
    
    @Test
    @Order(3)
    void shouldManageTodoLifecycleWithMongoDB() {
        AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MONGODB);
        setMongoDBProperties(config);
        todoService = new TodoService();
        Todo todo = todoService.createTodo("Task to complete");
        
        assertFalse(todo.isDone(), "Initial todo should not be done");
        Todo completed = todoService.markTodoComplete(todo.getId());
        assertTrue(completed.isDone(), "Todo should be marked as done");

        Todo incomplete = todoService.markTodoIncomplete(todo.getId());
        assertFalse(incomplete.isDone(), "Todo should be marked as not done");

        todoService.deleteTodo(todo.getId());
        Optional<Todo> deleted = todoService.getTodoById(todo.getId());
        assertFalse(deleted.isPresent(), "Deleted todo should not be found");
    }
    
    @Order(4)
    void shouldManageTodoLifecycleWithMySQL() {
    
    	AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MYSQL);
        setMySQLProperties(config);
        todoService = new TodoService();
        Todo todo = todoService.createTodo("Task to complete");

        assertFalse(todo.isDone(), "Initial todo should not be done");

        Todo completed = todoService.markTodoComplete(todo.getId());
        assertTrue(completed.isDone(), "Todo should be marked as done");
        Todo incomplete = todoService.markTodoIncomplete(todo.getId());
        assertFalse(incomplete.isDone(), "Todo should be marked as not done");
        todoService.deleteTodo(todo.getId());
        Optional<Todo> deleted = todoService.getTodoById(todo.getId());
        assertFalse(deleted.isPresent(), "Deleted todo should not be found");
    }
    
    @Test
    @Order(5)
    void shouldManageTagsAndRelationshipsWithMongoDB() {
       
    	AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MONGODB);
        setMongoDBProperties(config);
        todoService = new TodoService();

        Tag tag = todoService.createTag("urgent");
        Todo todo = todoService.createTodo("Important task");
        
        assertNotNull(tag.getId(), "Tag should have an ID");
        assertEquals("urgent", tag.getName(), "Tag name should match");

        Todo tagged = todoService.addTagToTodo(todo.getId(), tag.getId());
        
        assertEquals(1, tagged.getTags().size(), "Todo should have 1 tag");
        assertTrue(tagged.getTags().stream().anyMatch(t -> t.getName().equals("urgent")),
                "Todo should contain 'urgent' tag");

        Todo untagged = todoService.removeTagFromTodo(todo.getId(), tag.getId());
        
        assertEquals(0, untagged.getTags().size(), "Todo should have no tags after removal");
    }
    
    @Test
    @Order(6)
    void shouldManageTagsAndRelationshipsWithMySQL() {
     
    	AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MYSQL);
        setMySQLProperties(config);
        todoService = new TodoService();

        Tag tag = todoService.createTag("work");
        Todo todo = todoService.createTodo("Work task");

        Todo tagged = todoService.addTagToTodo(todo.getId(), tag.getId());
        assertEquals(1, tagged.getTags().size(), "Todo should have 1 tag");
        assertTrue(tagged.getTags().stream().anyMatch(t -> t.getName().equals("work")),
                "Todo should contain 'work' tag");

        Todo untagged = todoService.removeTagFromTodo(todo.getId(), tag.getId());
        assertEquals(0, untagged.getTags().size(), "Todo should have no tags after removal");
    }

    	// Helper Method
    
    private void setMongoDBProperties(AppConfig config) {
        try {
            Field propsField = AppConfig.class.getDeclaredField("properties");
            propsField.setAccessible(true);
            java.util.Properties props = (java.util.Properties) propsField.get(config);
            
            String connectionString = mongoDBContainer.getReplicaSetUrl();
            String[] parts = connectionString.replace("mongodb://", "").split(":");
            String host = parts[0];
            String port = parts[1].split("/")[0];

            props.setProperty("mongodb.host", host);
            props.setProperty("mongodb.port", port);
            props.setProperty("mongodb.database", "testdb_" + System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure MongoDB properties", e);
        }
    }

    private void setMySQLProperties(AppConfig config) {
        try {
            Field propsField = AppConfig.class.getDeclaredField("properties");
            propsField.setAccessible(true);
            java.util.Properties props = (java.util.Properties) propsField.get(config);

            props.setProperty("mysql.url", mySQLContainer.getJdbcUrl());
            props.setProperty("mysql.username", mySQLContainer.getUsername());
            props.setProperty("mysql.password", mySQLContainer.getPassword());
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure MySQL properties", e);
        }
    }
}