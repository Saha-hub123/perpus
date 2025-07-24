package perpus;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/perpus";
    private static final String USER = "root"; // ganti jika berbeda
    private static final String PASSWORD = ""; // ganti jika ada password

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
