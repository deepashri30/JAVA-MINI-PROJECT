# TodoListApp4

TodoListApp4 is a simple Java Swing application for managing a to-do list. This application allows users to add, edit, and delete tasks, and it visually distinguishes tasks based on their status.

## Features

- Add new tasks
- Edit existing tasks
- Delete tasks
- Visual indicators for task status:
  - Due Today: Orange background, Black text
  - Missed: Red background, White text
  - Default: Green background, Black text

## Requirements

- Java Development Kit (JDK) 8 or higher

## Installation

1. Clone the repository or download the source code.
```bash
git clone https://github.com/deepashri30/JavaToDoListapp
cd JavaToDoListapp
```
2. Compile the Java source file
```bash
javac TodoListApp4.java
```
3. Create a manifest file named  `MANIFEST.txt` with the following content:
```text
Main-Class: TodoListApp4
```

4. Create the  executable JAR file
```bash
jar cfm TodoListApp4.jar manifest.txt *.class
```

## Usage 
Run the application using the following command:
```bash 
java -jar TodoListApp4.jar
```

## Code Overview
### Main class 

The The main class of the application is ``` TodoListApp4```. It initializes the GUI and handles the main logic of the application.

### Task Renderer
The application uses a custom cell renderer to visually distinguish tasks based on their status:

```java 
@Override
public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
```

### Contributing
Contributions are welcome! Please fork the repository and submit a pull request with your changes.
