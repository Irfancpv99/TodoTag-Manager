package com.todoapp.service;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.repository.RepositoryFactory;
import com.todoapp.repository.TagRepository;
import com.todoapp.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void shouldGetAllTodos() {
        List<Todo> todos = List.of(new Todo("Task 1"));
        when(todoRepository.findAll()).thenReturn(todos);

        List<Todo> result = todoService.getAllTodos();

        assertEquals(todos, result);
    }

    @Test
    void shouldGetTodoById() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        Optional<Todo> result = todoService.getTodoById(1L);

        assertTrue(result.isPresent());
        assertEquals(todo, result.get());
    }

    @Test
    void shouldSaveTodo() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.save(todo)).thenReturn(todo);

        Todo result = todoService.saveTodo(todo);

        assertEquals(todo, result);
        verify(todoRepository).save(todo);
    }

    @Test
    void shouldCreateTodo() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        Todo result = todoService.createTodo("  Task 1  ");

        assertNotNull(result);
        verify(todoRepository).save(any(Todo.class));
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
    void shouldDeleteTodo() {
        doNothing().when(todoRepository).deleteById(1L);

        assertDoesNotThrow(() -> todoService.deleteTodo(1L));
        verify(todoRepository).deleteById(1L);
    }

    @Test
    void shouldMarkTodoComplete() {
        Todo todo = new Todo("Task 1");
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        Todo result = todoService.markTodoComplete(1L);

        assertTrue(result.isDone());
        verify(todoRepository).save(todo);
    }

    @Test
    void shouldThrowExceptionWhenMarkingNonExistentTodoComplete() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> todoService.markTodoComplete(1L));
    }

    @Test
    void shouldMarkTodoIncomplete() {
        Todo todo = new Todo("Task 1");
        todo.setDone(true);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        Todo result = todoService.markTodoIncomplete(1L);

        assertFalse(result.isDone());
        verify(todoRepository).save(todo);
    }

    @Test
    void shouldThrowExceptionWhenMarkingNonExistentTodoIncomplete() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> todoService.markTodoIncomplete(1L));
    }

    @Test
    void shouldGetCompletedTodos() {
        List<Todo> todos = List.of(new Todo("Task 1"));
        when(todoRepository.findByDone(true)).thenReturn(todos);

        List<Todo> result = todoService.getCompletedTodos();

        assertEquals(todos, result);
    }

    @Test
    void shouldGetIncompleteTodos() {
        List<Todo> todos = List.of(new Todo("Task 1"));
        when(todoRepository.findByDone(false)).thenReturn(todos);

        List<Todo> result = todoService.getIncompleteTodos();

        assertEquals(todos, result);
    }

    @Test
    void shouldSearchTodos() {
        List<Todo> todos = List.of(new Todo("Task 1"));
        when(todoRepository.findByDescriptionContaining("Task")).thenReturn(todos);

        List<Todo> result = todoService.searchTodos("Task");

        assertEquals(todos, result);
    }

    @Test
    void shouldThrowExceptionWhenSearchingTodosWithNull() {
        assertThrows(IllegalArgumentException.class, () -> todoService.searchTodos(null));
    }

    @Test
    void shouldGetAllTags() {
        List<Tag> tags = List.of(new Tag("Work"));
        when(tagRepository.findAll()).thenReturn(tags);

        List<Tag> result = todoService.getAllTags();

        assertEquals(tags, result);
    }

    @Test
    void shouldGetTagById() {
        Tag tag = new Tag("Work");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Optional<Tag> result = todoService.getTagById(1L);

        assertTrue(result.isPresent());
        assertEquals(tag, result.get());
    }

    @Test
    void shouldSaveTag() {
        Tag tag = new Tag("Work");
        when(tagRepository.save(tag)).thenReturn(tag);

        Tag result = todoService.saveTag(tag);

        assertEquals(tag, result);
        verify(tagRepository).save(tag);
    }

    @Test
    void shouldCreateTag() {
        Tag tag = new Tag("Work");
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        Tag result = todoService.createTag("  Work  ");

        assertNotNull(result);
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
        doNothing().when(tagRepository).deleteById(1L);

        assertDoesNotThrow(() -> todoService.deleteTag(1L));
        verify(tagRepository).deleteById(1L);
    }

    @Test
    void shouldFindTagByName() {
        Tag tag = new Tag("Work");
        when(tagRepository.findByName("Work")).thenReturn(Optional.of(tag));

        Optional<Tag> result = todoService.findTagByName("Work");

        assertTrue(result.isPresent());
        assertEquals(tag, result.get());
    }

    @Test
    void shouldSearchTags() {
        List<Tag> tags = List.of(new Tag("Work"));
        when(tagRepository.findByNameContaining("Work")).thenReturn(tags);

        List<Tag> result = todoService.searchTags("Work");

        assertEquals(tags, result);
    }

    @Test
    void shouldThrowExceptionWhenSearchingTagsWithNull() {
        assertThrows(IllegalArgumentException.class, () -> todoService.searchTags(null));
    }

    @Test
    void shouldAddTagToTodo() {
        Todo todo = new Todo("Task 1");
        Tag tag = new Tag("Work");
        tag.setId(1L);
        
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(todoRepository.save(todo)).thenReturn(todo);

        Todo result = todoService.addTagToTodo(1L, 1L);

        assertTrue(result.getTags().contains(tag)); // Verify tag was added
        verify(todoRepository).save(todo);
    }

    @Test
    void shouldThrowExceptionWhenAddingTagToNonExistentTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());
        when(tagRepository.findById(1L)).thenReturn(Optional.of(new Tag("Work")));

        assertThrows(IllegalArgumentException.class, () -> todoService.addTagToTodo(1L, 1L));
    }

    @Test
    void shouldThrowExceptionWhenAddingNonExistentTagToTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(new Todo("Task 1")));
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> todoService.addTagToTodo(1L, 1L));
    }

    @Test
    void shouldRemoveTagFromTodo() {
        Todo todo = new Todo("Task 1");
        Tag tag = new Tag("Work");
        tag.setId(1L);
        todo.addTag(tag); // Pre-add the tag
        
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(todoRepository.save(todo)).thenReturn(todo);

        Todo result = todoService.removeTagFromTodo(1L, 1L);

        assertFalse(result.getTags().contains(tag)); // Verify tag was removed
        verify(todoRepository).save(todo);
    }

    @Test
    void shouldThrowExceptionWhenRemovingTagFromNonExistentTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());
        when(tagRepository.findById(1L)).thenReturn(Optional.of(new Tag("Work")));

        assertThrows(IllegalArgumentException.class, () -> todoService.removeTagFromTodo(1L, 1L));
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentTagFromTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(new Todo("Task 1")));
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> todoService.removeTagFromTodo(1L, 1L));
    }

    @Test
    void shouldExecuteWithTransactionOnSuccess() {
        try (MockedStatic<RepositoryFactory> mockedFactory = mockStatic(RepositoryFactory.class)) {
            RepositoryFactory factory = mock(RepositoryFactory.class);
            when(factory.createTodoRepository()).thenReturn(todoRepository);
            when(factory.createTagRepository()).thenReturn(tagRepository);
            mockedFactory.when(RepositoryFactory::getInstance).thenReturn(factory);
            
            Todo todo = new Todo("Task 1");
            when(todoRepository.save(any(Todo.class))).thenReturn(todo);
            
            TodoService service = new TodoService();
            Todo result = service.saveTodo(todo);
            
            assertNotNull(result);
            verify(factory).beginTransaction();
            verify(factory).commitTransaction();
            verify(factory, never()).rollbackTransaction();
        }
    }
    @Test
    void shouldExecuteWithTransactionOnFailure() {
        try (MockedStatic<RepositoryFactory> mockedFactory = mockStatic(RepositoryFactory.class)) {
            RepositoryFactory factory = mock(RepositoryFactory.class);
            when(factory.createTodoRepository()).thenReturn(todoRepository);
            when(factory.createTagRepository()).thenReturn(tagRepository);
            mockedFactory.when(RepositoryFactory::getInstance).thenReturn(factory);
            
            when(todoRepository.save(any(Todo.class))).thenThrow(new RuntimeException("DB Error"));
            
            TodoService service = new TodoService();
            Todo todo = new Todo("Task 1");
            
            assertThrows(RuntimeException.class, () -> service.saveTodo(todo));
            
            verify(factory).beginTransaction();
            verify(factory).rollbackTransaction();
            verify(factory, never()).commitTransaction();
        }
    }
}