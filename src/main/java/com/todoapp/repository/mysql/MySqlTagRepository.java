package com.todoapp.repository.mysql;

import com.todoapp.model.Tag;
import com.todoapp.repository.TagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class MySqlTagRepository implements TagRepository {
    
    private final EntityManager entityManager;

    public MySqlTagRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Tag> findAll() {
        TypedQuery<Tag> query = entityManager.createQuery("SELECT t FROM Tag t", Tag.class);
        return query.getResultList();
    }

    @Override
    public Optional<Tag> findById(Long id) {
        Tag tag = entityManager.find(Tag.class, id);
        return Optional.ofNullable(tag);
    }

    @Override
    public Tag save(Tag tag) {
        if (tag.getId() == null) {
            entityManager.persist(tag);
            return tag;
        } else {
            return entityManager.merge(tag);
        }
    }

    @Override
    public void delete(Tag tag) {
        if (entityManager.contains(tag)) {
            entityManager.remove(tag);
        } else {
            Tag managed = entityManager.find(Tag.class, tag.getId());
            if (managed != null) {
                entityManager.remove(managed);
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        Tag tag = entityManager.find(Tag.class, id);
        if (tag != null) {
            entityManager.remove(tag);
        }
    }

    @Override
    public Optional<Tag> findByName(String name) {
        try {
            TypedQuery<Tag> query = entityManager.createQuery(
                "SELECT t FROM Tag t WHERE t.name = :name", Tag.class);
            query.setParameter("name", name);
            Tag tag = query.getSingleResult();
            return Optional.of(tag);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Tag> findByNameContaining(String keyword) {
        TypedQuery<Tag> query = entityManager.createQuery(
            "SELECT t FROM Tag t WHERE t.name LIKE :keyword", Tag.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }
}