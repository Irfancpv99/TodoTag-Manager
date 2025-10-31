package com.todoapp;

import com.todoapp.config.AppConfig;
import com.todoapp.gui.MainFrame;
import com.todoapp.gui.MainFrameController;
import com.todoapp.service.TodoService;

import javax.swing.SwingUtilities;

public class TodoApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppConfig config = new AppConfig();
             
            TodoService todoService = new TodoService(config);
            
            MainFrameController controller = new MainFrameController(todoService);
            MainFrame mainFrame = new MainFrame(controller);
            
            mainFrame.refreshTodos();
            mainFrame.refreshTags();
            mainFrame.setVisible(true);
        });
    }
}