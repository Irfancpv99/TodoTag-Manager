package com.todoapp.gui;

import com.todoapp.config.AppConfig;
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

import javax.swing.SwingUtilities;
import java.lang.reflect.Field;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MainFrameE2ETest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

    private FrameFixture window;
    private MainFrame frame;
    private Robot robot;
    private static AppConfig appConfig;

    @BeforeAll
    static void setupDatabase() {
        resetSingletons();
        appConfig = createMongoDBConfig();
    }
    
    @BeforeEach
    void setUp() {
        cleanDatabase();
        appConfig = createMongoDBConfig();

        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        robot.settings().delayBetweenEvents(100);
        robot.settings().eventPostingDelay(200);
        
        frame = GuiActionRunner.execute(() -> {
            TodoService service = new TodoService(appConfig);
            MainFrameController controller = new MainFrameController(service);
            return new MainFrame(controller);
        });
        
        window = new FrameFixture(robot, frame);
        window.show();
        
        Pause.pause(1500);
    }
    
    @AfterEach
    void tearDown() {
        if (window != null) {
            try {
                window.cleanUp();
            } catch (Exception ignored) {
                // Cleanup failure acceptable
            }
        }
        if (robot != null) {
            try {
                robot.cleanUp();
            } catch (Exception ignored) {
                // Cleanup failure acceptable
            }
        }
        cleanDatabase();
    }
    
    @Test
    @Order(1)
    @DisplayName("Complete todo lifecycle: add, mark done, delete")
    void todoLifecycle() {
        setTextDirectly("todoDescriptionField", "Buy groceries");
        window.button("addTodoButton").click();
        
        waitForTableUpdate(1);

        assertThat(window.table("todoTable").rowCount()).isEqualTo(1);
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(1)).value())
            .isEqualTo("Buy groceries");
        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
            .isEqualTo("false");

        window.table("todoTable").selectRows(0);
        Pause.pause(500);
        window.button("toggleDoneButton").click();
        Pause.pause(1500);

        assertThat(window.table("todoTable").cell(TableCell.row(0).column(2)).value())
            .isEqualTo("true");

        window.table("todoTable").selectRows(0);
        Pause.pause(500);
        window.button("deleteButton").click();
        Pause.pause(1500);

        waitForTableUpdate(0);
        assertThat(window.table("todoTable").rowCount()).isZero();
    }
    
    @Test
    @Order(2)
    @DisplayName("Complete tag workflow: create, assign, remove")
    void tagWorkflow() {
        setTextDirectly("todoDescriptionField", "Complete project");
        window.button("addTodoButton").click();
        Pause.pause(1500);
        waitForTableUpdate(1);

        setTextDirectly("tagNameField", "urgent");
        window.button("addTagButton").click();
        Pause.pause(1000);
        waitForListUpdate("availableTagsList", 1);

        assertThat(window.list("availableTagsList").contents()).hasSize(1);
        assertThat(window.list("availableTagsList").contents()[0]).contains("urgent");

        window.table("todoTable").selectRows(0);
        Pause.pause(800);
        window.list("availableTagsList").selectItem(0);
        Pause.pause(500);
        window.button("addTagToTodoButton").click();
        Pause.pause(1500);
        waitForListUpdate("tagList", 1);

        assertThat(window.list("tagList").contents()).hasSize(1);
        assertThat(window.list("tagList").contents()[0]).contains("urgent");

        window.list("tagList").selectItem(0);
        Pause.pause(500);
        window.button("removeTagFromTodoButton").click();
        Pause.pause(1500);
        waitForListUpdate("tagList", 0);

        assertThat(window.list("tagList").contents()).isEmpty();

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
        addTodo("Buy groceries");
        addTodo("Buy tickets");
        addTodo("Clean house");

        waitForTableUpdate(3);
        assertThat(window.table("todoTable").rowCount()).isEqualTo(3);

        setTextDirectly("searchField", "Buy");
        window.button("searchButton").click();
        Pause.pause(1000);
        waitForTableUpdate(2);
        assertThat(window.table("todoTable").rowCount()).isEqualTo(2);

        window.button("showAllButton").click();
        Pause.pause(1000);
        waitForTableUpdate(3);
        assertThat(window.table("todoTable").rowCount()).isEqualTo(3);

        window.table("todoTable").selectRows(0);
        Pause.pause(500);
        window.button("editButton").click();
        Pause.pause(800);
        
        try {
            window.dialog().textBox().setText("Updated task");
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> 
                window.dialog().textBox().target().setText("Updated task"));
            Pause.pause(300);
        }
        
        window.dialog().button(withText("OK")).click();
        Pause.pause(1500);
    }
    
    @Test
    @Order(4)
    @DisplayName("Multiple tags on single todo")
    void multipleTagsWorkflow() {
        setTextDirectly("todoDescriptionField", "Important meeting");
        window.button("addTodoButton").click();
        Pause.pause(1500);
        waitForTableUpdate(1);

        addTag("urgent");
        Pause.pause(800);
        addTag("work");
        Pause.pause(800);
        addTag("meeting");
        Pause.pause(1000);
        
        waitForListUpdate("availableTagsList", 3);
        assertThat(window.list("availableTagsList").contents()).hasSize(3);

        window.table("todoTable").selectRows(0);
        Pause.pause(1000);

        window.list("availableTagsList").selectItem(0);
        Pause.pause(500);
        window.button("addTagToTodoButton").click();
        Pause.pause(1500);
        
        waitForListUpdate("tagList", 1);

        window.table("todoTable").selectRows(0);
        Pause.pause(800);
        
        window.list("availableTagsList").selectItem(1);
        Pause.pause(500);
        window.button("addTagToTodoButton").click();
        Pause.pause(1500);
        
        waitForListUpdate("tagList", 2);

        assertThat(window.list("tagList").contents()).hasSize(2);
    }
    
    private void setTextDirectly(String fieldName, String text) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                window.textBox(fieldName).target().setText(text);
            });
            Pause.pause(200);
        } catch (Exception e) {
          
            try {
                window.textBox(fieldName).setText(text);
            } catch (Exception ex) {
                
                window.textBox(fieldName).click();
                window.textBox(fieldName).enterText(text);
            }
        }
    }
    
    private void addTag(String name) {
        setTextDirectly("tagNameField", name);
        window.button("addTagButton").click();
        Pause.pause(800);
    }

    private void addTodo(String description) {
        setTextDirectly("todoDescriptionField", description);
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
                    // Cleanup failure is acceptable
                }
            });
            service.getAllTags().forEach(tag -> {
                try {
                    service.deleteTag(tag.getId());
                } catch (Exception ignored) {
                    // Cleanup failure is acceptable
                }
            });
        } catch (Exception ignored) {
            // Database not ready yet
        }
    }

    private static void resetSingletons() {
        resetField("com.todoapp.repository.RepositoryFactory", "instance");
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
            // Field reset failure is acceptable
        }
    }
}