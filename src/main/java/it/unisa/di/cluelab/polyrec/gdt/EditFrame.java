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
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Edit class frame.
 */
@SuppressWarnings("checkstyle:classdataabstractioncoupling")
public class EditFrame extends JFrame {
    private static final long serialVersionUID = 6010850818289505846L;

    public EditFrame(String className, MainFrame mainClass) {
        setTitle("Edit Class " + className);

        setResizable(false);
        setSize(500, 500);
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final Box boxLayout = Box.createVerticalBox();

        // String[] labels = {"Score Limit: "};
        // int numPairs = labels.length;
        final int numPairs = 1;
        // Create and populate the panel.
        final JPanel p = new JPanel(new SpringLayout());
        // for (int i = 0; i < numPairs; i++) {
        final JLabel label = new JLabel("New Class Name", SwingConstants.TRAILING);

        p.add(label);
        final JTextField text = new JTextField(10);

        text.setText(className);
        text.setName("className");
        label.setLabelFor(text);
        p.add(text);
        // }

        // Lay out the panel.
        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(p, numPairs, 2, 10, 10, 10, 30);
        boxLayout.add(p);
        final JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // System.out.println("contains
                // -"+text.getText()+"-:?"+mainClass.recognizer.getClassNames().contains(text.getText()));
                if (mainClass.getRecognizer().getClassNames().contains(text.getText().toLowerCase())) {
                    JOptionPane.showMessageDialog(mainClass, "The class name " + text.getText() + " already exists ",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    mainClass.getRecognizer().editClassName(className, text.getText());
                    if (!className.equals(text.getText())) {
                        final DashboardScreen screen = new DashboardScreen(mainClass, false);
                        screen.display.setText("Class " + className + " successfully modified");
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
