import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame(String role) {
        // Set the title and default close operation
        setTitle("Smart Shopping Management System - " + role);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the size and center the window
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Create a tabbed pane to switch between different panels
        JTabbedPane tabbedPane = new JTabbedPane();

        // Customize the tab appearance
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 16));
        tabbedPane.setForeground(new Color(70, 130, 180));
        tabbedPane.setBackground(new Color(245, 245, 245));

        // Role-specific panel assignment
        configureRolePanels(role, tabbedPane);

        // Add the tabbed pane to the main frame
        add(tabbedPane);
    }

    private void configureRolePanels(String role, JTabbedPane tabbedPane) {
        if (role.equalsIgnoreCase("Manager")) {
            tabbedPane.addTab("Inventory Management", new InventoryPanel());
            tabbedPane.addTab("Supplier Management", new SupplierPanel());
            tabbedPane.addTab("Analytics & Reporting", new AnalyticsPanel());
            tabbedPane.addTab("Staff Management", new StaffPanel());
        } else if (role.equalsIgnoreCase("Seller")) {
            // Create shared panels for better integration
            ProductListPanel productListPanel = new ProductListPanel();
            AnalyticsPanel analyticsPanel = new AnalyticsPanel();

            tabbedPane.addTab("Available Products", productListPanel);
            tabbedPane.addTab("POS System", new POSPanel(productListPanel, analyticsPanel));
            tabbedPane.addTab("Sales Analytics", analyticsPanel);
            tabbedPane.addTab("Customer Management", new CRMPanel());
        } else {
            JOptionPane.showMessageDialog(this, "Invalid role: " + role, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0); // Exit if an invalid role is detected
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Display login screen
            showLoginScreen();
        });
    }

    private static void showLoginScreen() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(450, 300);
        loginFrame.setLocationRelativeTo(null); // Center the window

        LoginPanel loginPanel = new LoginPanel();
        loginFrame.add(loginPanel);

        loginFrame.setVisible(true);
    }
}
