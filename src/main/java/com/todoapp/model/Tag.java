package com.todoapp.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Tag entity - With Todo relationship support and JPA annotations
 * 
 * REFACTOR PHASE: Adding JPA annotations for MySQL support
 * Following TDD: enhance model for database persistence without changing behavior
 */
@Entity
@Table(name = "tags")
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @ManyToMany(mappedBy = "tags")
    private Set<Todo> todos = new HashSet<>();
    
    /**
     * Default constructor required by JPA
     */
    public Tag() {
    }
    
    /**
     * Creates a new Tag with the given name.
     *
     * @param name the name of the tag, cannot be null
     * @throws IllegalArgumentException if name is null
     */
    public Tag(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }
    
    /**
     * Returns a defensive copy of the todos associated with this tag.
     * 
     * @return a new Set containing all todos (never null)
     */
    public Set<Todo> getTodos() {
        return new HashSet<>(todos);
    }
    
    /**
     * Returns the internal todos set for bidirectional relationship management.
     * This method should only be used by Todo class.
     * 
     * @return the internal todos set
     */
    Set<Todo> getTodosInternal() {
        return todos;
    }
    
    /**
     * Sets the todos for this tag, replacing any existing todos.
     * 
     * @param todos the new set of todos (null will be treated as empty set)
     */
    public void setTodos(Set<Todo> todos) {
        this.todos = todos != null ? new HashSet<>(todos) : new HashSet<>();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag tag = (Tag) obj;
        
        // If both have IDs, compare by ID
        if (id != null && tag.id != null) {
            return Objects.equals(id, tag.id);
        }
        
        // If both have no ID, they are different instances (not equal)
        if (id == null && tag.id == null) {
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
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}