import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class CRMPanel extends JPanel {
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, emailField, phoneField, purchaseHistoryField;
    private JButton addButton, updateButton, deleteButton, searchButton;
    private JTextField searchField;

    public CRMPanel() {
        setLayout(new BorderLayout(10, 10));  // Padding for the main layout

        // Initialize the table model with column names
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Purchase History"}, 0);
        customerTable = new JTable(tableModel);

        // Load the customer data from the database
        loadCustomerData();

        // Customize table appearance
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.setRowHeight(30);  // Increase row height for better readability
        customerTable.setGridColor(Color.LIGHT_GRAY);  // Light grid color
        customerTable.setIntercellSpacing(new Dimension(0, 1));  // Reduce space between rows

        // Add the table to a scroll pane and then add it to the panel
        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // Form for adding/updating customers at the top
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Customer Information"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        formPanel.add(new JLabel("Name:"), gbc);
        nameField = new JTextField(15);
        nameField.setPreferredSize(new Dimension(200, 30));
        nameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));  // Border for text fields
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField(15);
        emailField.setPreferredSize(new Dimension(200, 30));
        emailField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Phone:"), gbc);
        phoneField = new JTextField(15);
        phoneField.setPreferredSize(new Dimension(200, 30));
        phoneField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Purchase History:"), gbc);
        purchaseHistoryField = new JTextField(15);
        purchaseHistoryField.setPreferredSize(new Dimension(200, 30));
        purchaseHistoryField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gbc.gridx = 1;
        formPanel.add(purchaseHistoryField, gbc);

        // Action Buttons
        addButton = new JButton("Add Customer");
        updateButton = new JButton("Update Customer");
        deleteButton = new JButton("Delete Customer");

        // Style buttons
        styleButton(addButton);
        styleButton(updateButton);
        styleButton(deleteButton);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));  // Center aligned with space between buttons
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Add form and button panels to the main panel
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCustomer();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCustomer();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCustomer();
            }
        });
    }

    private void loadCustomerData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String purchaseHistory = rs.getString("purchase_history");

                // Add the customer data to the table
                tableModel.addRow(new Object[]{id, name, email, phone, purchaseHistory});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customer data.");
        }
    }

    private void addCustomer() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String purchaseHistory = purchaseHistoryField.getText();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO customers (name, email, phone, purchase_history) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setString(4, purchaseHistory);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Customer added successfully.");
                loadCustomerData(); // Refresh the customer data
                clearForm();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding customer.");
        }
    }

    private void updateCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String purchaseHistory = purchaseHistoryField.getText();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE customers SET name = ?, email = ?, phone = ?, purchase_history = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, email);
                    pstmt.setString(3, phone);
                    pstmt.setString(4, purchaseHistory);
                    pstmt.setInt(5, id);
                    pstmt.executeUpdate();
                    tableModel.setValueAt(name, selectedRow, 1);
                    tableModel.setValueAt(email, selectedRow, 2);
                    tableModel.setValueAt(phone, selectedRow, 3);
                    tableModel.setValueAt(purchaseHistory, selectedRow, 4);
                    JOptionPane.showMessageDialog(this, "Customer updated successfully.");
                    clearForm();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating customer.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No customer selected for update.");
        }
    }

    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM customers WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Customer deleted successfully.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting customer.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No customer selected for deletion.");
        }
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        purchaseHistoryField.setText("");
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(0x4CAF50));  // Green background
        button.setForeground(Color.WHITE);  // White text
        button.setPreferredSize(new Dimension(150, 40));  // Button size
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0x45A049));  // Darker green on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0x4CAF50));  // Original color when not hovered
            }
        });
    }
}
