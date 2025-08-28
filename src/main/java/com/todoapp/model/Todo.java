package com.todoapp.model;


public class Todo {
    
    private Long id;
    private String description;
    private boolean done;
    
    public Todo(String description) {
        this.description = description;
        this.done = false; // Default to not done
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
    
    public boolean isDone() {
        return done;
    }
    
    public void setDone(boolean done) {
        this.done = done;
    }
}