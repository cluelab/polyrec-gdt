package it.unisa.di.cluelab.polyrec.gdt;

import javax.swing.table.DefaultTableModel;

/**
 * Features table model.
 */
public class FeaturesTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 3109070163844054547L;

    public FeaturesTableModel(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
    }

    @Override
    @SuppressWarnings("checkstyle:returncount")
    public Class<?> getColumnClass(int col) {
        if (col == 2) {
            // second column accepts only Integer values
            return Integer.class;
        }
        if (col > 2 && col < 16) {
            // second column accepts only Integer values
            return Double.class;
        } else {
            // other columns accept String values
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // So I make every cell non-editable
        return false;
    }
}
