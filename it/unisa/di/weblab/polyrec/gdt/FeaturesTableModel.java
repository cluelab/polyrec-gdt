package it.unisa.di.weblab.polyrec.gdt;

import javax.swing.table.DefaultTableModel;

public class FeaturesTableModel extends DefaultTableModel {

	public FeaturesTableModel(Object rowData[][], Object columnNames[]) {
		super(rowData, columnNames);
	}

	@Override
	public Class getColumnClass(int col) {
		if (col == 2) // second column accepts only Integer
						// values
			return Integer.class;
		if (col > 2 && col<16) // second column accepts only
									// Integer
			// values
			return Double.class;
		else
			return String.class; // other columns accept String
									// values
	}
	
	 public boolean isCellEditable(int row, int column) {
         return false; //So I make every cell non-editable
     }
}