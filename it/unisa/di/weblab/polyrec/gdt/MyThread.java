package it.unisa.di.weblab.polyrec.gdt;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import it.unisa.di.weblab.polyrec.Gesture;

public class MyThread implements Runnable {

	Gesture g;
	private Box templatePanel;
	private String className;
	private DashboardScreen dashboardScreen;
	private int templateIndex;
	
	   public MyThread(Gesture g, Box tpanel, String className, DashboardScreen dashboardScreen, int templateIndex) {
		  this.g = g;
		  this.templatePanel = tpanel;
		  this.className = className;
		  this.dashboardScreen = dashboardScreen;
		  this.templateIndex = templateIndex;
	   }

	   public void run() {
		   
		   Thumbnail tempThumbnail = new Thumbnail(this.g);
			tempThumbnail.addMouseListener(dashboardScreen.dashboardListener);
			tempThumbnail.setName("thumbnail_" + className + "_" + templateIndex);
			tempThumbnail.setToolTipText("Show Template Detail");
			tempThumbnail.setCursor(new Cursor(Cursor.HAND_CURSOR));
			tempThumbnail.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseEntered(java.awt.event.MouseEvent evt) {

					if (!MainFrame.isModalDialogShowing())
						templatePanel.setBorder(new LineBorder(Color.lightGray, 2));

				}

				public void mouseExited(java.awt.event.MouseEvent evt) {
					if (!MainFrame.isModalDialogShowing())
						templatePanel.setBorder(new LineBorder(new Color(0, 0, 0, 0), 2));

				}
			});

			templatePanel.add(tempThumbnail);
			//System.out.println("fine disegno"+className+" "+templateIndex);
	   }
	}