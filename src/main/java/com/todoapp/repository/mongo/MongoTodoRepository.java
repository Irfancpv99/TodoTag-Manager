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

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

/**
 * MongoDB implementation of TodoRepository
 * 
 * GREEN PHASE: Minimal implementation to make TodoRepository tests pass
 * Following TDD: implement just enough functionality to satisfy tests
 */
public class MongoTodoRepository implements TodoRepository {
    
    private final MongoCollection<Document> collection;
    private final MongoTagRepository tagRepository;
    private Long nextId = 1L;

    public MongoTodoRepository(MongoClient mongoClient, String databaseName, MongoTagRepository tagRepository) {
        this.tagRepository = tagRepository;
        if (mongoClient != null) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            this.collection = database.getCollection("todos");
            initializeNextId();
        } else {
            // For testing purposes when client is null
            this.collection = null;
        }
    }

    private void initializeNextId() {
        if (collection == null) return;
        
        // Find the highest ID in existing documents
        Document lastDoc = collection.find()
            .sort(new Document("_id", -1))
            .first();
        if (lastDoc != null) {
            nextId = lastDoc.getLong("_id") + 1;
        }
    }

    @Override
    public List<Todo> findAll() {
        if (collection == null) return new ArrayList<>();
        
        List<Todo> todos = new ArrayList<>();
        for (Document doc : collection.find()) {
            todos.add(documentToTodo(doc));
        }
        return todos;
    }

    @Override
    public Optional<Todo> findById(Long id) {
        if (collection == null) return Optional.empty();
        
        Document doc = collection.find(eq("_id", id)).first();
        return doc != null ? Optional.of(documentToTodo(doc)) : Optional.empty();
    }

    @Override
    public Todo save(Todo todo) {
        if (collection == null) {
            // For testing - just set ID and return
            if (todo.getId() == null) {
                todo.setId(nextId++);
            }
            return todo;
        }
        
        if (todo.getId() == null) {
            todo.setId(nextId++);
        }
        
        Document doc = todoToDocument(todo);
        collection.replaceOne(eq("_id", todo.getId()), doc, 
            new com.mongodb.client.model.ReplaceOptions().upsert(true));
        
        return todo;
    }

    @Override
    public void delete(Todo todo) {
        if (todo.getId() != null) {
            deleteById(todo.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        if (collection == null) return;
        
        collection.deleteOne(eq("_id", id));
    }

    @Override
    public List<Todo> findByDone(boolean done) {
        if (collection == null) return new ArrayList<>();
        
        List<Todo> todos = new ArrayList<>();
        for (Document doc : collection.find(eq("done", done))) {
            todos.add(documentToTodo(doc));
        }
        return todos;
    }

    @Override
    public List<Todo> findByDescriptionContaining(String keyword) {
        if (collection == null) return new ArrayList<>();
        
        List<Todo> todos = new ArrayList<>();
        for (Document doc : collection.find(regex("description", ".*" + keyword + ".*", "i"))) {
            todos.add(documentToTodo(doc));
        }
        return todos;
    }

    private Document todoToDocument(Todo todo) {
        Document doc = new Document()
            .append("_id", todo.getId())
            .append("description", todo.getDescription())
            .append("done", todo.isDone());

        // Store tag IDs
        List<Long> tagIds = new ArrayList<>();
        for (Tag tag : todo.getTags()) {
            tagIds.add(tag.getId());
        }
        doc.append("tagIds", tagIds);

        return doc;
    }

    private Todo documentToTodo(Document doc) {
        Todo todo = new Todo(doc.getString("description"));
        todo.setId(doc.getLong("_id"));
        todo.setDone(doc.getBoolean("done", false));

        // Load tags if tagRepository is available
        if (tagRepository != null) {
            Set<Tag> tags = new HashSet<>();
            List<Long> tagIds = doc.getList("tagIds", Long.class);
            if (tagIds != null) {
                for (Long tagId : tagIds) {
                    tagRepository.findById(tagId).ifPresent(tags::add);
                }
            }
            todo.setTags(tags);
        }

        return todo;
    }
}