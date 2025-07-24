package perpus;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

public class LibrarySystem {
    private Connection conn;

    public LibrarySystem() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/perpus", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addBookToDB(String title, String author, String imagePath, String category, String description) {
        String sql = "INSERT INTO books (title, author, image_path, category, description, available) VALUES (?, ?, ?, ?, ?, 1)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, imagePath);
            stmt.setString(4, category);
            stmt.setString(5, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void addMemberToDB(String name, String phone) {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO members (name, phone) VALUES (?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void borrowBookFromDB(int bookId, int memberId) {
        try {
            conn.setAutoCommit(false);

            PreparedStatement check = conn.prepareStatement("SELECT available FROM books WHERE id = ?");
            check.setInt(1, bookId);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getBoolean("available")) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime dueDate = now.plusDays(7);

                PreparedStatement insertLoan = conn.prepareStatement(
                    "INSERT INTO loans (book_id, member_id, loan_date, due_date) VALUES (?, ?, ?, ?)");
                insertLoan.setInt(1, bookId);
                insertLoan.setInt(2, memberId);
                insertLoan.setTimestamp(3, Timestamp.valueOf(now));
                insertLoan.setTimestamp(4, Timestamp.valueOf(dueDate));
                insertLoan.executeUpdate();

                PreparedStatement updateBook = conn.prepareStatement("UPDATE books SET available = 0 WHERE id = ?");
                updateBook.setInt(1, bookId);
                updateBook.executeUpdate();

                conn.commit();
            } else {
                System.out.println("Buku tidak tersedia.");
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }


    public void returnBookToDB(int bookId, int memberId) {
        try {
            conn.setAutoCommit(false);

            // Ambil informasi pinjaman yang aktif
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT l.id, l.due_date FROM loans l WHERE book_id = ? AND member_id = ? AND returned = 0"
            );
            checkStmt.setInt(1, bookId);
            checkStmt.setInt(2, memberId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) throw new SQLException("Pinjaman tidak ditemukan.");

            LocalDateTime dueDateTime = rs.getTimestamp("due_date").toLocalDateTime();
            int loanId = rs.getInt("id");


            // Update loan jadi returned
            PreparedStatement updateLoan = conn.prepareStatement("UPDATE loans SET returned = 1 WHERE id = ?");
            updateLoan.setInt(1, loanId);
            updateLoan.executeUpdate();

            // Update buku jadi tersedia
            PreparedStatement updateBook = conn.prepareStatement("UPDATE books SET available = 1 WHERE id = ?");
            updateBook.setInt(1, bookId);
            updateBook.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException(e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }





    public ArrayList<Book> getBooksFromDBList() {
        ArrayList<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM books";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String imagePath = rs.getString("image_path");
                String category = rs.getString("category");
                String description = rs.getString("description");
                boolean available = rs.getBoolean("available");

                books.add(new Book(id, title, author, imagePath, category, description, available));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }




    public ArrayList<Member> getMembersWithLoansFromDBList() throws Exception {
        ArrayList<Member> membersList = new ArrayList<>();
        Map<Integer, Member> memberMap = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT m.id AS member_id, m.name AS member_name, m.phone, " +
                         "b.id AS book_id, b.title, b.author, b.image_path, b.available, b.category, b.description, " +
                         "l.loan_date, l.due_date " +
                         "FROM members m " +
                         "LEFT JOIN loans l ON m.id = l.member_id AND l.returned = 0 " +
                         "LEFT JOIN books b ON l.book_id = b.id";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                String memberName = rs.getString("member_name");
                String phone = rs.getString("phone");

                Member member = memberMap.getOrDefault(memberId, new Member(memberId, memberName, phone));

                int bookId = rs.getInt("book_id");
                if (!rs.wasNull()) {
                    String title = rs.getString("title");
                    String author = rs.getString("author");
                    String imagePath = rs.getString("image_path");
                    String category = rs.getString("category");
                    String description = rs.getString("description");
                    boolean available = rs.getBoolean("available");
                    LocalDateTime loanDate = rs.getTimestamp("loan_date").toLocalDateTime();
                    LocalDateTime dueDate = rs.getTimestamp("due_date").toLocalDateTime();

                    Book book = new Book(bookId, title, author, imagePath, category, description, available);
                    Loan loan = new Loan(book, member, loanDate, dueDate);
                    member.addLoan(loan);
                }

                memberMap.putIfAbsent(memberId, member);
            }

            membersList.addAll(memberMap.values());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return membersList;
    }



    
    public void updateBookInDB(int bookId, String newTitle, String newAuthor, String imagePath, String category, String description) {
        String sql;
        boolean hasImage = (imagePath != null && !imagePath.isEmpty());

        if (hasImage) {
            sql = "UPDATE books SET title = ?, author = ?, image_path = ?, category = ?, description = ? WHERE id = ?";
        } else {
            sql = "UPDATE books SET title = ?, author = ?, category = ?, description = ? WHERE id = ?";
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newTitle);
            stmt.setString(2, newAuthor);
            if (hasImage) {
                stmt.setString(3, imagePath);
                stmt.setString(4, category);
                stmt.setString(5, description);
                stmt.setInt(6, bookId);
            } else {
                stmt.setString(3, category);
                stmt.setString(4, description);
                stmt.setInt(5, bookId);
            }
            stmt.executeUpdate();
            System.out.println("Buku berhasil diperbarui.");
        } catch (SQLException e) {
            System.err.println("Gagal memperbarui buku: " + e.getMessage());
        }
    }



    
    public void deleteBookFromDB(int bookId) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            System.out.println("Buku berhasil dihapus.");
        } catch (SQLException e) {
        	if (e.getMessage().contains("foreign key constraint fails")) {
                JOptionPane.showMessageDialog(null, "Buku sedang dipinjam, tidak bisa dihapus.", "Gagal Menghapus", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
            }
        }
    }
    
    public void updateMemberInDB(int id, String newName, String newPhone) {
        String sql = "UPDATE members SET name = ?, phone = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, newPhone);
            stmt.setInt(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal memperbarui anggota: " + e.getMessage());
        }
    }


    // Hapus member berdasarkan ID
    public void deleteMemberFromDB(int id) {
        String sql = "DELETE FROM members WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal menghapus anggota: " +
                (e.getMessage().contains("foreign key") ? "Anggota masih meminjam buku!" : e.getMessage()));
        }
    }
    
    public Book getBookById(int bookId) {
        for (Book book : getBooksFromDBList()) {
            if (book.getId() == bookId) {
                return book;
            }
        }
        return null;
    }
    
    public Member getMemberById(int memberId) throws Exception {
        Member member = null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name, phone FROM members WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, memberId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                member = new Member(id, name, phone);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return member;
    }



    
    public ArrayList<Loan> getLoanHistoryFromDBList() throws Exception {
        ArrayList<Loan> history = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
        	String sql = "SELECT l.id AS loan_id, m.id AS member_id, m.name AS member_name, " +
                    "b.id AS book_id, b.title AS book_title, l.loan_date, l.due_date, " +
                    "CASE WHEN l.returned = 1 THEN 'Sudah Dikembalikan' ELSE 'Belum Dikembalikan' END AS status " +
                    "FROM loans l " +
                    "JOIN members m ON l.member_id = m.id " +
                    "JOIN books b ON l.book_id = b.id " +
                    "ORDER BY l.returned ASC, l.due_date DESC";


            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int loanId = rs.getInt("loan_id");
                int memberId = rs.getInt("member_id");
                String memberName = rs.getString("member_name");
                int bookId = rs.getInt("book_id");
                String bookTitle = rs.getString("book_title");
                LocalDateTime loanDate = rs.getTimestamp("loan_date").toLocalDateTime();
                LocalDateTime dueDate = rs.getTimestamp("due_date").toLocalDateTime();
                String status = rs.getString("status");

                history.add(new Loan(loanId, memberId, memberName, bookId, bookTitle, loanDate, dueDate, status));
            }
        }
        return history;
    }
    
    public Loan getActiveLoan(int bookId, int memberId) throws Exception {
        Loan loan = null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT l.id AS loan_id, l.loan_date, l.due_date, " +
                         "b.id AS book_id, b.title, b.author, b.image_path, b.available, b.category, b.description, " +
                         "m.id AS member_id, m.name AS member_name, m.phone " +
                         "FROM loans l " +
                         "JOIN books b ON l.book_id = b.id " +
                         "JOIN members m ON l.member_id = m.id " +
                         "WHERE l.book_id = ? AND l.member_id = ? AND l.returned = 0";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Book book = new Book(
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("image_path"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getBoolean("available")
                );

                Member member = new Member(
                    rs.getInt("member_id"),
                    rs.getString("member_name"),
                    rs.getString("phone")
                );

                LocalDateTime loanDate = rs.getTimestamp("loan_date").toLocalDateTime();
                LocalDateTime dueDate = rs.getTimestamp("due_date").toLocalDateTime();

                loan = new Loan(book, member, loanDate, dueDate);
            }
        }

        return loan;
    }

    
    public ArrayList<Book> searchBooks(String keyword) throws Exception {
        ArrayList<Book> books = new ArrayList<>();
        boolean isNumeric = keyword.matches("\\d+");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM books WHERE " +
                         (isNumeric ? "id = ? OR " : "") +
                         "title LIKE ? OR author LIKE ? OR category LIKE ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            int index = 1;
            if (isNumeric) {
                stmt.setInt(index++, Integer.parseInt(keyword));
            }

            String pattern = "%" + keyword + "%";
            stmt.setString(index++, pattern);
            stmt.setString(index++, pattern);
            stmt.setString(index, pattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("image_path"),
                    rs.getString("category"),
                    rs.getString("description"),
                    
                    rs.getBoolean("available")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }
    
    public ArrayList<Member> searchMembers(String keyword) throws Exception {
        ArrayList<Member> membersList = new ArrayList<>();
        Map<Integer, Member> memberMap = new HashMap<>();
        boolean isNumeric = keyword.matches("\\d+");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT m.id AS member_id, m.name AS member_name, m.phone, " +
                         "b.id AS book_id, b.title, b.author, b.image_path, b.available, b.category, b.description, " +
                         "l.loan_date, l.due_date " +
                         "FROM members m " +
                         "LEFT JOIN loans l ON m.id = l.member_id AND l.returned = 0 " +
                         "LEFT JOIN books b ON l.book_id = b.id " +
                         "WHERE " + (isNumeric ? "m.id = ? OR " : "") +
                         "m.name LIKE ? OR m.phone LIKE ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            int index = 1;

            if (isNumeric) {
                stmt.setInt(index++, Integer.parseInt(keyword));
            }

            String pattern = "%" + keyword + "%";
            stmt.setString(index++, pattern); // name
            stmt.setString(index, pattern);   // phone

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                String memberName = rs.getString("member_name");
                String phone = rs.getString("phone");

                Member member = memberMap.getOrDefault(memberId, new Member(memberId, memberName, phone));

                int bookId = rs.getInt("book_id");
                if (!rs.wasNull()) {
                    String title = rs.getString("title");
                    String author = rs.getString("author");
                    String imagePath = rs.getString("image_path");
                    String category = rs.getString("category");
                    String description = rs.getString("description");
                    boolean available = rs.getBoolean("available");
                    LocalDateTime loanDate = rs.getTimestamp("loan_date").toLocalDateTime();
                    LocalDateTime dueDate = rs.getTimestamp("due_date").toLocalDateTime();

                    Book book = new Book(bookId, title, author, imagePath, category, description, available);
                    Loan loan = new Loan(book, member, loanDate, dueDate);
                    member.addLoan(loan);
                }

                memberMap.putIfAbsent(memberId, member);
            }

            membersList.addAll(memberMap.values());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return membersList;
    }
    
    
    public ArrayList<Loan> searchLoanHistory(String keyword) {
        ArrayList<Loan> loans = new ArrayList<>();
        boolean isNumeric = keyword.matches("\\d+");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT l.id AS loan_id, l.loan_date, l.due_date, l.returned, " +
                         "m.id AS member_id, m.name AS member_name, " +
                         "b.id AS book_id, b.title AS book_title " +
                         "FROM loans l " +
                         "JOIN members m ON l.member_id = m.id " +
                         "JOIN books b ON l.book_id = b.id " +
                         "WHERE " +
                         (isNumeric ? "l.id = ? OR m.id = ? OR b.id = ? OR " : "") +
                         "LOWER(m.name) LIKE ? OR LOWER(b.title) LIKE ? OR LOWER(CASE WHEN l.returned = 1 THEN 'Sudah Dikembalikan' ELSE 'Belum Dikembalikan' END) LIKE ? " +
                         "ORDER BY l.loan_date DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);

            int index = 1;
            if (isNumeric) {
                int id = Integer.parseInt(keyword);
                stmt.setInt(index++, id);  // l.id
                stmt.setInt(index++, id);  // m.id
                stmt.setInt(index++, id);  // b.id
            }

            String pattern = "%" + keyword.toLowerCase() + "%";
            stmt.setString(index++, pattern); // m.name
            stmt.setString(index++, pattern); // b.title
            stmt.setString(index, pattern);   // status

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int loanId = rs.getInt("loan_id");
                int memberId = rs.getInt("member_id");
                String memberName = rs.getString("member_name");
                int bookId = rs.getInt("book_id");
                String bookTitle = rs.getString("book_title");

                LocalDateTime loanDate = rs.getTimestamp("loan_date").toLocalDateTime();
                LocalDateTime dueDate = rs.getTimestamp("due_date").toLocalDateTime();
                boolean returned = rs.getBoolean("returned");

                String status = returned ? "Sudah Dikembalikan" : "Belum Dikembalikan";

                Loan loan = new Loan(loanId, bookId, bookTitle, memberId, memberName, loanDate, dueDate, status);
                loans.add(loan);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return loans;
    }









}
