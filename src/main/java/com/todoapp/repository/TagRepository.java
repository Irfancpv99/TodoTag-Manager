package com.todoapp.repository;

import com.todoapp.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {
    
    List<Tag> findAll();
    
    Optional<Tag> findById(Long id);
    
    Tag save(Tag tag);
    
    void delete(Tag tag);
    
    void deleteById(Long id);
    
    Optional<Tag> findByName(String name);
    
    
    List<Tag> findByNameContaining(String keyword);
}