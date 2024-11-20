import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SupplierPanel extends JPanel {
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, contactField, productsSuppliedField;
    private JButton addButton, updateButton, deleteButton;

    public SupplierPanel() {
        setLayout(new BorderLayout(10, 10));  // Add padding between components

        // Initialize the table model and set column names
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Contact", "Products Supplied"}, 0);
        supplierTable = new JTable(tableModel);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // Single row selection
        supplierTable.setRowHeight(30); // Increase row height for readability

        // Load the supplier data from the database
        loadSupplierData();

        // Add the table to a scroll pane and add to the panel with styling
        JScrollPane tableScrollPane = new JScrollPane(supplierTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Remove border around table
        add(tableScrollPane, BorderLayout.CENTER);

        // Add a form for entering supplier details at the top with padding and styling
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);

        // Add a panel for the buttons with styling
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));  // 3 rows, 2 columns, 10px padding

        formPanel.setBorder(BorderFactory.createTitledBorder("Supplier Details"));

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Contact:"));
        contactField = new JTextField();
        contactField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(contactField);

        formPanel.add(new JLabel("Products Supplied:"));
        productsSuppliedField = new JTextField();
        productsSuppliedField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(productsSuppliedField);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();

        addButton = new JButton("Add Supplier");
        updateButton = new JButton("Update Supplier");
        deleteButton = new JButton("Delete Supplier");

        // Button styling for consistency
        styleButton(addButton);
        styleButton(updateButton);
        styleButton(deleteButton);

        // Add buttons with some spacing between them
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Action Listeners for buttons
        addButton.addActionListener(e -> addSupplier());
        updateButton.addActionListener(e -> updateSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());

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

    private void loadSupplierData() {
        String query = "SELECT * FROM suppliers";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            // Clear existing data in the table
            tableModel.setRowCount(0);

            // Add the supplier data to the table
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String contact = rs.getString("contact");
                String productsSupplied = rs.getString("products_supplied");
                tableModel.addRow(new Object[]{id, name, contact, productsSupplied});
            }
        } catch (SQLException e) {
            showErrorMessage("Error loading supplier data.");
        }
    }

    private void addSupplier() {
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String productsSupplied = productsSuppliedField.getText().trim();

        if (validateFields(name, contact, productsSupplied)) {
            String query = "INSERT INTO suppliers (name, contact, products_supplied) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, name);
                pstmt.setString(2, contact);
                pstmt.setString(3, productsSupplied);
                pstmt.executeUpdate();

                // Get the generated ID and add it to the table
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        tableModel.addRow(new Object[]{generatedId, name, contact, productsSupplied});
                        showMessage("Supplier added successfully.");
                    }
                }
            } catch (SQLException e) {
                showErrorMessage("Error adding supplier.");
            }
        }
    }

    private void updateSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String productsSupplied = productsSuppliedField.getText().trim();

            if (validateFields(name, contact, productsSupplied)) {
                String query = "UPDATE suppliers SET name = ?, contact = ?, products_supplied = ? WHERE id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {

                    pstmt.setString(1, name);
                    pstmt.setString(2, contact);
                    pstmt.setString(3, productsSupplied);
                    pstmt.setInt(4, id);
                    pstmt.executeUpdate();

                    // Update the table with the new values
                    tableModel.setValueAt(name, selectedRow, 1);
                    tableModel.setValueAt(contact, selectedRow, 2);
                    tableModel.setValueAt(productsSupplied, selectedRow, 3);
                    showMessage("Supplier updated successfully.");
                } catch (SQLException e) {
                    showErrorMessage("Error updating supplier.");
                }
            }
        } else {
            showErrorMessage("No supplier selected for update.");
        }
    }

    private void deleteSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);

            String query = "DELETE FROM suppliers WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setInt(1, id);
                pstmt.executeUpdate();

                // Remove the supplier from the table
                tableModel.removeRow(selectedRow);
                showMessage("Supplier deleted successfully.");
            } catch (SQLException e) {
                showErrorMessage("Error deleting supplier.");
            }
        } else {
            showErrorMessage("No supplier selected for deletion.");
        }
    }

    private boolean validateFields(String name, String contact, String productsSupplied) {
        if (name.isEmpty() || contact.isEmpty() || productsSupplied.isEmpty()) {
            showErrorMessage("Please fill in all fields.");
            return false;
        }
        return true;
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
