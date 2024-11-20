import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton managerRadioButton, sellerRadioButton;
    private int failedAttempts = 0;

    public LoginPanel() {
        // Set layout to GridBagLayout for more control over component resizing
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 245)); // Soft background color
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding between components

        // Title label in a centered style
        JLabel titleLabel = new JLabel("Smart Shopping System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(60, 179, 113));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Position the title label
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        // Panel for the input fields with reduced margins and padding
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2, 8, 10)); // Smaller spacing between rows and columns
        inputPanel.setOpaque(false);

        // Create input labels and text fields
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setForeground(new Color(70, 70, 70));
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(new Color(70, 70, 70));
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));

        // Role selection radio buttons
        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        roleLabel.setForeground(new Color(70, 70, 70));

        managerRadioButton = new JRadioButton("Manager");
        sellerRadioButton = new JRadioButton("Seller");
        managerRadioButton.setOpaque(false);
        sellerRadioButton.setOpaque(false);

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(managerRadioButton);
        roleGroup.add(sellerRadioButton);

        JPanel rolePanel = new JPanel();
        rolePanel.setOpaque(false);
        rolePanel.add(managerRadioButton);
        rolePanel.add(sellerRadioButton);

        // Add components to the input panel
        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(roleLabel);
        inputPanel.add(rolePanel);

        // Position the input panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(inputPanel, gbc);

        // Login button with improved visibility and hover effect
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setBackground(new Color(60, 179, 113)); // Green color for visibility
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginUser();
            }
        });

        // Hover effect for the login button
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(50, 150, 90));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(60, 179, 113));
            }
        });

        // Panel for the button and centered positioning
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginButton);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Position the button panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // Make the window resize gracefully
        this.setPreferredSize(new Dimension(400, 300));
    }

    private void loginUser() {
        String username = usernameField.getText();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        if (!managerRadioButton.isSelected() && !sellerRadioButton.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please select a role.");
            return;
        }

        // Authenticate user
        if (authenticateUser(username, new String(password))) {
            if (managerRadioButton.isSelected()) {
                showMainFrame("Manager");
            } else if (sellerRadioButton.isSelected()) {
                showMainFrame("Seller");
            }
        } else {
            failedAttempts++;
            if (failedAttempts >= 3) {
                JOptionPane.showMessageDialog(this, "Too many failed attempts! Please try again later.");
                System.exit(0); // Close application after 3 failed attempts
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password. Attempt " + failedAttempts + " of 3.");
            }
        }
    }

    private boolean authenticateUser(String username, String password) {
        // Simple hardcoded credentials for testing purposes
        return username.equals("admin") && password.equals("0123");
    }

    private void showMainFrame(String role) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(role);
            mainFrame.setVisible(true);
        });

        // Close the login window
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose();
    }
}
