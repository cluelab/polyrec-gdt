package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

public class PointersFrame extends JFrame {

	public PointersFrame(String className, int templateNumber, MainFrame mainClass) {
		setTitle("Number of Pointers");

		setResizable(false);
		setSize(500, 500);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Box boxLayout = Box.createVerticalBox();

		int numPairs = 1;
		// Create and populate the panel.
		JPanel p = new JPanel(new SpringLayout());

		JLabel label = new JLabel("Pointers", JLabel.TRAILING);

		p.add(label);
		int prevValue = mainClass.getRecognizer().getTemplate(className).get(templateNumber).getGesture().getPointers();
		SpinnerModel model = new SpinnerNumberModel(
				prevValue, // initial
				0, // min
				5, // max
				1); // step
		JSpinner pointersNum = new JSpinner(model);
		pointersNum.setToolTipText("Set number of pointers");
		

		p.add(pointersNum);
	

		// Lay out the panel.
		SpringUtilities.makeCompactGrid(p, numPairs, 2, // rows, cols
				10, 10, // initX, initY
				10, 30); // xPad, yPad
		boxLayout.add(p);
		JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				int newvalue = 1;
				try {
					newvalue = (Integer)pointersNum.getValue();
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(mainClass, "You have to enter an integer from 1 to 5", "Format Error",
							JOptionPane.WARNING_MESSAGE);

				}
				if (newvalue > 5) {

					JOptionPane.showMessageDialog(mainClass, "The number must be between 1 and 5", "Format Error",
							JOptionPane.WARNING_MESSAGE);
				} else {

					if (prevValue != newvalue) {
						mainClass.getRecognizer().getTemplate(className).get(templateNumber).getGesture()
								.setPointers(newvalue);

						DashboardScreen screen = new DashboardScreen(mainClass, false);
						screen.display.setText("Number of pointers for template " + templateNumber + " of class "
								+ className + " successfully modified");
						mainClass.setScreen(screen);
					}
					dispose();

				}

			}
		});
		okPanel.add(ok);
		boxLayout.add(okPanel);
		add(boxLayout);

		pack();
	}

}
