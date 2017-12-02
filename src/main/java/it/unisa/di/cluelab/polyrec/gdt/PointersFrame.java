package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Number of pointers.
 */
@SuppressWarnings({"checkstyle:classdataabstractioncoupling", "checkstyle:multiplestringliterals"})
public class PointersFrame extends JFrame {
    private static final long serialVersionUID = -5197676772145110655L;

    public PointersFrame(String className, int templateNumber, MainFrame mainClass) {
        setTitle("Number of Pointers");

        setResizable(false);
        setSize(500, 500);
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final Box boxLayout = Box.createVerticalBox();

        final int numPairs = 1;
        // Create and populate the panel.
        final JPanel p = new JPanel(new SpringLayout());

        final JLabel label = new JLabel("Pointers", SwingConstants.TRAILING);

        p.add(label);
        final int prevValue = mainClass.getRecognizer().getTemplate(className).get(templateNumber).getGesture()
                .getPointers();
        // initial, min, max,step
        final SpinnerModel model = new SpinnerNumberModel(prevValue, 0, 5, 1);
        final JSpinner pointersNum = new JSpinner(model);
        pointersNum.setToolTipText("Set number of pointers");

        p.add(pointersNum);

        // Lay out the panel.
        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(p, numPairs, 2, 10, 10, 10, 30);
        boxLayout.add(p);
        final JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton ok = new JButton("OK");
        @SuppressWarnings("checkstyle:anoninnerlength")
        final ActionListener aListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int newvalue = 1;
                try {
                    newvalue = (Integer) pointersNum.getValue();
                } catch (final NumberFormatException ex) {
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

                        final DashboardScreen screen = new DashboardScreen(mainClass, false);
                        screen.display.setText("Number of pointers for template " + templateNumber + " of class "
                                + className + " successfully modified");
                        mainClass.setScreen(screen);
                    }
                    dispose();

                }

            }
        };
        ok.addActionListener(aListener);
        okPanel.add(ok);
        boxLayout.add(okPanel);
        add(boxLayout);

        pack();
    }

}
