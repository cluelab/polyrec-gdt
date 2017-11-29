package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.event.MouseEvent;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public class FeaturesTableHeader extends JTableHeader {
    
    public FeaturesTableHeader(TableColumnModel tcm)
    {
        super(tcm);
    }
  
    public String getToolTipText(MouseEvent e) { 
        String text ="";
        java.awt.Point p = e.getPoint();
        int colIndex = columnAtPoint(p);
        int realColumnIndex = getTable().convertColumnIndexToModel(colIndex);
        if (realColumnIndex == 3) 
            text = "Polyline lines";
        if (realColumnIndex == 4) 
            text = "Sum of absolute angle (..f10..)";
        if (realColumnIndex == 5) 
                text = "Length of path of the gesture (f8)";
        if (realColumnIndex == 6) 
            text = "Distance between first and last point (f5)";
        if (realColumnIndex == 7) 
            text = "Area of bounding box";
        if (realColumnIndex == 8) 
            text = "Length of diagonal of the bounding box (f3)";
        if (realColumnIndex == 9) 
            text = "Angle of Bounding Box (f4)";
        if (realColumnIndex == 10) 
            text = "Cosine of starting angle (...f1...)";
        if (realColumnIndex == 11) 
            text = "Sin of starting angle (...f2...)";
        if (realColumnIndex == 12) 
            text = "Cosine of end angle";
        if (realColumnIndex == 13) 
            text = "Angle of line from starting point to end point of gesture";
        if (realColumnIndex == 14) 
            text = "Cos of Angle of line from starting point to end point of gesture (f6)";
        if (realColumnIndex == 15) 
            text = "Sin of Angle of line from starting point to end point of gesture (f7)";
        return text;
    }   
}    