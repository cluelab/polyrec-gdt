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
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class Settings extends JFrame {

    public static Properties applicationProps = new Properties();

    public Settings() {
        setTitle("Settings");

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
        final JLabel scoreLimitLabel = new JLabel("Score Limit", SwingConstants.TRAILING);

        p.add(scoreLimitLabel);
        final JTextField scoreLimitText = new JTextField(5);

        scoreLimitText.setText(applicationProps.getProperty("scorelimit"));
        scoreLimitText.setName("scorelimit");
        scoreLimitLabel.setLabelFor(scoreLimitText);
        p.add(scoreLimitText);
        // }

        // Lay out the panel.
        SpringUtilities.makeCompactGrid(p, numPairs, 2, // rows, cols
                10, 10, // initX, initY
                10, 30); // xPad, yPad
        boxLayout.add(p);
        final JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {

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
            final FileInputStream in = new FileInputStream("config.properties");

            applicationProps.load(in);
        } catch (final IOException e) {

            e.printStackTrace();
        }
        applicationProps.putIfAbsent("scorelimit", "87");
    }

    public static void saveSettings() {
        try {
            final FileOutputStream out = new FileOutputStream("config.properties");

            applicationProps.store(out, "---No Comment---");
            out.close();
        } catch (final IOException e) {

            e.printStackTrace();
        }
    }

}
