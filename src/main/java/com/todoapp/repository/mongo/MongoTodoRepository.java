package com.todoapp.repository.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
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


public class MongoTodoRepository implements TodoRepository {
    
    private final MongoCollection<Document> collection;
    private final MongoClient mongoClient;
    private final MongoTagRepository tagRepository;
    private Long nextId = 1L;

    public MongoTodoRepository(String connectionString, String databaseName, 
                               MongoTagRepository tagRepository) {
        this.mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.collection = database.getCollection("todos");
        this.tagRepository = tagRepository;
        initializeNextId();
    }

    private void initializeNextId() {
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
        List<Todo> todos = new ArrayList<>();
        for (Document doc : collection.find()) {
            todos.add(documentToTodo(doc));
        }
        return todos;
    }

    @Override
    public Optional<Todo> findById(Long id) {
        Document doc = collection.find(eq("_id", id)).first();
        return doc != null ? Optional.of(documentToTodo(doc)) : Optional.empty();
    }

    @Override
    public Todo save(Todo todo) {
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
        collection.deleteOne(eq("_id", id));
    }

    @Override
    public List<Todo> findByDone(boolean done) {
        List<Todo> todos = new ArrayList<>();
        for (Document doc : collection.find(eq("done", done))) {
            todos.add(documentToTodo(doc));
        }
        return todos;
    }

    @Override
    public List<Todo> findByDescriptionContaining(String keyword) {
        List<Todo> todos = new ArrayList<>();
        for (Document doc : collection.find(regex("description", ".*" + keyword + ".*", "i"))) {
            todos.add(documentToTodo(doc));
        }
        return todos;
    }

    // Helper method for testing
    public void deleteAll() {
        collection.deleteMany(new Document());
        nextId = 1L;
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

        // Load tags
        Set<Tag> tags = new HashSet<>();
        List<Long> tagIds = doc.getList("tagIds", Long.class);
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                tagRepository.findById(tagId).ifPresent(tags::add);
            }
        }
        todo.setTags(tags);

        return todo;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}