package com.todoapp;

import com.todoapp.config.AppConfig;
import com.todoapp.gui.MainFrame;
import com.todoapp.gui.MainFrameController;
import com.todoapp.service.TodoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.SwingUtilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoApplicationTest {

    @Test
    void testMainExecutesWithoutException() {
        try (MockedStatic<SwingUtilities> swingUtilities = mockStatic(SwingUtilities.class);
             MockedConstruction<AppConfig> appConfigMock = mockConstruction(AppConfig.class);
             MockedConstruction<TodoService> todoServiceMock = mockConstruction(TodoService.class);
             MockedConstruction<MainFrameController> controllerMock = mockConstruction(MainFrameController.class);
             MockedConstruction<MainFrame> mainFrameMock = mockConstruction(MainFrame.class)) {

            swingUtilities.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        Runnable runnable = invocation.getArgument(0);
                        runnable.run();
                        return null;
                    });

            TodoApplication.main(new String[]{});

            assertThat(appConfigMock.constructed()).hasSize(1);
            assertThat(todoServiceMock.constructed()).hasSize(1);
            assertThat(controllerMock.constructed()).hasSize(1);
            assertThat(mainFrameMock.constructed()).hasSize(1);

            MainFrame mainFrame = mainFrameMock.constructed().get(0);
            verify(mainFrame).refreshTodos();
            verify(mainFrame).refreshTags();
            verify(mainFrame).setVisible(true);
        }
    }

    @Test
    void testConstructor() {
        TodoApplication app = new TodoApplication();
        assertThat(app).isNotNull();
    }
}