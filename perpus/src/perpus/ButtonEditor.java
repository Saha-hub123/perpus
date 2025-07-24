package perpus;

import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellEditor;

class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
    private final JPanel panel = new JPanel();
    private final JButton editBtn = new JButton("Edit");
    private final JButton deleteBtn = new JButton("Hapus");
    private int selectedRow;
    private final JTable table;
    private final LibrarySystem library;
    private final Runnable refreshAction;
    private final boolean isBookTable;

    public ButtonEditor(JTable table, LibrarySystem library, Runnable refreshAction, boolean isBookTable) {
        this.table = table;
        this.library = library;
        this.refreshAction = refreshAction;
        this.isBookTable = isBookTable;

        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.add(editBtn);
        panel.add(deleteBtn);

        editBtn.addActionListener(e -> {
            fireEditingStopped();
            int id = (int) table.getValueAt(selectedRow, 0);
            if (isBookTable) {
            	String title = JOptionPane.showInputDialog("Judul baru:", table.getValueAt(selectedRow, 2));
            	String author = JOptionPane.showInputDialog("Penulis baru:", table.getValueAt(selectedRow, 3));
            	String category = JOptionPane.showInputDialog("Kategori baru:", table.getValueAt(selectedRow, 4));
            	String description = JOptionPane.showInputDialog("Deskripsi baru:", table.getValueAt(selectedRow, 5));

            	if (title != null && author != null && category != null && description != null) {
            	    int confirm = JOptionPane.showConfirmDialog(null, "Ingin mengubah gambar buku?", "Ubah Gambar", JOptionPane.YES_NO_OPTION);
            	    String imagePath = null;

            	    if (confirm == JOptionPane.YES_OPTION) {
            	        JFileChooser fileChooser = new JFileChooser();
            	        fileChooser.setDialogTitle("Pilih Gambar Baru");
            	        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
            	        int result = fileChooser.showOpenDialog(null);

            	        if (result == JFileChooser.APPROVE_OPTION) {
            	            File selectedFile = fileChooser.getSelectedFile();
            	            imagePath = selectedFile.getAbsolutePath();
            	        }
            	    }

            	    // Panggil fungsi dengan urutan: title, author, imagePath, category, description
            	    library.updateBookInDB(id, title, author, imagePath, category, description);
            	    refreshAction.run();
            	}



            } else {
            	String name = JOptionPane.showInputDialog("Nama baru:", table.getValueAt(selectedRow, 1));
            	if (name != null && !name.trim().isEmpty()) {
            	    String phone = JOptionPane.showInputDialog("Nomor Telepon baru:");
            	    if (phone != null && !phone.trim().isEmpty()) {
            	        library.updateMemberInDB(id, name, phone);
            	        refreshAction.run();
            	    } else {
            	        JOptionPane.showMessageDialog(null, "Nomor telepon tidak boleh kosong.");
            	    }
            	}

            }
        });

        deleteBtn.addActionListener(e -> {
            fireEditingStopped();
            int id = (int) table.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(null,
                    isBookTable ? "Hapus buku ini?" : "Hapus anggota ini?", 
                    "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (isBookTable) {
                        library.deleteBookFromDB(id);
                    } else {
                        library.deleteMemberFromDB(id);
                    }
                    refreshAction.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Gagal menghapus: " + ex.getMessage());
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                  boolean isSelected, int row, int column) {
        this.selectedRow = row;
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }
}
