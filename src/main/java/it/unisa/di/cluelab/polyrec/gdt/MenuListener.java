package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import it.unisa.di.cluelab.polyrec.bluetooh.sendapp.AvailableDevice;
import it.unisa.di.cluelab.polyrec.bluetooh.sendapp.ObexPutClient;
import it.unisa.di.cluelab.polyrec.bluetooh.sendapp.RemoteDeviceDiscovery;

/**
 * Menu listener.
 */
@SuppressWarnings({"checkstyle:classfanoutcomplexity", "checkstyle:classdataabstractioncoupling",
    "checkstyle:multiplestringliterals"})
public class MenuListener implements ActionListener {

    private final MainFrame mainFrame;

    public MenuListener(MainFrame mainFrame) {

        this.mainFrame = mainFrame;

    }

    @Override
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:executablestatementcount", "checkstyle:javancss",
        "checkstyle:methodlength", "checkstyle:npathcomplexity", "checkstyle:returncount"})
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == mainFrame.getMenu().exit) {

            mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
            return;
        }
        if (e.getSource() == mainFrame.getMenu().settings) {
            new Settings();
            return;
        }
        // samples
        if (e.getSource() == mainFrame.getMenu().replaceClassesSamples) {
            mainFrame.getRecognizer().removeClasses();
            mainFrame.getRecognizer().addSamples();
            mainFrame.setScreen(new DashboardScreen(mainFrame, true));
            mainFrame.getMenu().updateMenu();
            return;
        }
        if (e.getSource() == mainFrame.getMenu().addClassesSamples) {
            mainFrame.getRecognizer().addSamples();
            mainFrame.setScreen(new DashboardScreen(mainFrame, true));
            mainFrame.getMenu().updateMenu();
            return;
        }
        // open from file
        if (e.getSource() == mainFrame.getMenu().replaceClasses) {

            final DashboardScreen dashboardScreen = new DashboardScreen(mainFrame, true);

            CursorToolkit.startWaitCursor(mainFrame.getRootPane());
            // if (importsKB(ExtendedPolyRecognizerGSS.REPLACE)) {
            if (imports(ExtendedPolyRecognizerGSS.REPLACE)) {
                dashboardScreen.display.set("Class and Templates imported from selected file", 0);

                mainFrame.setScreen(new DashboardScreen(mainFrame, true));
                if (mainFrame.getOpenedFile() != null) {
                    mainFrame.setTitle("PolyRec GDT (" + mainFrame.getOpenedFile() + ")");
                }

            } else {
                dashboardScreen.display.set("Error importing gesture set from selected file", 1);
            }
            CursorToolkit.stopWaitCursor(mainFrame.getRootPane());
            mainFrame.getMenu().updateMenu();

            return;
        }
        if (e.getSource() == mainFrame.getMenu().addTemplates) {

            final DashboardScreen dashboardScreen = new DashboardScreen(mainFrame, true);
            CursorToolkit.startWaitCursor(mainFrame.getRootPane());
            try {
                if (imports(ExtendedPolyRecognizerGSS.ADD_TEMPLATES)) {

                    dashboardScreen.display.set("Class and Templates imported from selected file", 0);
                    mainFrame.setScreen(new DashboardScreen(mainFrame, true));
                    mainFrame.setTitle("PolyRec GDT (" + mainFrame.getOpenedFile() + ")");

                } else {
                    dashboardScreen.display.set("Error importing gesture set from selected file", 1);
                }
            } catch (final Exception e1) {

                e1.printStackTrace();
                CursorToolkit.stopWaitCursor(mainFrame.getRootPane());
            }
            CursorToolkit.stopWaitCursor(mainFrame.getRootPane());
            mainFrame.getMenu().updateMenu();
            return;
        }
        // export recognizer
        if (e.getSource() == mainFrame.getMenu().exportRecognizer) {
            if (mainFrame.getRecognizer().getTamplatesNumber() == 0) {
                JOptionPane.showMessageDialog(mainFrame, "No gesture to export.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            final String code = mainFrame.getRecognizer().exportJava();
            final String text = "You may recognize gestures with PolyRec in your Java application by pasting the\n"
                    + "code below into the body of a Java class (you may need to add the PolyRec library\n"
                    + "to the build path and import it.unisa.di.cluelab.polyrec.PolyRecognizerGSS).\n\n" + code;
            final JTextArea textarea = new JTextArea(text, 25, 82);
            textarea.setTabSize(4);
            textarea.setFont(new Font("monospaced", Font.PLAIN, 14));
            textarea.setEditable(false);
            final JPopupMenu popup = new JPopupMenu();
            final JMenuItem item = new JMenuItem(new DefaultEditorKit.CopyAction());
            item.setText("Copy");
            popup.add(item);
            textarea.setComponentPopupMenu(popup);
            final JScrollPane scrollPane = new JScrollPane(textarea);
            final String[] btns = new String[] {"Copy code to clipboard", "Cancel"};
            if (JOptionPane.showOptionDialog(mainFrame, scrollPane, "Export Recognizer", JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, btns, btns[1]) == JOptionPane.YES_OPTION) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null);
            }
            return;
        }
        // new Gesture Set
        if (e.getSource() == mainFrame.getMenu().newGS) {
            int result = JOptionPane.CLOSED_OPTION;
            if (mainFrame.getMenu().save.isEnabled() || mainFrame.getMenu().saveas.isEnabled()) {
                result = JOptionPane.showConfirmDialog(null, "Save Gesture Set before closing?", "Confirm",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    if (mainFrame.getMenu().save.isEnabled()) {
                        mainFrame.getMenu().save.doClick();
                    } else if (mainFrame.getMenu().saveas.isEnabled()) {
                        mainFrame.getMenu().saveas.doClick();
                    }

                }
            }
            mainFrame.getRecognizer().removeClasses();
            mainFrame.setOpenedFile("");
            final DashboardScreen dashboardScreen = new DashboardScreen(mainFrame, false);
            mainFrame.setScreen(dashboardScreen);
            mainFrame.setTitle("PolyRec GDT");

            mainFrame.getMenu().updateMenu();

        }
        if (e.getSource() == mainFrame.getMenu().saveas) {
            try {
                exports(true);
                mainFrame.setTitle("PolyRec GDT (" + mainFrame.getOpenedFile() + ")");

                if (mainFrame.getScreen() instanceof DashboardScreen) {
                    final DashboardScreen screen = (DashboardScreen) mainFrame.getScreen();
                    screen.display.set("File Saved", 0);
                } else if (mainFrame.getScreen() instanceof TemplateScreen) {
                    final TemplateScreen screen = (TemplateScreen) mainFrame.getScreen();
                    screen.display.set("File Saved", 0);
                }
            } catch (final Exception e1) {
                JOptionPane.showMessageDialog(mainFrame, "Error saving gesture set\n(" + e1.getMessage() + ")", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            mainFrame.getMenu().updateMenu();
        }
        // save gesture set
        if (e.getSource() == mainFrame.getMenu().save) {

            try {
                System.out.println(exports(false));
                if (mainFrame.getScreen() instanceof DashboardScreen) {
                    final DashboardScreen screen = (DashboardScreen) mainFrame.getScreen();
                    screen.display.set("File Saved", 0);
                } else if (mainFrame.getScreen() instanceof TemplateScreen) {
                    final TemplateScreen screen = (TemplateScreen) mainFrame.getScreen();
                    screen.display.set("File Saved", 0);
                }
            } catch (final Exception e1) {
                JOptionPane.showMessageDialog(mainFrame, "Error saving gesture set\n(" + e1.getMessage() + ")", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        // send app to mobile device to draw gestures
        if (e.getSource() == mainFrame.getMenu().send) {

            final JOptionPane messagePane = new JOptionPane("Searching for Devices...Please Wait...",
                    JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {}, null);

            final JDialog dialog = messagePane.createDialog(mainFrame, "Send App");

            CursorToolkit.startWaitCursor(messagePane);

            @SuppressWarnings("checkstyle:anoninnerlength")
            final SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() {

                    Vector<AvailableDevice> discoveredDevices = null;
                    try {

                        final RemoteDeviceDiscovery deviceDiscovery = new RemoteDeviceDiscovery();
                        discoveredDevices = deviceDiscovery.getDevicesDiscovered();
                        CursorToolkit.stopWaitCursor(messagePane);
                    } catch (final InterruptedException e) {

                        e.printStackTrace();
                    } catch (final BluetoothStateException e) {
                        dialog.dispose();
                        JOptionPane.showMessageDialog(null, "Attiva il Bluetooth del PC", "Bluetooth",
                                JOptionPane.WARNING_MESSAGE);

                    }
                    dialog.dispose();
                    if (discoveredDevices != null && discoveredDevices.size() > 0) {
                        final String[] possibilities = new String[discoveredDevices.size()];

                        for (int i = 0; i < discoveredDevices.size(); i++) {
                            final AvailableDevice availableDevice = discoveredDevices.elementAt(i);
                            try {
                                possibilities[i] = i + "." + availableDevice.getDevice().getFriendlyName(false) + " ("
                                        + availableDevice.getDevice().getBluetoothAddress() + ")";
                            } catch (final IOException e) {

                                e.printStackTrace();
                            }
                        }

                        final String selected = (String) JOptionPane.showInputDialog(mainFrame, "Send App to:",
                                "Devices Found", JOptionPane.WARNING_MESSAGE, null, possibilities, null);
                        final String element = selected.substring(0, selected.indexOf("."));
                        final ObexPutClient obexClient = new ObexPutClient(
                                discoveredDevices.elementAt(Integer.parseInt(element)).getObexServiceURL());
                        try {
                            obexClient.send();
                        } catch (final IOException e) {
                            JOptionPane.showMessageDialog(mainFrame, "Error Sending apk");
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "No device Found");
                    }

                    return null;
                }

                // this is called when background thread above has
                // completed.
                @Override
                protected void done() {
                    // dialog.dispose();
                };
            };
            sw.execute();

            dialog.setVisible(true);

            return;
        }
    }

    /**
     * 
     * Save gesture set.
     */
    boolean exports(boolean saveas) throws Exception {
        String choosedFile = "";
        String fileExt = "";
        if (saveas) {
            final JFileChooser saveFile = new JFileChooser();

            final FileNameExtensionFilter filter1 = new FileNameExtensionFilter(MainFrame.EXTENSION_XML, "xml");
            final FileNameExtensionFilter filter2 = new FileNameExtensionFilter(MainFrame.EXTENSION_PGS, "pgs");
            saveFile.setFileFilter(filter1);
            saveFile.setFileFilter(filter2);
            final int retrival = saveFile.showSaveDialog(null);

            if (retrival == JFileChooser.APPROVE_OPTION) {
                choosedFile = saveFile.getSelectedFile().toString();
                System.out.println("file selezionato" + saveFile.getSelectedFile().toString());
                fileExt = saveFile.getFileFilter().getDescription();
            } else {
                return false;
            }
        } else {
            choosedFile = mainFrame.getOpenedFile();
            fileExt = mainFrame.getExtOpenedFile();
        }

        if (fileExt != null && fileExt.equals(MainFrame.EXTENSION_XML)) {

            if (!choosedFile.endsWith(".xml")) {
                choosedFile = choosedFile + ".xml";
            }
            mainFrame.getRecognizer().saveTemplatesXML(new File(choosedFile));

        } else if (fileExt != null && fileExt.equals(MainFrame.EXTENSION_PGS)) {

            final File f1 = new File("gestures.pgs");
            mainFrame.getRecognizer().saveTemplatesPGS(f1);

            if (!choosedFile.endsWith(".pgs")) {
                choosedFile = choosedFile + ".pgs";
            }
            final File f2 = new File(choosedFile);

            copyFile(f1, f2);

        }

        if (saveas) {
            mainFrame.setOpenedFile(choosedFile);
            mainFrame.setExtOpenedFile(fileExt);

        }

        return true;

    }

    // aggiunto temporaneamente per importare dataset KB per valutazione tesi
    // boolean importsKB(int method) {
    //
    // // this.rInvariant = method2;
    //
    // // store = new GestureStore();
    //
    // // load gestures from logs
    // mainFrame.getRecognizer().removeClasses();
    // FileListManager flm = new FileListManager("datasets/mykb", "xml");
    // ArrayList<File> files = flm.getFileList();
    // System.out.println("Adding " + files.size() + " gestures to store");
    // for (int i = 0; i < files.size(); i++) {
    // FileImporter fi = new FileImporter(files.get(i));
    // Gesture gesture = fi.importInfo(true);
    //
    // // if (gesture.getInfo().getSubject() != 5)
    // mainFrame.getRecognizer().addTemplate(gesture.getInfo().getName(), gesture);
    //
    // }
    // System.out.println("Gestures added to store");
    // return true;
    // }

    @SuppressWarnings({"checkstyle:executablestatementcount", "checkstyle:returncount"})
    boolean imports(int method) {

        final JFileChooser openFile = new JFileChooser();

        final FileNameExtensionFilter filter1 = new FileNameExtensionFilter(MainFrame.EXTENSION_XML, "xml");
        final FileNameExtensionFilter filter2 = new FileNameExtensionFilter(MainFrame.EXTENSION_PGS, "pgs");

        openFile.setFileFilter(filter1);
        openFile.setFileFilter(filter2);

        final int retrival = openFile.showOpenDialog(null);
        try {
            if (retrival == JFileChooser.APPROVE_OPTION) {

                final String selectedFile = openFile.getSelectedFile().toString();
                final String fileExt = openFile.getFileFilter().getDescription();

                CursorToolkit.startWaitCursor(mainFrame.getRootPane());
                if (fileExt.equals(MainFrame.EXTENSION_XML)) {

                    final StreamSource schemaFile = new StreamSource(
                            getClass().getResourceAsStream("/schema-validator.xsd"));
                    final Source xmlFile = new StreamSource(new File(selectedFile));
                    final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    final Schema schema = schemaFactory.newSchema(schemaFile);
                    final Validator validator = schema.newValidator();
                    try {
                        validator.validate(xmlFile);
                        System.out.println(xmlFile.getSystemId() + " is valid");
                    } catch (final SAXException e) {
                        JOptionPane.showMessageDialog(mainFrame,
                                xmlFile.getSystemId() + " is NOT valid reason:" + e.getLocalizedMessage());
                        return false;
                    }

                    mainFrame.getRecognizer().loadTemplatesXML(openFile.getSelectedFile(),
                            method == ExtendedPolyRecognizerGSS.REPLACE);
                    System.out.println(mainFrame.getRecognizer().getTamplatesNumber());

                } else if (fileExt.equals(MainFrame.EXTENSION_PGS)) {

                    final File f1 = new File(selectedFile);

                    final File f2 = new File("gestures.pgs");

                    try (InputStream in = new FileInputStream(f1); OutputStream out = new FileOutputStream(f2)) {
                        final byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    }

                    mainFrame.getRecognizer().loadTemplatesPGS(f1, method == ExtendedPolyRecognizerGSS.REPLACE);

                }
                mainFrame.setExtOpenedFile(fileExt);
                mainFrame.setOpenedFile(selectedFile);

            }
        } catch (final Exception e) {
            CursorToolkit.stopWaitCursor(mainFrame.getRootPane());
            System.out.println("errore");
            JOptionPane.showMessageDialog(mainFrame, "Errore nell'importazione del File (" + e.getMessage() + ")");
            return false;
        }
        return true;

    }

    private void copyFile(File f1, File f2) throws IOException {

        try (InputStream in = new FileInputStream(f1); OutputStream out = new FileOutputStream(f2)) {
            final byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }

}
