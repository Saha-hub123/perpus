package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import model.Book;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class BookDetailDialog extends JDialog {

    public BookDetailDialog(JFrame parent, Book book) {
        super(parent, "Detail Buku", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(contentPanel);

        // Gambar buku
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (book.getImagePath() != null && new File(book.getImagePath()).exists()) {
            ImageIcon icon = new ImageIcon(new ImageIcon(book.getImagePath()).getImage()
                    .getScaledInstance(150, 190, Image.SCALE_SMOOTH));
            imageLabel.setIcon(icon);
        } else {
            imageLabel.setText("Tidak ada gambar");
            imageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        }
        contentPanel.add(imageLabel, BorderLayout.WEST);

        // Detail buku
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        detailPanel.add(createLabel("Judul:", book.getTitle()));
        detailPanel.add(createLabel("Penulis:", book.getAuthor()));
        detailPanel.add(createLabel("Kategori:", book.getCategory()));
        detailPanel.add(createLabel("Deskripsi:", "<html><body style='width:250px'>" + book.getDescription() + "</body></html>"));
        detailPanel.add(createLabel("Status:", book.isAvailable() ? "Tersedia" : "Dipinjam"));

        contentPanel.add(detailPanel, BorderLayout.CENTER);
    }

    private JPanel createLabel(String title, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JLabel labelTitle = new JLabel(title);
        labelTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel labelValue = new JLabel(value);
        labelValue.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(labelTitle);
        panel.add(labelValue);
        panel.setOpaque(false);
        return panel;
    }
}

