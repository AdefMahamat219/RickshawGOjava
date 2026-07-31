package storage;                        // ← line 1

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/rickshawgo";
    private static final String USER     = "root";
    private static final String PASSWORD = "Adef7808"; // ← change this

    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(
                    URL, USER, PASSWORD
                );
                System.out.println("✅ MySQL Connected!");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ MySQL Connection Failed!");
            e.printStackTrace();
        }
        return connection;
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connection is alive!");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("🔒 MySQL Connection Closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}