package com.todoapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.repository.TagRepository;
import com.todoapp.repository.TodoRepository;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {
    private TodoRepository todoRepository;
    private TagRepository tagRepository;
    private TodoService todoService;

    @BeforeEach
    void setUp() {
        todoRepository = mock(TodoRepository.class);
        tagRepository = mock(TagRepository.class);
        todoService = new TodoService(todoRepository, tagRepository);
    }
    
    
//    			TODO TEST 
    
    @Test
    void shouldGetAllTodos() {
        List todos = List.of(new Todo("Task 1"));
        when(todoRepository.findAll()).thenReturn(todos);

        assertEquals(todos, todoService.getAllTodos());
    }
    
    @Test
    void shouldGetTodoById() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        Optional result = todoService.getTodoById(1L);

        assertTrue(result.isPresent());
        assertEquals(todo, result.get());
    }
    @Test
    void shouldSaveTodo() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.save(todo)).thenReturn(todo);

        assertEquals(todo, todoService.saveTodo(todo));
        verify(todoRepository).save(todo);
    }
    @Test
    void shouldThrowExceptionWhenCreatingTodoWithNullDescription() {
        assertThrows(IllegalArgumentException.class, () -> todoService.createTodo(null));
    }
    @Test
    void shouldThrowExceptionWhenCreatingTodoWithEmptyDescription() {
        assertThrows(IllegalArgumentException.class, () -> todoService.createTodo("   "));
    }  
    @Test
    void shouldCreateTodo() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        assertNotNull(todoService.createTodo("  Task 1  "));
        verify(todoRepository).save(any(Todo.class));
    }
    
    @Test
    void shouldDeleteTodo() {
        assertDoesNotThrow(() -> todoService.deleteTodo(1L));
        verify(todoRepository).deleteById(1L);
    }
    
    @Test
    void shouldThrowExceptionWhenMarkingNonExistentTodoComplete() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> todoService.markTodoComplete(1L));
    }
    
    @Test
    void shouldMarkTodoComplete() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        assertTrue(todoService.markTodoComplete(1L).isDone());
        verify(todoRepository).save(todo);
    }
    
    @Test
    void shouldMarkTodoIncomplete() {
        Todo todo = new Todo("Task 1");
        todo.setDone(true);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        assertFalse(todoService.markTodoIncomplete(1L).isDone());
        verify(todoRepository).save(todo);
    }
    
    @Test
    void shouldThrowExceptionWhenMarkingNonExistentTodoIncomplete() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> todoService.markTodoIncomplete(1L));
    }
    
    @Test
    void shouldGetCompletedTodos() {
        List todos = List.of(new Todo("Task 1"));
        when(todoRepository.findByDone(true)).thenReturn(todos);

        assertEquals(todos, todoService.getCompletedTodos());
    }
    
    @Test
    void shouldGetIncompleteTodos() {
        List todos = List.of(new Todo("Task 1"));
        when(todoRepository.findByDone(false)).thenReturn(todos);

        assertEquals(todos, todoService.getIncompleteTodos());
    }
    
    @Test
    void shouldSearchTodos() {
        List todos = List.of(new Todo("Task 1"));
        when(todoRepository.findByDescriptionContaining("Task")).thenReturn(todos);

        assertEquals(todos, todoService.searchTodos("Task"));
    }
    @Test
    void shouldThrowExceptionWhenSearchingTodosWithNull() {
        assertThrows(IllegalArgumentException.class, () -> todoService.searchTodos(null));
    }
    
//    					TAG TEST
    
    @Test
    void shouldGetAllTags() {
        List tags = List.of(new Tag("Work"));
        when(tagRepository.findAll()).thenReturn(tags);

        assertEquals(tags, todoService.getAllTags());
    }
    
    @Test
    void shouldGetTagById() {
        Tag tag = new Tag("Work");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Optional result = todoService.getTagById(1L);

        assertTrue(result.isPresent());
        assertEquals(tag, result.get());
    }
    
    @Test
    void shouldSaveTag() {
        Tag tag = new Tag("Work");
        when(tagRepository.save(tag)).thenReturn(tag);

        assertEquals(tag, todoService.saveTag(tag));
        verify(tagRepository).save(tag);
    }
    
    @Test
    void shouldCreateTag() {
        Tag tag = new Tag("Work");
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        assertNotNull(todoService.createTag("  Work  "));
        verify(tagRepository).save(any(Tag.class));
    }
    
    @Test
    void shouldThrowExceptionWhenCreatingTagWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> todoService.createTag(null));
    }
    
    @Test
    void shouldThrowExceptionWhenCreatingTagWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> todoService.createTag("   "));
    }
    
    @Test
    void shouldDeleteTag() {
        assertDoesNotThrow(() -> todoService.deleteTag(1L));
        verify(tagRepository).deleteById(1L);
    }
    
    @Test
    void shouldFindTagByName() {
        Tag tag = new Tag("Work");
        when(tagRepository.findByName("Work")).thenReturn(Optional.of(tag));

        Optional result = todoService.findTagByName("Work");

        assertTrue(result.isPresent());
        assertEquals(tag, result.get());
    }
    
    @Test
    void shouldThrowExceptionWhenSearchingTagsWithNull() {
        assertThrows(IllegalArgumentException.class, () -> todoService.searchTags(null));
    }

}