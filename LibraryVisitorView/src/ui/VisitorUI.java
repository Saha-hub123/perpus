package ui;

import dao.BookDAO;
import model.Book;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

public class VisitorUI extends JFrame {
    private JTable bookTable;
    private JTextField searchField;
    private JButton searchButton;
    private BookDAO bookDAO = new BookDAO();

    public VisitorUI() {
        // Gunakan FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Gagal mengatur FlatLaf.");
        }

        setTitle("📚 Katalog Buku - Pengunjung Perpustakaan");
        setSize(950, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(Color.white);

        // Panel atas (judul + search)
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        topPanel.setBackground(Color.white);

        JLabel title = new JLabel("📖 Daftar Buku", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        topPanel.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.white);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(200, 35));
        searchField.setToolTipText("Cari berdasarkan judul, penulis, kategori, atau ID...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        searchButton = new JButton("🔍 Cari");
        searchButton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        searchButton.setBackground(new Color(70, 130, 180));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setPreferredSize(new Dimension(100, 35));
        searchButton.addActionListener(e -> updateTable());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Tabel buku
        bookTable = new JTable();
        bookTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bookTable.setRowHeight(80);
        updateTable();

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(new EmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);
        
        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) {
                    int row = bookTable.getSelectedRow();
                    int bookId = (int) bookTable.getValueAt(row, 0);
                    Book selectedBook = bookDAO.getBookById(bookId);

                    if (selectedBook != null) {
                        new BookDetailDialog(VisitorUI.this, selectedBook).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(VisitorUI.this, "Buku tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        setVisible(true);
    }


    private void updateTable() {
        String keyword = searchField.getText().trim();
        ArrayList<Book> books = bookDAO.getAllBooks(keyword);

        String[] columnNames = {"ID", "Gambar", "Judul", "Penulis", "Kategori", "Deskripsi", "Status"};
        Object[][] data = new Object[books.size()][7];

        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            ImageIcon icon = null;
            String path = b.getImagePath();
            if (path != null && !path.isEmpty()) {
                File file = new File(path);
                if (file.exists()) {
                    Image img = new ImageIcon(path).getImage().getScaledInstance(60, 80, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(img);
                }
            }

            data[i][0] = b.getId();
            data[i][1] = icon;
            data[i][2] = b.getTitle();
            data[i][3] = b.getAuthor();
            data[i][4] = b.getCategory();
            data[i][5] = b.getDescription();
            data[i][6] = b.isAvailable() ? "Tersedia" : "Dipinjam";

        }
       


        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return false;
            }

            public Class<?> getColumnClass(int column) {
                return column == 1 ? ImageIcon.class : String.class;
            }
        };

        bookTable.setModel(model);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VisitorUI::new);
    }
}

