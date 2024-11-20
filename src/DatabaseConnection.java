import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/shopping_system";  // Update your database name
    private static final String USER = "root";  // Your MySQL username
    private static final String PASSWORD = "";  // Your MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}
