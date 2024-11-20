import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
public class ProductListPanel extends JPanel {
    private JTable productTable;

    public ProductListPanel() {
        // Set the layout
        setLayout(new BorderLayout());

        // Add a label to the top
        JLabel titleLabel = new JLabel("Available Products", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(70, 130, 180));
        add(titleLabel, BorderLayout.NORTH);

        // Create a table to display the product data
        productTable = new JTable();

        // Set table properties
        productTable.setFillsViewportHeight(true);
        productTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Fetch data from the database and display it in the table
        fetchProductData();

        // Add the table to a scroll pane for better viewing
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void fetchProductData() {
        // Database connection information
        String url = "jdbc:mysql://localhost:3306/shopping_system"; // Adjust for your database
        String user = "root"; // Replace with your MySQL username
        String password = ""; // Replace with your MySQL password

        // SQL query to fetch product data
        String query = "SELECT id, name, category, price, quantity FROM products";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Get the column names dynamically
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            // Get the row data dynamically
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }

            // Create a table model and set it for the JTable
            productTable.setModel(new DefaultTableModel(data, columnNames));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching data from database: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshProductData() {
        fetchProductData();
    }
}
