package com.todoapp;

import com.todoapp.gui.MainFrame;

import javax.swing.*;

/**
 * TodoApplication - Swing version main class
 * Java 17 compatible
 */
public class TodoApplication {
    
    public static void main(String[] args) {
        // Set the look and feel to the system's native look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If system L&F fails, continue with default
            System.err.println("Failed to set system look and feel: " + e.getMessage());
        }
        
        // Create and show the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}