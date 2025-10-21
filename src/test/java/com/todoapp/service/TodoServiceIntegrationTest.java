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
    
    @Test
    @Order(7)
    void shouldSearchTodosWithMongoDB() {

    	AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MONGODB);
        setMongoDBProperties(config);
        todoService = new TodoService();

        todoService.createTodo("Buy groceries");
        todoService.createTodo("Buy tickets");
        todoService.createTodo("Clean house");

        List<Todo> results = todoService.searchTodos("Buy");

        assertEquals(2, results.size(), "Should find 2 todos containing 'Buy'");
        assertTrue(results.stream().allMatch(t -> t.getDescription().contains("Buy")),
                "All results should contain 'Buy'");
    }

    @Test
    @Order(8)
    void shouldSearchTodosWithMySQL() {

    	AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MYSQL);
        setMySQLProperties(config);
        todoService = new TodoService();

        todoService.createTodo("Buy groceries");
        todoService.createTodo("Buy tickets");
        todoService.createTodo("Clean house");

        List<Todo> results = todoService.searchTodos("Buy");

        assertEquals(2, results.size(), "Should find 2 todos containing 'Buy'");
        assertTrue(results.stream().allMatch(t -> t.getDescription().contains("Buy")),
                "All results should contain 'Buy'");
    }
    
    @Test
    @Order(9)
    void shouldFilterCompletedAndIncompleteTodosWithMongoDB() {

        AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MONGODB);
        setMongoDBProperties(config);
        todoService = new TodoService();

        Todo todo1 = todoService.createTodo("Task 1");
        Todo todo2 = todoService.createTodo("Task 2");
        
        todoService.markTodoComplete(todo1.getId());
        
        Optional<Todo> refetchedTodo1 = todoService.getTodoById(todo1.getId());
        Optional<Todo> refetchedTodo2 = todoService.getTodoById(todo2.getId());
        
        assertTrue(refetchedTodo1.isPresent(), "Todo 1 should exist");
        assertTrue(refetchedTodo2.isPresent(), "Todo 2 should exist");
        assertTrue(refetchedTodo1.get().isDone(), "Todo 1 should be done");
        assertFalse(refetchedTodo2.get().isDone(), "Todo 2 should be incomplete");

        List<Todo> completed = todoService.getCompletedTodos();
        List<Todo> incomplete = todoService.getIncompleteTodos();

        assertEquals(1, completed.size(), "Should have 1 completed todo");
        assertEquals(1, incomplete.size(), "Should have 1 incomplete todo");
        assertTrue(completed.get(0).isDone(), "Completed list should contain done todos");
        assertFalse(incomplete.get(0).isDone(), "Incomplete list should contain not-done todos");
    }
  
    @Test
    @Order(10)
    void shouldFilterCompletedAndIncompleteTodosWithMySQL() {
    	AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MYSQL);
        setMySQLProperties(config);
        todoService = new TodoService();

        Todo todo1 = todoService.createTodo("Task 1");
        Todo todo2 = todoService.createTodo("Task 2");
        
        todoService.markTodoComplete(todo1.getId());
        
        Optional<Todo> refetchedTodo1 = todoService.getTodoById(todo1.getId());
        Optional<Todo> refetchedTodo2 = todoService.getTodoById(todo2.getId());
        
        assertTrue(refetchedTodo1.isPresent(), "Todo 1 should exist");
        assertTrue(refetchedTodo2.isPresent(), "Todo 2 should exist");
        assertTrue(refetchedTodo1.get().isDone(), "Todo 1 should be done");
        assertFalse(refetchedTodo2.get().isDone(), "Todo 2 should be incomplete");

        // When: We filter
        List<Todo> completed = todoService.getCompletedTodos();
        List<Todo> incomplete = todoService.getIncompleteTodos();

        assertEquals(1, completed.size(), "Should have 1 completed todo");
        assertEquals(1, incomplete.size(), "Should have 1 incomplete todo");
        assertTrue(completed.get(0).isDone(), "Completed list should have done todos");
        assertFalse(incomplete.get(0).isDone(), "Incomplete list should have not-done todos");
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