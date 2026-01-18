package com.todoapp.repository.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.todoapp.model.Tag;
import com.todoapp.repository.TagRepository;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

public class MongoTagRepository implements TagRepository {
    
    private final MongoCollection<Document> collection;
    private final MongoClient mongoClient;
    private Long nextId = 1L;

    public MongoTagRepository(String connectionString, String databaseName) {
        this.mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.collection = database.getCollection("tags");
        initializeNextId();
    }
    
    // Package-private constructor for testing
    MongoTagRepository(MongoCollection<Document> collection, MongoClient mongoClient) {
        this.collection = collection;
        this.mongoClient = mongoClient;
        this.nextId = 1L;
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
    public List<Tag> findAll() {
        List<Tag> tags = new ArrayList<>();
        for (Document doc : collection.find()) {
            tags.add(documentToTag(doc));
        }
        return tags;
    }

    @Override
    public Optional<Tag> findById(Long id) {
        Document doc = collection.find(eq("_id", id)).first();
        return doc != null ? Optional.of(documentToTag(doc)) : Optional.empty();
    }

    @Override
    public Tag save(Tag tag) {
        if (tag.getId() == null) {
            tag.setId(nextId++);
        }
        
        Document doc = tagToDocument(tag);
        collection.replaceOne(eq("_id", tag.getId()), doc, 
            new com.mongodb.client.model.ReplaceOptions().upsert(true));
        
        return tag;
    }

    @Override
    public void delete(Tag tag) {
        if (tag.getId() != null) {
            deleteById(tag.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        collection.deleteOne(eq("_id", id));
    }

    @Override
    public Optional<Tag> findByName(String name) {
        Document doc = collection.find(eq("name", name)).first();
        return doc != null ? Optional.of(documentToTag(doc)) : Optional.empty();
    }

    @Override
    public List<Tag> findByNameContaining(String keyword) {
        List<Tag> tags = new ArrayList<>();
        for (Document doc : collection.find(regex("name", ".*" + keyword + ".*", "i"))) {
            tags.add(documentToTag(doc));
        }
        return tags;
    }

    // Helper method for testing
    public void deleteAll() {
        collection.deleteMany(new Document());
        nextId = 1L;
    }

    private Document tagToDocument(Tag tag) {
        return new Document()
            .append("_id", tag.getId())
            .append("name", tag.getName());
    }

    private Tag documentToTag(Document doc) {
        Tag tag = new Tag(doc.getString("name"));
        tag.setId(doc.getLong("_id"));
        return tag;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}