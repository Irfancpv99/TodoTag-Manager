package com.todoapp.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String WINDOW_TITLE = "Todo Manager - TDD Development Demo Application";

    // UI Components
    private JTextField todoDescriptionField;
    private JTable todoTable;
    
    // Controller and data models
    private MainFrameController controller;

    public MainFrame(MainFrameController controller) {
        super(WINDOW_TITLE);
        this.controller = controller;
        
        initializeComponents();
        setupLayout();
        setupListeners();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        todoDescriptionField = new JTextField(30);
        todoDescriptionField.setName("todoDescriptionField");
        
        todoTable = new JTable();
        todoTable.setName("todoTable");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.add(new JLabel("Add Todo:"));
        topPanel.add(todoDescriptionField);
        
        JButton addButton = new JButton("Add Todo");
        addButton.setName("addTodoButton");
        topPanel.add(addButton);
        
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(todoTable), BorderLayout.CENTER);
    }

    private void setupListeners() {
        findButton("addTodoButton").addActionListener(e -> addTodo());
    }

    private JButton findButton(String name) {
        for (Component comp : ((JPanel)getContentPane().getComponent(0)).getComponents()) {
            if (comp instanceof JButton && name.equals(comp.getName())) {
                return (JButton) comp;
            }
        }
        return null;
    }

    public void addTodo() {
        String description = todoDescriptionField.getText();
        if (description != null && !description.trim().isEmpty()) {
            controller.addTodo(description.trim());
            todoDescriptionField.setText("");
        }
    }
}