package perpus;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

class ButtonRenderer extends JPanel implements TableCellRenderer {
    public ButtonRenderer() {
        setLayout(new FlowLayout());
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Hapus");
        add(editBtn);
        add(deleteBtn);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}