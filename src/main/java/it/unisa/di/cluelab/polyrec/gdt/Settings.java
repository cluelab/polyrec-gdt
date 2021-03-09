package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Setting frame.
 */
@SuppressWarnings({"checkstyle:classdataabstractioncoupling", "checkstyle:multiplestringliterals"})
public class Settings extends JFrame {

    static final Properties APPLICATION_PROPS = new Properties();

    private static final long serialVersionUID = 8009954606425204167L;

    @SuppressWarnings("checkstyle:executablestatementcount")
    public Settings(MainFrame mainFrame) {

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

        scoreLimitText.setText(APPLICATION_PROPS.getProperty("scorelimit"));
        scoreLimitText.setName("scorelimit");
        scoreLimitLabel.setLabelFor(scoreLimitText);
        p.add(scoreLimitText);
        // }

        final JPanel r = new JPanel(new SpringLayout());
        final JLabel recognizerLabel = new JLabel("Recognizer", SwingConstants.TRAILING);
        r.add(recognizerLabel);
        final JComboBox<String> recognizerCB = new JComboBox<>(new String[] {"PolyRec", "$Q", "$P+", "$P"});
        recognizerCB.setName("recognizer");
        recognizerCB.setSelectedItem(APPLICATION_PROPS.getProperty(recognizerCB.getName()));
        r.add(recognizerCB);
        SpringUtilities.makeCompactGrid(r, numPairs, 2, 10, 10, 10, 30);

        // Lay out the panel.
        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(p, numPairs, 2, 10, 10, 10, 30);
        boxLayout.add(p);
        boxLayout.add(r);
        final JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                APPLICATION_PROPS.setProperty(scoreLimitText.getName(), scoreLimitText.getText());
                APPLICATION_PROPS.setProperty(recognizerCB.getName(), recognizerCB.getSelectedItem().toString());
                saveSettings();
                mainFrame.switchRecognizer(Settings.APPLICATION_PROPS.getProperty("recognizer"));
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
            final File config = new File("config.properties");
            if (config.exists()) {
                try (FileInputStream is = new FileInputStream(config)) {
                    APPLICATION_PROPS.load(is);
                }
            }
        } catch (final IOException e) {

            e.printStackTrace();
        }
        APPLICATION_PROPS.putIfAbsent("scorelimit", "87");
        APPLICATION_PROPS.putIfAbsent("recognizer", "PolyRec");
    }

    public static void saveSettings() {
        try {
            try (FileOutputStream out = new FileOutputStream("config.properties")) {
                APPLICATION_PROPS.store(out, "---No Comment---");
            }
        } catch (final IOException e) {

            e.printStackTrace();
        }
    }

}
