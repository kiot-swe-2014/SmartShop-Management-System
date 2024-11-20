import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class POSPanel extends JPanel {
    private JTextField productIdField;
    private JTextField quantityField;
    private JTextArea receiptArea;
    private ExecutorService executorService;
    private ProductListPanel productListPanel;
    private AnalyticsPanel analyticsPanel;

    public POSPanel(ProductListPanel productListPanel, AnalyticsPanel analyticsPanel) {
        this.productListPanel = productListPanel;
        this.analyticsPanel = analyticsPanel;

        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // Initialize executor service
        executorService = Executors.newSingleThreadExecutor();

        // Input Panel
        JPanel inputPanel = createInputPanel();

        // Receipt Area
        receiptArea = new JTextArea(10, 30);
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add components to the main panel
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel productIdLabel = new JLabel("Product ID:");
        productIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        productIdField = new JTextField();
        quantityField = new JTextField();

        JButton addToCartButton = new JButton("Sell Product");
        styleButton(addToCartButton);
        addToCartButton.addActionListener(e -> addToCart());

        inputPanel.add(productIdLabel);
        inputPanel.add(productIdField);
        inputPanel.add(quantityLabel);
        inputPanel.add(quantityField);
        inputPanel.add(addToCartButton);

        return inputPanel;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(60, 179, 113));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(50, 150, 90)));
    }

    private void addToCart() {
        String productId = productIdField.getText();
        String quantity = quantityField.getText();

        if (!validateInput(productId, quantity)) return;

        executorService.submit(() -> processSale(Integer.parseInt(productId), Integer.parseInt(quantity)));
    }

    private boolean validateInput(String productId, String quantity) {
        if (productId.isEmpty() || quantity.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (!isNumeric(productId) || !isNumeric(quantity)) {
            JOptionPane.showMessageDialog(this, "Product ID and Quantity must be numeric.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void processSale(int productId, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String query = "SELECT * FROM products WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, productId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    int availableQuantity = rs.getInt("quantity");

                    if (quantity > availableQuantity) {
                        showErrorMessage("Not enough stock available.");
                        conn.rollback();
                        return;
                    }

                    double totalPrice = price * quantity;

                    // Update receipt
                    SwingUtilities.invokeLater(() -> updateReceipt(name, quantity, totalPrice));

                    // Reduce stock in the database
                    updateStock(conn, productId, quantity);

                    // Log sale in sales_history
                    logSale(conn, productId, quantity, totalPrice);

                    // Commit transaction
                    conn.commit();

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Sale processed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        if (productListPanel != null) {
                            productListPanel.refreshProductData();
                        }
                        if (analyticsPanel != null) {
                            analyticsPanel.refreshReport();
                        }
                    });
                } else {
                    showErrorMessage("Product not found.");
                    conn.rollback();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Database error: " + e.getMessage());
        }
    }

    private void updateStock(Connection conn, int productId, int quantity) throws SQLException {
        String updateQuery = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            updateStmt.setInt(1, quantity);
            updateStmt.setInt(2, productId);
            updateStmt.executeUpdate();
        }
    }

    private void logSale(Connection conn, int productId, int quantity, double totalPrice) throws SQLException {
        String insertSaleQuery = "INSERT INTO sales_history (product_id, quantity_sold, sale_date, total_price) VALUES (?, ?, NOW(), ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertSaleQuery)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, quantity);
            stmt.setDouble(3, totalPrice);
            stmt.executeUpdate();
        }

    }


    private void updateReceipt(String name, int quantity, double totalPrice) {
        receiptArea.append(String.format("Product: %s\nQuantity: %d\nTotal: $%.2f\n\n", name, quantity, totalPrice));
    }

    private void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void shutDown() {
        executorService.shutdown();
    }
}
