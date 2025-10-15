package com.todoapp.repository.mysql;

import com.todoapp.model.Todo;
import com.todoapp.repository.TodoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class MySqlTodoRepository implements TodoRepository {
    
    private final EntityManager entityManager;

    public MySqlTodoRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Todo> findAll() {
        TypedQuery<Todo> query = entityManager.createQuery("SELECT t FROM Todo t", Todo.class);
        return query.getResultList();
    }

    @Override
    public Optional<Todo> findById(Long id) {
        Todo todo = entityManager.find(Todo.class, id);
        return Optional.ofNullable(todo);
    }

    @Override
    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            entityManager.persist(todo);
            return todo;
        } else {
            return entityManager.merge(todo);
        }
    }

    @Override
    public void delete(Todo todo) {
        if (entityManager.contains(todo)) {
            entityManager.remove(todo);
        } else {
            Todo managed = entityManager.find(Todo.class, todo.getId());
            if (managed != null) {
                entityManager.remove(managed);
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        Todo todo = entityManager.find(Todo.class, id);
        if (todo != null) {
            entityManager.remove(todo);
        }
    }

    @Override
    public List<Todo> findByDone(boolean done) {
        TypedQuery<Todo> query = entityManager.createQuery(
            "SELECT t FROM Todo t WHERE t.done = :done", Todo.class);
        query.setParameter("done", done);
        return query.getResultList();
    }

    @Override
    public List<Todo> findByDescriptionContaining(String keyword) {
        TypedQuery<Todo> query = entityManager.createQuery(
            "SELECT t FROM Todo t WHERE t.description LIKE :keyword", Todo.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }
}