package com.todoapp.gui;

import com.todoapp.config.AppConfig;
import com.todoapp.service.TodoService;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.swing.*;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class MainFrameE2ETest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    private static AppConfig appConfig;
    private FrameFixture window;
    private Robot robot;

    @BeforeAll
    static void setupConfig() {
        appConfig = createMongoDBConfig();
    }

    @BeforeEach
    void setUp() {
        cleanDatabase();

        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        
        MainFrame frame = GuiActionRunner.execute(() -> {
            TodoService service = new TodoService(appConfig);
            MainFrameController controller = new MainFrameController(service);
            return new MainFrame(controller);
        });
        
        window = new FrameFixture(robot, frame);
        window.show();
    }

    @AfterEach
    void tearDown() {
        if (window != null) {
            window.cleanUp();
        }
        if (robot != null) {
            robot.cleanUp();
        }
    }

    @Test
    @DisplayName("Add todo and verify it appears in table")
    void testAddTodo() {
        // Add 
        window.textBox("todoDescriptionField").enterText("Buy groceries");
        window.button("addTodoButton").click();

        // Verify it appears in table
        waitForRowCount(1);
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(1)).value())
                .isEqualTo("Buy groceries");
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
                .isEqualTo("false");
    }

    @Test
    @DisplayName("Toggle todo done status")
    void testToggleTodoDone() {
        // Add  
        addTodo("Complete assignment");
        window.table("todoTable").selectRows(0);

        // Toggle done
        window.button("toggleDoneButton").click();

        // Verify status changed
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
                .isEqualTo("true");
    }

    @Test
    @DisplayName("Delete todo")
    void testDeleteTodo() {
        // Add 
        addTodo("Task to delete");
        waitForRowCount(1);

        // Delete it
        window.table("todoTable").selectRows(0);
        window.button("deleteButton").click();

        // Verify deleted
        waitForRowCount(0);
    }

    @Test
    @DisplayName("Create tag and add to todo")
    void testTagWorkflow() {
        // Add 
        addTodo("Tagged task");
        waitForRowCount(1);

        // Add tag
        addTag("urgent");
        waitForListSize("availableTagsList", 1);

        // Add tag  
        window.table("todoTable").selectRows(0);
        window.list("availableTagsList").selectItem(0);
        window.button("addTagToTodoButton").click();

        // Verify tag appears in todo's tags
        waitForListSize("tagList", 1);
        assertThat(window.list("tagList").contents()[0]).contains("urgent");
    }

    @Test
    @DisplayName("Remove tag from todo")
    void testRemoveTagFromTodo() {
        // Setup: Add   tag
        addTodo("Task with tag");
        addTag("removable");
        
        window.table("todoTable").selectRows(0);
        window.list("availableTagsList").selectItem(0);
        window.button("addTagToTodoButton").click();
        waitForListSize("tagList", 1);

        // Remove tag
        window.list("tagList").selectItem(0);
        window.button("removeTagFromTodoButton").click();

        // Verify removed
        waitForListSize("tagList", 0);
    }

    @Test
    @DisplayName("Delete tag")
    void testDeleteTag() {
        // Add tag
        addTag("deletable");
        waitForListSize("availableTagsList", 1);

        // Delete it
        window.list("availableTagsList").selectItem(0);
        window.button("deleteTagButton").click();

        // Verify deleted
        waitForListSize("availableTagsList", 0);
    }

    @Test
    @DisplayName("Search todos")
    void testSearchTodos() {
        // Add multiple 
        addTodo("Buy groceries");
        addTodo("Buy tickets");
        addTodo("Clean house");
        waitForRowCount(3);

        // Search for "Buy"
        window.textBox("searchField").enterText("Buy");
        window.button("searchButton").click();
        waitForRowCount(2);

        // Show all
        window.button("showAllButton").click();
        waitForRowCount(3);
    }

    @Test
    @DisplayName("Edit todo description")
    void testEditTodo() {
        // Add 
        addTodo("Original description");
        waitForRowCount(1);

        // Select and edit
        window.table("todoTable").selectRows(0);
        window.button("editButton").click();

        // Handle dialog with proper wait
        Pause.pause(300);
        window.dialog().textBox().deleteText().enterText("Updated description");
        window.dialog().button(buttonWithText("OK")).click();

        // Verify updated
        Pause.pause(500);
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(1)).value())
                .isEqualTo("Updated description");
    }

    @Test
    @DisplayName("Add multiple tags to single todo")
    void testMultipleTags() {
        // Add 
        addTodo("Multi-tagged task");

        // Add multiple tags
        addTag("urgent");
        addTag("work");
        addTag("important");
        waitForListSize("availableTagsList", 3);

        // Select  and add two tags
        window.table("todoTable").selectRows(0);
        
        window.list("availableTagsList").selectItem(0);
        window.button("addTagToTodoButton").click();
        waitForListSize("tagList", 1);

        window.list("availableTagsList").selectItem(1);
        window.button("addTagToTodoButton").click();
        waitForListSize("tagList", 2);

        // Verify both tags present
        assertThat(window.list("tagList").contents()).hasSize(2);
    }

    // Helper methods

    private void addTodo(String description) {
        window.textBox("todoDescriptionField").deleteText(); 
        window.textBox("todoDescriptionField").enterText(description);
        window.button("addTodoButton").click();
        Pause.pause(200);
    }

    private void addTag(String name) {
        window.textBox("tagNameField").deleteText();
        window.textBox("tagNameField").enterText(name);
        window.button("addTagButton").click();
        Pause.pause(200);
    }
    
    private void waitForRowCount(int expectedCount) {
        Pause.pause(new Condition("Table row count = " + expectedCount) {
            @Override
            public boolean test() {
                return window.table("todoTable").rowCount() == expectedCount;
            }
        }, Timeout.timeout(5000));
    }

    private void waitForListSize(String listName, int expectedSize) {
        Pause.pause(new Condition("List '" + listName + "' size = " + expectedSize) {
            @Override
            public boolean test() {
                return window.list(listName).contents().length == expectedSize;
            }
        }, Timeout.timeout(5000));
    }

    private GenericTypeMatcher<JButton> buttonWithText(String text) {
        return new GenericTypeMatcher<>(JButton.class) {
            @Override
            protected boolean isMatching(JButton button) {
                return text.equals(button.getText());
            }
        };
    }

    private static AppConfig createMongoDBConfig() {
        Properties props = new Properties();
        props.setProperty("database.type", "MONGODB");

        String connectionString = mongoDBContainer.getReplicaSetUrl();
        String[] parts = connectionString.replace("mongodb://", "").split(":");
        String host = parts[0];
        String port = parts[1].split("/")[0];

        props.setProperty("mongodb.host", host);
        props.setProperty("mongodb.port", port);
        props.setProperty("mongodb.database", "e2e_test");

        return new AppConfig(props);
    }

    private void cleanDatabase() {
        try {
            TodoService service = new TodoService(appConfig);
            
            // Delete all 
            service.getAllTodos().forEach(todo -> {
                try {
                    service.deleteTodo(todo.getId());
                } catch (Exception ignored) {
                    // Ignore deletion errors during cleanup
                }
            });

            // Delete all tags
            service.getAllTags().forEach(tag -> {
                try {
                    service.deleteTag(tag.getId());
                } catch (Exception ignored) {
                    // Ignore deletion errors during cleanup
                }
            });
        } catch (Exception ignored) {
            // If cleanup fails, tests will still run with dirty data
        }
    }
}