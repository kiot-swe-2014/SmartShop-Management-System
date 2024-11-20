import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AnalyticsPanel extends JPanel {
    private JTextArea reportArea;
    private JButton dailyButton, weeklyButton, monthlyButton, annualButton, refreshButton;
    private Connection connection;

    public AnalyticsPanel() {
        setLayout(new BorderLayout());

        // Create a panel to hold the report and button
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        // Create and set up the JTextArea for the sales report
        reportArea = new JTextArea(10, 30);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Arial", Font.PLAIN, 14));  // Set a pleasant font
        reportArea.setBackground(new Color(245, 245, 245));  // Light gray background for better readability
        reportArea.setText("Sales Report: \n");

        // Add the text area to a scroll pane
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Adding some padding
        topPanel.add(scrollPane, BorderLayout.CENTER);

        // Create buttons for daily, weekly, monthly, and annual reports
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 5, 10, 10));  // Arrange buttons horizontally

        dailyButton = new JButton("Daily Report");
        weeklyButton = new JButton("Weekly Report");
        monthlyButton = new JButton("Monthly Report");
        annualButton = new JButton("Annual Report");
        refreshButton = new JButton("Refresh Report");

        // Set up button appearance
        setButtonStyle(dailyButton);
        setButtonStyle(weeklyButton);
        setButtonStyle(monthlyButton);
        setButtonStyle(annualButton);
        setButtonStyle(refreshButton);

        // Add buttons to the panel
        buttonPanel.add(dailyButton);
        buttonPanel.add(weeklyButton);
        buttonPanel.add(monthlyButton);
        buttonPanel.add(annualButton);
        buttonPanel.add(refreshButton);

        // Action listeners for the buttons
        dailyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateDailyReport();
            }
        });

        weeklyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateWeeklyReport();
            }
        });

        monthlyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateMonthlyReport();
            }
        });

        annualButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateAnnualReport();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshReport();
            }
        });

        // Add the button panel to the top panel
        topPanel.add(buttonPanel, BorderLayout.NORTH);

        // Add the top panel to the main panel
        add(topPanel, BorderLayout.NORTH);

        // Connect to the database
        connectToDatabase();
    }

    // Helper method to set consistent button style
    private void setButtonStyle(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(new Color(100, 200, 100));  // Green button for refreshing
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
    }

    // Function to simulate refreshing the report (e.g., updating with actual data)
    public void refreshReport() {
        // Fetch updated data and show summary in the JTextArea
        reportArea.setText("Sales Report: \n");

        // Example: Aggregate total sales, products sold, and customers from sales_history
        try (Statement stmt = connection.createStatement()) {
            String query = """
            SELECT 
                IFNULL(SUM(total_price), 0) AS total_sales, 
                IFNULL(SUM(quantity_sold), 0) AS total_products_sold, 
                COUNT(DISTINCT customer_id) AS total_customers 
            FROM sales_history;
        """;
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                reportArea.append("Total Sales: $" + rs.getDouble("total_sales") + "\n");
                reportArea.append("Total Products Sold: " + rs.getInt("total_products_sold") + "\n");
                reportArea.append("Total Customers: " + rs.getInt("total_customers") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Function to connect to the database
    private void connectToDatabase() {
        try {
            // Connect to your database (replace with your own connection details)
            String url = "jdbc:mysql://localhost:3306/shopping_system";
            String user = "root";
            String password = "";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Populate sales_data table with data from sales_history
    private void populateSalesData(String dateRange) {
        try {
            String insertQuery = "";

            switch (dateRange.toLowerCase()) {
                case "daily":
                    insertQuery = """
                    INSERT INTO sales_data (sale_date, total_sales, products_sold, customer_id)
                    SELECT 
                        CURDATE() AS sale_date,
                        IFNULL(SUM(sh.total_price), 0) AS total_sales,
                        IFNULL(SUM(sh.quantity), 0) AS products_sold,
                        sh.customer_id
                    FROM sales_history sh
                    WHERE DATE(sh.sale_date) = CURDATE() 
                      AND sh.customer_id IS NOT NULL
                    GROUP BY sh.customer_id;
                """;
                    break;
                case "weekly":
                    insertQuery = """
                    INSERT INTO sales_data (sale_date, total_sales, products_sold, customer_id)
                    SELECT 
                        CURDATE() AS sale_date,
                        IFNULL(SUM(sh.total_price), 0) AS total_sales,
                        IFNULL(SUM(sh.quantity), 0) AS products_sold,
                        sh.customer_id
                    FROM sales_history sh
                    WHERE YEARWEEK(sh.sale_date, 1) = YEARWEEK(CURDATE(), 1) 
                      AND sh.customer_id IS NOT NULL
                    GROUP BY sh.customer_id;
                """;
                    break;
                case "monthly":
                    insertQuery = """
                    INSERT INTO sales_data (sale_date, total_sales, products_sold, customer_id)
                    SELECT 
                        CURDATE() AS sale_date,
                        IFNULL(SUM(sh.total_price), 0) AS total_sales,
                        IFNULL(SUM(sh.quantity), 0) AS products_sold,
                        sh.customer_id
                    FROM sales_history sh
                    WHERE YEAR(sh.sale_date) = YEAR(CURDATE()) 
                      AND MONTH(sh.sale_date) = MONTH(CURDATE()) 
                      AND sh.customer_id IS NOT NULL
                    GROUP BY sh.customer_id;
                """;
                    break;
                case "annual":
                    insertQuery = """
                    INSERT INTO sales_data (sale_date, total_sales, products_sold, customer_id)
                    SELECT 
                        CURDATE() AS sale_date,
                        IFNULL(SUM(sh.total_price), 0) AS total_sales,
                        IFNULL(SUM(sh.quantity), 0) AS products_sold,
                        sh.customer_id
                    FROM sales_history sh
                    WHERE YEAR(sh.sale_date) = YEAR(CURDATE()) 
                      AND sh.customer_id IS NOT NULL
                    GROUP BY sh.customer_id;
                """;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid date range: " + dateRange);
            }

            Statement stmt = connection.createStatement();
            stmt.executeUpdate(insertQuery);
            JOptionPane.showMessageDialog(this, dateRange + " sales data populated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error populating " + dateRange + " sales data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Functions for different types of reports
    private void generateDailyReport() {
        // First, populate the sales_data table with the latest data
        populateSalesData("daily");

        try {
            String query = "SELECT SUM(total_sales) AS daily_sales, SUM(products_sold) AS products_sold, COUNT(DISTINCT customer_id) AS new_customers " +
                    "FROM sales_data WHERE DATE(sale_date) = CURDATE()";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                reportArea.setText("Daily Sales Report: \n");
                reportArea.append("Total Sales Today: $" + rs.getDouble("daily_sales") + "\n");
                reportArea.append("Total Products Sold: " + rs.getInt("products_sold") + "\n");
                reportArea.append("New Customers Today: " + rs.getInt("new_customers") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateWeeklyReport() {
        // First, populate the sales_data table with the latest data
        populateSalesData("weekly");

        try {
            String query = "SELECT SUM(total_sales) AS weekly_sales, SUM(products_sold) AS products_sold, COUNT(DISTINCT customer_id) AS new_customers " +
                    "FROM sales_data WHERE WEEK(sale_date) = WEEK(CURDATE())";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                reportArea.setText("Weekly Sales Report: \n");
                reportArea.append("Total Sales This Week: $" + rs.getDouble("weekly_sales") + "\n");
                reportArea.append("Total Products Sold: " + rs.getInt("products_sold") + "\n");
                reportArea.append("New Customers This Week: " + rs.getInt("new_customers") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateMonthlyReport() {
        // First, populate the sales_data table with the latest data
        populateSalesData("monthly");

        try {
            String query = "SELECT SUM(total_sales) AS monthly_sales, SUM(products_sold) AS products_sold, COUNT(DISTINCT customer_id) AS new_customers " +
                    "FROM sales_data WHERE MONTH(sale_date) = MONTH(CURDATE())";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                reportArea.setText("Monthly Sales Report: \n");
                reportArea.append("Total Sales This Month: $" + rs.getDouble("monthly_sales") + "\n");
                reportArea.append("Total Products Sold: " + rs.getInt("products_sold") + "\n");
                reportArea.append("New Customers This Month: " + rs.getInt("new_customers") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateAnnualReport() {
        // First, populate the sales_data table with the latest data
        populateSalesData("annual");

        try {
            String query = "SELECT SUM(total_sales) AS annual_sales, SUM(products_sold) AS products_sold, COUNT(DISTINCT customer_id) AS new_customers " +
                    "FROM sales_data WHERE YEAR(sale_date) = YEAR(CURDATE())";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                reportArea.setText("Annual Sales Report: \n");
                reportArea.append("Total Sales This Year: $" + rs.getDouble("annual_sales") + "\n");
                reportArea.append("Total Products Sold: " + rs.getInt("products_sold") + "\n");
                reportArea.append("New Customers This Year: " + rs.getInt("new_customers") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}