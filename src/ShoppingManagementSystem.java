import javax.swing.*;

public class ShoppingManagementSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Show the login screen
            JFrame loginFrame = new JFrame("Login");
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.setSize(450, 300);
            loginFrame.setLocationRelativeTo(null); // Center on screen

            LoginPanel loginPanel = new LoginPanel(); // Handles role-based login
            loginFrame.add(loginPanel);

            loginFrame.setVisible(true);
        });
    }
}
