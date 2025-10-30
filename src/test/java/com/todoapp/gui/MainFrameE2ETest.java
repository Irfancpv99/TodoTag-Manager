package com.todoapp.gui;

import com.todoapp.config.AppConfig;
import com.todoapp.config.DatabaseType;
import com.todoapp.service.TodoService;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.timing.Pause;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MainFrameE2ETest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

    private FrameFixture window;
    private MainFrame frame;
    private Robot robot;

    @BeforeAll
    static void setupDatabase() throws Exception {
        resetSingletons();
        configureMongoDB();
    }

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        resetSingletons();
        configureMongoDB();

        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        frame = GuiActionRunner.execute(() -> {
            TodoService service = new TodoService();
            MainFrameController controller = new MainFrameController(service);
            return new MainFrame(controller);
        });
        window = new FrameFixture(robot, frame);
        window.show();
    }

    @AfterEach
    void tearDown() {
        if (window != null) window.cleanUp();
        if (robot != null) robot.cleanUp();
        cleanDatabase();
    }

    @Test
    @Order(1)
    @DisplayName("Complete todo lifecycle: add, mark done, delete")
    void todoLifecycle() {
        window.textBox("todoDescriptionField").enterText("Buy groceries");
        window.button("addTodoButton").click();
        Pause.pause(1000);
        waitForTableUpdate(1);

        assertThat(window.table("todoTable").rowCount()).isEqualTo(1);
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(1)).value())
            .isEqualTo("Buy groceries");
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
            .isEqualTo("false");

        window.table("todoTable").selectRows(0);
        window.button("toggleDoneButton").click();
        Pause.pause(1500);

        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
            .isEqualTo("true");

        // Delete
        window.table("todoTable").selectRows(0);
        window.button("deleteButton").click();
        Pause.pause(1500);

        waitForTableUpdate(0);
        assertThat(window.table("todoTable").rowCount()).isZero();
    }
    
    @Test
    @Order(2)
    @DisplayName("Complete tag workflow: create, assign, remove")
    void tagWorkflow() {
        // Add with extra pause to ensure completion
        window.textBox("todoDescriptionField").enterText("Complete project");
        window.button("addTodoButton").click();
        Pause.pause(1500);
        waitForTableUpdate(1);

        // Add tag with extra pause
        window.textBox("tagNameField").enterText("urgent");
        window.button("addTagButton").click();
        Pause.pause(1000);
        waitForListUpdate("availableTagsList", 1);

        assertThat(window.list("availableTagsList").contents()).hasSize(1);
        assertThat(window.list("availableTagsList").contents()[0]).contains("urgent");

        // Add tag 
        window.table("todoTable").selectRows(0);
        Pause.pause(800);
        window.list("availableTagsList").selectItem(0);
        Pause.pause(500);
        window.button("addTagToTodoButton").click();
        Pause.pause(1500);
        waitForListUpdate("tagList", 1);

        assertThat(window.list("tagList").contents()).hasSize(1);
        assertThat(window.list("tagList").contents()[0]).contains("urgent");

        // Remove tag 
        window.list("tagList").selectItem(0);
        Pause.pause(500);
        window.button("removeTagFromTodoButton").click();
        Pause.pause(1500);
        waitForListUpdate("tagList", 0);

        assertThat(window.list("tagList").contents()).isEmpty();

        // Delete tag
        window.list("availableTagsList").selectItem(0);
        Pause.pause(500);
        window.button("deleteTagButton").click();
        Pause.pause(1500);
        waitForListUpdate("availableTagsList", 0);

        assertThat(window.list("availableTagsList").contents()).isEmpty();
    }
    
    @Test
    @Order(3)
    @DisplayName("Search and edit workflow")
    void searchAndEdit() {
        // Add multiple with delays
        addTodo("Buy groceries");
        addTodo("Buy tickets");
        addTodo("Clean house");
        
        Pause.pause(1500);
        assertThat(window.table("todoTable").rowCount()).isEqualTo(3);

        window.textBox("searchField").enterText("Buy");
        window.button("searchButton").click();
        Pause.pause(1500);

        assertThat(window.table("todoTable").rowCount()).isEqualTo(2);

        // Show all
        window.button("showAllButton").click();
        Pause.pause(1500);

        assertThat(window.table("todoTable").rowCount()).isEqualTo(3);

        // Edit  
        window.table("todoTable").selectRows(0);
        window.button("editButton").click();
        Pause.pause(800);

        window.dialog().textBox().deleteText().enterText("Updated task");
        window.dialog().button(withText("OK")).click();
        Pause.pause(1500);
    }
    
    @Test
    @Order(4)
    @DisplayName("Multiple tags on single todo")
    void multipleTagsWorkflow() {
        // Add 
        window.textBox("todoDescriptionField").enterText("Important meeting");
        window.button("addTodoButton").click();
        Pause.pause(1200);

        // Add multiple tags
        addTag("urgent");
        addTag("work");
        addTag("meeting");
        
        Pause.pause(800);
        assertThat(window.list("availableTagsList").contents()).hasSize(3);

        // Select 
        window.table("todoTable").selectRows(0);
        Pause.pause(800);

        // Add first tag
        window.list("availableTagsList").selectItem(0);
        window.button("addTagToTodoButton").click();
        Pause.pause(1200);

        // Add second tag
        window.list("availableTagsList").selectItem(1);
        window.button("addTagToTodoButton").click();
        Pause.pause(1200);

        assertThat(window.list("tagList").contents()).hasSize(2);
    }
    
    private void addTag(String name) {
        window.textBox("tagNameField").enterText(name);
        window.button("addTagButton").click();
        Pause.pause(800);
    }

    private void addTodo(String description) {
        window.textBox("todoDescriptionField").enterText(description);
        window.button("addTodoButton").click();
        Pause.pause(800);
    }

    private void waitForTableUpdate(int expectedRowCount) {
        org.assertj.swing.timing.Timeout timeout = org.assertj.swing.timing.Timeout.timeout(10000);
        org.assertj.swing.timing.Condition condition = new org.assertj.swing.timing.Condition(
            "Table row count to be " + expectedRowCount) {
            @Override
            public boolean test() {
                return window.table("todoTable").rowCount() == expectedRowCount;
            }
        };
        Pause.pause(condition, timeout);
    }

    private void waitForListUpdate(String listName, int expectedSize) {
        org.assertj.swing.timing.Timeout timeout = org.assertj.swing.timing.Timeout.timeout(10000);
        org.assertj.swing.timing.Condition condition = new org.assertj.swing.timing.Condition(
            "List '" + listName + "' size to be " + expectedSize) {
            @Override
            public boolean test() {
                return window.list(listName).contents().length == expectedSize;
            }
        };
        Pause.pause(condition, timeout);
    }

    private static void configureMongoDB() throws Exception {
        AppConfig config = AppConfig.getInstance();
        config.setDatabaseType(DatabaseType.MONGODB);

        Field propsField = AppConfig.class.getDeclaredField("properties");
        propsField.setAccessible(true);
        java.util.Properties props = (java.util.Properties) propsField.get(config);

        String connectionString = mongoDBContainer.getReplicaSetUrl();
        String[] parts = connectionString.replace("mongodb://", "").split(":");
        String host = parts[0];
        String port = parts[1].split("/")[0];

        props.setProperty("mongodb.host", host);
        props.setProperty("mongodb.port", port);
        props.setProperty("mongodb.database", "e2e_test");
    }

    private void cleanDatabase() {
        try {
            TodoService service = new TodoService();
            service.getAllTodos().forEach(todo -> {
                try {
                    service.deleteTodo(todo.getId());
                } catch (Exception ignored) {
                    // Intentionally ignored - best effort cleanup
                }
            });
            service.getAllTags().forEach(tag -> {
                try {
                    service.deleteTag(tag.getId());
                } catch (Exception ignored) {
                    // Intentionally ignored - best effort cleanup
               }
            });
        } catch (Exception ignored) {
            // Intentionally ignored - cleanup failure should not fail test
         }
    }

    private static void resetSingletons() {
        resetField("com.todoapp.config.AppConfig", "instance");
        resetField("com.todoapp.repository.RepositoryFactory", "instance");
        resetField("com.todoapp.config.DatabaseManager", "instance");
    }
    
    private org.assertj.swing.core.GenericTypeMatcher<javax.swing.JButton> withText(String text) {
        return new org.assertj.swing.core.GenericTypeMatcher<>(javax.swing.JButton.class) {
            @Override
            protected boolean isMatching(javax.swing.JButton button) {
                return text.equals(button.getText());
            }
        };
    }

    private static void resetField(String className, String fieldName) {
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception ignored) {
            // Intentionally ignored - field may not exist or be accessible
             }
    }
}