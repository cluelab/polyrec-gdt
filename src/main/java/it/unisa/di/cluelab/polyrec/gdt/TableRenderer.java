package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TableRenderer extends JLabel implements TableCellRenderer {


    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
    	
        setText(value.toString()); 
       
        String text = "Row = "+row+" and column = "+column;
        setToolTipText(text);
        return this; 
    }

}