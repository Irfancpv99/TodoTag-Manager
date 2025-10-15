package com.todoapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @ManyToMany(mappedBy = "tags")
    private Set<Todo> todos = new HashSet<>();
   
    public Tag() {
    }
   
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
   
    public Set<Todo> getTodos() {
        return new HashSet<>(todos);
    }
    
   
    Set<Todo> getTodosInternal() {
        return todos;
    }
    
    public void setTodos(Set<Todo> todos) {
        this.todos = todos != null ? new HashSet<>(todos) : new HashSet<>();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag tag = (Tag) obj;
        
        return id != null && tag.id != null && Objects.equals(id, tag.id);
    }
    
    
    @Override
    public int hashCode() {
        return name != null ? Objects.hash(name) : 0;
    }
    
    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}