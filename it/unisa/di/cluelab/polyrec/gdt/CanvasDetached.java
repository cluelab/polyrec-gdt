package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

import it.unisa.di.cluelab.polyrec.Gesture;

public class CanvasDetached extends JFrame {

	public CanvasDetached(Gesture gesture) {


		setUndecorated(true);
		setBackground(new Color(0,0,0, 0.00f));
	   
		setPreferredSize(new Dimension(600,600));
		setSize(new Dimension(600,600));
		ComponentResizer cr = new ComponentResizer();
		cr.registerComponent(this);
		cr.setSnapSize(new Dimension(10, 10));
		Thumbnail thumb = new Thumbnail(gesture);
		thumb.setOpaque(false);
		add(thumb);
	
		setVisible(true);
	
		
		
		
		
	}
	
	
	

}
