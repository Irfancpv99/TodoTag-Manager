package com.todoapp.gui;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.BasicRobot;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
}