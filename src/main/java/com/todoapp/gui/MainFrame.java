package com.todoapp.gui;

import com.todoapp.model.Todo;
import com.todoapp.model.Tag;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String WINDOW_TITLE = "Todo Manager - TDD Development Demo Application";
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(MainFrame.class.getName());

    
    // UI Components
    private JTextField todoDescriptionField;
    private JTextField searchField;
    private JTextField tagNameField;
    JTable todoTable;
    private TodoTableModel todoTableModel;
    
    // Tag components
    private JList<Tag> tagList;
    private JList<Tag> availableTagsList;
    private DefaultListModel<Tag> tagListModel;
    private DefaultListModel<Tag> availableTagsListModel;
  
    private transient MainFrameController controller;

    public MainFrame(MainFrameController controller) {
        super(WINDOW_TITLE);
        this.controller = controller;
        
        initializeComponents();
        setupLayout();
        setupListeners();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        todoDescriptionField = new JTextField(30);
        todoDescriptionField.setName("todoDescriptionField");
        
        searchField = new JTextField(20);
        searchField.setName("searchField");
        
        tagNameField = new JTextField(20);
        tagNameField.setName("tagNameField");
        
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
        
        // Initialize tag components
        tagListModel = new DefaultListModel<>();
        availableTagsListModel = new DefaultListModel<>();
        tagList = createList(tagListModel, "tagList");
        availableTagsList = createList(availableTagsListModel, "availableTagsList");
    }

    private JList<Tag> createList(DefaultListModel<Tag> model, String name) {
        JList<Tag> list = new JList<>(model);
        list.setName(name);
        return list;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Create top panel with vertical layout
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        // Add  input row
        topPanel.add(createInputRow("Add Todo:", todoDescriptionField, "addTodoButton", "Add Todo"));
        
        // Add tag input row
        topPanel.add(createInputRow("Add Tag:", tagNameField, "addTagButton", "Add Tag"));
        
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
        
        // Center panel with table and tags
        JPanel centerPanel = createCenterPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(createTodoPanel(), BorderLayout.CENTER);
        panel.add(createTagPanel(), BorderLayout.EAST);
        return panel;
    }

    private JPanel createTodoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(todoTable), BorderLayout.CENTER);
        
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
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTagPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(280, 0));
        addTagSection(panel, "Todo Tags:", tagList);
        panel.add(Box.createVerticalStrut(10));
        addTagSection(panel, "All Available Tags:", availableTagsList);
        
        // Add buttons to tag panel
        panel.add(Box.createVerticalStrut(10));
        addAlignedButton(panel, "Add Tag to Todo", "addTagToTodoButton");
        addAlignedButton(panel, "Remove Tag from Todo", "removeTagFromTodoButton");
        
        // Add separator and button to tag panel
        panel.add(Box.createVerticalStrut(5));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(5));
        addAlignedButton(panel, "Delete Selected Tag", "deleteTagButton");
        
        return panel;
    }

    private void addTagSection(JPanel parent, String title, JList<Tag> list) {
        JLabel label = createBoldLabel(title);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(label);
        parent.add(Box.createVerticalStrut(5));
        
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(280, 120));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(scrollPane);
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private void addAlignedButton(JPanel parent, String text, String name) {
        JButton button = new JButton(text);
        button.setName(name);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        parent.add(button);
        parent.add(Box.createVerticalStrut(5));
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
        findButton("addTagButton").addActionListener(e -> addTag());
        findButton("addTagToTodoButton").addActionListener(e -> addTagToTodo());
        findButton("removeTagFromTodoButton").addActionListener(e -> removeTagFromTodo());
        findButton("deleteTagButton").addActionListener(e -> deleteTag());
        todoDescriptionField.addActionListener(e -> addTodo());
        searchField.addActionListener(e -> searchTodos());
        tagNameField.addActionListener(e -> addTag());
        todoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Todo selected = getSelectedTodo();
                updateTodoTags(selected);
            }
        });
    }

    private JButton findButton(String name) {
        Component component = findComponentByName(this, name);
        if (component instanceof JButton button) {
            return button;
        }
        throw new IllegalStateException("Button not found: " + name);
    }

    private Component findComponentByName(Container container, String name) {
        if (name.equals(container.getName())) {
            return container;
        }
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container containerComponent) {
                Component found = findComponentByName(containerComponent, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public void addTodo() {
        executeAction(() -> {
            String description = getTextOrEmpty(todoDescriptionField);
            if (description.isEmpty()) {
                SwingUtilities.invokeLater(() -> showMessage(
                    "Description cannot be empty", "Invalid Input", JOptionPane.WARNING_MESSAGE
                ));
                return;
            }
            
            Todo todo = controller.addTodo(description);
            if (todo != null) {
                SwingUtilities.invokeLater(() -> {
                    refreshTodos();
                    todoDescriptionField.setText("");
                });
            }
        }, "Failed to add todo");
    }

    public void addTag() {
        executeAction(() -> {
            String tagName = getTextOrEmpty(tagNameField);
            if (tagName.isEmpty()) {
                SwingUtilities.invokeLater(() -> showMessage(
                    "Tag name cannot be empty", "Invalid Input", JOptionPane.WARNING_MESSAGE
                ));
                return;
            }
            
            Tag tag = controller.addTag(tagName);
            if (tag != null) {
                SwingUtilities.invokeLater(() -> {
                    refreshTags();
                    tagNameField.setText("");
                });
            }
        }, "Failed to add tag");
    }

    public void addTagToTodo() {
        executeAction(() -> {
            Todo todo = getSelectedTodo();
            Tag tag = availableTagsList.getSelectedValue();
            
            if (todo == null || tag == null) {
                SwingUtilities.invokeLater(() -> showMessage(
                    "Please select both a todo and a tag", "Invalid Selection", JOptionPane.WARNING_MESSAGE
                ));
                return;
            }
            
            if (controller.addTagToTodo(todo.getId(), tag.getId())) {
                SwingUtilities.invokeLater(() -> {
                    refreshTags();
                    refreshTodos();
                    for (int i = 0; i < todoTableModel.getRowCount(); i++) {
                        if (todoTableModel.getTodoAt(i).getId().equals(todo.getId())) {
                            todoTable.setRowSelectionInterval(i, i);
                            updateTodoTags(todoTableModel.getTodoAt(i));
                            break;
                        }
                    }
                });
            }
        }, "Failed to add tag to todo");
    }

    public void removeTagFromTodo() {
        Todo todo = getSelectedTodo();
        Tag tag = tagList.getSelectedValue();
        
        if (todo == null || tag == null) {
            SwingUtilities.invokeLater(() -> showMessage(
                "Please select both a todo and a tag to remove", "Invalid Selection", JOptionPane.WARNING_MESSAGE
            ));
            return;
        }

        if (controller.removeTagFromTodo(todo.getId(), tag.getId())) {
            SwingUtilities.invokeLater(() -> {
                refreshTags();
                refreshTodos();
                for (int i = 0; i < todoTableModel.getRowCount(); i++) {
                    if (todoTableModel.getTodoAt(i).getId().equals(todo.getId())) {
                        todoTable.setRowSelectionInterval(i, i);
                        updateTodoTags(todoTableModel.getTodoAt(i));
                        break;
                    }
                }
            });
        }
    }

    public void deleteTag() {
        executeAction(() -> {
            Tag selected = availableTagsList.getSelectedValue();
            if (selected == null) {
                return;
            }
            if (controller.deleteTag(selected.getId())) {
                SwingUtilities.invokeLater(() -> {
                    refreshTags();
                    refreshTodos(); 
                    tagListModel.clear();
                });
            }
        }, "Failed to delete tag");
    }

    public void editTodo() {
        executeAction(() -> {
            int selectedRow = todoTable.getSelectedRow();
            if (selectedRow < 0) {
                SwingUtilities.invokeLater(() -> showMessage(
                    "Please select a todo to edit", "No Selection", JOptionPane.INFORMATION_MESSAGE
                ));
                return;
            }
            Todo todo = todoTableModel.getTodoAt(selectedRow);
            String newDescription = (String) JOptionPane.showInputDialog(
                this, "Edit todo description:", "Edit Todo",
                JOptionPane.PLAIN_MESSAGE, null, null, todo.getDescription()
            );
            if (newDescription != null && !newDescription.trim().isEmpty() && controller.updateTodoDescription(todo.getId(), newDescription)) {
                SwingUtilities.invokeLater(() -> {
                    refreshTodos();
                    todoTable.setRowSelectionInterval(selectedRow, selectedRow);
                });
            }
        }, "Failed to edit todo");
    }

    public void deleteTodo() {
        executeAction(() -> {
            Todo selected = getSelectedTodo();
            if (selected == null) return;
            if (controller.deleteTodo(selected.getId())) {
                SwingUtilities.invokeLater(() -> {
                    refreshTodos();
                    tagListModel.clear();
                });
            }
        }, "Failed to delete todo");
    }

    public void toggleTodoDone() {
        executeAction(() -> {
            int selectedRow = todoTable.getSelectedRow();
            if (selectedRow >= 0) {
                Todo todo = todoTableModel.getTodoAt(selectedRow);
                Boolean newStatus = controller.toggleTodoDone(todo.getId());
                if (newStatus != null) {
                    SwingUtilities.invokeLater(() -> {
                        refreshTodos();
                        todoTable.setRowSelectionInterval(selectedRow, selectedRow);
                    });
                }
            }
        }, "Failed to toggle todo status");
    }

    public void searchTodos() {
        executeAction(() -> {
            String keyword = getTextOrEmpty(searchField);
            List<Todo> results = controller.searchTodos(keyword);
            SwingUtilities.invokeLater(() -> todoTableModel.setTodos(results));
        }, "Failed to search todos");
    }

    public void showAllTodos() {
        executeAction(() -> SwingUtilities.invokeLater(() -> {
            refreshTodos();
            searchField.setText("");
        }), "Failed to show all todos");
    }

    public void refreshTodos() {
        int selectedRow = todoTable.getSelectedRow();
        List<Todo> todos = controller.getAllTodos();
        todoTableModel.setTodos(todos);
        
        // Restore selection if it was valid
        if (selectedRow >= 0 && selectedRow < todos.size()) {
            todoTable.setRowSelectionInterval(selectedRow, selectedRow);
            updateTodoTags(todoTableModel.getTodoAt(selectedRow));
        } else {
            updateTodoTags(null);
        }
    }

    public void refreshTags() {
        List<Tag> tags = controller.getAllTags();
        availableTagsListModel.clear();
        tags.forEach(availableTagsListModel::addElement);
    }

    private void updateTodoTags(Todo todo) {
        tagListModel.clear();
        if (todo != null && todo.getTags() != null) {
            todo.getTags().forEach(tagListModel::addElement);
        }
    }

    private void executeAction(Runnable action, String errorMessage) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.log(java.util.logging.Level.SEVERE, errorMessage, e);
            SwingUtilities.invokeLater(() -> showMessage(
                errorMessage + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
            ));
        }
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private String getTextOrEmpty(JTextField textField) {
        String text = textField.getText();
        return (text != null) ? text.trim() : "";
    }

    public Todo getSelectedTodo() {
        int selectedRow = todoTable.getSelectedRow();
        return selectedRow >= 0 ? todoTableModel.getTodoAt(selectedRow) : null;
    }

    // TodoTable-Model 
    static class TodoTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final String[] columnNames = {"ID", "Description", "Done"};
        private transient List<Todo> todos = new ArrayList<>();

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