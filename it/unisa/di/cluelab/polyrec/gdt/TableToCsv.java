package it.unisa.di.cluelab.polyrec.gdt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author roberto
 *
 */
public class TableToCsv {
	
	public TableToCsv(JTable table) {
		super();
		this.table = table;
	}


	private JTable table; 
	
	
	/**
	 * Convert and save table to csv file
	 * @throws IOException
	 */
	public void convert() throws IOException{
		
			 
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("features-table.csv"));
		FileNameExtensionFilter filter1 = new FileNameExtensionFilter("Comma-Separated Values (.csv)", "csv");
		chooser.setFileFilter(filter1);
        int state = chooser.showSaveDialog(null);
        File file = chooser.getSelectedFile();
        
        if (file != null && state == JFileChooser.APPROVE_OPTION) {
        	BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        	 
            FeaturesTableModel model = (FeaturesTableModel) table.getModel();
            for (int h = 0 ; h < model.getColumnCount();h++){
              bw.write(model.getColumnName(h).toString());
              if (h+1 != model.getColumnCount())
                bw.write(";");
            }
            bw.newLine();
           
            for (int clmCnt = model.getColumnCount(), rowCnt = model
                            .getRowCount(), i = 0; i < rowCnt; i++) {
                    for (int j = 0; j < clmCnt; j++) {
                            if (model.getValueAt(i, j) != null){
                              String value = model.getValueAt(i, j).toString();
                              bw.write(value);
                            }
                            if(j+1 != clmCnt)
                              bw.write(";");
                    }
                    bw.newLine();
            }

            bw.flush();
            bw.close();


        }
            
	}
}
