import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StaffPanel extends JPanel {
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, roleField, scheduleField;
    private JButton addButton, updateButton, deleteButton;

    public StaffPanel() {
        setLayout(new BorderLayout(10, 10));  // Add padding between components

        // Initialize the table model and set column names
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Role", "Schedule"}, 0);
        staffTable = new JTable(tableModel);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // Single row selection
        staffTable.setRowHeight(30); // Increase row height for readability

        // Load the staff data from the database
        loadStaffData();

        // Add the table to a scroll pane and add to the panel
        JScrollPane tableScrollPane = new JScrollPane(staffTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Remove border around table
        add(tableScrollPane, BorderLayout.CENTER);

        // Add a form for entering staff details at the top
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);

        // Add a panel for the buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));  // 3 rows, 2 columns, 10px padding

        formPanel.setBorder(BorderFactory.createTitledBorder("Staff Details"));

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Role:"));
        roleField = new JTextField();
        roleField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(roleField);

        formPanel.add(new JLabel("Schedule:"));
        scheduleField = new JTextField();
        scheduleField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(scheduleField);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();

        addButton = new JButton("Add Staff");
        updateButton = new JButton("Update Staff");
        deleteButton = new JButton("Delete Staff");

        // Button styling for consistency
        styleButton(addButton);
        styleButton(updateButton);
        styleButton(deleteButton);

        // Add buttons with some spacing between them
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Action Listeners for buttons
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStaff();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStaff();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteStaff();
            }
        });

        return buttonPanel;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(0x4CAF50));  // Green background
        button.setForeground(Color.WHITE);  // White text
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setFont(new Font("Arial", Font.BOLD, 14));  // Larger, bold font
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));  // Border around buttons
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  // Hand cursor for buttons
    }

    private void loadStaffData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM staff")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String role = rs.getString("role");
                String schedule = rs.getString("schedule");

                // Add the staff data to the table
                tableModel.addRow(new Object[]{id, name, role, schedule});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading staff data.");
        }
    }

    private void addStaff() {
        String name = nameField.getText().trim();
        String role = roleField.getText().trim();
        String schedule = scheduleField.getText().trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO staff (name, role, schedule) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, role);
                pstmt.setString(3, schedule);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Staff added successfully.");
                // Add the new staff to the table
                tableModel.addRow(new Object[]{getLastInsertedId(conn), name, role, schedule});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding staff.");
        }
    }

    private int getLastInsertedId(Connection conn) throws SQLException {
        String query = "SELECT LAST_INSERT_ID()";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1; // Return -1 if no ID is found
    }

    private void updateStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = nameField.getText().trim();
            String role = roleField.getText().trim();
            String schedule = scheduleField.getText().trim();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE staff SET name = ?, role = ?, schedule = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, role);
                    pstmt.setString(3, schedule);
                    pstmt.setInt(4, id);
                    pstmt.executeUpdate();
                    // Update the table with the new values
                    tableModel.setValueAt(name, selectedRow, 1);
                    tableModel.setValueAt(role, selectedRow, 2);
                    tableModel.setValueAt(schedule, selectedRow, 3);
                    JOptionPane.showMessageDialog(this, "Staff updated successfully.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating staff.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No staff selected for update.");
        }
    }

    private void deleteStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM staff WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                    // Remove the staff from the table
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Staff deleted successfully.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting staff.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No staff selected for deletion.");
        }
    }
}
