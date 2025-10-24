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
    private JTextField searchField;
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
        
        searchField = new JTextField(20);
        searchField.setName("searchField");
        
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
        
        // Create top panel with vertical layout
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        // Add todo input row
        topPanel.add(createInputRow("Add Todo:", todoDescriptionField, "addTodoButton", "Add Todo"));
        
        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        searchButton.setName("searchButton");
        searchPanel.add(searchButton);
        JButton showAllButton = new JButton("Show All");
        showAllButton.setName("showAllButton");
        searchPanel.add(showAllButton);
        topPanel.add(searchPanel);
        
        topPanel.add(new JSeparator());
        
        // Center panel with table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(todoTable), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editButton = new JButton("Edit Todo");
        editButton.setName("editButton");
        JButton deleteButton = new JButton("Delete Todo");
        deleteButton.setName("deleteButton");
        JButton toggleButton = new JButton("Toggle Done");
        toggleButton.setName("toggleDoneButton");
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(toggleButton);
        
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createInputRow(String labelText, JTextField textField, String buttonName, String buttonText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.add(new JLabel(labelText));
        panel.add(textField);
        JButton button = new JButton(buttonText);
        button.setName(buttonName);
        panel.add(button);
        return panel;
    }

    private void setupListeners() {
        findButton("addTodoButton").addActionListener(e -> addTodo());
        findButton("editButton").addActionListener(e -> editTodo());
        findButton("deleteButton").addActionListener(e -> deleteTodo());
        findButton("toggleDoneButton").addActionListener(e -> toggleTodoDone());
        findButton("searchButton").addActionListener(e -> searchTodos());
        findButton("showAllButton").addActionListener(e -> showAllTodos());
        todoDescriptionField.addActionListener(e -> addTodo());
        searchField.addActionListener(e -> searchTodos());
    }

    private JButton findButton(String name) {
        return findButtonInContainer(getContentPane(), name);
    }

    private JButton findButtonInContainer(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton && name.equals(comp.getName())) {
                return (JButton) comp;
            }
            if (comp instanceof Container) {
                JButton button = findButtonInContainer((Container) comp, name);
                if (button != null) {
                    return button;
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

    public void editTodo() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a todo to edit", 
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Todo todo = todoTableModel.getTodoAt(selectedRow);
        String newDescription = (String) JOptionPane.showInputDialog(
            this, "Edit todo description:", "Edit Todo",
            JOptionPane.PLAIN_MESSAGE, null, null, todo.getDescription()
        );
        if (newDescription != null && !newDescription.trim().isEmpty()) {
            if (controller.updateTodoDescription(todo.getId(), newDescription)) {
                refreshTodos();
                todoTable.setRowSelectionInterval(selectedRow, selectedRow);
            }
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

    public void searchTodos() {
        String keyword = searchField.getText();
        if (keyword == null) {
            keyword = "";
        }
        keyword = keyword.trim();
        List<Todo> results = controller.searchTodos(keyword);
        todoTableModel.setTodos(results);
    }

    public void showAllTodos() {
        refreshTodos();
        searchField.setText("");
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