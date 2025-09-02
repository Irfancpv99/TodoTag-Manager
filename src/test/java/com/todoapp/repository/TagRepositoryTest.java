package com.todoapp.repository;

import com.todoapp.model.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TagRepositoryTest {

    @Mock
    private TagRepository tagRepository;

    @Test
    void shouldFindAllTags() {
        List<Tag> expectedTags = Arrays.asList(
            new Tag("work"),
            new Tag("urgent")
        );
        
        when(tagRepository.findAll()).thenReturn(expectedTags);
        
        List<Tag> actualTags = tagRepository.findAll();
        
        assertEquals(expectedTags, actualTags);
        verify(tagRepository).findAll();
    }
    
    @Test
    void shouldFindTagById() {
        Tag expectedTag = new Tag("work");
        expectedTag.setId(1L);
        
        when(tagRepository.findById(1L)).thenReturn(Optional.of(expectedTag));
        
        Optional<Tag> actualTag = tagRepository.findById(1L);
        
        assertTrue(actualTag.isPresent());
        assertEquals(expectedTag, actualTag.get());
        verify(tagRepository).findById(1L);
    }
    
    @Test
    void shouldReturnEmptyWhenTagNotFound() {
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());
        
        Optional<Tag> actualTag = tagRepository.findById(999L);
        
        assertFalse(actualTag.isPresent());
        verify(tagRepository).findById(999L);
    }
    
    @Test
    void shouldSaveTag() {
        Tag tagToSave = new Tag("personal");
        Tag savedTag = new Tag("personal");
        savedTag.setId(1L);
        
        when(tagRepository.save(tagToSave)).thenReturn(savedTag);
        
        Tag result = tagRepository.save(tagToSave);
        
        assertEquals(savedTag, result);
        verify(tagRepository).save(tagToSave);
    }
    
    @Test
    void shouldDeleteTagById() {
        Long tagId = 1L;
        
        doNothing().when(tagRepository).deleteById(tagId);
        
        tagRepository.deleteById(tagId);
        
        verify(tagRepository).deleteById(tagId);
    }
    
    @Test
    void shouldDeleteTag() {
        Tag tag = new Tag("work");
        tag.setId(1L);
        
        doNothing().when(tagRepository).delete(tag);
        
        tagRepository.delete(tag);
        
        verify(tagRepository).delete(tag);
    }
    
    @Test
    void shouldFindTagByName() {
        Tag expectedTag = new Tag("urgent");
        expectedTag.setId(1L);
        
        when(tagRepository.findByName("urgent")).thenReturn(Optional.of(expectedTag));
        
        Optional<Tag> actualTag = tagRepository.findByName("urgent");
        
        assertTrue(actualTag.isPresent());
        assertEquals("urgent", actualTag.get().getName());
        verify(tagRepository).findByName("urgent");
    }
    
    @Test
    void shouldFindTagsByNameContaining() {
        List<Tag> searchResults = Arrays.asList(
            new Tag("important")
        );
        
        when(tagRepository.findByNameContaining("import")).thenReturn(searchResults);
        
        List<Tag> actualTags = tagRepository.findByNameContaining("import");
        
        assertEquals(searchResults, actualTags);
        verify(tagRepository).findByNameContaining("import");
    }
}