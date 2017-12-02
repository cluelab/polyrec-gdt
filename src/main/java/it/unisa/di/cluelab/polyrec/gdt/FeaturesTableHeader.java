package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.event.MouseEvent;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * Features table header.
 */
public class FeaturesTableHeader extends JTableHeader {
    private static final long serialVersionUID = -948843630374291064L;

    public FeaturesTableHeader(TableColumnModel tcm) {
        super(tcm);
    }

    @Override
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:returncount"})
    public String getToolTipText(MouseEvent e) {
        final java.awt.Point p = e.getPoint();
        final int colIndex = columnAtPoint(p);
        final int realColumnIndex = getTable().convertColumnIndexToModel(colIndex);
        switch (realColumnIndex) {
            case 3:
                return "Polyline lines";
            case 4:
                return "Sum of absolute angle (..f10..)";
            case 5:
                return "Length of path of the gesture (f8)";
            case 6:
                return "Distance between first and last point (f5)";
            case 7:
                return "Area of bounding box";
            case 8:
                return "Length of diagonal of the bounding box (f3)";
            case 9:
                return "Angle of Bounding Box (f4)";
            case 10:
                return "Cosine of starting angle (...f1...)";
            case 11:
                return "Sin of starting angle (...f2...)";
            case 12:
                return "Cosine of end angle";
            case 13:
                return "Angle of line from starting point to end point of gesture";
            case 14:
                return "Cos of Angle of line from starting point to end point of gesture (f6)";
            case 15:
                return "Sin of Angle of line from starting point to end point of gesture (f7)";
            default:
                return "";
        }
    }
}
