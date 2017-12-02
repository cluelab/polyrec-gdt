package it.unisa.di.cluelab.polyrec.gdt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author roberto
 *
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public class TableToCsv {

    private final JTable table;

    public TableToCsv(JTable table) {
        super();
        this.table = table;
    }

    /**
     * Convert and save table to csv file.
     * 
     * @throws IOException
     *             If an exception occurs while writing the file
     */
    public void convert() throws IOException {

        final JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("features-table.csv"));
        final FileNameExtensionFilter filter1 = new FileNameExtensionFilter("Comma-Separated Values (.csv)", "csv");
        chooser.setFileFilter(filter1);
        final int state = chooser.showSaveDialog(null);
        final File file = chooser.getSelectedFile();

        if (file != null && state == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(file, StandardCharsets.ISO_8859_1.name())) {

                final FeaturesTableModel model = (FeaturesTableModel) table.getModel();
                for (int h = 0; h < model.getColumnCount(); h++) {
                    pw.print(model.getColumnName(h).toString());
                    if (h + 1 != model.getColumnCount()) {
                        pw.print(';');
                    }
                }
                pw.println();

                for (int clmCnt = model.getColumnCount(), rowCnt = model.getRowCount(), i = 0; i < rowCnt; i++) {
                    for (int j = 0; j < clmCnt; j++) {
                        if (model.getValueAt(i, j) != null) {
                            final String value = model.getValueAt(i, j).toString();
                            pw.print(value);
                        }
                        if (j + 1 != clmCnt) {
                            pw.print(';');
                        }
                    }
                    pw.println();
                }

                pw.flush();
            }

        }

    }
}
