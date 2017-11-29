package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class Settings extends JFrame {

	public static Properties applicationProps = new Properties();

	public Settings() {
		setTitle("Settings");
	

		setResizable(false);
		setSize(500, 500);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Box boxLayout = Box.createVerticalBox();

		//String[] labels = { "Score Limit: " };
		//int numPairs = labels.length;
		int numPairs = 1;
		// Create and populate the panel.
		JPanel p = new JPanel(new SpringLayout());
		//for (int i = 0; i < numPairs; i++) {
			JLabel scoreLimitLabel = new JLabel("Score Limit", JLabel.TRAILING);
			
			p.add(scoreLimitLabel);
			JTextField scoreLimitText = new JTextField(5);
			
			scoreLimitText.setText(applicationProps.getProperty("scorelimit"));
			scoreLimitText.setName("scorelimit");
			scoreLimitLabel.setLabelFor(scoreLimitText);
			p.add(scoreLimitText);
		//}

		// Lay out the panel.
		SpringUtilities.makeCompactGrid(p, numPairs, 2, // rows, cols
				10, 10, // initX, initY
				10, 30); // xPad, yPad
		boxLayout.add(p);
		JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener(){

			
			
			@Override
			public void actionPerformed(ActionEvent e) {
				applicationProps.setProperty(scoreLimitText.getName(), scoreLimitText.getText());
				saveSettings();
				dispose();
				
			}
		});
		okPanel.add(ok);
		boxLayout.add(okPanel);
		add(boxLayout);

		pack();
	}

	public static void loadSettings() {

		try {
			FileInputStream in = new FileInputStream("config.properties");

			applicationProps.load(in);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public static void saveSettings() {
		try {
			FileOutputStream out = new FileOutputStream("config.properties");

			applicationProps.store(out, "---No Comment---");
			out.close();
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}

}
