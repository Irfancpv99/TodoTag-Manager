package com.todoapp.service;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.repository.RepositoryFactory;
import com.todoapp.repository.TagRepository;
import com.todoapp.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TagRepository tagRepository;

    private TodoService todoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        todoService = new TodoService(todoRepository, tagRepository);
    }

    @Test
    void testDefaultConstructor() {
        try (MockedStatic<RepositoryFactory> mockedFactory = mockStatic(RepositoryFactory.class)) {
            RepositoryFactory mockFactory = mock(RepositoryFactory.class);
            when(mockFactory.createTodoRepository()).thenReturn(todoRepository);
            when(mockFactory.createTagRepository()).thenReturn(tagRepository);
            mockedFactory.when(RepositoryFactory::getInstance).thenReturn(mockFactory);
            
            assertNotNull(new TodoService());
        }
    }

    @Test
    void testTodoCrudOperations() {
        when(todoRepository.findAll()).thenReturn(Arrays.asList(new Todo("task")));
        when(todoRepository.findById(1L)).thenReturn(Optional.of(new Todo("task")));
        when(todoRepository.save(any())).thenReturn(new Todo("saved"));
        when(todoRepository.findByDone(true)).thenReturn(Arrays.asList(new Todo("done", true)));
        when(todoRepository.findByDone(false)).thenReturn(Arrays.asList(new Todo("todo", false)));
        when(todoRepository.findByDescriptionContaining("key")).thenReturn(Arrays.asList(new Todo("key task")));

        assertFalse(todoService.getAllTodos().isEmpty());
        assertTrue(todoService.getTodoById(1L).isPresent());
        assertNotNull(todoService.saveTodo(new Todo("test")));
        assertFalse(todoService.getCompletedTodos().isEmpty());
        assertFalse(todoService.getIncompleteTodos().isEmpty());
        assertFalse(todoService.searchTodos("key").isEmpty());

        todoService.deleteTodo(1L);
        verify(todoRepository).deleteById(1L);
    }

    @Test
    void testTagCrudOperations() {
        when(tagRepository.findAll()).thenReturn(Arrays.asList(new Tag("work")));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(new Tag("work")));
        when(tagRepository.save(any())).thenReturn(new Tag("saved"));
        when(tagRepository.findByName("urgent")).thenReturn(Optional.of(new Tag("urgent")));
        when(tagRepository.findByNameContaining("imp")).thenReturn(Arrays.asList(new Tag("important")));

        assertFalse(todoService.getAllTags().isEmpty());
        assertTrue(todoService.getTagById(1L).isPresent());
        assertNotNull(todoService.saveTag(new Tag("test")));
        assertTrue(todoService.findTagByName("urgent").isPresent());
        assertFalse(todoService.searchTags("imp").isEmpty());

        todoService.deleteTag(1L);
        verify(tagRepository).deleteById(1L);
    }

    @Test
    void testCreateTodoValidation() {
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo todo = invocation.getArgument(0);
            return new Todo(todo.getDescription());
        });

        assertEquals("test", todoService.createTodo("test").getDescription());
        assertThrows(IllegalArgumentException.class, () -> todoService.createTodo(null));
        assertThrows(IllegalArgumentException.class, () -> todoService.createTodo(""));
        assertThrows(IllegalArgumentException.class, () -> todoService.createTodo("   "));
    }

    @Test
    void testCreateTagValidation() {
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag tag = invocation.getArgument(0);
            return new Tag(tag.getName());
        });

        assertEquals("work", todoService.createTag("work").getName());
        assertThrows(IllegalArgumentException.class, () -> todoService.createTag(null));
        assertThrows(IllegalArgumentException.class, () -> todoService.createTag(""));
        assertThrows(IllegalArgumentException.class, () -> todoService.createTag("   "));
    }

    @Test
    void testSearchValidation() {
        assertThrows(IllegalArgumentException.class, () -> todoService.searchTodos(null));
        assertThrows(IllegalArgumentException.class, () -> todoService.searchTags(null));
    }

    @Test
    void testMarkTodoComplete() {
        Todo todo = new Todo("task");
        todo.setId(1L);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(any())).thenReturn(todo);

        assertTrue(todoService.markTodoComplete(1L).isDone());

        when(todoRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> todoService.markTodoComplete(2L));
    }

    @Test
    void testMarkTodoIncomplete() {
        Todo todo = new Todo("task");
        todo.setId(1L);
        todo.setDone(true);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(any())).thenReturn(todo);

        assertFalse(todoService.markTodoIncomplete(1L).isDone());
        
        when(todoRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> todoService.markTodoIncomplete(2L));
    }

    @Test
    void testTagTodoRelationships() {
        Todo todo = new Todo("task");
        todo.setId(1L);
        Tag tag = new Tag("work");
        tag.setId(2L);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(tagRepository.findById(2L)).thenReturn(Optional.of(tag));
        
        Todo savedTodo = new Todo("task");
        savedTodo.setId(1L);
        savedTodo.addTag(tag);
        
        when(todoRepository.save(any())).thenReturn(savedTodo);

        Todo result = todoService.addTagToTodo(1L, 2L);
        assertNotNull(result);
        assertTrue(todo.getTags().contains(tag));

        todo.addTag(tag); 
        Todo savedTodoAfterRemove = new Todo("task");
        savedTodoAfterRemove.setId(1L);
        
        when(todoRepository.save(any())).thenReturn(savedTodoAfterRemove);
        
        Todo removeResult = todoService.removeTagFromTodo(1L, 2L);
        assertNotNull(removeResult);
        assertFalse(todo.getTags().contains(tag));

        when(todoRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> todoService.addTagToTodo(3L, 2L));
        assertThrows(IllegalArgumentException.class, () -> todoService.removeTagFromTodo(3L, 2L));
    }
    
    @Test
    void testTransactionManagementSuccess() {
        try (MockedStatic<RepositoryFactory> mockedFactory = mockStatic(RepositoryFactory.class)) {
            RepositoryFactory mockFactory = mock(RepositoryFactory.class);
            
            when(mockFactory.createTodoRepository()).thenReturn(todoRepository);
            when(mockFactory.createTagRepository()).thenReturn(tagRepository);
            mockedFactory.when(RepositoryFactory::getInstance).thenReturn(mockFactory);
            
            TodoService productionService = new TodoService();
            
            Todo savedTodo = new Todo("test transaction");
            savedTodo.setId(1L);
            when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
            
            Todo result = productionService.createTodo("test transaction");
            
            verify(mockFactory).beginTransaction();
            verify(mockFactory).commitTransaction();
            verify(mockFactory, never()).rollbackTransaction();
            
            assertNotNull(result);
            assertEquals("test transaction", result.getDescription());
        }
    }
    
    @Test
    void testTransactionManagementRollback() {
        try (MockedStatic<RepositoryFactory> mockedFactory = mockStatic(RepositoryFactory.class)) {
            RepositoryFactory mockFactory = mock(RepositoryFactory.class);
            
            when(mockFactory.createTodoRepository()).thenReturn(todoRepository);
            when(mockFactory.createTagRepository()).thenReturn(tagRepository);
            mockedFactory.when(RepositoryFactory::getInstance).thenReturn(mockFactory);
            
            TodoService productionService = new TodoService();
            
            when(todoRepository.save(any(Todo.class)))
                .thenThrow(new RuntimeException("Database error"));
            
            assertThrows(RuntimeException.class, 
                () -> productionService.createTodo("test rollback"));
            
            verify(mockFactory).beginTransaction();
            verify(mockFactory).rollbackTransaction();
            verify(mockFactory, never()).commitTransaction();
        }
    }
}