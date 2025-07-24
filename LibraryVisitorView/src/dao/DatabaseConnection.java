package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/perpus"; // sesuaikan nama DB
    private static final String USER = "root"; // sesuaikan username MySQL
    private static final String PASSWORD = ""; // sesuaikan password MySQL jika ada

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
