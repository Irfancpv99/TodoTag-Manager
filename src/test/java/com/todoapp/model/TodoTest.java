package com.todoapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TodoTest {

    @Test
    void shouldCreateTodoWithDescription() {
        Todo todo = new Todo("Learn TDD");
        
        assertEquals("Learn TDD", todo.getDescription());
        assertFalse(todo.isDone());
        assertNull(todo.getId());
    }
    
    @Test
    void shouldCreateTodoWithDescriptionAndStatus() {
        Todo todoTrue = new Todo("Task", true);
        Todo todoFalse = new Todo("Task", false);
        
        assertTrue(todoTrue.isDone());
        assertFalse(todoFalse.isDone());
    }

    @Test
    void shouldCreateDefaultTodo() {
        Todo todo = new Todo();
        
        assertNull(todo.getDescription());
        assertFalse(todo.isDone()); // Critical for PIT - tests default false assignment
    }
    
    @Test
    void shouldRejectNullDescription() {
        assertThrows(IllegalArgumentException.class, () -> new Todo(null));
        assertThrows(IllegalArgumentException.class, () -> new Todo(null, true));
        
        Todo todo = new Todo("Test");
        assertThrows(IllegalArgumentException.class, () -> todo.setDescription(null));
    }
    
    @Test
    void shouldSetIdAndDescription() {
        Todo todo = new Todo("Test");
        
        todo.setId(1L);
        todo.setDescription("Updated");
        todo.setDone(true);
        
        assertEquals(1L, todo.getId());
        assertEquals("Updated", todo.getDescription());
        assertTrue(todo.isDone());
        
        todo.setDone(false);
        assertFalse(todo.isDone());
    }
    
    @Test
    void shouldHandleEqualityCorrectly() {
        Todo todo1 = new Todo("Task1");
        Todo todo2 = new Todo("Task2");
        Todo todo3 = new Todo("Task3");
        
        // Same object reference
        assertEquals(todo1, todo1);
        
        // Both null IDs - should be false
        assertNotEquals(todo1, todo2);
        
        // Same IDs - should be equal regardless of other fields
        todo1.setId(1L);
        todo2.setId(1L);
        assertEquals(todo1, todo2);
        
        // Different IDs
        todo2.setId(2L);
        assertNotEquals(todo1, todo2);
        
        // One null ID, one not null
        assertNotEquals(todo2, todo3);
        assertNotEquals(todo3, todo2);
        
        // Null object
        assertNotEquals(todo1, null);
        
        // Different class
        assertNotEquals(todo1, "string");
    }
    
    @Test
    void shouldCalculateHashCodeBasedOnDescription() {
        Todo todo1 = new Todo("Same description");
        Todo todo2 = new Todo("Same description");
        Todo todo3 = new Todo("Different description");
        Todo todoNull = new Todo();
        
        // Same description = same hashCode
        assertEquals(todo1.hashCode(), todo2.hashCode());
        
        // Different descriptions = different hashCode
        assertNotEquals(todo1.hashCode(), todo3.hashCode());
        
        // Null description should return 0
        assertEquals(0, todoNull.hashCode());
        
        // HashCode should be consistent regardless of ID
        todo1.setId(1L);
        todo2.setId(2L);
        assertEquals(todo1.hashCode(), todo2.hashCode()); // Still same because same description
    }
    
    @Test
    void shouldMaintainHashSetIntegrityInManyToManyRelationship() {
        Todo todo1 = new Todo("Task 1");
        Todo todo2 = new Todo("Task 2");
        Tag workTag = new Tag("work");
        Tag urgentTag = new Tag("urgent");
        
        // Test that entities can be added to HashSets before and after ID assignment
        todo1.addTag(workTag);
        todo1.addTag(urgentTag);
        todo2.addTag(workTag);
        
        // Verify collections work correctly
        assertEquals(2, todo1.getTags().size());
        assertEquals(1, todo2.getTags().size());
        assertEquals(2, workTag.getTodos().size());
        assertEquals(1, urgentTag.getTodos().size());
        
        // Assign IDs (simulating persistence) - hashCode should remain consistent
        todo1.setId(1L);
        todo2.setId(2L);
        workTag.setId(10L);
        urgentTag.setId(11L);
        
        // Collections should still work correctly after ID assignment
        assertTrue(todo1.getTags().contains(workTag));
        assertTrue(todo1.getTags().contains(urgentTag));
        assertTrue(workTag.getTodos().contains(todo1));
        assertTrue(workTag.getTodos().contains(todo2));
        
        // Remove operations should still work
        todo1.removeTag(urgentTag);
        assertEquals(1, todo1.getTags().size());
        assertEquals(0, urgentTag.getTodos().size());
    }
    
    @Test
    void shouldHandleNullTagOperations() {
        Todo todo = new Todo("Test");
        int originalSize = todo.getTags().size();
        
        // addTag with null should be ignored - tests line 2: if (tag != null)
        todo.addTag(null);
        assertEquals(originalSize, todo.getTags().size());
        
        // removeTag with null should be ignored - tests line 9: if (tag != null) 
        todo.removeTag(null);
        assertEquals(originalSize, todo.getTags().size());
    }
    
    @Test
    void shouldProvideDefensiveCopiesAndHandleNulls() {
        Todo todo = new Todo("Test");
        Tag tag = new Tag("work");
        todo.addTag(tag);
        
        // getTags returns defensive copy
        todo.getTags().clear();
        assertEquals(1, todo.getTags().size());
        
        // setTags with null creates empty set
        todo.setTags(null);
        assertNotNull(todo.getTags());
        assertEquals(0, todo.getTags().size());
        
        // setTags with non-null creates defensive copy
        java.util.Set<Tag> originalSet = new java.util.HashSet<>();
        originalSet.add(tag);
        todo.setTags(originalSet);
        originalSet.clear();
        assertEquals(1, todo.getTags().size());
    }
    
    @Test
    void shouldGenerateCorrectStringRepresentation() {
        Todo todo = new Todo("Test task");
        todo.setId(42L);
        todo.setDone(true);
        
        String result = todo.toString();
        
        assertTrue(result.contains("id=42"));
        assertTrue(result.contains("description='Test task'"));
        assertTrue(result.contains("done=true"));
    }
}