package com.todoapp.gui;

import com.todoapp.model.Todo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String WINDOW_TITLE = "Todo Manager - TDD Development Demo Application";

    // UI Components
    private JTextField todoDescriptionField;
    private JTable todoTable;
    private TodoTableModel todoTableModel;
  
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
        
        todoTableModel = new TodoTableModel();
        todoTable = new JTable(todoTableModel);
        todoTable.setName("todoTable");
        
        todoTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleTodoDone();
                }
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.add(new JLabel("Add Todo:"));
        topPanel.add(todoDescriptionField);
        
        JButton addButton = new JButton("Add Todo");
        addButton.setName("addTodoButton");
        topPanel.add(addButton);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(todoTable), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton deleteButton = new JButton("Delete Todo");
        deleteButton.setName("deleteButton");
        JButton toggleButton = new JButton("Toggle Done");
        toggleButton.setName("toggleDoneButton");
        buttonPanel.add(deleteButton);
        buttonPanel.add(toggleButton);
        
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void setupListeners() {
        findButton("addTodoButton").addActionListener(e -> addTodo());
        findButton("deleteButton").addActionListener(e -> deleteTodo());
        findButton("toggleDoneButton").addActionListener(e -> toggleTodoDone());
        todoDescriptionField.addActionListener(e -> addTodo());
    }

    private JButton findButton(String name) {
        Component topPanel = getContentPane().getComponent(0);
        if (topPanel instanceof JPanel) {
            for (Component comp : ((JPanel) topPanel).getComponents()) {
                if (comp instanceof JButton && name.equals(comp.getName())) {
                    return (JButton) comp;
                }
            }
        }
        
        Component centerPanel = getContentPane().getComponent(1);
        if (centerPanel instanceof JPanel) {
            for (Component comp : ((JPanel) centerPanel).getComponents()) {
                if (comp instanceof JPanel) {
                    for (Component innerComp : ((JPanel) comp).getComponents()) {
                        if (innerComp instanceof JButton && name.equals(innerComp.getName())) {
                            return (JButton) innerComp;
                        }
                    }
                }
            }
        }
        
        return null;
    }

    public void addTodo() {
        String description = todoDescriptionField.getText();
        if (description != null && !description.trim().isEmpty()) {
            controller.addTodo(description.trim());
            todoDescriptionField.setText("");
            refreshTodos();
        }
    }

    public void deleteTodo() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow >= 0) {
            Todo todo = todoTableModel.getTodoAt(selectedRow);
            if (controller.deleteTodo(todo.getId())) {
                refreshTodos();
            }
        }
    }

    public void toggleTodoDone() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow >= 0) {
            Todo todo = todoTableModel.getTodoAt(selectedRow);
            Boolean newStatus = controller.toggleTodoDone(todo.getId());
            if (newStatus != null) {
                refreshTodos();
             
                todoTable.setRowSelectionInterval(selectedRow, selectedRow);
            }
        }
    }

    public void refreshTodos() {
        List<Todo> todos = controller.getAllTodos();
        todoTableModel.setTodos(todos);
    }

    // TodoTableModel 
    static class TodoTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final String[] columnNames = {"ID", "Description", "Done"};
        private List<Todo> todos = new ArrayList<>();

        public void setTodos(List<Todo> todos) {
            this.todos = new ArrayList<>(todos);
            fireTableDataChanged();
        }

        public Todo getTodoAt(int row) {
            return todos.get(row);
        }

        @Override
        public int getRowCount() {
            return todos.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Todo todo = todos.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> todo.getId();
                case 1 -> todo.getDescription();
                case 2 -> todo.isDone();
                default -> null;
            };
        }
    }
}