package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * TableCellRenderer with tooltip.
 */
public class TableRenderer extends JLabel implements TableCellRenderer {
    private static final long serialVersionUID = -4126753932381447173L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        setText(value.toString());

        final String text = "Row = " + row + " and column = " + column;
        setToolTipText(text);
        return this;
    }

}
