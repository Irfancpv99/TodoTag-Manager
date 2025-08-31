package com.todoapp.repository.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.repository.TodoRepository;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;


public class MongoTodoRepositoryTest implements TodoRepository {
    
    private static final Logger LOGGER = Logger.getLogger(MongoTodoRepositoryTest.class.getName());
    private static final String COLLECTION_NAME = "todos";
    
    private final MongoCollection<Document> collection;
    private final MongoTagRepository tagRepository;
    private Long nextId = 1L;

    public MongoTodoRepositoryTest(MongoClient mongoClient, String databaseName, MongoTagRepository tagRepository) {
        this.tagRepository = tagRepository;
        
        if (mongoClient != null && databaseName != null) {
            try {
                MongoDatabase database = mongoClient.getDatabase(databaseName);
                this.collection = database.getCollection(COLLECTION_NAME);
                initializeNextId();
                LOGGER.info("MongoDB TodoRepository initialized for database: " + databaseName);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to initialize MongoDB TodoRepository", e);
                throw new RuntimeException("Failed to initialize MongoDB connection", e);
            }
        } else {
            this.collection = null;
            LOGGER.warning("MongoDB TodoRepository initialized with null client - testing mode");
        }
    }

    private void initializeNextId() {
        if (!isConnectionAvailable()) return;
        
        try {
            Document lastDoc = collection.find()
                .sort(new Document("_id", -1))
                .first();
            if (lastDoc != null) {
                nextId = lastDoc.getLong("_id") + 1;
            }
            LOGGER.fine("Next ID initialized to: " + nextId);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to initialize next ID, using default", e);
        }
    }

    @Override
    public List<Todo> findAll() {
        if (!isConnectionAvailable()) return new ArrayList<>();
        
        List<Todo> todos = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                todos.add(documentToTodo(doc));
            }
            LOGGER.fine("Found " + todos.size() + " todos");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find all todos", e);
            throw new RuntimeException("Failed to retrieve todos from database", e);
        }
        return todos;
    }

    @Override
    public Optional<Todo> findById(Long id) {
        if (!isConnectionAvailable() || id == null) return Optional.empty();
        
        try {
            Document doc = collection.find(eq("_id", id)).first();
            Optional<Todo> result = doc != null ? Optional.of(documentToTodo(doc)) : Optional.empty();
            LOGGER.fine("Find by ID " + id + ": " + (result.isPresent() ? "found" : "not found"));
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find todo by ID: " + id, e);
            throw new RuntimeException("Failed to retrieve todo from database", e);
        }
    }

    @Override
    public Todo save(Todo todo) {
        validateTodo(todo);
        
        if (!isConnectionAvailable()) {
           if (todo.getId() == null) {
                todo.setId(nextId++);
            }
            return todo;
        }
        
        try {
            if (todo.getId() == null) {
                todo.setId(nextId++);
            }
            
            Document doc = todoToDocument(todo);
            collection.replaceOne(eq("_id", todo.getId()), doc, 
                new com.mongodb.client.model.ReplaceOptions().upsert(true));
            
            LOGGER.fine("Saved todo with ID: " + todo.getId());
            return todo;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save todo: " + todo, e);
            throw new RuntimeException("Failed to save todo to database", e);
        }
    }

    @Override
    public void delete(Todo todo) {
        if (todo != null && todo.getId() != null) {
            deleteById(todo.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        if (!isConnectionAvailable() || id == null) return;
        
        try {
            long deletedCount = collection.deleteOne(eq("_id", id)).getDeletedCount();
            LOGGER.fine("Deleted todo ID " + id + ": " + (deletedCount > 0 ? "success" : "not found"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to delete todo by ID: " + id, e);
            throw new RuntimeException("Failed to delete todo from database", e);
        }
    }

    @Override
    public List<Todo> findByDone(boolean done) {
        if (!isConnectionAvailable()) return new ArrayList<>();
        
        List<Todo> todos = new ArrayList<>();
        try {
            for (Document doc : collection.find(eq("done", done))) {
                todos.add(documentToTodo(doc));
            }
            LOGGER.fine("Found " + todos.size() + " todos with done=" + done);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find todos by done status: " + done, e);
            throw new RuntimeException("Failed to search todos in database", e);
        }
        return todos;
    }

    @Override
    public List<Todo> findByDescriptionContaining(String keyword) {
        if (!isConnectionAvailable() || keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Todo> todos = new ArrayList<>();
        try {
            String escapedKeyword = escapeRegexSpecialChars(keyword.trim());
            for (Document doc : collection.find(regex("description", ".*" + escapedKeyword + ".*", "i"))) {
                todos.add(documentToTodo(doc));
            }
            LOGGER.fine("Found " + todos.size() + " todos containing: " + keyword);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find todos by description containing: " + keyword, e);
            throw new RuntimeException("Failed to search todos in database", e);
        }
        return todos;
    }

    // Private helper methods
    
    private boolean isConnectionAvailable() {
        return collection != null;
    }
    
    private void validateTodo(Todo todo) {
        if (todo == null) {
            throw new IllegalArgumentException("Todo cannot be null");
        }
        if (todo.getDescription() == null || todo.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Todo description cannot be null or empty");
        }
    }
    
    private String escapeRegexSpecialChars(String input) {
        return input.replaceAll("([\\[\\]{}()*+?.\\\\^$|])", "\\\\$1");
    }

    private Document todoToDocument(Todo todo) {
        Document doc = new Document()
            .append("_id", todo.getId())
            .append("description", todo.getDescription().trim())
            .append("done", todo.isDone());

        // Store tag IDs
        List<Long> tagIds = new ArrayList<>();
        for (Tag tag : todo.getTags()) {
            if (tag.getId() != null) {
                tagIds.add(tag.getId());
            }
        }
        doc.append("tagIds", tagIds);

        return doc;
    }

    private Todo documentToTodo(Document doc) {
        try {
            String description = doc.getString("description");
            if (description == null) {
                throw new IllegalStateException("Todo description cannot be null in database");
            }
            
            Todo todo = new Todo(description);
            todo.setId(doc.getLong("_id"));
            todo.setDone(doc.getBoolean("done", false));

            if (tagRepository != null) {
                Set<Tag> tags = new HashSet<>();
                List<Long> tagIds = doc.getList("tagIds", Long.class);
                if (tagIds != null) {
                    for (Long tagId : tagIds) {
                        try {
                            tagRepository.findById(tagId).ifPresent(tags::add);
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Failed to load tag with ID: " + tagId, e);
                            // Continue loading other tags
                        }
                    }
                }
                todo.setTags(tags);
            }

            return todo;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to convert document to Todo: " + doc, e);
            throw new RuntimeException("Failed to parse todo from database", e);
        }
    }
}