package perpus;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

public class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

    public MultiLineCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setText(value == null ? "" : value.toString());

        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        // Set ukuran lebar kolom, tinggi bebas agar hitung preferensi tinggi
        setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);
        int preferredHeight = getPreferredSize().height;

        if (table.getRowHeight(row) < preferredHeight) {
            table.setRowHeight(row, preferredHeight);
        }

        return this;
    }
}

