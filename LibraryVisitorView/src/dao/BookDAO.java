package dao;

import model.Book;
import java.sql.*;
import java.util.ArrayList;

public class BookDAO {
    public ArrayList<Book> getAllBooks(String keyword) {
        ArrayList<Book> books = new ArrayList<>();
        boolean isNumeric = keyword.matches("\\d+"); // cek jika keyword berupa angka

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/perpus", "root", "")) {

            String sql = "SELECT * FROM books WHERE " +
                         (isNumeric ? "id = ? OR " : "") +
                         "title LIKE ? OR author LIKE ? OR category LIKE ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            int paramIndex = 1;

            if (isNumeric) {
                stmt.setInt(paramIndex++, Integer.parseInt(keyword));
            }

            String searchPattern = "%" + keyword + "%";
            stmt.setString(paramIndex++, searchPattern);
            stmt.setString(paramIndex++, searchPattern);
            stmt.setString(paramIndex, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("image_path"),
                    rs.getBoolean("available")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    
    public Book getBookById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("image_path"),
                    rs.getBoolean("available")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}

