package com.todoapp.repository.mysql;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MySqlTodoRepositoryTest {

    @Mock
    private EntityManager entityManager;
    
    @Mock
    private TypedQuery<Todo> query;
    
    private MySqlTodoRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new MySqlTodoRepository(entityManager);
    }
    
    @Test
    void shouldSaveNewTodo() {
        Todo todo = new Todo("Test task");
        
        Todo result = repository.save(todo);
        
        verify(entityManager).persist(todo);
        assertEquals(todo, result);
    }
    
    @Test
    void shouldSaveExistingTodo() {
        Todo todo = new Todo("Test task");
        todo.setId(1L);
        Todo mergedTodo = new Todo("Merged task");
        when(entityManager.merge(todo)).thenReturn(mergedTodo);
        
        Todo result = repository.save(todo);
        
        verify(entityManager).merge(todo);
        assertEquals(mergedTodo, result);
    }
    
    @Test
    void shouldFindAll() {
        List<Todo> todos = Arrays.asList(new Todo("Task 1"));
        when(entityManager.createQuery("SELECT t FROM Todo t", Todo.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(todos);
        
        List<Todo> result = repository.findAll();
        
        assertEquals(todos, result);
    }
    
    @Test
    void shouldFindById() {
        Todo todo = new Todo("Test task");
        when(entityManager.find(Todo.class, 1L)).thenReturn(todo);
        
        Optional<Todo> result = repository.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(todo, result.get());
    }
    
    @Test
    void shouldReturnEmptyWhenNotFoundById() {
        when(entityManager.find(Todo.class, 999L)).thenReturn(null);
        
        Optional<Todo> result = repository.findById(999L);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void shouldDeleteManagedEntity() {
        Todo todo = new Todo("Test task");
        when(entityManager.contains(todo)).thenReturn(true);
        
        repository.delete(todo);
        
        verify(entityManager).remove(todo);
    }
    
    @Test
    void shouldDeleteUnmanagedEntity() {
        Todo todo = new Todo("Test task");
        todo.setId(1L);
        Todo managedTodo = new Todo("Managed task");
        when(entityManager.contains(todo)).thenReturn(false);
        when(entityManager.find(Todo.class, 1L)).thenReturn(managedTodo);
        
        repository.delete(todo);
        
        verify(entityManager).remove(managedTodo);
    }
    
    @Test
    void shouldHandleDeleteWhenEntityNotFound() {
        Todo todo = new Todo("Test task");
        todo.setId(1L);
        when(entityManager.contains(todo)).thenReturn(false);
        when(entityManager.find(Todo.class, 1L)).thenReturn(null);
        
        repository.delete(todo);
        
        verify(entityManager, never()).remove(any());
    }
    
    @Test
    void shouldDeleteById() {
        Todo todo = new Todo("Test task");
        when(entityManager.find(Todo.class, 1L)).thenReturn(todo);
        
        repository.deleteById(1L);
        
        verify(entityManager).remove(todo);
    }
    
    @Test
    void shouldHandleDeleteByIdWhenNotFound() {
        when(entityManager.find(Todo.class, 999L)).thenReturn(null);
        
        repository.deleteById(999L);
        
        verify(entityManager, never()).remove(any());
    }
    
    @Test
    void shouldFindByDoneStatus() {
        List<Todo> doneTodos = Arrays.asList(new Todo("Done task", true));
        when(entityManager.createQuery("SELECT t FROM Todo t WHERE t.done = :done", Todo.class)).thenReturn(query);
        when(query.setParameter("done", true)).thenReturn(query);
        when(query.getResultList()).thenReturn(doneTodos);
        
        List<Todo> result = repository.findByDone(true);
        
        assertEquals(doneTodos, result);
    }
    
    @Test
    void shouldFindByDescriptionContaining() {
        List<Todo> todos = Arrays.asList(new Todo("Important meeting"));
        when(entityManager.createQuery("SELECT t FROM Todo t WHERE t.description LIKE :keyword", Todo.class)).thenReturn(query);
        when(query.setParameter("keyword", "%important%")).thenReturn(query);
        when(query.getResultList()).thenReturn(todos);
        
        List<Todo> result = repository.findByDescriptionContaining("important");
        
        assertEquals(todos, result);
    }
    
    @Test
    void shouldFindByTag() {
        Tag tag = new Tag("work");
        tag.setId(1L);
        Todo todo1 = new Todo("Work task 1");
        Todo todo2 = new Todo("Work task 2");
        List<Todo> todos = Arrays.asList(todo1, todo2);
        
        when(entityManager.createQuery("SELECT t FROM Todo t JOIN t.tags tag WHERE tag = :tag", Todo.class)).thenReturn(query);
        when(query.setParameter("tag", tag)).thenReturn(query);
        when(query.getResultList()).thenReturn(todos);
        
        List<Todo> result = repository.findByTag(tag);
        
        assertEquals(2, result.size());
        assertEquals(todos, result);
        verify(query).setParameter("tag", tag);
    }
}