package com.todoapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TagTest {
	
   
	@Test
    void shouldAllowSettingName() {
        
        Tag tag = new Tag("original");
        tag.setName("updated");
        assertEquals("updated", tag.getName());
    }
	
	@Test
    void shouldHaveNullIdWhenCreated() {
       Tag tag = new Tag("urgent");
        assertNull(tag.getId());
    }
	
	@Test
	    void shouldThrowExceptionWhenCreatedWithNullName() {      
	        assertThrows(IllegalArgumentException.class, () -> {
	            new Tag(null);
	        });
	    }
	 
	 @Test
	    void shouldAllowSettingId() {
	        Tag tag = new Tag("personal");  
	        tag.setId(1L);
	        assertEquals(1L, tag.getId());
	    }
	 
    @Test
    void shouldThrowExceptionWhenSettingNullName() {
        Tag tag = new Tag("test");
        assertThrows(IllegalArgumentException.class, () -> {
            tag.setName(null);
        });
    }
    
    @Test
    void shouldBeEqualWhenSameId() {
        Tag tag1 = new Tag("work");
        Tag tag2 = new Tag("urgent");
        
        tag1.setId(1L);
        tag2.setId(1L);
        
        assertEquals(tag1, tag2);
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Tag tag1 = new Tag("work");
        Tag tag2 = new Tag("work");
        
        tag1.setId(1L);
        tag2.setId(2L);
        
        assertNotEquals(tag1, tag2);
    }
    
    @Test
    void shouldNotBeEqualWhenNoId() {
        Tag tag1 = new Tag("work");
        Tag tag2 = new Tag("work");
        
        assertNotEquals(tag1, tag2);
    }
    
    @Test
    void shouldNotBeEqualToNull() {
        Tag tag = new Tag("test");
        
        assertNotEquals(tag, null);
    }
    
    @Test
    void shouldNotBeEqualToDifferentClass() {
        Tag tag = new Tag("test");
        
        assertNotEquals(tag, "not a tag");
    }
    
    @Test
    void shouldHaveStringRepresentation() {
        Tag tag = new Tag("work");
        tag.setId(1L);
        
        String result = tag.toString();
        
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("name='work'"));
    }

    @Test
    void shouldCreateTagWithName() {
        String name = "work";
        
        Tag tag = new Tag(name);
        
        assertNotNull(tag);
        assertEquals("work", tag.getName());
    }
 
}