package com.todoapp.repository;

import com.todoapp.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {
    List<Tag> findAll();
    Optional<Tag> findById(Long id);
    Tag save(Tag tag);
    void deleteById(Long id);
    void delete(Tag tag);
    Optional<Tag> findByName(String name);
    List<Tag> findByNameContaining(String keyword);
}