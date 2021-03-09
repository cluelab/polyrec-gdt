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

import org.apache.commons.lang3.SystemUtils;
import org.xml.sax.SAXException;

import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.GestureInfo;
import it.unisa.di.cluelab.polyrec.TPoint;
import it.unisa.di.cluelab.polyrec.bluetooth.sendapp.AvailableDevice;
import it.unisa.di.cluelab.polyrec.bluetooth.sendapp.ObexPutClient;
import it.unisa.di.cluelab.polyrec.bluetooth.sendapp.RemoteDeviceDiscovery;

/**
 * Menu listener.
 */
@SuppressWarnings({"checkstyle:classfanoutcomplexity", "checkstyle:classdataabstractioncoupling",
    "checkstyle:multiplestringliterals"})
public class MenuListener implements ActionListener {
    // removing previous and then add classes from file
    private static final int REPLACE = 0;
    // add new classes without remove previous, class with same name are replaced
    @SuppressWarnings("unused")
    private static final int ADD = 1;
    // add new classes and add templates to class with same name
    private static final int ADD_TEMPLATES = 2;

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
            new Settings(mainFrame);
            return;
        }
        // samples
        if (e.getSource() == mainFrame.getMenu().replaceClassesSamples) {
            mainFrame.getRecognizer().removeClasses();
            addSamples(mainFrame.getRecognizer());
            mainFrame.setScreen(new DashboardScreen(mainFrame, true));
            mainFrame.getMenu().updateMenu();
            return;
        }
        if (e.getSource() == mainFrame.getMenu().addClassesSamples) {
            addSamples(mainFrame.getRecognizer());
            mainFrame.setScreen(new DashboardScreen(mainFrame, true));
            mainFrame.getMenu().updateMenu();
            return;
        }
        // open from file
        if (e.getSource() == mainFrame.getMenu().replaceClasses) {

            final DashboardScreen dashboardScreen = new DashboardScreen(mainFrame, true);

            CursorToolkit.startWaitCursor(mainFrame.getRootPane());
            // if (importsKB(ExtendedPolyRecognizerGSS.REPLACE)) {
            if (imports(REPLACE)) {
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
                if (imports(ADD_TEMPLATES)) {

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
            if (mainFrame.getRecognizer().getTemplatesNumber() == 0) {
                JOptionPane.showMessageDialog(mainFrame, "No gesture to export.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            final String code = mainFrame.getRecognizer().exportJava();
            final String rec = Settings.APPLICATION_PROPS.getProperty("recognizer");
            final String text = "You may recognize gestures with " + rec + " in your Java application by pasting the\n"
                    + "code below into the body of a Java class (you may need to add the " + rec + " library\n"
                    + "to the build path).\n\n" + code;
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
            if (!SystemUtils.IS_OS_WINDOWS) {
                JOptionPane.showMessageDialog(mainFrame, "Bluetooth works only on Windows.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

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
                }
            };
            sw.execute();

            dialog.setVisible(true);

            return;
        }
    }

    /**
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
            try (FileOutputStream out = new FileOutputStream(choosedFile)) {
                mainFrame.getRecognizer().saveTemplatesXML(out);
            }

        } else if (fileExt != null && fileExt.equals(MainFrame.EXTENSION_PGS)) {

            final File f1 = new File("gestures.pgs");
            try (FileOutputStream out = new FileOutputStream(f1)) {
                mainFrame.getRecognizer().saveTemplatesPGS(out);
            }

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

                    try (FileInputStream in = new FileInputStream(openFile.getSelectedFile())) {
                        mainFrame.getRecognizer().loadTemplatesXML(in, method == REPLACE);
                    }
                    System.out.println(mainFrame.getRecognizer().getTemplatesNumber());

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

                    try (FileInputStream in = new FileInputStream(f1)) {
                        mainFrame.getRecognizer().loadTemplatesPGS(in, method == REPLACE);
                    }

                }
                mainFrame.setExtOpenedFile(fileExt);
                mainFrame.setOpenedFile(selectedFile);

            }
        } catch (final RuntimeException e) {
            throw e;
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

    private void addTemplate(GDTRecognizer recognizer, String name, int... xys) {
        final Gesture g = new Gesture();
        for (int i = 0; i < xys.length; i += 2) {
            g.addPoint(new TPoint(xys[i], xys[i + 1], 0));
        }
        g.setInfo(new GestureInfo(0, null, name, 0));
        recognizer.addTemplate(name, g);
    }

    // gestural keyboard
    @SuppressWarnings({"checkstyle:executablestatementcount", "checkstyle:javancss", "checkstyle:methodlength"})
    void addSamples(GDTRecognizer recognizer) {
        String cname;
        cname = "left";
        // addTemplate(recognizer, cname, 0, 0, -32, 0, -64, 0);

        cname = "right";
        // addTemplate(recognizer, cname, 0, 0, 32, 0, 64, 0);

        cname = "up";
        // addTemplate(recognizer, cname, 0, 0, 0, -32, 0, -64);

        cname = "down";
        // addTemplate(recognizer, cname, 0, 0, 0, 32, 0, 64);

        cname = "paste";
        // addTemplate(recognizer, cname, 0, 0, 32, 64, 64, 0);
        // addTemplate(recognizer, cname, 0, 0, 32, 48, 64, 0);

        cname = "Check";
        // addTemplate(recognizer, cname, 91, 185, 93, 185, 95, 185, 97, 185, 100, 188, 102, 189, 104, 190, 106, 193,
        // 108, 195, 110,
        // 198, 112, 201, 112, 204, 114, 204, 115, 207, 117, 210, 118, 212, 120, 214, 121, 217, 122, 219, 123, 222,
        // 124, 224, 126, 226, 127, 229, 129, 231, 130, 233, 129, 231, 129, 228, 129, 226, 129, 224, 129, 221, 129,
        // 218, 129, 212, 129, 208, 130, 198, 132, 189, 134, 182, 137, 173, 143, 164, 147, 157, 151, 151, 155, 144,
        // 161, 137, 165, 131, 171, 122, 174, 118, 176, 114, 177, 112, 177, 114, 175, 116, 173, 118);

        cname = "Caret";
        // addTemplate(recognizer, cname, 79, 245, 79, 242, 79, 239, 80, 237, 80, 234, 81, 232, 82, 230, 84, 224, 86,
        // 220, 86, 218,
        // 87, 216, 88, 213, 90, 207, 91, 202, 92, 200, 93, 194, 94, 192, 96, 189, 97, 186, 100, 179, 102, 173, 105,
        // 165, 107, 160, 109, 158, 112, 151, 115, 144, 117, 139, 119, 136, 119, 134, 120, 132, 121, 129, 122, 127,
        // 124, 125, 126, 124, 129, 125, 131, 127, 132, 130, 136, 139, 141, 154, 145, 166, 151, 182, 156, 193, 157,
        // 196, 161, 209, 162, 211, 167, 223, 169, 229, 170, 231, 173, 237, 176, 242, 177, 244, 179, 250, 181, 255,
        // 182, 257);

        cname = "xDown";
        // addTemplate(recognizer, cname, 0, 0, 48, 48, 40, 56, 32, 64, 24, 56, 16, 48, 64, 0);

        cname = "cut";
        // addTemplate(recognizer, cname, 87, 142, 89, 145, 91, 148, 93, 151, 96, 155, 98, 157, 100, 160, 102, 162, 106,
        // 167, 108,
        // 169, 108, 169, 110, 171, 115, 177, 119, 183, 123, 189, 127, 193, 129, 196, 133, 200, 137, 206, 140, 209, 143,
        // 212, 146, 215, 151, 220, 153, 222, 155, 223, 157, 225, 158, 223, 157, 218, 155, 211, 154, 208, 152, 200,
        // 150, 189, 148, 179, 147, 170, 147, 158, 147, 148, 147, 141, 147, 136, 144, 135, 142, 137, 140, 139, 135,
        // 145, 131, 152, 124, 163, 116, 177, 108, 191, 100, 206, 94, 217, 91, 222, 89, 225, 87, 226, 87, 224);

        cname = "copy";
        // addTemplate(recognizer, cname, 63, 10, 44, 0, 20, 0, 10, 5, 1, 20, 0, 32, 1, 44, 10, 59, 20, 63, 44, 63, 63,
        // 54);
        // addTemplate(recognizer, cname, 63, 0, 20, 32, 63, 66);

        // da rivedere (dovrebbe essere una z)
        cname = "exit";
        // addTemplate(recognizer, cname, 0, 0, 64, 0, 64, 64, 0, 64);

        cname = "o";
        addTemplate(recognizer, cname, 832, 134, 826, 134, 826, 128, 793, 107, 753, 98, 706, 100, 618, 129, 574, 158,
                529, 210, 482, 275, 449, 348, 435, 423, 444, 489, 486, 551, 568, 606, 621, 628, 741, 636, 857, 608, 937,
                556, 991, 496, 1035, 421, 1066, 350, 1068, 290, 1033, 223, 980, 160, 883, 103, 743, 82, 684, 92);
        addTemplate(recognizer, cname, 563, 243, 561, 243, 555, 244, 537, 249, 503, 259, 472, 270, 445, 284, 432, 294,
                411, 312, 391, 336, 370, 377, 357, 424, 349, 472, 348, 513, 352, 556, 361, 599, 376, 642, 400, 680, 429,
                712, 472, 744, 521, 774, 573, 793, 625, 804, 680, 808, 727, 806, 772, 799, 810, 789, 843, 774, 873, 753,
                898, 729, 921, 700, 948, 663, 968, 627, 994, 536, 998, 488, 999, 448, 997, 404, 993, 357, 985, 304, 972,
                257, 953, 221, 931, 195, 898, 170, 857, 150, 817, 137, 776, 129, 740, 125, 702, 126, 701, 126);
        addTemplate(recognizer, cname, 896, 375, 891, 372, 879, 368, 854, 360, 822, 348, 780, 334, 744, 326, 705, 319,
                663, 317, 620, 318, 578, 325, 541, 336, 505, 352, 472, 370, 448, 389, 424, 413, 402, 441, 386, 471, 375,
                503, 372, 536, 376, 569, 384, 600, 395, 628, 411, 655, 430, 682, 453, 709, 486, 740, 526, 768, 571, 791,
                619, 802, 670, 809, 721, 809, 775, 803, 828, 792, 879, 773, 928, 751, 969, 724, 1003, 691, 1035, 647,
                1059, 592, 1070, 533, 1068, 471, 1049, 412, 1014, 365, 939, 322, 825, 304, 688, 319, 563, 352);

        cname = "f";
        addTemplate(recognizer, cname, 513, 524, 515, 524, 524, 524, 545, 521, 579, 512, 614, 494, 653, 468, 691, 436,
                722, 395, 753, 350, 776, 299, 791, 258, 799, 221, 804, 190, 805, 169, 804, 156, 803, 148, 801, 143, 799,
                139, 797, 135, 794, 133, 790, 131, 786, 131, 781, 131, 777, 134, 771, 139, 762, 156, 750, 196, 742, 253,
                739, 316, 740, 382, 747, 452, 754, 524, 762, 592, 768, 648, 771, 696, 773, 733, 772, 762, 768, 784, 762,
                802, 754, 814, 734, 823, 688, 825, 628, 812, 577, 798, 545, 787, 528, 777, 520, 767, 516, 752, 521, 732,
                533, 709, 565, 678, 612, 645, 678, 617, 752, 595, 827, 577, 904, 558, 979, 536, 1033, 517);
        addTemplate(recognizer, cname, 433, 570, 436, 569, 448, 567, 475, 564, 514, 556, 571, 539, 634, 513, 694, 480,
                752, 443, 800, 401, 838, 360, 868, 313, 886, 265, 887, 218, 875, 174, 851, 131, 823, 98, 782, 74, 744,
                65, 711, 73, 687, 93, 665, 133, 654, 188, 655, 253, 663, 328, 677, 400, 692, 472, 705, 538, 713, 598,
                717, 654, 714, 704, 701, 752, 681, 792, 657, 822, 632, 842, 605, 855, 575, 854, 553, 839, 531, 797, 518,
                746, 520, 694, 534, 646, 559, 604, 599, 567, 672, 530, 754, 510, 840, 493, 913, 471, 965, 444, 971,
                439);
        addTemplate(recognizer, cname, 599, 698, 599, 695, 599, 694, 606, 690, 633, 677, 676, 651, 726, 615, 774, 573,
                820, 525, 866, 471, 901, 420, 922, 375, 931, 337, 927, 306, 912, 280, 879, 257, 832, 239, 778, 236, 732,
                243, 678, 262, 645, 283, 622, 305, 608, 328, 604, 352, 610, 380, 629, 411, 667, 445, 725, 487, 780, 524,
                827, 560, 866, 597, 897, 644, 915, 698, 919, 754, 911, 806, 885, 851, 847, 886, 781, 917, 710, 925, 646,
                916, 593, 896, 558, 874, 540, 854, 529, 827, 541, 781, 582, 719, 667, 633, 795, 525, 944, 413, 1111,
                297, 1145, 274);

        cname = "v";
        addTemplate(recognizer, cname, 568, 288, 568, 288, 568, 288, 569, 291, 577, 313, 592, 369, 610, 452, 643, 634,
                675, 766, 686, 801, 692, 816, 694, 820, 695, 821, 694, 819, 695, 816, 699, 809, 709, 787, 729, 748, 764,
                687, 811, 607, 867, 516, 931, 417, 996, 320, 1059, 231, 1108, 169, 1148, 127);
        addTemplate(recognizer, cname, 481, 243, 481, 243, 482, 245, 488, 255, 500, 279, 517, 314, 534, 354, 555, 396,
                581, 440, 609, 493, 633, 544, 653, 593, 669, 636, 683, 674, 695, 705, 705, 730, 713, 754, 720, 774, 726,
                792, 731, 805, 737, 817, 743, 826, 748, 834, 752, 840, 755, 843, 758, 845, 760, 846, 762, 846, 764, 844,
                766, 840, 769, 830, 774, 812, 781, 781, 788, 741, 795, 699, 801, 657, 805, 615, 809, 571, 814, 531, 819,
                491, 828, 452, 842, 412, 859, 376, 878, 339, 896, 301, 911, 270, 927, 243, 938, 222, 946, 208, 951, 199,
                953, 193, 953, 189, 953, 187, 951, 185, 950, 184);
        addTemplate(recognizer, cname, 500, 321, 500, 321, 500, 321, 502, 325, 519, 361, 548, 441, 583, 539, 615, 635,
                630, 685, 651, 751, 664, 794, 672, 816, 675, 823, 676, 826, 676, 826, 676, 824, 678, 819, 686, 802, 705,
                763, 734, 701, 772, 626, 813, 549, 863, 467, 912, 394, 956, 342, 998, 294, 1030, 265, 1053, 245, 1063,
                239, 1067, 237, 1068, 237, 1068, 239, 1068, 241);

        cname = "s";
        addTemplate(recognizer, cname, 954, 98, 949, 95, 922, 84, 864, 73, 785, 76, 694, 101, 624, 139, 580, 168, 552,
                199, 538, 235, 550, 269, 591, 305, 679, 338, 780, 356, 883, 371, 969, 386, 1042, 411, 1078, 437, 1095,
                476, 1080, 529, 1035, 582, 957, 639, 862, 683, 755, 711, 646, 724, 527, 732, 411, 740, 315, 747);
        addTemplate(recognizer, cname, 937, 129, 935, 128, 927, 125, 904, 121, 855, 112, 788, 104, 714, 98, 668, 94,
                616, 91, 570, 90, 533, 96, 500, 110, 473, 132, 447, 162, 427, 197, 414, 230, 408, 259, 407, 285, 409,
                311, 414, 332, 421, 347, 429, 358, 439, 366, 454, 375, 478, 385, 512, 394, 552, 404, 596, 416, 641, 428,
                688, 439, 736, 451, 785, 461, 834, 468, 880, 474, 930, 482, 977, 494, 1023, 514, 1059, 536, 1086, 563,
                1109, 602, 1118, 640, 1118, 674, 1106, 707, 1081, 741, 1037, 778, 972, 814, 904, 840, 838, 857, 777,
                866, 721, 868, 664, 863, 610, 853, 556, 841, 509, 829, 470, 818, 440, 808, 415, 799, 397, 791, 386, 783,
                379, 779, 375, 775, 371, 773);
        addTemplate(recognizer, cname, 943, 300, 940, 297, 931, 290, 902, 275, 850, 259, 783, 250, 726, 250, 666, 255,
                611, 268, 564, 287, 530, 307, 507, 327, 492, 349, 489, 372, 498, 396, 528, 419, 582, 445, 658, 464, 735,
                469, 813, 469, 886, 471, 952, 480, 1006, 495, 1050, 514, 1078, 534, 1099, 564, 1112, 604, 1110, 646,
                1096, 684, 1070, 716, 1019, 754, 943, 787, 854, 808, 760, 810, 654, 802, 552, 786, 464, 767, 394, 753,
                345, 743, 309, 737, 302, 736);

        cname = "cl";
        addTemplate(recognizer, cname, 528, 394, 529, 394, 549, 396, 595, 391, 639, 375, 672, 348, 692, 313, 702, 280,
                701, 251, 693, 225, 679, 204, 660, 188, 636, 177, 604, 174, 571, 180, 543, 195, 512, 218, 483, 247, 456,
                288, 438, 329, 424, 369, 417, 409, 416, 450, 422, 496, 435, 546, 450, 593, 472, 632, 496, 664, 518, 686,
                543, 706, 577, 719, 608, 726, 644, 728, 680, 724, 713, 714, 751, 695, 786, 667, 827, 629, 870, 581, 906,
                533, 943, 485, 974, 438, 1000, 392, 1020, 349, 1034, 309, 1044, 267, 1043, 236, 1037, 204, 1026, 179,
                1013, 158, 999, 140, 984, 124, 960, 112, 934, 106, 913, 108, 891, 118, 866, 135, 843, 156, 822, 189,
                808, 226, 802, 269, 802, 315, 807, 363, 817, 415, 833, 468, 855, 517, 880, 568, 903, 614, 931, 658, 960,
                695, 992, 732, 1029, 768, 1066, 800, 1102, 828, 1138, 851, 1154, 857);
        addTemplate(recognizer, cname, 579, 134, 579, 133, 579, 133, 578, 133, 571, 134, 550, 139, 511, 149, 463, 159,
                408, 171, 360, 185, 311, 209, 282, 231, 253, 263, 229, 304, 211, 352, 202, 402, 200, 448, 203, 495, 212,
                541, 227, 586, 245, 627, 266, 665, 292, 697, 326, 727, 373, 754, 421, 773, 481, 779, 538, 777, 597, 762,
                653, 742, 706, 720, 760, 692, 807, 659, 854, 622, 894, 582, 930, 533, 958, 480, 975, 434, 986, 388, 990,
                352, 988, 317, 982, 289, 973, 266, 963, 253, 952, 243, 941, 237, 929, 236, 918, 237, 906, 242, 891, 248,
                874, 258, 859, 269, 845, 282, 831, 301, 821, 323, 815, 351, 815, 387, 821, 425, 834, 465, 873, 537, 898,
                571, 925, 599, 961, 622, 995, 637, 1033, 648, 1070, 659, 1111, 672, 1150, 684, 1190, 696, 1230, 707,
                1270, 719, 1284, 723);
        addTemplate(recognizer, cname, 732, 335, 730, 334, 727, 331, 717, 329, 686, 328, 636, 332, 585, 339, 533, 349,
                483, 361, 439, 375, 397, 395, 362, 416, 334, 436, 302, 465, 278, 496, 263, 522, 254, 548, 251, 575, 254,
                601, 263, 627, 281, 653, 306, 680, 342, 707, 387, 729, 483, 753, 534, 757, 589, 755, 645, 744, 702, 727,
                763, 705, 822, 677, 877, 648, 927, 617, 969, 585, 1005, 550, 1036, 511, 1060, 472, 1074, 438, 1079, 402,
                1076, 370, 1067, 344, 1051, 321, 1030, 302, 1005, 286, 974, 277, 940, 277, 911, 283, 878, 301, 846, 325,
                821, 349, 793, 385, 771, 421, 753, 459, 742, 499, 739, 540, 746, 583, 766, 622, 794, 657, 838, 690, 905,
                721, 980, 739, 1055, 745, 1127, 742, 1184, 731, 1232, 718, 1246, 712);

        // comment
        cname = "slash";
        addTemplate(recognizer, cname, 499, 767, 497, 767, 494, 767, 492, 767, 491, 767, 492, 765, 499, 753, 518, 724,
                552, 673, 600, 602, 652, 525, 695, 461, 733, 401, 766, 347, 793, 298, 813, 255, 831, 216, 845, 185, 857,
                164, 866, 151, 871, 143, 874, 138, 876, 134, 878, 133, 880, 131, 880, 129);
        addTemplate(recognizer, cname, 612, 804, 612, 801, 612, 797, 615, 786, 626, 761, 645, 723, 671, 683, 701, 638,
                732, 588, 764, 535, 795, 483, 821, 434, 847, 390, 869, 350, 888, 315, 902, 288, 910, 268, 913, 255, 915,
                248, 915, 243, 915, 241, 915, 239, 915, 237, 915, 235);

        cname = "qm";
        addTemplate(recognizer, cname, 716, 461, 712, 458, 700, 449, 679, 435, 658, 415, 635, 385, 613, 350, 596, 319,
                585, 293, 584, 273, 591, 253, 605, 234, 640, 212, 683, 196, 726, 190, 767, 194, 801, 205, 828, 220, 850,
                239, 867, 262, 875, 295, 870, 331, 849, 376, 815, 422, 773, 466, 726, 513, 681, 561, 648, 605, 626, 649,
                618, 695, 618, 756, 626, 825, 634, 875);
        addTemplate(recognizer, cname, 612, 380, 612, 380, 612, 381, 609, 385, 598, 394, 571, 404, 543, 400, 519, 386,
                496, 363, 470, 325, 453, 288, 444, 255, 440, 221, 441, 190, 447, 159, 459, 129, 477, 101, 501, 76, 531,
                49, 564, 28, 600, 14, 637, 9, 675, 13, 714, 25, 757, 45, 800, 71, 843, 104, 882, 142, 914, 184, 936,
                225, 946, 259, 945, 288, 936, 312, 923, 330, 905, 345, 874, 360, 842, 374, 813, 388, 787, 405, 764, 424,
                744, 449, 729, 483, 721, 517, 716, 554, 717, 586, 720, 614, 725, 644, 731, 671, 736, 693, 740, 711, 744,
                723, 745, 734, 746, 750, 746, 756);
        addTemplate(recognizer, cname, 618, 611, 616, 609, 607, 601, 583, 580, 556, 547, 529, 498, 512, 441, 510, 398,
                518, 361, 535, 329, 570, 299, 626, 273, 688, 259, 753, 257, 805, 266, 849, 284, 874, 301, 888, 323, 892,
                346, 869, 388, 836, 426, 793, 468, 746, 516, 700, 574, 665, 639, 646, 714, 645, 789, 656, 843, 668, 872,
                675, 883, 681, 885, 684, 881);

        cname = "e";
        addTemplate(recognizer, cname, 485, 637, 483, 637, 481, 637, 479, 637, 480, 638, 486, 650, 502, 672, 518, 686,
                549, 706, 591, 722, 636, 731, 684, 732, 726, 727, 772, 714, 818, 696, 865, 671, 908, 641, 948, 609, 977,
                579, 1000, 549, 1016, 523, 1026, 499, 1030, 479, 1028, 458, 1022, 433, 1011, 406, 995, 377, 975, 352,
                948, 328, 920, 309, 890, 292, 860, 279, 832, 270, 803, 265, 775, 265, 747, 268, 728, 276, 711, 287, 693,
                306, 673, 332, 654, 365, 640, 402, 632, 443, 630, 486, 632, 519, 639, 553, 652, 580, 673, 606, 696, 629,
                729, 657, 766, 684, 804, 705, 849, 723, 892, 735, 940, 743, 983, 749, 1029, 753, 1070, 756, 1110, 758,
                1145, 760, 1172, 760, 1197, 760, 1216, 759, 1230, 757, 1238, 757, 1242, 757, 1244, 757, 1246, 757);
        addTemplate(recognizer, cname, 376, 667, 376, 665, 380, 666, 401, 664, 443, 658, 500, 644, 557, 626, 670, 565,
                720, 530, 768, 491, 808, 456, 845, 423, 876, 387, 898, 355, 914, 319, 918, 286, 915, 254, 905, 223, 888,
                196, 862, 172, 826, 151, 787, 136, 749, 129, 714, 130, 679, 137, 648, 150, 621, 169, 594, 198, 568, 234,
                547, 276, 538, 309, 534, 345, 536, 378, 547, 408, 566, 440, 591, 474, 619, 505, 655, 538, 697, 574, 745,
                608, 790, 634, 844, 661, 892, 683, 940, 701, 981, 714, 1023, 725, 1061, 732, 1096, 736, 1126, 738, 1148,
                738, 1164, 738, 1172, 738);
        addTemplate(recognizer, cname, 372, 752, 372, 749, 380, 747, 417, 743, 484, 740, 563, 732, 636, 714, 682, 697,
                739, 666, 793, 632, 836, 599, 872, 569, 898, 539, 921, 498, 938, 454, 946, 419, 946, 395, 940, 378, 929,
                363, 916, 350, 895, 334, 867, 321, 841, 313, 817, 310, 790, 310, 763, 314, 737, 323, 712, 339, 686, 371,
                657, 420, 637, 477, 629, 525, 629, 569, 642, 609, 670, 652, 708, 697, 763, 745, 831, 787, 903, 814, 970,
                824, 973, 825);

        cname = "cb";
        // addTemplate(recognizer, cname, 372, 253, 370, 257, 365, 258, 352, 259, 335, 256, 317, 245, 303, 233, 291,
        // 220, 280, 205,
        // 272, 193, 268, 183, 266, 174, 267, 163, 268, 155, 275, 146, 284, 136, 294, 128, 306, 122, 319, 117, 332,
        // 115, 344, 116, 359, 123, 377, 134, 395, 153, 410, 172, 424, 194, 434, 217, 443, 243, 445, 265, 446, 288,
        // 441, 312, 431, 330, 419, 347, 406, 364, 384, 378, 362, 390, 335, 396, 315, 397, 296, 394, 281, 380, 270,
        // 363, 266, 339, 266, 318, 279, 293, 292, 275, 310, 257, 339, 232, 368, 213, 399, 191, 430, 170, 454, 150,
        // 477, 134, 490, 121, 500, 112, 508, 90);

        cname = "sb";
        addTemplate(recognizer, cname, 434, 160, 431, 163, 430, 164, 423, 166, 411, 170, 395, 174, 368, 175, 339, 176,
                315, 175, 295, 173, 280, 174, 274, 174, 269, 176, 265, 179, 262, 183, 260, 191, 260, 202, 260, 217, 260,
                238, 259, 267, 257, 294, 256, 322, 256, 349, 256, 369, 256, 386, 256, 398, 256, 406, 256, 411, 256,
                416);
        addTemplate(recognizer, cname, 1017, 132, 1014, 129, 1008, 128, 991, 128, 941, 131, 888, 131, 833, 130, 775,
                130, 721, 129, 671, 129, 632, 130, 607, 132, 591, 136, 582, 138, 577, 140, 574, 141, 571, 142, 570, 143,
                568, 145, 566, 149, 564, 155, 562, 167, 560, 187, 558, 213, 558, 243, 560, 281, 562, 324, 565, 375, 572,
                426, 580, 480, 589, 531, 599, 581, 606, 625, 613, 665, 617, 694, 620, 712, 620, 728, 620, 738, 620, 747,
                620, 754, 619, 758, 619, 762, 617, 764, 616, 765, 614, 765, 613, 765);
        addTemplate(recognizer, cname, 915, 340, 912, 337, 903, 333, 880, 324, 843, 315, 802, 307, 760, 302, 727, 302,
                702, 305, 685, 309, 677, 313, 672, 315, 670, 318, 669, 322, 670, 331, 675, 369, 680, 442, 680, 518, 677,
                583, 675, 635, 675, 673, 676, 701, 678, 721, 680, 732, 682, 740, 683, 743, 683, 745, 684, 746);

        cname = "rb";
        addTemplate(recognizer, cname, 364, 67, 361, 71, 356, 75, 349, 81, 342, 88, 336, 98, 330, 109, 324, 121, 319,
                135, 314, 149, 309, 165, 305, 182, 302, 198, 299, 215, 297, 232, 297, 246, 297, 260, 297, 273, 297, 289,
                297, 303, 297, 317, 297, 331, 297, 345, 298, 356, 300, 367, 302, 377, 307, 391, 310, 399, 313, 407, 317,
                414, 322, 421, 326, 428, 332, 433, 336, 437, 340, 441, 346, 446, 351, 450, 359, 454, 364, 457, 370,
                460);
        addTemplate(recognizer, cname, 737, 67, 736, 67, 733, 67, 727, 67, 713, 69, 691, 76, 661, 93, 629, 116, 612,
                129, 584, 152, 558, 178, 532, 210, 507, 245, 487, 280, 471, 313, 460, 342, 452, 369, 449, 395, 447, 418,
                447, 440, 447, 466, 448, 496, 449, 527, 451, 560, 453, 592, 456, 627, 461, 667, 468, 705, 477, 745, 487,
                780, 497, 810, 509, 840, 535, 893, 572, 939, 590, 956, 606, 970, 618, 978, 629, 984, 642, 990, 657, 995,
                672, 1000, 681, 1004);
        addTemplate(recognizer, cname, 1007, 191, 1004, 188, 990, 185, 955, 181, 903, 178, 837, 178, 772, 181, 708, 190,
                644, 206, 581, 228, 524, 256, 474, 289, 431, 329, 388, 380, 357, 434, 344, 486, 345, 540, 366, 596, 406,
                650, 474, 700, 563, 736, 663, 753, 762, 758, 852, 754, 917, 746, 970, 738, 998, 733, 1014, 730, 1020,
                730, 1021, 730);

        // opening ngle bracket
        cname = "lt";
        addTemplate(recognizer, cname, 395, 139, 388, 141, 380, 147, 372, 157, 363, 170, 352, 184, 341, 198, 329, 212,
                314, 226, 300, 238, 287, 247, 276, 256, 268, 261, 260, 267, 255, 272, 249, 278, 244, 282, 239, 288, 235,
                292, 231, 295, 229, 297, 225, 300, 222, 302, 221, 304, 218, 306, 219, 307, 217, 308, 217, 310, 217, 311,
                217, 312, 221, 313, 228, 316, 241, 322, 270, 335, 299, 350, 329, 365, 359, 381, 389, 399, 406, 410, 421,
                418, 432, 423, 439, 427, 445, 430, 448, 431, 450, 433, 452, 434, 452, 435, 452, 436, 452, 437);
        addTemplate(recognizer, cname, 868, 122, 868, 122, 868, 122, 866, 122, 863, 124, 850, 134, 830, 152, 797, 182,
                751, 220, 684, 264, 637, 294, 590, 324, 542, 356, 498, 387, 461, 415, 434, 436, 415, 451, 403, 461, 397,
                465, 395, 467, 394, 467, 402, 468, 418, 472, 441, 480, 475, 496, 513, 518, 567, 555, 625, 603, 690, 658,
                758, 717, 823, 773, 878, 817, 922, 849, 955, 873, 976, 887, 985, 893, 989, 896, 990, 896, 990, 896, 990,
                896, 990, 895);
        addTemplate(recognizer, cname, 936, 350, 930, 349, 906, 354, 854, 378, 780, 420, 705, 466, 644, 505, 589, 537,
                552, 556, 531, 568, 521, 572, 517, 574, 516, 574, 515, 574, 515, 574, 515, 574, 518, 574, 539, 577, 597,
                590, 682, 611, 782, 642, 883, 680, 975, 714, 1055, 741, 1116, 759, 1157, 770, 1172, 771, 1177, 771,
                1176, 770, 1174, 770, 1170, 770, 1169, 770);

        // closing ngle bracket
        cname = "gt";
        addTemplate(recognizer, cname, 274, 124, 279, 124, 282, 124, 287, 126, 299, 134, 318, 147, 344, 166, 381, 185,
                421, 207, 465, 228, 506, 247, 529, 259, 543, 268, 551, 275, 556, 280, 562, 286, 564, 290, 568, 295, 571,
                298, 574, 302, 575, 306, 578, 311, 579, 317, 580, 322, 580, 329, 566, 339, 541, 354, 506, 369, 460, 385,
                412, 396, 373, 405, 336, 410, 313, 414, 297, 415, 283, 417, 274, 418, 267, 420, 260, 420, 257, 421, 254,
                422, 251, 422, 250, 422);
        addTemplate(recognizer, cname, 539, 167, 549, 171, 587, 192, 638, 224, 697, 257, 758, 289, 788, 306, 835, 332,
                878, 352, 911, 368, 935, 381, 951, 390, 962, 397, 968, 400, 972, 404, 976, 410, 978, 418, 980, 424, 981,
                431, 980, 436, 977, 444, 972, 451, 963, 460, 949, 471, 930, 485, 908, 500, 884, 515, 857, 532, 829, 551,
                797, 572, 762, 596, 728, 618, 692, 641, 656, 663, 619, 683, 586, 699, 559, 711, 537, 718, 522, 724, 509,
                727, 500, 731, 493, 733, 488, 735, 486, 735, 484, 735, 482, 735, 480, 735, 479, 735);
        addTemplate(recognizer, cname, 477, 300, 481, 301, 498, 307, 553, 329, 621, 354, 703, 377, 787, 396, 868, 411,
                946, 425, 1008, 435, 1058, 444, 1090, 450, 1112, 454, 1123, 456, 1128, 458, 1129, 458, 1129, 458, 1129,
                458, 1129, 458, 1129, 458, 1127, 458, 1120, 462, 1088, 482, 1018, 513, 921, 550, 805, 592, 689, 632,
                586, 670, 512, 700, 465, 718, 443, 727, 438, 729, 436, 729, 436, 729, 440, 726, 443, 725, 447, 723, 449,
                721, 451, 721, 450, 721);

        cname = "m";
        addTemplate(recognizer, cname, 237, 459, 237, 454, 239, 445, 244, 427, 248, 396, 250, 367, 251, 337, 251, 300,
                251, 266, 249, 146, 250, 147, 250, 146, 251, 146, 252, 146, 254, 149, 256, 154, 263, 168, 273, 182, 283,
                198, 292, 214, 301, 228, 309, 242, 318, 258, 326, 274, 333, 283, 337, 290, 341, 294, 344, 299, 345, 301,
                347, 302, 349, 304, 350, 305, 351, 305, 353, 303, 356, 299, 361, 291, 369, 280, 383, 259, 397, 232, 410,
                207, 424, 180, 436, 159, 447, 142, 457, 129, 463, 119, 467, 112, 473, 105, 476, 101, 479, 98, 481, 97,
                483, 96, 485, 96, 486, 97, 486, 102, 488, 113, 491, 143, 493, 175, 493, 204, 494, 238, 494, 270, 493,
                295, 490, 324, 489, 350, 487, 371, 487, 389, 487, 399, 487, 406, 487, 411, 487, 415, 487, 418, 487, 421,
                487, 422, 487, 424);
        addTemplate(recognizer, cname, 540, 802, 540, 802, 540, 799, 538, 784, 534, 752, 524, 702, 510, 641, 500, 594,
                487, 531, 478, 465, 470, 401, 467, 347, 466, 297, 466, 253, 466, 209, 468, 174, 470, 147, 473, 125, 475,
                113, 477, 109, 479, 106, 481, 106, 484, 109, 491, 122, 504, 149, 523, 192, 547, 246, 571, 299, 600, 352,
                623, 393, 641, 426, 654, 446, 664, 456, 672, 463, 677, 466, 680, 466, 683, 464, 688, 460, 695, 453, 706,
                440, 723, 415, 749, 383, 779, 344, 809, 302, 840, 257, 871, 208, 896, 164, 916, 120, 929, 90, 938, 70,
                943, 60, 946, 55, 947, 53, 948, 64, 952, 94, 958, 142, 966, 205, 976, 273, 985, 346, 993, 420, 1002,
                485, 1008, 533, 1013, 573, 1015, 600, 1016, 621, 1016, 640, 1014, 658, 1011, 678, 1008, 693, 1005, 706,
                1003, 717, 1000, 730, 997, 743);
        addTemplate(recognizer, cname, 432, 803, 432, 800, 433, 797, 448, 779, 485, 735, 533, 665, 569, 595, 603, 523,
                629, 450, 649, 382, 667, 320, 682, 272, 693, 239, 701, 218, 703, 208, 703, 204, 703, 202, 703, 204, 704,
                219, 708, 258, 717, 316, 731, 378, 749, 428, 768, 466, 784, 492, 795, 503, 803, 508, 815, 502, 832, 483,
                857, 443, 882, 395, 901, 349, 918, 303, 931, 260, 939, 225, 943, 203, 943, 190, 941, 186, 939, 184, 938,
                186, 938, 204, 947, 258, 964, 337, 984, 426, 1007, 509, 1033, 587, 1061, 660, 1083, 717, 1097, 755,
                1107, 776, 1112, 783, 1113, 784, 1113, 785, 1113, 784);

        cname = "p";
        addTemplate(recognizer, cname, 353, 475, 353, 472, 357, 461, 360, 450, 361, 438, 361, 427, 364, 407, 364, 385,
                364, 365, 361, 344, 357, 319, 353, 286, 349, 251, 348, 217, 345, 196, 344, 184, 344, 173, 344, 159, 350,
                140, 355, 124, 358, 116, 360, 111, 362, 107, 379, 99, 407, 88, 437, 75, 449, 71, 459, 68, 473, 67, 496,
                68, 522, 75, 553, 84, 579, 95, 593, 102, 601, 110, 605, 117, 605, 126, 601, 137, 591, 147, 574, 161,
                545, 173, 514, 186, 481, 197, 447, 207, 420, 212, 395, 215, 368, 216, 331, 214, 317, 213);
        addTemplate(recognizer, cname, 612, 822, 612, 818, 609, 790, 598, 735, 577, 660, 556, 582, 543, 512, 534, 445,
                528, 382, 526, 326, 527, 279, 530, 240, 536, 207, 544, 181, 555, 159, 570, 135, 587, 113, 604, 93, 628,
                74, 659, 57, 692, 46, 732, 38, 773, 36, 817, 39, 861, 48, 903, 61, 945, 77, 981, 101, 1014, 133, 1050,
                178, 1075, 232, 1087, 284, 1087, 330, 1054, 412, 1018, 442, 955, 474, 882, 497, 811, 512, 745, 519, 684,
                517, 633, 511, 590, 502, 551, 489, 521, 478, 498, 470, 485, 466, 481, 464);
        addTemplate(recognizer, cname, 626, 824, 622, 819, 612, 804, 590, 774, 565, 731, 543, 681, 525, 636, 483, 491,
                477, 448, 477, 409, 484, 374, 497, 343, 515, 316, 541, 291, 581, 269, 625, 252, 679, 241, 733, 239, 787,
                244, 844, 254, 895, 268, 942, 289, 978, 309, 1003, 330, 1024, 357, 1038, 389, 1037, 424, 1022, 457, 997,
                490, 948, 524, 876, 552, 794, 566, 700, 570, 613, 561, 541, 551, 490, 544, 461, 537, 451, 535);
    }

}
