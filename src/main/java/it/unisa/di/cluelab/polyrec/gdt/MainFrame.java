package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import it.unisa.di.cluelab.polyrec.Gesture;

/**
 * @author roberto
 *
 */
public class MainFrame extends JFrame implements WindowListener {
    static final String EXTENSION_PGS = "PolyRec gesture set (.pgs)";
    static final String EXTENSION_XML = "Extensible markup language (.xml)";

    static final int MAINSCREEN = 0;
    static final int DETAILSCREEN = 1;

    private static final long serialVersionUID = 4781489406562650482L;

    protected int screenMode = MAINSCREEN;

    // name of opened file
    private String openedFile;
    // extension of opened file
    private String extOpenedFile;

    private transient GDTRecognizer recognizer;

    private JPanel container;
    private final transient Menu menu;

    public MainFrame(String recognizer) throws IOException {

        switchRecognizer(recognizer);

        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setTitle("PolyRec GDT");

        // setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 980));
        setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);

        setVisible(true);

        menu = new Menu(this);
        setScreen(new DashboardScreen(this, false));

    }

    public void setScreen(JPanel panel) {

        container = panel;

        setContentPane(container);

        repaint();

    }

    public JPanel getScreen() {
        return container;

    }

    @Override
    public void paint(Graphics g) {

        paintComponents(g);

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        final File file = new File("gestures.pgs");
        file.deleteOnExit();
        int result = JOptionPane.CLOSED_OPTION;

        if (menu.save.isEnabled() || menu.saveas.isEnabled()) {
            result = JOptionPane.showConfirmDialog(null, "Save Gesture Set before closing?", "Confirm",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                if (menu.save.isEnabled()) {
                    menu.save.doClick();
                } else if (menu.saveas.isEnabled()) {
                    menu.saveas.doClick();
                }

                dispose();
                System.exit(0);
            } else if (result == JOptionPane.NO_OPTION) {

                dispose();
                System.exit(0);
            }

        } else {

            dispose();
            System.exit(0);
        }

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    public GDTRecognizer getRecognizer() {
        return recognizer;
    }

    public void setRecognizer(GDTRecognizer recognizer) {
        this.recognizer = recognizer;
    }

    public String getOpenedFile() {
        return openedFile;
    }

    public void setOpenedFile(String openedFile) {
        this.openedFile = openedFile;
    }

    public String getExtOpenedFile() {
        return extOpenedFile;
    }

    public void setExtOpenedFile(String extOpenedFile) {
        this.extOpenedFile = extOpenedFile;
    }

    public Menu getMenu() {
        return menu;
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    public void switchRecognizer(String recognizerName) {
        GDTRecognizer old = null;
        switch (recognizerName) {
            case "PolyRec":
                if (!(recognizer instanceof ExtendedPolyRecognizerGSS)) {
                    old = recognizer;
                    recognizer = new ExtendedPolyRecognizerGSS();
                }
                break;
            case "$Q":
                if (!(recognizer instanceof DollarRecognizer)
                        || ((DollarRecognizer) recognizer).getType() != DollarRecognizer.Type.Q) {
                    old = recognizer;
                    recognizer = new DollarRecognizer(DollarRecognizer.Type.Q);
                }
                break;
            case "$P+":
                if (!(recognizer instanceof DollarRecognizer)
                        || ((DollarRecognizer) recognizer).getType() != DollarRecognizer.Type.P_PLUS) {
                    old = recognizer;
                    recognizer = new DollarRecognizer(DollarRecognizer.Type.P_PLUS);
                }
                break;
            case "$P":
                if (!(recognizer instanceof DollarRecognizer)
                        || ((DollarRecognizer) recognizer).getType() != DollarRecognizer.Type.P) {
                    old = recognizer;
                    recognizer = new DollarRecognizer(DollarRecognizer.Type.P);
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal recognizer: " + recognizerName);
        }
        if (old != null) {
            for (Entry<String, List<Gesture>> e : old.getTemplates().entrySet()) {
                recognizer.addTemplates(e.getKey(), e.getValue());
            }
        }
    }

    public static boolean isModalDialogShowing() {
        final Window[] windows = Window.getWindows();
        if (windows != null) {
            for (final Window w : windows) {
                if (w.isShowing() && w instanceof Dialog) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        Settings.loadSettings();

        new MainFrame(Settings.APPLICATION_PROPS.getProperty("recognizer"));
    }

}
