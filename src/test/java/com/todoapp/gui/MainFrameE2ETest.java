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
        assertThat(window.table("todoTable").rowCount()).isEqualTo(0);
    }

    private void waitForTableUpdate(int expectedRowCount) {
     }

    private static void configureMongoDB() throws Exception {
     }

    private void cleanDatabase() {
      }

    private static void resetSingletons() throws Exception {
   	}
}