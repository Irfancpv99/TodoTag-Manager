package com.todoapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    

    public Todo() {
    }

    public Todo(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.description = description;
        this.done = false;
    }
    
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
    
    public Set<Tag> getTags() {
        return new HashSet<>(tags);
    }
    
    public void setTags(Set<Tag> tags) {
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
    }
    
    public void addTag(Tag tag) {
        if (tag != null) {
            tags.add(tag);
            tag.getTodosInternal().add(this);
        }
    }
    
    public void removeTag(Tag tag) {
        if (tag != null) {
            tags.remove(tag);
            tag.getTodosInternal().remove(this);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Todo)) {
            return false;
        }
        Todo todo = (Todo) obj;
        
        if (id == null || todo.id == null) {
            return false;
        }
        
        return Objects.equals(id, todo.id);
    }
    
    @Override
    public int hashCode() {
        return description != null ? Objects.hash(description) : 0;
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