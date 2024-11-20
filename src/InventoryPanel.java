import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InventoryPanel extends JPanel {
    private JTable inventoryTable;
    private DefaultTableModel tableModel;

    public InventoryPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240)); // Light background color for the panel

        // Table Model and Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Description", "Price", "Quantity", "Reorder Level"}, 0);
        inventoryTable = new JTable(tableModel) {
            // Customize the table for better visuals
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };

        inventoryTable.setFont(new Font("Arial", Font.PLAIN, 14));
        inventoryTable.setRowHeight(30);
        inventoryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        inventoryTable.getTableHeader().setBackground(new Color(70, 130, 180)); // Header background color
        inventoryTable.getTableHeader().setForeground(Color.WHITE); // Header text color
        inventoryTable.setSelectionBackground(new Color(173, 216, 230)); // Selection background color

        // Load data on initialization
        loadInventoryData();

        // Buttons for actions
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240)); // Background color for button panel

        JButton addButton = new JButton("Add Product");
        JButton deleteButton = new JButton("Delete Product");
        JButton updateButton = new JButton("Update Product");

        // Style buttons
        styleButton(addButton);
        styleButton(deleteButton);
        styleButton(updateButton);

        addButton.addActionListener(e -> addProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        updateButton.addActionListener(e -> updateProduct());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);

        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(60, 179, 113)); // Green color
        button.setForeground(Color.WHITE); // White text color
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(50, 150, 90)));
        button.setPreferredSize(new Dimension(150, 40)); // Set button size
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor for interactivity
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 150, 90)); // Darken on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 179, 113)); // Revert back
            }
        });
    }

    private void loadInventoryData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                int reorderLevel = rs.getInt("reorder_level");

                tableModel.addRow(new Object[]{id, name, description, price, quantity, reorderLevel});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading inventory data.");
        }
    }

    private void addProduct() {
        String name = JOptionPane.showInputDialog(this, "Enter product name:");
        String description = JOptionPane.showInputDialog(this, "Enter product description:");
        String priceStr = JOptionPane.showInputDialog(this, "Enter product price:");
        String quantityStr = JOptionPane.showInputDialog(this, "Enter product quantity:");
        String reorderLevelStr = JOptionPane.showInputDialog(this, "Enter reorder level:");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO products (name, description, price, quantity, reorder_level) VALUES (?, ?, ?, ?, ?)")) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, Double.parseDouble(priceStr));
            pstmt.setInt(4, Integer.parseInt(quantityStr));
            pstmt.setInt(5, Integer.parseInt(reorderLevelStr));
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product added successfully!");
            tableModel.setRowCount(0); // Clear the table
            loadInventoryData(); // Reload data

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding product.");
        }
    }

    private void deleteProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {

            pstmt.setInt(1, productId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product deleted successfully!");
            tableModel.removeRow(selectedRow);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting product.");
        }
    }

    private void updateProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.");
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = JOptionPane.showInputDialog(this, "Enter new product name:", tableModel.getValueAt(selectedRow, 1));
        String description = JOptionPane.showInputDialog(this, "Enter new product description:", tableModel.getValueAt(selectedRow, 2));
        String priceStr = JOptionPane.showInputDialog(this, "Enter new product price:", tableModel.getValueAt(selectedRow, 3));
        String quantityStr = JOptionPane.showInputDialog(this, "Enter new product quantity:", tableModel.getValueAt(selectedRow, 4));
        String reorderLevelStr = JOptionPane.showInputDialog(this, "Enter new reorder level:", tableModel.getValueAt(selectedRow, 5));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE products SET name = ?, description = ?, price = ?, quantity = ?, reorder_level = ? WHERE id = ?")) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, Double.parseDouble(priceStr));
            pstmt.setInt(4, Integer.parseInt(quantityStr));
            pstmt.setInt(5, Integer.parseInt(reorderLevelStr));
            pstmt.setInt(6, productId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product updated successfully!");
            tableModel.setRowCount(0); // Clear the table
            loadInventoryData(); // Reload data

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating product.");
        }
    }
}
