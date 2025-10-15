package com.todoapp.repository.mysql;

import com.todoapp.model.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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
class MySqlTagRepositoryTest {

    @Mock
    private EntityManager entityManager;
    
    @Mock
    private TypedQuery<Tag> query;
    
    private MySqlTagRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new MySqlTagRepository(entityManager);
    }
    
    @Test
    void shouldSaveNewTag() {
        Tag tag = new Tag("work");
        
        Tag result = repository.save(tag);
        
        verify(entityManager).persist(tag);
        assertEquals(tag, result);
    }
    
    @Test
    void shouldSaveExistingTag() {
        Tag tag = new Tag("work");
        tag.setId(1L);
        Tag mergedTag = new Tag("merged");
        when(entityManager.merge(tag)).thenReturn(mergedTag);
        
        Tag result = repository.save(tag);
        
        verify(entityManager).merge(tag);
        assertEquals(mergedTag, result);
    }
    
    @Test
    void shouldFindAll() {
        List<Tag> tags = Arrays.asList(new Tag("work"));
        when(entityManager.createQuery("SELECT t FROM Tag t", Tag.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(tags);
        
        List<Tag> result = repository.findAll();
        
        assertEquals(tags, result);
    }
    
    @Test
    void shouldFindById() {
        Tag tag = new Tag("work");
        when(entityManager.find(Tag.class, 1L)).thenReturn(tag);
        
        Optional<Tag> result = repository.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(tag, result.get());
    }
    
    @Test
    void shouldReturnEmptyWhenNotFoundById() {
        when(entityManager.find(Tag.class, 999L)).thenReturn(null);
        
        Optional<Tag> result = repository.findById(999L);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void shouldDeleteManagedEntity() {
        Tag tag = new Tag("work");
        when(entityManager.contains(tag)).thenReturn(true);
        
        repository.delete(tag);
        
        verify(entityManager).remove(tag);
    }
    
    @Test
    void shouldDeleteUnmanagedEntity() {
        Tag tag = new Tag("work");
        tag.setId(1L);
        Tag managedTag = new Tag("managed");
        when(entityManager.contains(tag)).thenReturn(false);
        when(entityManager.find(Tag.class, 1L)).thenReturn(managedTag);
        
        repository.delete(tag);
        
        verify(entityManager).remove(managedTag);
    }
    
    @Test
    void shouldHandleDeleteWhenEntityNotFound() {
        Tag tag = new Tag("work");
        tag.setId(1L);
        when(entityManager.contains(tag)).thenReturn(false);
        when(entityManager.find(Tag.class, 1L)).thenReturn(null);
        
        repository.delete(tag);
        
        verify(entityManager, never()).remove(any());
    }
    
    @Test
    void shouldDeleteById() {
        Tag tag = new Tag("work");
        when(entityManager.find(Tag.class, 1L)).thenReturn(tag);
        
        repository.deleteById(1L);
        
        verify(entityManager).remove(tag);
    }
    
    @Test
    void shouldHandleDeleteByIdWhenNotFound() {
        when(entityManager.find(Tag.class, 999L)).thenReturn(null);
        
        repository.deleteById(999L);
        
        verify(entityManager, never()).remove(any());
    }
    
    @Test
    void shouldFindByName() {
        Tag tag = new Tag("urgent");
        when(entityManager.createQuery("SELECT t FROM Tag t WHERE t.name = :name", Tag.class)).thenReturn(query);
        when(query.setParameter("name", "urgent")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(tag);
        
        Optional<Tag> result = repository.findByName("urgent");
        
        assertTrue(result.isPresent());
        assertEquals(tag, result.get());
    }
    
    @Test
    void shouldReturnEmptyWhenNameNotFound() {
        when(entityManager.createQuery("SELECT t FROM Tag t WHERE t.name = :name", Tag.class)).thenReturn(query);
        when(query.setParameter("name", "nonexistent")).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());
        
        Optional<Tag> result = repository.findByName("nonexistent");
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void shouldFindByNameContaining() {
        List<Tag> tags = Arrays.asList(new Tag("important-work"));
        when(entityManager.createQuery("SELECT t FROM Tag t WHERE t.name LIKE :keyword", Tag.class)).thenReturn(query);
        when(query.setParameter("keyword", "%important%")).thenReturn(query);
        when(query.getResultList()).thenReturn(tags);
        
        List<Tag> result = repository.findByNameContaining("important");
        
        assertEquals(tags, result);
    }
}