package com.todoapp.gui;

import com.todoapp.model.Tag;
import com.todoapp.model.Todo;
import com.todoapp.service.TodoService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    // UI Components
    private JTextField todoDescriptionField;
    private JTextField tagNameField;
    private JTextField searchField;
    private JTable todoTable;
    private JList<Tag> tagList;
    private JList<Tag> availableTagsList;
    
    private JButton addTodoButton;
    private JButton addTagButton;
    private JButton searchButton;
    private JButton showAllButton;
    private JButton deleteButton;
    private JButton editButton;
    private JButton toggleDoneButton;
    private JButton addTagToTodoButton;
    private JButton removeTagFromTodoButton;
    private JButton deleteTagButton;

    // Controller and data models
    private MainFrameController controller;
    private TodoTableModel todoTableModel;
    private DefaultListModel<Tag> tagListModel;
    private DefaultListModel<Tag> availableTagsListModel;

    public MainFrame() {
        super("Todo Manager - TDD Development Demo Application");
        initializeServices();
        initializeComponents();
        setupLayout();
        setupListeners();
        loadInitialData();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private void initializeServices() {
        TodoService todoService = new TodoService();
        controller = new MainFrameController(todoService);
        todoTableModel = new TodoTableModel();
        tagListModel = new DefaultListModel<>();
        availableTagsListModel = new DefaultListModel<>();
    }

    private void initializeComponents() {
        // Text fields
        todoDescriptionField = new JTextField(30);
        todoDescriptionField.setToolTipText("Enter todo description...");
        
        tagNameField = new JTextField(20);
        tagNameField.setToolTipText("Enter tag name...");
        
        searchField = new JTextField(20);
        searchField.setToolTipText("Search todos...");

        // Table
        todoTable = new JTable(todoTableModel);
        todoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todoTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        todoTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        todoTable.getColumnModel().getColumn(2).setPreferredWidth(80);

        // Lists
        tagList = new JList<>(tagListModel);
        tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagList.setCellRenderer(new TagListRenderer());
        
        availableTagsList = new JList<>(availableTagsListModel);
        availableTagsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableTagsList.setCellRenderer(new TagListRenderer());

        // Buttons
        addTodoButton = new JButton("Add Todo");
        addTagButton = new JButton("Add Tag");
        searchButton = new JButton("Search");
        showAllButton = new JButton("Show All");
        deleteButton = new JButton("Delete Todo");
        editButton = new JButton("Edit Todo");
        toggleDoneButton = new JButton("Toggle Done");
        addTagToTodoButton = new JButton("Add Tag to Todo");
        removeTagFromTodoButton = new JButton("Remove Tag from Todo");
        deleteTagButton = new JButton("Delete Selected Tag");
        deleteTagButton.setBackground(new Color(255, 107, 107));
        deleteTagButton.setForeground(Color.WHITE);
        deleteTagButton.setOpaque(true);
        deleteTagButton.setBorderPainted(false);
        deleteTagButton.setFocusPainted(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Top panel with input fields
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel with table and tag management
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with status
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel statusLabel = new JLabel("Todo Manager - TDD Development Demo Application");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC));
        bottomPanel.add(statusLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add Todo section
        JPanel addTodoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addTodoPanel.add(new JLabel("Add Todo:"));
        addTodoPanel.add(todoDescriptionField);
        addTodoPanel.add(addTodoButton);
        topPanel.add(addTodoPanel);

        // Add Tag section
        JPanel addTagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addTagPanel.add(new JLabel("Add Tag:"));
        addTagPanel.add(tagNameField);
        addTagPanel.add(addTagButton);
        topPanel.add(addTagPanel);

        // Search section
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        topPanel.add(searchPanel);

        topPanel.add(new JSeparator());

        return topPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side - Todo table
        JPanel todoPanel = createTodoPanel();
        centerPanel.add(todoPanel, BorderLayout.CENTER);

        // Right side - Tag management
        JPanel tagPanel = createTagPanel();
        centerPanel.add(tagPanel, BorderLayout.EAST);

        return centerPanel;
    }

    private JPanel createTodoPanel() {
        JPanel todoPanel = new JPanel(new BorderLayout(5, 5));

        JLabel label = new JLabel("Todos:");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        todoPanel.add(label, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(todoTable);
        todoPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(toggleDoneButton);
        todoPanel.add(buttonPanel, BorderLayout.SOUTH);

        return todoPanel;
    }

    private JPanel createTagPanel() {
        JPanel tagPanel = new JPanel();
        tagPanel.setLayout(new BoxLayout(tagPanel, BoxLayout.Y_AXIS));
        tagPanel.setPreferredSize(new Dimension(280, 0));

        // Todo Tags section
        JLabel todoTagsLabel = new JLabel("Todo Tags:");
        todoTagsLabel.setFont(todoTagsLabel.getFont().deriveFont(Font.BOLD));
        todoTagsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagPanel.add(todoTagsLabel);
        tagPanel.add(Box.createVerticalStrut(5));

        JScrollPane tagScrollPane = new JScrollPane(tagList);
        tagScrollPane.setPreferredSize(new Dimension(280, 120));
        tagScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagPanel.add(tagScrollPane);
        tagPanel.add(Box.createVerticalStrut(10));

        // Available Tags section
        JLabel availableTagsLabel = new JLabel("All Available Tags:");
        availableTagsLabel.setFont(availableTagsLabel.getFont().deriveFont(Font.BOLD));
        availableTagsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagPanel.add(availableTagsLabel);
        tagPanel.add(Box.createVerticalStrut(5));

        JScrollPane availableTagsScrollPane = new JScrollPane(availableTagsList);
        availableTagsScrollPane.setPreferredSize(new Dimension(280, 120));
        availableTagsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagPanel.add(availableTagsScrollPane);
        tagPanel.add(Box.createVerticalStrut(10));

        // Tag action buttons
        addTagToTodoButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addTagToTodoButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addTagToTodoButton.getPreferredSize().height));
        tagPanel.add(addTagToTodoButton);
        tagPanel.add(Box.createVerticalStrut(5));

        removeTagFromTodoButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        removeTagFromTodoButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, removeTagFromTodoButton.getPreferredSize().height));
        tagPanel.add(removeTagFromTodoButton);
        tagPanel.add(Box.createVerticalStrut(10));

        tagPanel.add(new JSeparator());
        tagPanel.add(Box.createVerticalStrut(5));

        deleteTagButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteTagButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, deleteTagButton.getPreferredSize().height));
        tagPanel.add(deleteTagButton);

        return tagPanel;
    }

    private void setupListeners() {
        addTodoButton.addActionListener(e -> addTodo());
        addTagButton.addActionListener(e -> addTag());
        searchButton.addActionListener(e -> searchTodos());
        showAllButton.addActionListener(e -> showAllTodos());
        editButton.addActionListener(e -> editTodo());
        deleteButton.addActionListener(e -> deleteTodo());
        toggleDoneButton.addActionListener(e -> toggleTodoDone());
        addTagToTodoButton.addActionListener(e -> addTagToTodo());
        removeTagFromTodoButton.addActionListener(e -> removeTagFromTodo());
        deleteTagButton.addActionListener(e -> deleteTag());

        // Table selection listener
        todoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateTodoTags(getSelectedTodo());
            }
        });

        todoTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editTodo();
                }
            }
        });

        // Enter key listeners
        todoDescriptionField.addActionListener(e -> addTodo());
        tagNameField.addActionListener(e -> addTag());
        searchField.addActionListener(e -> searchTodos());
    }

    private void loadInitialData() {
        refreshTodos();
        refreshTags();
    }

    // ============ Public Action Methods ============

    public void addTodo() {
        String description = getTextOrEmpty(todoDescriptionField);
        
        try {
            Todo created = controller.addTodo(description);
            if (created != null) {
                SwingUtilities.invokeLater(() -> {
                    clearField(todoDescriptionField);
                    refreshTodos();
                });
            }
        } catch (Exception e) {
            handleError("Failed to add todo", e);
        }
    }

    public void addTag() {
        String tagName = getTextOrEmpty(tagNameField);
        
        try {
            Tag created = controller.addTag(tagName);
            if (created != null) {
                SwingUtilities.invokeLater(() -> {
                    clearField(tagNameField);
                    refreshTags();
                });
            }
        } catch (Exception e) {
            handleError("Failed to add tag", e);
        }
    }

    public void deleteTodo() {
        Todo selected = getSelectedTodo();
        if (selected == null) return;

        try {
            if (controller.deleteTodo(selected.getId())) {
                SwingUtilities.invokeLater(this::refreshTodos);
            }
        } catch (Exception e) {
            handleError("Failed to delete todo", e);
        }
    }

    public void editTodo() {
        Todo selected = getSelectedTodo();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a todo to edit",
                "No Selection",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String newDescription = (String) JOptionPane.showInputDialog(
            this,
            "Edit todo description:",
            "Edit Todo",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            selected.getDescription()
        );

        if (newDescription == null || newDescription.trim().isEmpty()) {
            return;
        }

        try {
            if (controller.updateTodoDescription(selected.getId(), newDescription)) {
                SwingUtilities.invokeLater(this::refreshTodos);
            }
        } catch (Exception e) {
            handleError("Failed to edit todo", e);
        }
    }

    public void deleteTag() {
        Tag selected = getSelectedTag(availableTagsList);
        if (selected == null) return;

        try {
            if (controller.deleteTag(selected.getId())) {
                SwingUtilities.invokeLater(() -> {
                    refreshTags();
                    refreshTodos();
                });
            }
        } catch (Exception e) {
            handleError("Failed to delete tag", e);
        }
    }

    public void toggleTodoDone() {
        Todo selected = getSelectedTodo();
        if (selected == null) return;

        try {
            Boolean newStatus = controller.toggleTodoDone(selected.getId());
            if (newStatus != null) {
                SwingUtilities.invokeLater(this::refreshTodos);
            }
        } catch (Exception e) {
            handleError("Failed to toggle todo", e);
        }
    }

    public void searchTodos() {
        String keyword = getTextOrEmpty(searchField);

        try {
            List<Todo> results = controller.searchTodos(keyword);
            SwingUtilities.invokeLater(() -> todoTableModel.setTodos(results));
        } catch (Exception e) {
            handleError("Search failed", e);
        }
    }

    public void showAllTodos() {
        SwingUtilities.invokeLater(() -> {
            refreshTodos();
            clearField(searchField);
        });
    }

    public void addTagToTodo() {
        Todo todo = getSelectedTodo();
        Tag tag = getSelectedTag(availableTagsList);
        if (todo == null || tag == null) return;

        try {
            if (controller.addTagToTodo(todo.getId(), tag.getId())) {
                SwingUtilities.invokeLater(() -> {
                    refreshTodos();
                    updateTodoTags(todo);
                });
            }
        } catch (Exception e) {
            handleError("Failed to add tag to todo", e);
        }
    }

    public void removeTagFromTodo() {
        Todo todo = getSelectedTodo();
        Tag tag = getSelectedTag(tagList);
        if (todo == null || tag == null) return;

        try {
            if (controller.removeTagFromTodo(todo.getId(), tag.getId())) {
                SwingUtilities.invokeLater(() -> {
                    refreshTodos();
                    updateTodoTags(todo);
                });
            }
        } catch (Exception e) {
            handleError("Failed to remove tag", e);
        }
    }

    // ============ Helper Methods ============

    private void refreshTodos() {
        try {
            List<Todo> todos = controller.getAllTodos();
            todoTableModel.setTodos(todos);
        } catch (Exception e) {
            handleError("Failed to load todos", e);
        }
    }

    private void refreshTags() {
        try {
            List<Tag> tags = controller.getAllTags();
            availableTagsListModel.clear();
            tags.forEach(availableTagsListModel::addElement);
        } catch (Exception e) {
            handleError("Failed to load tags", e);
        }
    }

    private void updateTodoTags(Todo todo) {
        tagListModel.clear();
        if (todo != null && todo.getTags() != null) {
            todo.getTags().forEach(tagListModel::addElement);
        }
    }

    private String getTextOrEmpty(JTextField field) {
        if (field == null) return "";
        String text = field.getText();
        return text == null ? "" : text.trim();
    }

    private void clearField(JTextField field) {
        if (field != null) field.setText("");
    }

    private Todo getSelectedTodo() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow >= 0) {
            return todoTableModel.getTodoAt(selectedRow);
        }
        return null;
    }

    private Tag getSelectedTag(JList<Tag> list) {
        return list != null ? list.getSelectedValue() : null;
    }

    private void handleError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, 
                message + ": " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE)
        );
    }

    // ============ Table Model ============

    private static class TodoTableModel extends AbstractTableModel {
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
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> Long.class;
                case 1 -> String.class;
                case 2 -> Boolean.class;
                default -> Object.class;
            };
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

    // ============ Custom Renderer for Tags ============

    private static class TagListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Tag tag) {
                setText(tag.getName());
            }
            
            return this;
        }
    }
}