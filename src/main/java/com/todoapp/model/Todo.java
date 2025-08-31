package com.todoapp.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Todo entity - With Tag relationship support and JPA annotations
 * 
 * REFACTOR PHASE: Adding JPA annotations for MySQL support
 * Following TDD: enhance model for database persistence without changing behavior
 */
@Entity
@Table(name = "todos")
public class Todo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String description;
    
    private boolean done;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "todo_tags",
        joinColumns = @JoinColumn(name = "todo_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    
    /**
     * Default constructor required by JPA
     */
    public Todo() {
        this.done = false;
    }
    
    /**
     * Creates a new Todo with the given description.
     * The Todo will be marked as not done by default.
     *
     * @param description the description of the todo, cannot be null
     * @throws IllegalArgumentException if description is null
     */
    public Todo(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.description = description;
        this.done = false;
    }
    
    /**
     * Creates a new Todo with the given description and done status.
     *
     * @param description the description of the todo, cannot be null
     * @param done the initial done status
     * @throws IllegalArgumentException if description is null
     */
    public Todo(String description, boolean done) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.description = description;
        this.done = done;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.description = description;
    }
    
    public boolean isDone() {
        return done;
    }
    
    public void setDone(boolean done) {
        this.done = done;
    }
    
    /**
     * Returns a defensive copy of the tags associated with this todo.
     * 
     * @return a new Set containing all tags (never null)
     */
    public Set<Tag> getTags() {
        return new HashSet<>(tags);
    }
    
    /**
     * Sets the tags for this todo, replacing any existing tags.
     * 
     * @param tags the new set of tags (null will be treated as empty set)
     */
    public void setTags(Set<Tag> tags) {
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
    }
    
    /**
     * Adds a tag to this todo and maintains bidirectional relationship.
     * 
     * @param tag the tag to add (null will be ignored)
     */
    public void addTag(Tag tag) {
        if (tag != null) {
            tags.add(tag);
            tag.getTodosInternal().add(this);
        }
    }
    
    /**
     * Removes a tag from this todo and maintains bidirectional relationship.
     * 
     * @param tag the tag to remove (null will be ignored)
     */
    public void removeTag(Tag tag) {
        if (tag != null) {
            tags.remove(tag);
            tag.getTodosInternal().remove(this);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Todo todo = (Todo) obj;
        
        // If both have IDs, compare by ID
        if (id != null && todo.id != null) {
            return Objects.equals(id, todo.id);
        }
        
        // If both have no ID, they are different instances (not equal)
        if (id == null && todo.id == null) {
            return false;
        }
        
        // One has ID, other doesn't - not equal
        return false;
    }
    
    @Override
    public int hashCode() {
        // Use ID if available, otherwise use object identity
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }
    
    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", done=" + done +
                '}';
    }
}