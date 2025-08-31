package com.todoapp.repository.mongo;

import com.mongodb.client.MongoClient;
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
    private Long nextId = 1L;

    public MongoTagRepository(MongoClient mongoClient, String databaseName) {
        if (mongoClient != null) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            this.collection = database.getCollection("tags");
            initializeNextId();
        } else {
            this.collection = null;
        }
    }

    private void initializeNextId() {
        if (collection == null) return;
        
        Document lastDoc = collection.find()
            .sort(new Document("_id", -1))
            .first();
        if (lastDoc != null) {
            nextId = lastDoc.getLong("_id") + 1;
        }
    }

    @Override
    public List<Tag> findAll() {
        if (collection == null) return new ArrayList<>();
        
        List<Tag> tags = new ArrayList<>();
        for (Document doc : collection.find()) {
            tags.add(documentToTag(doc));
        }
        return tags;
    }

    @Override
    public Optional<Tag> findById(Long id) {
        if (collection == null) return Optional.empty();
        
        Document doc = collection.find(eq("_id", id)).first();
        return doc != null ? Optional.of(documentToTag(doc)) : Optional.empty();
    }

    @Override
    public Tag save(Tag tag) {
        if (collection == null) {
             if (tag.getId() == null) {
                tag.setId(nextId++);
            }
            return tag;
        }
        
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
        if (collection == null) return;
        
        collection.deleteOne(eq("_id", id));
    }

    @Override
    public Optional<Tag> findByName(String name) {
        if (collection == null) return Optional.empty();
        
        Document doc = collection.find(eq("name", name)).first();
        return doc != null ? Optional.of(documentToTag(doc)) : Optional.empty();
    }

    @Override
    public List<Tag> findByNameContaining(String keyword) {
        if (collection == null) return new ArrayList<>();
        
        List<Tag> tags = new ArrayList<>();
        for (Document doc : collection.find(regex("name", ".*" + keyword + ".*", "i"))) {
            tags.add(documentToTag(doc));
        }
        return tags;
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
}