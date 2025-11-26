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
import java.awt.*;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class MainFrameE2ETest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    private static AppConfig appConfig;
    private FrameFixture window;
    private Robot robot;
    private MainFrame frame;

    @BeforeAll
    static void setupConfig() {
        appConfig = createMongoDBConfig();
    }

    @BeforeEach
    void setUp() {
        cleanDatabase();

        robot = BasicRobot.robotWithCurrentAwtHierarchy();

        frame = GuiActionRunner.execute(() -> {
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
        addTodo("Buy groceries");
        waitForRowCount(1);
        
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(1)).value())
                .isEqualTo("Buy groceries");
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
                .isEqualTo("false");

        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        window.button("toggleDoneButton").click();
        waitForCellValue(0, 2, "true");
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
                .isEqualTo("true");

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

        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        window.button("deleteButton").click();
        waitForRowCount(0);
    }

    @Test
    @DisplayName("Tag Management and Todo-Tag Relationships")
    void testTagManagementWorkflow() {
        addTodo("Important task");
        waitForRowCount(1);

        addTag("urgent");
        addTag("work");
        addTag("important");
        waitForListSize("availableTagsList", 3);

        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        
        window.list("availableTagsList").selectItem(0);
        robot.waitForIdle();
        window.button("addTagToTodoButton").click();
        waitForListSize("tagList", 1);
        assertThat(window.list("tagList").contents()[0]).contains("urgent");

        window.list("availableTagsList").selectItem(1);
        robot.waitForIdle();
        window.button("addTagToTodoButton").click();
        waitForListSize("tagList", 2);
        assertThat(window.list("tagList").contents()).hasSize(2);

        window.list("tagList").selectItem(0);
        robot.waitForIdle();
        window.button("removeTagFromTodoButton").click();
        waitForListSize("tagList", 1);

        window.list("availableTagsList").selectItem(2);
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

        GuiActionRunner.execute(() -> {
            JTextField field = (JTextField) window.textBox("searchField").target();
            field.setText("Buy");
        });
        robot.waitForIdle();
        window.button("searchButton").click();
        waitForRowCount(2);
        
        assertThat(window.table("todoTable").rowCount()).isEqualTo(2);

        window.button("showAllButton").click();
        waitForRowCount(3);
        assertThat(window.table("todoTable").rowCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Double-click toggles todo done status")
    void testDoubleClickToggleTodo() {
        addTodo("Test double-click");
        waitForRowCount(1);
        
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
                .isEqualTo("false");
        
        window.table("todoTable").cell(TableCell.row(0).column(1)).doubleClick();
        waitForCellValue(0, 2, "true");
        
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
                .isEqualTo("true");
    }

    @Test
    @DisplayName("Enter key on text fields triggers appropriate actions")
    void testEnterKeyActions() {
        GuiActionRunner.execute(() -> {
            JTextField todoField = (JTextField) window.textBox("todoDescriptionField").target();
            todoField.setText("Enter key todo");
        });
        robot.waitForIdle();
        window.textBox("todoDescriptionField").pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);
        waitForRowCount(1);
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(1)).value())
                .isEqualTo("Enter key todo");

        GuiActionRunner.execute(() -> {
            JTextField tagField = (JTextField) window.textBox("tagNameField").target();
            tagField.setText("enter-tag");
        });
        robot.waitForIdle();
        window.textBox("tagNameField").pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);
        waitForListSize("availableTagsList", 1);
        
        String tagName = window.list("availableTagsList").contents()[0];
        assertThat(tagName).contains("enter-tag");

        addTodo("Search test todo");
        waitForRowCount(2);
        
        GuiActionRunner.execute(() -> {
            JTextField searchField = (JTextField) window.textBox("searchField").target();
            searchField.setText("Search test");
        });
        robot.waitForIdle();
        window.textBox("searchField").pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);
        waitForRowCount(1);
    }

    @Test
    @DisplayName("Test getText with null handling - coverage for line 407")
    void testGetTextWithNullField() {
        GuiActionRunner.execute(() -> {
            JTextField field = frame.todoDescriptionField;
            field.setText(null);
            String result = frame.getText(field);
            assertThat(result).isEqualTo("");
        });
    }

    @Test
    @DisplayName("Test findButton with non-container components - coverage for line 424")
    void testFindButtonWithNonContainerComponents() {
        GuiActionRunner.execute(() -> {
            JPanel testPanel = new JPanel();
            JLabel label = new JLabel("Test Label");
            testPanel.add(label);
            
            JButton testButton = new JButton("Test");
            testButton.setName("testButton");
            testPanel.add(testButton);
            
            JButton found = frame.findButton(testPanel, "testButton");
            assertThat(found).isNotNull();
            assertThat(found.getText()).isEqualTo("Test");
        });
    }

    @Test
    @DisplayName("Table selection updates todo tags display")
    void testTableSelectionUpdatesTagDisplay() {
        addTodo("Task with tags");
        waitForRowCount(1);
        
        addTag("tag1");
        addTag("tag2");
        waitForListSize("availableTagsList", 2);
        
        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        
        window.list("availableTagsList").selectItem(0);
        robot.waitForIdle();
        window.button("addTagToTodoButton").click();
        waitForListSize("tagList", 1);
        
        window.table("todoTable").target().clearSelection();
        robot.waitForIdle();
        Pause.pause(500);
        
        assertThat(window.list("tagList").contents()).hasSize(0);
        
        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        Pause.pause(500);
        
        assertThat(window.list("tagList").contents()).hasSize(1);
    }

    @Test
    @DisplayName("Refresh todos preserves selection when possible")
    void testRefreshTodosPreservesSelection() {
        addTodo("Todo 1");
        addTodo("Todo 2");
        addTodo("Todo 3");
        waitForRowCount(3);
        
        window.table("todoTable").selectRows(1);
        robot.waitForIdle();
        
        GuiActionRunner.execute(() -> {
            frame.refreshTodos();
        });
        Pause.pause(1000);
        
        assertThat(window.table("todoTable").target().getSelectedRow()).isEqualTo(1);
    }

    @Test
    @DisplayName("Refresh todos clears tags when selection out of bounds")
    void testRefreshTodosClearsTagsWhenSelectionOutOfBounds() {
        addTodo("Single todo");
        waitForRowCount(1);
        
        addTag("test-tag");
        waitForListSize("availableTagsList", 1);
        
        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        
        window.list("availableTagsList").selectItem(0);
        robot.waitForIdle();
        window.button("addTagToTodoButton").click();
        waitForListSize("tagList", 1);
        
        window.table("todoTable").selectRows(0);
        robot.waitForIdle();
        window.button("deleteButton").click();
        waitForRowCount(0);
        
        Pause.pause(500);
        assertThat(window.list("tagList").contents()).hasSize(0);
    }

    @Test
    @DisplayName("Column names are correctly set in table model")
    void testTableModelColumnNames() {
        GuiActionRunner.execute(() -> {
            assertThat(frame.todoTableModel.getColumnName(0)).isEqualTo("ID");
            assertThat(frame.todoTableModel.getColumnName(1)).isEqualTo("Description");
            assertThat(frame.todoTableModel.getColumnName(2)).isEqualTo("Done");
            assertThat(frame.todoTableModel.getColumnCount()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Table model returns correct values for all columns including default case")
    void testTableModelGetValueAt() {
        addTodo("Test todo for columns");
        waitForRowCount(1);
        
        GuiActionRunner.execute(() -> {
            Object idValue = frame.todoTableModel.getValueAt(0, 0);
            Object descValue = frame.todoTableModel.getValueAt(0, 1);
            Object doneValue = frame.todoTableModel.getValueAt(0, 2);
            Object defaultValue = frame.todoTableModel.getValueAt(0, 999);
            
            assertThat(idValue).isNotNull();
            assertThat(descValue).isEqualTo("Test todo for columns");
            assertThat(doneValue).isEqualTo(false);
            assertThat(defaultValue).isNull();
        });
    }

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