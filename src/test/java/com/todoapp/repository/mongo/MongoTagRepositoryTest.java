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
import java.util.logging.Logger;
import java.util.logging.Level;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

/**
 * MongoDB implementation of TagRepository - REFACTORED
 * 
 * REFACTOR PHASE: Improved error handling, logging, and code organization
 * All existing tests must still pass - no behavior changes
 */
public class MongoTagRepositoryTest implements TagRepository {
    
    private static final Logger LOGGER = Logger.getLogger(MongoTagRepository.class.getName());
    private static final String COLLECTION_NAME = "tags";
    
    private final MongoCollection<Document> collection;
    private Long nextId = 1L;

    public MongoTagRepositoryTest(MongoClient mongoClient, String databaseName) {
        if (mongoClient != null && databaseName != null) {
            try {
                MongoDatabase database = mongoClient.getDatabase(databaseName);
                this.collection = database.getCollection(COLLECTION_NAME);
                initializeNextId();
                LOGGER.info("MongoDB TagRepository initialized for database: " + databaseName);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to initialize MongoDB TagRepository", e);
                throw new RuntimeException("Failed to initialize MongoDB connection", e);
            }
        } else {
            this.collection = null;
            LOGGER.warning("MongoDB TagRepository initialized with null client - testing mode");
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
    public List<Tag> findAll() {
        if (!isConnectionAvailable()) return new ArrayList<>();
        
        List<Tag> tags = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                tags.add(documentToTag(doc));
            }
            LOGGER.fine("Found " + tags.size() + " tags");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find all tags", e);
            throw new RuntimeException("Failed to retrieve tags from database", e);
        }
        return tags;
    }

    @Override
    public Optional<Tag> findById(Long id) {
        if (!isConnectionAvailable() || id == null) return Optional.empty();
        
        try {
            Document doc = collection.find(eq("_id", id)).first();
            Optional<Tag> result = doc != null ? Optional.of(documentToTag(doc)) : Optional.empty();
            LOGGER.fine("Find by ID " + id + ": " + (result.isPresent() ? "found" : "not found"));
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find tag by ID: " + id, e);
            throw new RuntimeException("Failed to retrieve tag from database", e);
        }
    }

    @Override
    public Tag save(Tag tag) {
        validateTag(tag);
        
        if (!isConnectionAvailable()) {
            // Testing mode - just set ID and return
            if (tag.getId() == null) {
                tag.setId(nextId++);
            }
            return tag;
        }
        
        try {
            if (tag.getId() == null) {
                tag.setId(nextId++);
            }
            
            Document doc = tagToDocument(tag);
            collection.replaceOne(eq("_id", tag.getId()), doc, 
                new com.mongodb.client.model.ReplaceOptions().upsert(true));
            
            LOGGER.fine("Saved tag with ID: " + tag.getId());
            return tag;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save tag: " + tag, e);
            throw new RuntimeException("Failed to save tag to database", e);
        }
    }

    @Override
    public void delete(Tag tag) {
        if (tag != null && tag.getId() != null) {
            deleteById(tag.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        if (!isConnectionAvailable() || id == null) return;
        
        try {
            long deletedCount = collection.deleteOne(eq("_id", id)).getDeletedCount();
            LOGGER.fine("Deleted tag ID " + id + ": " + (deletedCount > 0 ? "success" : "not found"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to delete tag by ID: " + id, e);
            throw new RuntimeException("Failed to delete tag from database", e);
        }
    }

    @Override
    public Optional<Tag> findByName(String name) {
        if (!isConnectionAvailable() || name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            Document doc = collection.find(eq("name", name)).first();
            Optional<Tag> result = doc != null ? Optional.of(documentToTag(doc)) : Optional.empty();
            LOGGER.fine("Find by name '" + name + "': " + (result.isPresent() ? "found" : "not found"));
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find tag by name: " + name, e);
            throw new RuntimeException("Failed to retrieve tag from database", e);
        }
    }

    @Override
    public List<Tag> findByNameContaining(String keyword) {
        if (!isConnectionAvailable() || keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Tag> tags = new ArrayList<>();
        try {
            String escapedKeyword = escapeRegexSpecialChars(keyword.trim());
            for (Document doc : collection.find(regex("name", ".*" + escapedKeyword + ".*", "i"))) {
                tags.add(documentToTag(doc));
            }
            LOGGER.fine("Found " + tags.size() + " tags containing: " + keyword);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find tags by name containing: " + keyword, e);
            throw new RuntimeException("Failed to search tags in database", e);
        }
        return tags;
    }

    // Private helper methods
    
    private boolean isConnectionAvailable() {
        return collection != null;
    }
    
    private void validateTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
    }
    
    private String escapeRegexSpecialChars(String input) {
        return input.replaceAll("([\\[\\]{}()*+?.\\\\^$|])", "\\\\$1");
    }

    private Document tagToDocument(Tag tag) {
        return new Document()
            .append("_id", tag.getId())
            .append("name", tag.getName().trim());
    }

    private Tag documentToTag(Document doc) {
        try {
            String name = doc.getString("name");
            if (name == null) {
                throw new IllegalStateException("Tag name cannot be null in database");
            }
            
            Tag tag = new Tag(name);
            tag.setId(doc.getLong("_id"));
            return tag;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to convert document to Tag: " + doc, e);
            throw new RuntimeException("Failed to parse tag from database", e);
        }
    }
}
            