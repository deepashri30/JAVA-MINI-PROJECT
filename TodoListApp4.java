import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TodoListApp4 {

    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable todoTable;
    private JTextField todoInputField;
    private JTextField dueDateField;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> priorityComboBox;
    private JLabel dueDateLabel;
    private Properties taskProperties;
    private final String TASK_FILE = "tasks.properties";
    private Timer reminderTimer = new Timer(true);

    public TodoListApp4() {
        frame = new JFrame("Enhanced Todo List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.getContentPane().setBackground(new Color(240, 248, 255)); // Light background color

        // Display a motivational quote at the top
        String[] quotes = {
                "Stay organized, stay productive!",
                "A task a day keeps stress away!",
                "The journey to success starts with a single task.",
                "One step at a time. Keep going!",
                "Success is built on small, consistent actions."
        };
        Random rand = new Random();
        JLabel quoteLabel = new JLabel(quotes[rand.nextInt(quotes.length)], SwingConstants.CENTER);
        quoteLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        quoteLabel.setForeground(new Color(70, 130, 180)); // Steel blue color

        // Due date label to show the task due date
        dueDateLabel = new JLabel("Task Due Date: ", SwingConstants.CENTER);
        dueDateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dueDateLabel.setForeground(new Color(50, 205, 50)); // Lime green color

        // Quote panel to keep it separate from other elements
        JPanel quotePanel = new JPanel();
        quotePanel.setBackground(new Color(240, 248, 255));
        quotePanel.setLayout(new BorderLayout());
        quotePanel.add(dueDateLabel, BorderLayout.NORTH);
        quotePanel.add(quoteLabel, BorderLayout.CENTER);

        String[] columnNames = {"Task", "Due Date", "Category", "Priority", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        todoTable = new JTable(tableModel);
        todoTable.setDefaultRenderer(Object.class, new TaskTableCellRenderer()); // Custom renderer for color-coded status

        todoInputField = new JTextField(15);
        dueDateField = new JTextField(10);

        String[] categories = {"Work", "Personal", "Urgent"};
        categoryComboBox = new JComboBox<>(categories);

        String[] priorities = {"High", "Medium", "Low"};
        priorityComboBox = new JComboBox<>(priorities);

        JButton addButton = new JButton("Add");
        JButton removeButton = new JButton("Remove");
        JButton editButton = new JButton("Edit");
        JButton changePriorityButton = new JButton("Change Priority");

        loadTasks();

        addButton.addActionListener(e -> addTask());
        removeButton.addActionListener(e -> removeTask());
        editButton.addActionListener(e -> editTask());
        changePriorityButton.addActionListener(e -> changePriority());

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Task:"));
        inputPanel.add(todoInputField);
        inputPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        inputPanel.add(dueDateField);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryComboBox);
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(priorityComboBox);
        inputPanel.add(addButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editButton);
        buttonPanel.add(changePriorityButton);
        buttonPanel.add(removeButton);

        // Main panel layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(quotePanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(todoTable), BorderLayout.CENTER);

        // Create a bottom panel for input fields and buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        bottomPanel.add(inputPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        bottomPanel.add(buttonPanel, gbc);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
    }

    private void addTask() {
        String newItem = todoInputField.getText().trim();
        String dueDate = dueDateField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();
        String priority = (String) priorityComboBox.getSelectedItem();
        if (!newItem.isEmpty() && !dueDate.isEmpty()) {
            Object[] task = {newItem, dueDate, category, priority, getTaskStatus(dueDate)};
            tableModel.addRow(task);
            setReminder(newItem, dueDate);
            saveTasks();
            todoInputField.setText("");
            dueDateField.setText("");
            updateDueDateLabel(dueDate); // Update due date label when a new task is added
        }
    }

    private void removeTask() {
        int selectedIndex = todoTable.getSelectedRow();
        if (selectedIndex != -1) {
            tableModel.removeRow(selectedIndex);
            saveTasks();
            // Update due date label if task is removed
            if (tableModel.getRowCount() > 0) {
                updateDueDateLabel((String) tableModel.getValueAt(0, 1));
            } else {
                dueDateLabel.setText("Task Due Date: ");
            }
        }
    }

    private void editTask() {
        int selectedIndex = todoTable.getSelectedRow();
        if (selectedIndex != -1) {
            String currentTask = (String) tableModel.getValueAt(selectedIndex, 0);
            String newTaskText = JOptionPane.showInputDialog(frame, "Edit Task", currentTask);
            if (newTaskText != null && !newTaskText.trim().isEmpty()) {
                tableModel.setValueAt(newTaskText.trim(), selectedIndex, 0);
                saveTasks();
            }
        }
    }

    private void changePriority() {
        int selectedIndex = todoTable.getSelectedRow();
        if (selectedIndex != -1) {
            String newPriority = (String) JOptionPane.showInputDialog(frame, "Select New Priority", "Change Priority", JOptionPane.QUESTION_MESSAGE, null, new String[]{"High", "Medium", "Low"}, tableModel.getValueAt(selectedIndex, 3));
            if (newPriority != null) {
                tableModel.setValueAt(newPriority, selectedIndex, 3);
                saveTasks();
            }
        }
    }

    private String getTaskStatus(String dueDate) {
        LocalDate due = LocalDate.parse(dueDate);
        LocalDate today = LocalDate.now();
        if (due.isBefore(today)) {
            return "Missed"; // Task is overdue
        } else if (due.isEqual(today)) {
            return "Due Today"; // Task is due today
        } else {
            return "Upcoming"; // Task is upcoming
        }
    }

    private void loadTasks() {
        taskProperties = new Properties();
        try (FileInputStream fis = new FileInputStream(TASK_FILE)) {
            taskProperties.load(fis);
            for (String key : taskProperties.stringPropertyNames()) {
                String taskData = taskProperties.getProperty(key);
                try {
                    String[] parts = taskData.split("\\|");
                    tableModel.addRow(new Object[]{parts[0], parts[1], parts[2], parts[3], parts[4]});
                } catch (Exception e) {
                    System.err.println("Error loading task: " + taskData + " - " + e.getMessage());
                }
            }
            // Update the due date label if tasks are loaded
            if (tableModel.getRowCount() > 0) {
                updateDueDateLabel((String) tableModel.getValueAt(0, 1));
            }
        } catch (IOException e) {
            System.out.println("No previous tasks found. Starting fresh.");
        }
    }

    private void saveTasks() {
        taskProperties.clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String taskData = String.join("|", tableModel.getValueAt(i, 0).toString(),
                    tableModel.getValueAt(i, 1).toString(),
                    tableModel.getValueAt(i, 2).toString(),
                    tableModel.getValueAt(i, 3).toString(),
                    tableModel.getValueAt(i, 4).toString());
            taskProperties.setProperty("task" + i, taskData);
        }
        try (FileOutputStream fos = new FileOutputStream(TASK_FILE)) {
            taskProperties.store(fos, "Todo Tasks");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setReminder(String taskName, String dueDate) {
        LocalDate due = LocalDate.parse(dueDate);
        LocalDate today = LocalDate.now();
        if (due.isEqual(today)) {
            reminderTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    showTrayNotification(taskName);
                }
            }, 0); // Notify immediately if it's due today
        }
    }

    private void showTrayNotification(String taskName) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
                TrayIcon trayIcon = new TrayIcon(image, "Todo Reminder");
                tray.add(trayIcon);
                trayIcon.displayMessage("Task Reminder", taskName + " is due today!", TrayIcon.MessageType.INFO);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
        }
    }

    private void updateDueDateLabel(String dueDate) {
        dueDateLabel.setText("Task Due Date: " + dueDate);
    }

    // Inner Task class to represent each task
    static class Task {
        private String task;
        private String dueDate;
        private String category;
        private String priority;
        private boolean completed;

        public Task(String task, String dueDate, String category, String priority) {
            this.task = task;
            this.dueDate = dueDate;
            this.category = category;
            this.priority = priority;
            this.completed = false;
        }

        public String getTask() {
            return task;
        }

        public void setTask(String task) {
            this.task = task;
        }

        public String getDueDate() {
            return dueDate;
        }

        public String getCategory() {
            return category;
        }

        public String getPriority() {
            return priority;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        @Override
        public String toString() {
            return task + "|" + dueDate + "|" + category + "|" + priority + "|" + completed;
        }

        public static Task fromString(String str) throws Exception {
            String[] parts = str.split("\\|");
            if (parts.length != 5) throw new Exception("Invalid task format");
            Task task = new Task(parts[0], parts[1], parts[2], parts[3]);
            task.setCompleted(Boolean.parseBoolean(parts[4]));
            return task;
        }
    }

    // Custom renderer for JTable to color-code tasks based on due date status
    static class TaskTableCellRenderer extends DefaultTableCellRenderer {
        @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    String status = (String) table.getValueAt(row, 4);
    if (status.equals("Due Today")) {
        c.setBackground(Color.ORANGE);
        c.setForeground(Color.BLACK);
    } else if (status.equals("Missed")) {
        c.setBackground(Color.RED);
        c.setForeground(Color.WHITE);
    } else {
        c.setBackground(Color.GREEN);
        c.setForeground(Color.BLACK);
    }
    return c;
}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TodoListApp4::new);
    }
}
