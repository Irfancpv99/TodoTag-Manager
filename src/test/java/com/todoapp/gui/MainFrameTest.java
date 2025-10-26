package com.todoapp.gui;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MainFrameTest {

    abstract static class TestBase {
        protected FrameFixture window;
        protected MainFrame frame;
        protected Robot robot;
        protected MainFrameController mockController;

        @BeforeEach
        void setUp() {
            mockController = mock(MainFrameController.class);
            robot = BasicRobot.robotWithCurrentAwtHierarchy();
            frame = GuiActionRunner.execute(() -> new MainFrame(mockController));
            window = new FrameFixture(robot, frame);
            window.show();
        }

        @AfterEach
        void tearDown() {
            if (window != null) window.cleanUp();
            if (robot != null) robot.cleanUp();
        }

        protected Todo createTodo(Long id, String description, boolean done) {
            Todo todo = new Todo(description);
            todo.setId(id);
            todo.setDone(done);
            return todo;
        }

        protected Tag createTag(Long id, String name) {
            Tag tag = new Tag(name);
            tag.setId(id);
            return tag;
        }
    }

    @Nested
    class TodoOperations extends TestBase {

        @Test
        void addTodo_viaButton_createsTodo() {
            when(mockController.addTodo(anyString())).thenReturn(new Todo("Task"));
            when(mockController.getAllTodos()).thenReturn(List.of());

            window.textBox("todoDescriptionField").enterText("Task 1");
            window.button("addTodoButton").click();
            
            verify(mockController).addTodo("Task 1");
            window.textBox("todoDescriptionField").requireText("");
        }
        @Test
        void addTodo_viaEnterKey_createsTodo() {
            when(mockController.addTodo(anyString())).thenReturn(new Todo("Task"));
            when(mockController.getAllTodos()).thenReturn(List.of());

            window.textBox("todoDescriptionField").enterText("Task 2");
            window.textBox("todoDescriptionField").pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);
            
            verify(mockController).addTodo("Task 2");
            window.textBox("todoDescriptionField").requireText("");
        }
        @Test
        void deleteTodo_removesSelectedTodo() {
            Todo todo = createTodo(1L, "Task", false);
            when(mockController.getAllTodos()).thenReturn(List.of(todo));
            when(mockController.deleteTodo(1L)).thenReturn(true);

            frame.refreshTodos(); 
            window.table("todoTable").selectRows(0);
            window.button("deleteButton").click();

            verify(mockController).deleteTodo(1L);
        }
        @Test
        void toggleTodoDone_viaButton_togglesStatus() {
            Todo todo = createTodo(1L, "Task", false);
            when(mockController.getAllTodos()).thenReturn(List.of(todo));
            when(mockController.toggleTodoDone(1L)).thenReturn(true);

            frame.refreshTodos();
            window.table("todoTable").selectRows(0);
            window.button("toggleDoneButton").click();

            verify(mockController).toggleTodoDone(1L);
        }
        @Test
        void toggleTodoDone_viaDoubleClick_togglesStatus() {
            Todo todo = createTodo(1L, "Task", false);
            when(mockController.getAllTodos()).thenReturn(List.of(todo));
            when(mockController.toggleTodoDone(1L)).thenReturn(true);

            frame.refreshTodos();
            window.table("todoTable").doubleClick();

            verify(mockController).toggleTodoDone(1L);
        }
        @Test
        void editTodo_showsDialogAndUpdates() {
            Todo todo = createTodo(1L, "Original", false);
            when(mockController.getAllTodos()).thenReturn(List.of(todo));
            when(mockController.updateTodoDescription(1L, "Updated")).thenReturn(true);

            frame.refreshTodos();
            window.table("todoTable").selectRows(0);
            window.button("editButton").click();

            window.dialog().textBox().deleteText().enterText("Updated");
            window.dialog().button(JButtonMatcher.withText("OK")).click();

            verify(mockController).updateTodoDescription(1L, "Updated");
        }

        @Test
        void editTodo_cancelDoesNothing() {
            Todo todo = createTodo(1L, "Original", false);
            when(mockController.getAllTodos()).thenReturn(List.of(todo));

            frame.refreshTodos();
            window.table("todoTable").selectRows(0);
            window.button("editButton").click();

            window.dialog().button(JButtonMatcher.withText("Cancel")).click();

            verify(mockController, never()).updateTodoDescription(anyLong(), anyString());
        }
        @Test
        void searchTodos_viaButton_filtersResults() {
            when(mockController.searchTodos("Buy")).thenReturn(
                List.of(new Todo("Buy milk"), new Todo("Buy eggs"))
            );

            window.textBox("searchField").enterText("Buy");
            window.button("searchButton").click();

            verify(mockController).searchTodos("Buy");
        }

        @Test
        void searchTodos_viaEnterKey_filtersResults() {
            when(mockController.searchTodos("Test")).thenReturn(List.of());

            window.textBox("searchField").enterText("Test");
            window.textBox("searchField").pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);

            verify(mockController).searchTodos("Test");
        }
    }
    @Nested
    class TagOperations extends TestBase {

        @Test
        void addTag_viaButton_createsTag() {
            when(mockController.addTag(anyString())).thenReturn(new Tag("urgent"));
            when(mockController.getAllTags()).thenReturn(List.of());

            window.textBox("tagNameField").enterText("urgent");
            window.button("addTagButton").click();

            verify(mockController).addTag("urgent");
            window.textBox("tagNameField").requireText("");
        }

        @Test
        void addTag_viaEnterKey_createsTag() {
            when(mockController.addTag(anyString())).thenReturn(new Tag("work"));

            window.textBox("tagNameField").enterText("work");
            window.textBox("tagNameField").pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);

            verify(mockController).addTag("work");
            window.textBox("tagNameField").requireText("");
        }
        
        @Test
        void tagLists_showAvailableAndTodoTags() {
            Tag tag1 = createTag(1L, "urgent");
            Tag tag2 = createTag(2L, "work");
            Todo todo = createTodo(1L, "Task", false);
            todo.addTag(tag1);

            when(mockController.getAllTags()).thenReturn(List.of(tag1, tag2));
            when(mockController.getAllTodos()).thenReturn(List.of(todo));

            frame.refreshTags();
            frame.refreshTodos();
            
            window.table("todoTable").selectRows(0);

            assertThat(window.list("availableTagsList").contents()).hasSize(2);
            assertThat(window.list("tagList").contents()).hasSize(1);
        }

        @Test
        void updateTagList_whenTodoSelected() {
            Tag tag = createTag(1L, "urgent");
            Todo todo = createTodo(1L, "Task", false);
            todo.addTag(tag);

            when(mockController.getAllTodos()).thenReturn(List.of(todo));
            when(mockController.getAllTags()).thenReturn(List.of(tag));

            frame.refreshTodos();
            window.table("todoTable").selectRows(0);

            assertThat(window.list("tagList").contents()).hasSize(1);
            assertThat(window.list("tagList").contents()[0]).contains("urgent");
        }
        @Test
        void addTagToTodo_addsSelectedTagToSelectedTodo() {
            Tag tag = createTag(2L, "urgent");
            Todo todo = createTodo(1L, "Task", false);
            
            when(mockController.getAllTodos()).thenReturn(List.of(todo));
            when(mockController.getAllTags()).thenReturn(List.of(tag));
            when(mockController.addTagToTodo(1L, 2L)).thenReturn(true);

            frame.refreshTodos();
            frame.refreshTags();
            window.table("todoTable").selectRows(0);
            window.list("availableTagsList").selectItem(0);
            window.button("addTagToTodoButton").click();

            verify(mockController).addTagToTodo(1L, 2L);
        }
        @Test
        void removeTagFromTodo_removesSelectedTagFromTodo() {
            Tag tag = createTag(2L, "urgent");
            Todo todo = createTodo(1L, "Task", false);
            todo.addTag(tag);
            
            when(mockController.getAllTodos()).thenReturn(List.of(todo));
            when(mockController.getAllTags()).thenReturn(List.of(tag));
            when(mockController.removeTagFromTodo(1L, 2L)).thenReturn(true);

            frame.refreshTodos();
            frame.refreshTags();
            window.table("todoTable").selectRows(0);
            window.list("tagList").selectItem(0);
            window.button("removeTagFromTodoButton").click();

            verify(mockController).removeTagFromTodo(1L, 2L);
        }
        @Test
        void deleteTag_deletesSelectedTag() {
            Tag tag = createTag(1L, "urgent");
            
            when(mockController.getAllTags()).thenReturn(List.of(tag));
            when(mockController.deleteTag(1L)).thenReturn(true);

            frame.refreshTags();
            window.list("availableTagsList").selectItem(0);
            window.button("deleteTagButton").click();

            verify(mockController).deleteTag(1L);
        }
        @Test
        void addTodo_trimsWhitespace() {
            when(mockController.addTodo("Task")).thenReturn(new Todo("Task"));
            when(mockController.getAllTodos()).thenReturn(List.of());

            window.textBox("todoDescriptionField").enterText("  Task  ");
            window.button("addTodoButton").click();

            verify(mockController).addTodo("Task");
        }

        @Test
        void addTag_trimsWhitespace() {
            when(mockController.addTag("urgent")).thenReturn(new Tag("urgent"));

            window.textBox("tagNameField").enterText("  urgent  ");
            window.button("addTagButton").click();

            verify(mockController).addTag("urgent");
        }

        @Test
        void searchTodos_trimsWhitespace() {
            when(mockController.searchTodos("test")).thenReturn(List.of());

            window.textBox("searchField").enterText("  test  ");
            window.button("searchButton").click();

            verify(mockController).searchTodos("test");
        }
    }
    @Nested
    class ExceptionHandling extends TestBase {

        @Test
        void handleAddTodoFailure_showsErrorDialog() {
            when(mockController.addTodo(anyString())).thenThrow(
                new RuntimeException("Database error")
            );

            window.textBox("todoDescriptionField").enterText("Test");
            window.button("addTodoButton").click();

            window.optionPane().requireErrorMessage().okButton().click();
        }

        @Test
        void handleDeleteTodoFailure_showsErrorDialog() {
            Todo todo = createTodo(1L, "Task", false);
            when(mockController.getAllTodos()).thenReturn(List.of(todo));
            when(mockController.deleteTodo(1L)).thenThrow(
                new RuntimeException("Delete failed")
            );

            frame.refreshTodos();
            window.table("todoTable").selectRows(0);
            window.button("deleteButton").click();

            window.optionPane().requireErrorMessage().okButton().click();
        }
    }
}