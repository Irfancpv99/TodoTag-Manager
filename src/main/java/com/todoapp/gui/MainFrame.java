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

    // UI Components - package-private for testing
    JTextField todoDescriptionField;
    JTextField searchField;
    JTextField tagNameField;
    JTable todoTable;
    TodoTableModel todoTableModel;
    JList<Tag> tagList;
    JList<Tag> availableTagsList;
    DefaultListModel<Tag> tagListModel;
    DefaultListModel<Tag> availableTagsListModel;
  
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
        // Text fields
        todoDescriptionField = new JTextField(30);
        todoDescriptionField.setName("todoDescriptionField");
        searchField = new JTextField(20);
        searchField.setName("searchField");
        tagNameField = new JTextField(20);
        tagNameField.setName("tagNameField");
        
        // Table
        todoTableModel = new TodoTableModel();
        todoTable = new JTable(todoTableModel);
        todoTable.setName("todoTable");
        
        // Double-click listener for toggle
        todoTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleTodoDone();
                }
            }
        });
        
        // Lists
        tagListModel = new DefaultListModel<>();
        availableTagsListModel = new DefaultListModel<>();
        tagList = new JList<>(tagListModel);
        tagList.setName("tagList");
        availableTagsList = new JList<>(availableTagsListModel);
        availableTagsList.setName("availableTagsList");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(createTodoInputPanel());
        topPanel.add(createTagInputPanel());
        topPanel.add(createSearchPanel());
        
        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(createTodoPanel(), BorderLayout.CENTER);
        centerPanel.add(createTagPanel(), BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createTodoInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Add Todo:"));
        panel.add(todoDescriptionField);
        JButton btn = new JButton("Add Todo");
        btn.setName("addTodoButton");
        panel.add(btn);
        return panel;
    }

    private JPanel createTagInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Add Tag:"));
        panel.add(tagNameField);
        JButton btn = new JButton("Add Tag");
        btn.setName("addTagButton");
        panel.add(btn);
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        
        JButton searchBtn = new JButton("Search");
        searchBtn.setName("searchButton");
        panel.add(searchBtn);
        
        JButton showAllBtn = new JButton("Show All");
        showAllBtn.setName("showAllButton");
        panel.add(showAllBtn);
        
        return panel;
    }

    private JPanel createTodoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(todoTable), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton editBtn = new JButton("Edit Todo");
        editBtn.setName("editButton");
        buttonPanel.add(editBtn);
        
        JButton deleteBtn = new JButton("Delete Todo");
        deleteBtn.setName("deleteButton");
        buttonPanel.add(deleteBtn);
        
        JButton toggleBtn = new JButton("Toggle Done");
        toggleBtn.setName("toggleDoneButton");
        buttonPanel.add(toggleBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTagPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(280, 0));
        
        // Main section
        panel.add(new JLabel("Todo Tags:"));
        JScrollPane todoTagsScroll = new JScrollPane(tagList);
        todoTagsScroll.setPreferredSize(new Dimension(280, 120));
        panel.add(todoTagsScroll);
        panel.add(Box.createVerticalStrut(10));
        
        // Available tags section
        panel.add(new JLabel("All Available Tags:"));
        JScrollPane availableTagsScroll = new JScrollPane(availableTagsList);
        availableTagsScroll.setPreferredSize(new Dimension(280, 120));
        panel.add(availableTagsScroll);
        panel.add(Box.createVerticalStrut(10));
        
        // Buttons
        JButton addTagBtn = new JButton("Add Tag to Todo");
        addTagBtn.setName("addTagToTodoButton");
        panel.add(addTagBtn);
        
        JButton removeTagBtn = new JButton("Remove Tag from Todo");
        removeTagBtn.setName("removeTagFromTodoButton");
        panel.add(removeTagBtn);
        
        JButton deleteTagBtn = new JButton("Delete Selected Tag");
        deleteTagBtn.setName("deleteTagButton");
        panel.add(deleteTagBtn);
        
        return panel;
    }

    private void setupListeners() {
        // Button listeners
        getButton("addTodoButton").addActionListener(e -> addTodo());
        getButton("editButton").addActionListener(e -> editTodo());
        getButton("deleteButton").addActionListener(e -> deleteTodo());
        getButton("toggleDoneButton").addActionListener(e -> toggleTodoDone());
        getButton("searchButton").addActionListener(e -> searchTodos());
        getButton("showAllButton").addActionListener(e -> showAllTodos());
        getButton("addTagButton").addActionListener(e -> addTag());
        getButton("addTagToTodoButton").addActionListener(e -> addTagToTodo());
        getButton("removeTagFromTodoButton").addActionListener(e -> removeTagFromTodo());
        getButton("deleteTagButton").addActionListener(e -> deleteTag());
        
        // Text field enter key listeners
        todoDescriptionField.addActionListener(e -> addTodo());
        searchField.addActionListener(e -> searchTodos());
        tagNameField.addActionListener(e -> addTag());
        
        // Table selection listener - FIXED: removed getValueIsAdjusting check
        todoTable.getSelectionModel().addListSelectionListener(e -> 
            updateTodoTags(getSelectedTodo())
        );
    }

    // Core action methods
    public void addTodo() {
        String description = getText(todoDescriptionField);
        if (description.isEmpty()) {
            showWarning("Description cannot be empty");
            return;
        }
        
        Todo todo = controller.addTodo(description);
        if (todo != null) {
            SwingUtilities.invokeLater(() -> {
                refreshTodos();
                todoDescriptionField.setText("");
            });
        }
    }

    public void addTag() {
        String tagName = getText(tagNameField);
        if (tagName.isEmpty()) {
            showWarning("Tag name cannot be empty");
            return;
        }
        
        Tag tag = controller.addTag(tagName);
        if (tag != null) {
            SwingUtilities.invokeLater(() -> {
                refreshTags();
                tagNameField.setText("");
            });
        } else {
            showWarning("Tag already exists or could not be created");
        }
    }

    public void addTagToTodo() {
        Todo todo = getSelectedTodo();
        Tag tag = availableTagsList.getSelectedValue();
        
        if (todo == null || tag == null) {
            showWarning("Please select both a todo and a tag");
            return;
        }
        
        if (controller.addTagToTodo(todo.getId(), tag.getId())) {
            SwingUtilities.invokeLater(() -> {
                refreshTags();
                refreshTodos();
                reselectTodo(todo.getId());
            });
        }
    }

    public void removeTagFromTodo() {
        Todo todo = getSelectedTodo();
        Tag tag = tagList.getSelectedValue();
        
        if (todo == null || tag == null) {
            showWarning("Please select both a todo and a tag to remove");
            return;
        }

        if (controller.removeTagFromTodo(todo.getId(), tag.getId())) {
            SwingUtilities.invokeLater(() -> {
                refreshTags();
                refreshTodos();
                reselectTodo(todo.getId());
            });
        }
    }

    public void deleteTag() {
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
    }

    public void editTodo() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow < 0) {
            showInfo("Please select a todo to edit");
            return;
        }
        
        Todo todo = todoTableModel.getTodoAt(selectedRow);
        String newDescription = JOptionPane.showInputDialog(
            this, "Edit todo description:", todo.getDescription()
        );
        if (newDescription != null && !newDescription.trim().isEmpty() 
                && controller.updateTodoDescription(todo.getId(), newDescription)) {
            SwingUtilities.invokeLater(() -> {
                refreshTodos();
                todoTable.setRowSelectionInterval(selectedRow, selectedRow);
            });
        }
    }
        

    public void deleteTodo() {
        Todo selected = getSelectedTodo();
        if (selected == null) {
            return;
        }
        
        if (controller.deleteTodo(selected.getId())) {
            SwingUtilities.invokeLater(() -> {
                refreshTodos();
                tagListModel.clear();
            });
        }
    }

    public void toggleTodoDone() {
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        
        Todo todo = todoTableModel.getTodoAt(selectedRow);
        Boolean newStatus = controller.toggleTodoDone(todo.getId());
        
        if (newStatus != null) {
            SwingUtilities.invokeLater(() -> {
                refreshTodos();
                todoTable.setRowSelectionInterval(selectedRow, selectedRow);
            });
        }
    }

    public void searchTodos() {
        String keyword = getText(searchField);
        List<Todo> results = controller.searchTodos(keyword);
        SwingUtilities.invokeLater(() -> todoTableModel.setTodos(results));
    }

    public void showAllTodos() {
        SwingUtilities.invokeLater(() -> {
            refreshTodos();
            searchField.setText("");
        });
    }

    public void refreshTodos() {
        int selectedRow = todoTable.getSelectedRow();
        List<Todo> todos = controller.getAllTodos();
        todoTableModel.setTodos(todos);
        
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

    // Helper methods
    void updateTodoTags(Todo todo) {
        tagListModel.clear();
        if (todo != null && todo.getTags() != null) {
            todo.getTags().forEach(tagListModel::addElement);
        }
    }

    void reselectTodo(Long todoId) {
        for (int i = 0; i < todoTableModel.getRowCount(); i++) {
            if (todoTableModel.getTodoAt(i).getId().equals(todoId)) {
                todoTable.setRowSelectionInterval(i, i);
                updateTodoTags(todoTableModel.getTodoAt(i));
                break;
            }
        }
    }

    String getText(JTextField field) {
        String text = field.getText();
        return (text != null) ? text.trim() : "";
    }

    public Todo getSelectedTodo() {
        int selectedRow = todoTable.getSelectedRow();
        return selectedRow >= 0 ? todoTableModel.getTodoAt(selectedRow) : null;
    }

    JButton getButton(String name) {
        return findButton(this, name);
    }

    JButton findButton(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(name) && comp instanceof JButton jbutton) {
                return jbutton; 
            }
            if (comp instanceof Container subContainer) {
                JButton found = findButton(subContainer, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void showWarning(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.WARNING_MESSAGE)
        );
    }

    private void showInfo(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE)
        );
    }

    // TodoTableModel
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