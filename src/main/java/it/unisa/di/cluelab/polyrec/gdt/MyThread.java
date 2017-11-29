package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.Box;
import javax.swing.border.LineBorder;

import it.unisa.di.cluelab.polyrec.Gesture;

public class MyThread implements Runnable {

    Gesture g;
    private final Box templatePanel;
    private final String className;
    private final DashboardScreen dashboardScreen;
    private final int templateIndex;

    public MyThread(Gesture g, Box tpanel, String className, DashboardScreen dashboardScreen, int templateIndex) {
        this.g = g;
        this.templatePanel = tpanel;
        this.className = className;
        this.dashboardScreen = dashboardScreen;
        this.templateIndex = templateIndex;
    }

    @Override
    public void run() {

        final Thumbnail tempThumbnail = new Thumbnail(this.g);
        tempThumbnail.addMouseListener(dashboardScreen.dashboardListener);
        tempThumbnail.setName("thumbnail_" + className + "_" + templateIndex);
        tempThumbnail.setToolTipText("Show Template Detail");
        tempThumbnail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tempThumbnail.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {

                if (!MainFrame.isModalDialogShowing()) {
                    templatePanel.setBorder(new LineBorder(Color.lightGray, 2));
                }

            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!MainFrame.isModalDialogShowing()) {
                    templatePanel.setBorder(new LineBorder(new Color(0, 0, 0, 0), 2));
                }

            }
        });

        templatePanel.add(tempThumbnail);
        // System.out.println("fine disegno"+className+" "+templateIndex);
    }
}
