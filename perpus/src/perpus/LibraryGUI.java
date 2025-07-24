package perpus;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;
import javax.swing.filechooser.FileNameExtensionFilter;


public class LibraryGUI {
    private JFrame frame;
    private JTextArea outputArea;
    private LibrarySystem library;

    public LibraryGUI() {
        // Terapkan FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Gagal menerapkan FlatLaf.");
        }

        library = new LibrarySystem();

        frame = new JFrame("📚 Aplikasi Perpustakaan");
        frame.setSize(950, 580);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // Langsung tampilkan daftar buku (viewBooks() akan menampilkan ke frame)
        try {
			viewBooks("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        frame.setVisible(true);
    }

    private void addBook() {
        String title = JOptionPane.showInputDialog("Judul Buku:");
        String author = JOptionPane.showInputDialog("Penulis:");
        String category = JOptionPane.showInputDialog("Kategori:");
        String description = JOptionPane.showInputDialog("Deskripsi:");

        if (title != null && author != null && category != null && description != null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Pilih Gambar Buku");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "jpeg", "gif"));

            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String imagePath = selectedFile.getAbsolutePath();

                // Tambahkan buku ke database
                library.addBookToDB(title, author, imagePath, category, description);

                JOptionPane.showMessageDialog(frame, "Buku berhasil ditambahkan!");
                try {
					viewBooks("");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } else {
                JOptionPane.showMessageDialog(frame, "Gambar buku belum dipilih.");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Semua kolom harus diisi.");
        }
    }



    private void addMember() throws Exception {
        String name = JOptionPane.showInputDialog("Nama Anggota:");
        if (name != null && !name.trim().isEmpty()) {
            String phone = JOptionPane.showInputDialog("Nomor Telepon:");
            if (phone != null && !phone.trim().isEmpty()) {
                library.addMemberToDB(name, phone);
                JOptionPane.showMessageDialog(frame, "Anggota berhasil ditambahkan!");
                viewMembers("");
            } else {
                JOptionPane.showMessageDialog(frame, "Nomor telepon tidak boleh kosong.");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Nama tidak boleh kosong.");
        }
    }


    private void borrowBook() {
        try {
            int bookId = Integer.parseInt(JOptionPane.showInputDialog("ID Buku yang ingin dipinjam:"));
            int memberId = Integer.parseInt(JOptionPane.showInputDialog("ID Anggota:"));

            Book book = library.getBookById(bookId);
            Member member = library.getMemberById(memberId); // Pastikan method ini tersedia

            if (member == null) {
                JOptionPane.showMessageDialog(frame, "Anggota dengan ID tersebut tidak ditemukan.");
                return;
            }

            if (book == null) {
                JOptionPane.showMessageDialog(frame, "Buku tidak ditemukan.");
                return;
            }

            if (!book.isAvailable()) {
                JOptionPane.showMessageDialog(frame, "Buku sedang dipinjam dan tidak tersedia.");
                return;
            }

            library.borrowBookFromDB(bookId, memberId);
            JOptionPane.showMessageDialog(frame, "Buku berhasil dipinjam!");
            viewLoanHistory("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Gagal meminjam buku: " + e.getMessage());
        }
    }


    private void returnBook() {
        try {
            int memberId = Integer.parseInt(JOptionPane.showInputDialog("Masukkan ID Member:"));
            int bookId = Integer.parseInt(JOptionPane.showInputDialog("Masukkan ID Buku yang ingin dikembalikan:"));

            Book book = library.getBookById(bookId);
            if (book == null) {
                JOptionPane.showMessageDialog(frame, "Buku tidak ditemukan.");
                return;
            }

            if (book.isAvailable()) {
                JOptionPane.showMessageDialog(frame, "Buku ini belum dipinjam, tidak perlu dikembalikan.");
                return;
            }

            // Ambil data pinjaman dan periksa keterlambatan
            Loan loan = library.getActiveLoan(bookId, memberId); // kamu perlu buat method ini
            if (loan == null) {
                JOptionPane.showMessageDialog(frame, "Data pinjaman tidak ditemukan.");
                return;
            }

            if (LocalDateTime.now().isAfter(loan.getDueDate())) {
                int confirm = JOptionPane.showConfirmDialog(frame,
                    "Buku telah melewati jatuh tempo.\nDenda sebesar Rp50.000 harus dibayar.\nBayar sekarang?",
                    "Denda Keterlambatan", JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(frame, "Pengembalian dibatalkan sampai denda dibayar.");
                    return;
                }
            }

            library.returnBookToDB(bookId, memberId);
            JOptionPane.showMessageDialog(frame, "Buku berhasil dikembalikan!");
            viewLoanHistory("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "ID harus berupa angka.");
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan: " + e.getMessage());
        }
    }



    private void viewBooks(String keyword) throws Exception {
        String[] columnNames = {"ID", "Gambar", "Judul", "Penulis", "Kategori", "Deskripsi", "Status", "Aksi"};
        
        ArrayList<Book> books;
        if (keyword == null || keyword.trim().isEmpty()) {
            books = library.getBooksFromDBList(); // ambil semua buku
        } else {
            books = library.searchBooks(keyword); // kamu perlu method ini
        }

        Object[][] data = new Object[books.size()][8];
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            ImageIcon icon = null;

            if (b.getImagePath() != null && !b.getImagePath().isEmpty()) {
                File imgFile = new File(b.getImagePath());
                if (imgFile.exists()) {
                    Image img = new ImageIcon(b.getImagePath()).getImage()
                            .getScaledInstance(50, 70, Image.SCALE_SMOOTH);
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
            data[i][7] = "Aksi";
        }

        JTable table = new JTable(new DefaultTableModel(data, columnNames));
        table.setRowHeight(80);
        table.getColumn("Aksi").setCellRenderer(new ButtonRenderer());
        table.getColumn("Aksi").setCellEditor(new ButtonEditor(table, library, () -> {
			try {
				viewBooks(keyword);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, true));
        table.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());

        JScrollPane scrollPane = new JScrollPane(table);

        // 🔍 Panel Search
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        searchPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(keyword);
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setToolTipText("Cari berdasarkan ID, judul, penulis, atau kategori");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JButton searchButton = new JButton("🔍 Cari");
        searchButton.setPreferredSize(new Dimension(100, 35));
        searchButton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        searchButton.setBackground(new Color(60, 120, 200));
        searchButton.setForeground(Color.WHITE);

        searchButton.addActionListener(e -> {
			try {
				viewBooks(searchField.getText().trim());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Panel gabungan tombol dan search
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(getButtonPanel(), BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        // ⬇️ Tambahkan ke frame
        frame.getContentPane().removeAll();
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }






    private void viewMembers(String keyword) throws Exception {
        String[] columnNames = {"ID", "Nama", "Telepon", "Buku yang Dipinjam", "Aksi"};
        ArrayList<Member> members;

        if (keyword == null || keyword.trim().isEmpty()) {
            members = library.getMembersWithLoansFromDBList();
        } else {
            members = library.searchMembers(keyword); // Pastikan method ini ada di LibrarySystem
        }

        Object[][] data = new Object[members.size()][5];
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            data[i][0] = m.getMemberId();
            data[i][1] = m.getName();
            data[i][2] = m.getPhone();
            data[i][4] = "Edit / Hapus";

            ArrayList<Loan> loans = (ArrayList<Loan>) m.getLoans();
            if (loans.isEmpty()) {
                data[i][3] = "Tidak meminjam buku";
            } else {
                StringBuilder borrowedBooks = new StringBuilder();
                for (Loan loan : loans) {
                    borrowedBooks.append(loan.getBook().getId())
                                 .append(" - ")
                                 .append(loan.getBook().getTitle())
                                 .append(",\n");
                }
                data[i][3] = borrowedBooks.substring(0, borrowedBooks.length() - 2);
            }
        }

        JTable table = new JTable(data, columnNames);
        table.setRowHeight(35);
        table.getColumnModel().getColumn(3).setCellRenderer(new MultiLineCellRenderer());
        table.getColumn("Aksi").setCellRenderer(new ButtonRenderer());
        table.getColumn("Aksi").setCellEditor(new ButtonEditor(table, library, () -> {
            try {
                viewMembers(""); // Refresh tanpa keyword
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, false));

        // Panel pencarian
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        searchPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 35));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setToolTipText("Cari berdasarkan ID, nama atau telepon");
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton searchButton = new JButton("🔍 Cari");
        searchButton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        searchButton.setBackground(new Color(60, 120, 200));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> {
            try {
                viewMembers(searchField.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Panel atas: gabungkan tombol dan search
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(getButtonPanel(), BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.revalidate();
        frame.repaint();
    }




    private JPanel getButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 10, 10));
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);

        JButton btnAddBook = new JButton("📘 Tambah Buku");
        JButton btnAddMember = new JButton("👤 Tambah Anggota");
        JButton btnBorrowBook = new JButton("📥 Pinjam Buku");
        JButton btnReturnBook = new JButton("📤 Kembalikan Buku");
        JButton btnViewBooks = new JButton("📚 Lihat Daftar Buku");
        JButton btnViewMembers = new JButton("👥 Lihat Anggota");
        JButton btnViewHistory = new JButton("📖 Riwayat Peminjaman");

        JButton[] buttons = {
            btnAddBook, btnAddMember, btnBorrowBook,
            btnReturnBook, btnViewBooks, btnViewMembers, btnViewHistory
        };

        for (JButton btn : buttons) {
            btn.setFont(emojiFont);
            btn.setFocusPainted(false);
            panel.add(btn);
        }

        // Event listeners
        btnAddBook.addActionListener(e -> addBook());
        btnAddMember.addActionListener(e -> {
            try {
                addMember();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        btnBorrowBook.addActionListener(e -> borrowBook());
        btnReturnBook.addActionListener(e -> returnBook());
        btnViewBooks.addActionListener(e -> {
			try {
				viewBooks("");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
        btnViewMembers.addActionListener(e -> {
            try {
                viewMembers("");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        btnViewHistory.addActionListener(e -> {
            try {
                viewLoanHistory("");
            } catch (Exception e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "Gagal menampilkan riwayat peminjaman.");
            }
        });

        return panel;
    }



    public static void main(String[] args) {
        new LibraryGUI();
    }
    
    private void viewLoanHistory(String keyword) throws Exception {
        String[] columnNames = {"ID", "Nama Anggota", "Judul Buku", "Tanggal Pinjam", "Jatuh Tempo", "Status"};
        ArrayList<Loan> history = library.searchLoanHistory(keyword);

        Object[][] data = new Object[history.size()][6];
        for (int i = 0; i < history.size(); i++) {
            Loan loan = history.get(i);
            data[i][0] = loan.getLoanId();
            data[i][1] = loan.getMemberId() + " - " + loan.getMemberName();
            data[i][2] = loan.getBookId() + " - " + loan.getBookTitle();
            data[i][3] = loan.getLoanDate();
            data[i][4] = loan.getDueDate();
            data[i][5] = loan.getStatus();
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            public Class<?> getColumnClass(int column) {
                Object value = getValueAt(0, column);
                return (value != null) ? value.getClass() : Object.class;
            }
        };

        JTable table = new JTable(model) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                int modelRow = convertRowIndexToModel(row);  // penting saat sortir

                Object statusObj = getModel().getValueAt(modelRow, 5); // Status
                Object dueDateObj = getModel().getValueAt(modelRow, 4); // Jatuh Tempo

                if (statusObj != null && dueDateObj != null && dueDateObj instanceof LocalDateTime) {
                    String status = statusObj.toString();
                    LocalDateTime dueDate = (LocalDateTime) dueDateObj;

                    if (!status.equalsIgnoreCase("Sudah Dikembalikan") && dueDate.isBefore(LocalDateTime.now())) {
                        comp.setBackground(Color.RED);
                        comp.setForeground(Color.WHITE);
                    } else {
                        comp.setBackground(Color.WHITE);
                        comp.setForeground(Color.BLACK);
                    }
                } else {
                    comp.setBackground(Color.WHITE);
                    comp.setForeground(Color.BLACK);
                }

                return comp;
            }
        };
        table.setRowHeight(35);

        // 🔀 Sorter
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING)); // Tanggal Pinjam
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        // ✅ Rata kiri untuk kolom ID
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer); // Kolom ID

        JScrollPane scrollPane = new JScrollPane(table);

        // 🔍 Search Panel
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 35));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setToolTipText("Cari berdasarkan nama, judul buku, atau status...");

        JButton searchButton = new JButton("🔍 Cari");
        searchButton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        searchButton.setBackground(new Color(60, 120, 200));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setPreferredSize(new Dimension(100, 35));
        searchButton.addActionListener(e -> {
            try {
                viewLoanHistory(searchField.getText().trim());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(getButtonPanel(), BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        frame.getContentPane().removeAll();
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }






}
