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
        robot.waitForIdle();
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
    @DisplayName("Complete Todo CRUD Workflow")
    void testTodoCRUDWorkflow() {
        // CREATE: Add 
        GuiActionRunner.execute(() -> {
            JTextField field = (JTextField) window.textBox("todoDescriptionField").target();
            field.setText("Buy groceries");
        });
        robot.waitForIdle();
        window.button("addTodoButton").click();
        waitForRowCount(1);
        
        // VERIFY:  appears in table
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(1)).value())
                .isEqualTo("Buy groceries");
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
                .isEqualTo("false");

        // UPDATE: Toggle done status
        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        window.button("toggleDoneButton").click();
        waitForCellValue(0, 2, "true");
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
                .isEqualTo("true");

        // UPDATE: Edit description
        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        window.button("editButton").click();
        Pause.pause(1000);
        
        robot.waitForIdle();
        window.dialog().textBox().selectAll();
        window.dialog().textBox().enterText("Buy milk");
        window.dialog().button(buttonWithText("OK")).click();
        
        waitForCondition("Description updated", () -> {
            try {
                robot.waitForIdle();
                String cellValue = window.table("todoTable").cell(TableCell.row(0).column(1)).value();
                return "Buy milk".equals(cellValue);
            } catch (Exception e) {
                return false;
            }
        }, 10000);

        // DELETE: Remove 
        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        window.button("deleteButton").click();
        waitForRowCount(0);
    }

    @Test
    @DisplayName("Tag Management and Todo-Tag Relationships")
    void testTagManagementWorkflow() {
        // Create 
        addTodo("Important task");
        waitForRowCount(1);

        // CREATE: Add multiple tags
        addTag("urgent");
        addTag("work");
        addTag("important");
        waitForListSize("availableTagsList", 3);

        // ASSIGN: Add tags 
        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        
        window.list("availableTagsList").selectItem(0); // urgent
        robot.waitForIdle();
        window.button("addTagToTodoButton").click();
        waitForListSize("tagList", 1);
        assertThat(window.list("tagList").contents()[0]).contains("urgent");

        window.list("availableTagsList").selectItem(1); // work
        robot.waitForIdle();
        window.button("addTagToTodoButton").click();
        waitForListSize("tagList", 2);
        assertThat(window.list("tagList").contents()).hasSize(2);

     
        window.list("tagList").selectItem(0);
        robot.waitForIdle();
        window.button("removeTagFromTodoButton").click();
        waitForListSize("tagList", 1);

        // Delete tag from system
        window.list("availableTagsList").selectItem(2); // important
        robot.waitForIdle();
        window.button("deleteTagButton").click();
        waitForListSize("availableTagsList", 2);
    }

    @Test
    @DisplayName("Search and Filter Functionality")
    void testSearchAndFilterWorkflow() {
       
        addTodo("Buy groceries");
        addTodo("Buy tickets");
        addTodo("Clean house");
        waitForRowCount(3);

        // SEARCH: Filter by keyword
        GuiActionRunner.execute(() -> {
            JTextField field = (JTextField) window.textBox("searchField").target();
            field.setText("Buy");
        });
        robot.waitForIdle();
        window.button("searchButton").click();
        waitForRowCount(2);
        
        // Verify search results
        assertThat(window.table("todoTable").rowCount()).isEqualTo(2);

        // CLEAR
        window.button("showAllButton").click();
        waitForRowCount(3);
        assertThat(window.table("todoTable").rowCount()).isEqualTo(3);
    }

    // Helper methods

    private void addTodo(String description) {
        int currentCount = window.table("todoTable").rowCount();
        int expectedCount = currentCount + 1;
        
        GuiActionRunner.execute(() -> {
            JTextField field = (JTextField) window.textBox("todoDescriptionField").target();
            field.setText(description);
        });
        robot.waitForIdle();
        window.button("addTodoButton").click();
        waitForRowCount(expectedCount);
    }

    private void addTag(String name) {
        int currentCount = window.list("availableTagsList").contents().length;
        int expectedCount = currentCount + 1;
        
        GuiActionRunner.execute(() -> {
            JTextField field = (JTextField) window.textBox("tagNameField").target();
            field.setText(name);
        });
        robot.waitForIdle();
        window.button("addTagButton").click();
        waitForListSize("availableTagsList", expectedCount);
    }

    private void waitForCondition(String description, java.util.function.Supplier<Boolean> condition, int timeoutMs) {
        Pause.pause(new Condition(description) {
            @Override
            public boolean test() {
                return condition.get();
            }
        }, Timeout.timeout(timeoutMs));
    }

    private void waitForRowCount(int expectedCount) {
        Pause.pause(new Condition("Table row count = " + expectedCount) {
            @Override
            public boolean test() {
                try {
                    robot.waitForIdle();
                    return window.table("todoTable").rowCount() == expectedCount;
                } catch (Exception e) {
                    return false;
                }
            }
        }, Timeout.timeout(10000));
    }

    private void waitForCellValue(int row, int column, String expectedValue) {
        Pause.pause(new Condition("Cell (" + row + "," + column + ") = " + expectedValue) {
            @Override
            public boolean test() {
                try {
                    robot.waitForIdle();
                    String value = window.table("todoTable").cell(TableCell.row(row).column(column)).value();
                    return expectedValue.equals(value);
                } catch (Exception e) {
                    return false;
                }
            }
        }, Timeout.timeout(10000));
    }

    private void waitForListSize(String listName, int expectedSize) {
        Pause.pause(new Condition("List '" + listName + "' size = " + expectedSize) {
            @Override
            public boolean test() {
                try {
                    robot.waitForIdle();
                    return window.list(listName).contents().length == expectedSize;
                } catch (Exception e) {
                    return false;
                }
            }
        }, Timeout.timeout(10000));
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

            service.getAllTodos().forEach(todo -> {
                try {
                    service.deleteTodo(todo.getId());
                } catch (Exception ignored) {
                }
            });

            service.getAllTags().forEach(tag -> {
                try {
                    service.deleteTag(tag.getId());
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }
}