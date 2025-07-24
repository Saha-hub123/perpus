package perpus;

import java.awt.Component;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

class ImageRenderer extends DefaultTableCellRenderer {
    @Override
    public void setValue(Object value) {
        if (value instanceof ImageIcon) {
            setIcon((ImageIcon) value);
            setText("");
        } else {
            setIcon(null);
            setText(value != null ? value.toString() : "");
        }
    }
}

