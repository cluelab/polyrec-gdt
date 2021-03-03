package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.Polyline;
import it.unisa.di.cluelab.polyrec.Result;
import it.unisa.di.cluelab.polyrec.TPoint;

/**
 * Dashboard event listener.
 */
@SuppressWarnings({"checkstyle:classfanoutcomplexity", "checkstyle:classdataabstractioncoupling",
    "checkstyle:multiplestringliterals"})
public class DashboardListener implements ActionListener, MouseListener {

    private final DashboardScreen dashboardScreen;
    private final MainFrame mainClass;
    private ClusteringResult bestResult;

    public DashboardListener(DashboardScreen dashboardScreen, MainFrame mainClass) {
        this.dashboardScreen = dashboardScreen;
        this.mainClass = mainClass;

    }

    @Override
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:executablestatementcount", "checkstyle:javancss",
        "checkstyle:methodlength", "checkstyle:nestedifdepth", "checkstyle:npathcomplexity", "checkstyle:returncount"})
    public void actionPerformed(ActionEvent e) {
        if (mainClass.getScreen() instanceof DashboardScreen) {
            DashboardScreen.setScrollValue(
                    ((DashboardScreen) mainClass.getScreen()).scrollPane.getVerticalScrollBar().getValue(),
                    ((DashboardScreen) mainClass.getScreen()).scrollPane.getHorizontalScrollBar().getValue());
        }
        if (e.getSource() instanceof JButton) {

            final String buttonName = ((JButton) e.getSource()).getName();
            // String buttonText = ((JButton) e.getSource()).getText();

            if (buttonName != null && buttonName.startsWith("addgesture")) {
                final String[] nameparts = buttonName.split("_");
                try {
                    final TemplateScreen templateScreen = new TemplateScreen(mainClass);
                    mainClass.setScreen(templateScreen);
                    templateScreen.clearCanvas();
                    templateScreen.className = nameparts[1];
                    templateScreen.testing = false;

                    templateScreen.drawTemplate(TemplateScreen.CURRENT, TemplateScreen.getDrawMode(), true);
                } catch (final IOException e1) {

                    e1.printStackTrace();
                }
                return;
            }

            if (buttonName != null && buttonName.startsWith("editing")) {
                CursorToolkit.startWaitCursor(mainClass.getRootPane());

                final String[] nameparts = buttonName.split("_");

                final String classname = nameparts[1];

                final Clustering clustering = new Clustering(mainClass.getRecognizer());
                // determina k per il quale si ottiene il miglior clustering
                // utilizzando silhouette
                int k = clustering.silhouette(classname);

                CursorToolkit.stopWaitCursor(mainClass.getRootPane());
                // finestra scelta k
                do {
                    try {
                        final String input = JOptionPane.showInputDialog("Number of templates? (<"
                                + (mainClass.getRecognizer().getTemplate(classname).size()) + ")", k);
                        if (input == null) {
                            return;
                        }

                        k = Integer.parseInt(input);

                    } catch (final NumberFormatException ex) {
                        ex.printStackTrace();
                        k = 0;

                    }

                } while (k >= mainClass.getRecognizer().getTemplate(classname).size() || k < 1);

                double minCost = Double.MAX_VALUE;
                // esegui più volte per evitare minimo locale
                for (int i = 0; i < mainClass.getRecognizer().getTemplate(classname).size() / 2 || i < 5; i++) {

                    final ClusteringResult result = clustering.kmedoids(classname, k, 0);

                    if (result.getCost() < minCost) {
                        minCost = result.getCost();
                        bestResult = result;
                    }

                }
                final int[] medoids = bestResult.getMedoids();
                final List<Polyline> polylines = mainClass.getRecognizer().getTemplate(classname);
                final int size = polylines.size();

                final JSpinner[] inputField = new JSpinner[medoids.length];
                for (int i = 0; i < medoids.length; i++) {
                    // initial value, min, max, step
                    final SpinnerModel model = new SpinnerNumberModel(medoids[i], 0, size - 1, 1);
                    final JSpinner spinner = new JSpinner(model);
                    // cambia i medoidi selezionati
                    @SuppressWarnings("checkstyle:anoninnerlength")
                    final ChangeListener cListener = new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            // colora bordo templates medoidi
                            for (int i = 0; i < size; i++) {
                                boolean found = false;
                                for (int j = 0; j < inputField.length; j++) {
                                    if ((int) inputField[j].getValue() == i) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    // medoide
                                    dashboardScreen.panelsMap.get(classname)[i].setBorder(new LineBorder(Color.red, 2));
                                } else {
                                    // non medoide
                                    dashboardScreen.panelsMap.get(classname)[i]
                                            .setBorder(new LineBorder(new Color(145, 220, 90), 2));
                                }
                            }
                        }
                    };
                    spinner.addChangeListener(cListener);
                    inputField[i] = spinner;
                }

                final String[] buttons = {"Delete Other Templates", "Split Clusters", "Cancel"};
                final JOptionPane optionPane = new JOptionPane(inputField, JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.OK_CANCEL_OPTION);
                optionPane.setOptions(buttons);

                final JDialog dialog = optionPane.createDialog(mainClass, "Selected Templates");

                dialog.setModal(false);
                dialog.setVisible(true);
                CursorToolkit.stopWaitCursor(mainClass.getRootPane());
                @SuppressWarnings("checkstyle:anoninnerlength")
                final ComponentListener cListener = new ComponentListener() {

                    @Override
                    public void componentResized(ComponentEvent e) {
                    }

                    @Override
                    public void componentMoved(ComponentEvent e) {
                    }

                    @Override
                    public void componentShown(ComponentEvent e) {
                    }

                    @Override
                    public void componentHidden(ComponentEvent e) {

                        // delete medoids
                        if ("Delete Other Templates".equals(optionPane.getValue())) {

                            for (int i = 0; i < inputField.length; i++) {
                                for (int j = 0; j < inputField.length; j++) {
                                    if (i != j && inputField[i].getValue() == inputField[j].getValue()) {
                                        JOptionPane.showMessageDialog(mainClass, "Medoids value must be different.");
                                        return;
                                    }
                                }
                            }

                            for (int i = size - 1; i >= 0; i--) {

                                boolean found = false;
                                for (int j = 0; j < inputField.length; j++) {
                                    if ((int) inputField[j].getValue() == i) {
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    mainClass.getRecognizer().removePolyline(classname, i);
                                }
                            }

                            final DashboardScreen screen = new DashboardScreen(mainClass, false);
                            screen.display.set("Non medoids deleted from " + classname, 0);
                            mainClass.setScreen(screen);

                        } else if ("Cancel".equals(optionPane.getValue())) {
                            // do CANCEL stuff...
                            assert true;
                        } else if ("Split Clusters".equals(optionPane.getValue())) {
                            // System.out.println(bestResult.getMatrix());
                            for (int i = 0; i < bestResult.getMatrix().size(); i++) {
                                mainClass.getRecognizer().addClass(classname + "-" + i);
                                final ArrayList<Integer> cluster = bestResult.getMatrix().get(i);
                                for (int j = 0; j < cluster.size(); j++) {
                                    mainClass.getRecognizer().addTemplate(classname + "-" + i,
                                            polylines.get(cluster.get(j)));

                                }
                            }
                            mainClass.getRecognizer().removeClass(classname);

                            final DashboardScreen screen = new DashboardScreen(mainClass, false);

                            screen.display.set("Class " + classname + " has been splitted in "
                                    + bestResult.getMatrix().size() + " new classes", 0);
                            mainClass.setScreen(screen);
                        } else {
                            throw new IllegalStateException("Unexpected Option");
                        }
                    }
                };
                dialog.addComponentListener(cListener);

                // colora bordo templates medoidi
                for (int i = 0; i < size; i++) {

                    boolean found = false;
                    for (int j = 0; j < medoids.length; j++) {
                        if (medoids[j] == i) {
                            found = true;
                        }
                    }
                    if (!found) {
                        // medoide
                        dashboardScreen.panelsMap.get(classname)[i].setBorder(new LineBorder(Color.red, 2));
                    } else {
                        // non medoide
                        dashboardScreen.panelsMap.get(classname)[i]
                                .setBorder(new LineBorder(new Color(145, 220, 90), 2));
                    }
                }

                return;
            }

            if (e.getSource() == dashboardScreen.checkTemplates) {
                CursorToolkit.startWaitCursor(mainClass.getRootPane());
                final Properties applicationProps = Settings.APPLICATION_PROPS;

                final Display dialogDisplay = new Display();
                final JPanel displayPanel = new JPanel(new FlowLayout());
                displayPanel.add(dialogDisplay);
                dialogDisplay.set("Calculating similarity score of the gesture set...", 1);
                final int scorelimit = Integer.parseInt(applicationProps.getProperty("scorelimit"));

                // thread gesture set score
                (new Thread() {
                    @Override
                    public void run() {
                        for (int i = 100; i >= 0; i--) {
                            final String resultString = verifySimilarity(i, null);
                            if (!"".equals(resultString)) {
                                final String setScoreString = "Similarity Set Score: " + (i + 1);
                                if (i + 1 > scorelimit) {
                                    dashboardScreen.display.set(setScoreString + "% (higher than set limit)", 1);
                                    dialogDisplay.set(setScoreString, 1);
                                } else {
                                    dashboardScreen.display.set(setScoreString + "% (lower than set limit)", 0);
                                    dialogDisplay.set(setScoreString, 0);
                                }
                                break;
                            }
                        }
                    }
                }).start();

                // similarity window
                final JPanel container = new JPanel(new BorderLayout());
                final JEditorPane textArea = new JEditorPane();
                textArea.setContentType("text/html");

                final SpinnerModel model = new SpinnerNumberModel(
                        // initial, min, max, sep
                        scorelimit, 0, 100, 1);
                final JSpinner spinner = new JSpinner(model);
                spinner.setToolTipText("Set score threshold to detect similarity problems");

                final JPanel westPanel = new JPanel(new FlowLayout());
                westPanel.add(spinner);

                container.add(displayPanel, BorderLayout.NORTH);
                container.add(westPanel, BorderLayout.WEST);

                final JScrollPane scrollPane = new JScrollPane(textArea);

                container.add(scrollPane, BorderLayout.CENTER);
                scrollPane.setPreferredSize(new Dimension(500, 300));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                textArea.setOpaque(false);

                final JOptionPane optionpanel = new JOptionPane(container, JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.DEFAULT_OPTION);

                final JDialog dialog = optionpanel.createDialog(mainClass, "Verify Similarity");
                spinner.addChangeListener(new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent e) {
                        (new Thread() {
                            @Override
                            public void run() {
                                CursorToolkit.startWaitCursor(dialog.getRootPane());
                                final String resultString = verifySimilarity((Integer) spinner.getValue(), textArea);
                                if (!"".equals(resultString)) {
                                    textArea.setText("<span style='font-family:Arial;'>" + resultString + "</span>");
                                } else {
                                    textArea.setText("<span style='font-family:Arial;'>"
                                            + "<strong>No Problem Found</strong></span>");
                                }
                                CursorToolkit.stopWaitCursor(dialog.getRootPane());
                            }
                        }).start();
                    }
                });

                // Makes the dialog not modal
                dialog.setModal(false);
                dialog.setVisible(true);
                dialog.setResizable(true);

                (new Thread() {

                    @Override
                    public void run() {
                        final String resultString = verifySimilarity(
                                Integer.parseInt(applicationProps.getProperty("scorelimit")), textArea);
                        if (!"".equals(resultString)) {
                            textArea.setText("<span style='font-family:Arial;'>" + resultString + "</span>");

                        } else {
                            textArea.setText(
                                    "<span style='font-family:Arial;'><strong>No Problem Found</strong></span>");

                        }
                        CursorToolkit.stopWaitCursor(mainClass.getRootPane());

                    }
                }).start();

                return;
            }

            if (buttonName != null && buttonName.startsWith("features")) {
                final String[] nameparts = buttonName.split("_");
                showFeaturesTable(nameparts[1]);
                return;
            }
            if (e.getSource() == dashboardScreen.featuresButton) {

                showFeaturesTable(null);
                return;
            }
            if (e.getSource() == dashboardScreen.mergeClasses) {
                final JList<String> classesList = new JList<String>(
                        mainClass.getRecognizer().getClassNames().toArray(new String[0]));

                classesList.setSelectionModel(new DefaultListSelectionModel() {
                    private static final long serialVersionUID = 5382159576282787031L;

                    @Override
                    public void setSelectionInterval(int index0, int index1) {
                        if (super.isSelectedIndex(index0)) {
                            super.removeSelectionInterval(index0, index1);
                        } else {
                            super.addSelectionInterval(index0, index1);
                        }
                    }
                });

                final JScrollPane scrollPane = new JScrollPane(classesList);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                classesList.setOpaque(false);
                scrollPane.setPreferredSize(new Dimension(500, 300));

                final String[] buttons = {"Merge Selected Classes", "Cancel"};
                final JOptionPane optionpanel = new JOptionPane(classesList, JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.OK_CANCEL_OPTION);
                optionpanel.setOptions(buttons);

                final JDialog dialog = optionpanel.createDialog("Merge Classes");
                // Makes the dialog not modal
                dialog.setModal(false);
                dialog.setVisible(true);
                @SuppressWarnings("checkstyle:anoninnerlength")
                final ComponentListener cListener = new ComponentListener() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                    }

                    @Override
                    public void componentMoved(ComponentEvent e) {
                    }

                    @Override
                    public void componentShown(ComponentEvent e) {
                    }

                    @Override
                    public void componentHidden(ComponentEvent e) {

                        // merge selected classes
                        if ("Merge Selected Classes".equals(optionpanel.getValue())) {
                            final int[] classesIndexes = classesList.getSelectedIndices();
                            final String firstClass = classesList.getModel().getElementAt(classesIndexes[0]);
                            for (int i = 1; i < classesIndexes.length; i++) {

                                final String className = classesList.getModel()
                                        .getElementAt(classesIndexes[i]);
                                mainClass.getRecognizer().addTemplatesPl(firstClass,
                                        mainClass.getRecognizer().getTemplate(className));
                                mainClass.getRecognizer().removeClass(className);
                            }

                            final DashboardScreen screen = new DashboardScreen(mainClass, true);
                            mainClass.setScreen(screen);
                            screen.display
                                    .set("Selected classes are merged into " + firstClass.toUpperCase() + " class", 0);

                        }

                    }

                };
                dialog.addComponentListener(cListener);
                return;

            }

            if (e.getSource() == dashboardScreen.addClass) {
                final String name = dashboardScreen.className.getText();
                if (name.equals(DashboardScreen.DEFAULT_USER_DEFINED_STRING) || "".equals(name)) {

                    JOptionPane.showMessageDialog(mainClass, "You must enter a name for the class");
                } else {
                    if (!mainClass.getRecognizer().getClassNames().contains(name.toLowerCase())) {
                        mainClass.getRecognizer().addClass(name);

                        final DashboardScreen screen = new DashboardScreen(mainClass, false);

                        mainClass.setScreen(screen);

                        screen.display.set("Class " + name.toUpperCase() + " successfully added", 0);

                    } else {
                        JOptionPane.showMessageDialog(mainClass, "The class " + name + " already exists ");
                    }

                }
                mainClass.getMenu().updateMenu();
                return;

            }
            if (e.getSource() == dashboardScreen.deleteAllClasses) {
                final int selectedOption = JOptionPane.showConfirmDialog(null, "Are you sure?", "Delete Class",
                        JOptionPane.YES_NO_OPTION);
                if (selectedOption == JOptionPane.YES_OPTION) {

                    mainClass.getRecognizer().removeClasses();

                    final DashboardScreen screen = new DashboardScreen(mainClass, false);
                    mainClass.setScreen(screen);
                    screen.display.set("Classes successfully deleted", 0);
                    mainClass.getMenu().updateMenu();
                }

                return;
            }

            if (e.getSource() == dashboardScreen.testRecognizer) {

                try {
                    final TemplateScreen templateScreen = new TemplateScreen(mainClass);
                    mainClass.setScreen(templateScreen);
                    templateScreen.clearCanvas();
                    templateScreen.testing = true;
                    templateScreen.drawTemplate(TemplateScreen.CURRENT, TemplateScreen.MOUSE, true);
                } catch (final IOException e1) {

                    e1.printStackTrace();
                }
                return;
            }

            if (buttonName != null && buttonName.startsWith("deleteclass")) {

                final int selectedOption = JOptionPane.showConfirmDialog(null, "Are you sure?", "Delete Class",
                        JOptionPane.YES_NO_OPTION);
                if (selectedOption == JOptionPane.YES_OPTION) {
                    // CursorToolkit.startWaitCursor(mainClass.getRootPane());
                    final String[] nameparts = buttonName.split("_");

                    mainClass.getRecognizer().removeClass(nameparts[1]);

                    final DashboardScreen screen = new DashboardScreen(mainClass, false);

                    mainClass.setScreen(screen);

                    screen.display.set("Class " + nameparts[1].toUpperCase() + " successfully deleted", 0);
                    mainClass.getMenu().updateMenu();
                    // CursorToolkit.stopWaitCursor(mainClass.getRootPane());
                }

                return;
            }

            if (buttonName != null && buttonName.startsWith("rotinv")) {

                final int selectedOption = JOptionPane.showConfirmDialog(null, "Are you sure?", "Rotation Invariant",
                        JOptionPane.YES_NO_OPTION);
                if (selectedOption == JOptionPane.YES_OPTION) {
                    // CursorToolkit.startWaitCursor(mainScreen);
                    final String[] nameparts = buttonName.split("_");
                    final List<Polyline> templates = mainClass.getRecognizer().getTemplate(nameparts[1]);
                    String displayMessage;
                    if (nameparts.length > 2) {
                        displayMessage = "Template " + nameparts[2] + " of class " + nameparts[1].toUpperCase()
                                + " is now ";
                        try {
                            final Gesture g = templates.get(Integer.parseInt(nameparts[2])).getGesture();
                            g.setRotInv(!g.isRotInv());
                            displayMessage += g.isRotInv() ? "Rotation Invariant" : "Not Rotation Invariant";
                        } catch (final Exception e1) {
                            displayMessage += e1.toString();
                        }
                    } else {
                        displayMessage = "Templates of class " + nameparts[1].toUpperCase()
                                + " are now Rotation Invariant";
                        for (int i = 0; i < templates.size(); i++) {
                            templates.get(i).getGesture().setRotInv(true);
                        }
                    }

                    // int verticalScrollValue = ((MainScreen) mainClass.getScreen()).scrollPane.getVerticalScrollBar()
                    // .getValue();

                    final DashboardScreen screen = new DashboardScreen(mainClass, false);

                    // screen.verticalScrollValue = verticalScrollValue;
                    mainClass.setScreen(screen);
                    screen.display.set(displayMessage, 0);
                    // CursorToolkit.stopWaitCursor(mainScreen);
                }

            }
            if (buttonName != null && buttonName.startsWith("notrotinv")) {

                final int selectedOption = JOptionPane.showConfirmDialog(null, "Are you sure?",
                        "Not Rotation Invariant", JOptionPane.YES_NO_OPTION);
                if (selectedOption == JOptionPane.YES_OPTION) {
                    // CursorToolkit.startWaitCursor(mainClass.getRootPane());
                    final String[] nameparts = buttonName.split("_");

                    final List<Polyline> templates = mainClass.getRecognizer().getTemplate(nameparts[1]);
                    for (final Polyline polyline : templates) {
                        polyline.getGesture().setRotInv(false);
                    }

                    // int verticalScrollValue = ((MainScreen) mainClass.getScreen()).scrollPane.getVerticalScrollBar()
                    // .getValue();

                    final DashboardScreen screen = new DashboardScreen(mainClass, false);
                    // screen.verticalScrollValue = verticalScrollValue;
                    mainClass.setScreen(screen);
                    screen.display.set(
                            "Templates of class " + nameparts[1].toUpperCase() + " are now Not Rotation Invariant", 0);
                }

                return;
            }
            if (buttonName != null && buttonName.startsWith("editclass")) {
                final String[] nameparts = buttonName.split("_");

                new EditFrame(nameparts[1], mainClass);

                return;
            }

            if (buttonName != null && buttonName.startsWith("deletetemplate")) {
                final int selectedOption = JOptionPane.showConfirmDialog(null, "Are you sure?", "Delete Template",
                        JOptionPane.YES_NO_OPTION);
                if (selectedOption == JOptionPane.YES_OPTION) {

                    // CursorToolkit.startWaitCursor(mainClass.getRootPane());

                    // mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                    final String[] nameparts = buttonName.split("_");

                    mainClass.getRecognizer().removePolyline(nameparts[1], Integer.parseInt(nameparts[2]));

                    final DashboardScreen screen = new DashboardScreen(mainClass, false);

                    mainClass.setScreen(screen);

                    screen.display.set("Template successfully deleted from " + nameparts[1] + " class", 00);
                    mainClass.getMenu().updateMenu();

                    // mainScreen.revalidate();
                    // mainScreen.repaint();

                }

                return;
            }
            if (buttonName != null && buttonName.startsWith("detach")) {

                final String[] nameparts = buttonName.split("_");
                // Point point =
                // mainScreen.deleteAllClasses.getLocationOnScreen();
                // new
                // CanvasDetached(mainClass.getRecognizer().getTemplate(nameparts[1])
                // .get(Integer.parseInt(nameparts[2])).getGesture(), point);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new UndecoratedExample().createAnsShowGui(mainClass.getRecognizer().getTemplate(nameparts[1])
                                .get(Integer.parseInt(nameparts[2])).getGesture());
                    }
                });
                return;
            }

            if (buttonName != null && buttonName.startsWith("movetemplate")) {
                final String[] nameparts = buttonName.split("_");
                final String[] s = mainClass.getRecognizer().getClassNames().toArray(new String[0]);

                final String newClass = (String) JOptionPane.showInputDialog(null, "Select new class",
                        "Move template to another class", JOptionPane.QUESTION_MESSAGE, null, s, s[0]);

                if (newClass != null) {
                    mainClass.getRecognizer().addTemplate(newClass, mainClass.getRecognizer().getTemplate(nameparts[1])
                            .get(Integer.parseInt(nameparts[2])).getGesture());

                    mainClass.getRecognizer().removePolyline(nameparts[1], Integer.parseInt(nameparts[2]));

                    // int verticalScrollValue = ((MainScreen) mainClass.getScreen()).scrollPane.getVerticalScrollBar()
                    // .getValue();

                    final DashboardScreen screen = new DashboardScreen(mainClass, false);

                    mainClass.setScreen(screen);
                    screen.display.set("Template successfully moved from " + nameparts[1] + " to " + newClass, 0);
                }
                return;
            }
            if (buttonName != null && buttonName.startsWith("pointers")) {
                final String[] nameparts = buttonName.split("_");

                new PointersFrame(nameparts[1], Integer.parseInt(nameparts[2]), mainClass);

                return;
            }

        }
        return;
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        final JLayeredPane thumbnail = (JLayeredPane) arg0.getSource();
        if (thumbnail.getName().startsWith("thumbnail")) {
            final String[] nameparts = thumbnail.getName().split("_");
            try {
                final TemplateScreen templateScreen = new TemplateScreen(mainClass);
                mainClass.setScreen(templateScreen);

                templateScreen.className = nameparts[1];

                templateScreen.item = Integer.parseInt(nameparts[2]);
                templateScreen.saveGesture.setVisible(false);
                templateScreen.rotInv.setVisible(false);

                templateScreen.display
                        .set("Template " + (templateScreen.item) + " of class " + templateScreen.className, 0);

                templateScreen.showTemplate();

            } catch (final IOException e1) {

                e1.printStackTrace();
            }

            return;
        }

    }

    @Override
    public void mouseEntered(MouseEvent arg0) {

    }

    @Override
    public void mouseExited(MouseEvent arg0) {

    }

    @Override
    public void mousePressed(MouseEvent arg0) {

    }

    @Override
    public void mouseReleased(MouseEvent arg0) {

    }

    private String verifySimilarity(int scoreLimit, JEditorPane textArea) {

        final StringBuilder resultString = new StringBuilder();
        final String[] classes = mainClass.getRecognizer().getClassNames().toArray(new String[0]);
        // per ogni classe
        String classString = "";
        for (int i = 0; i < classes.length; i++) {

            final List<Polyline> polylines = mainClass.getRecognizer().getTemplate(classes[i]);
            classString = "<span style='font-family:Arial;'><br/>Checking Class " + classes[i].toUpperCase()
                    + "...</span>";
            if (textArea != null) {
                textArea.setText(
                        "<span style='font-family:Arial;'>" + (100 / classes.length) * i + " % </span>" + classString);

            }
            // per ogni template nella classe
            for (int p = 0; p < polylines.size(); p++) {

                // controlla template con p della classe i
                final List<ExtendedResult> r = mainClass.getRecognizer().verifyTemplate(polylines.get(p), classes[i],
                        scoreLimit);

                if (r.size() > 0) {
                    resultString.append("<b>Template " + p + " of Class " + classes[i].toUpperCase()
                            + " is too similar to:</b><br/>");
                    for (final Result result : r) {

                        resultString.append(result.toString() + "<br/>");

                    }
                    resultString.append("<br/>");
                }

            }

        }
        return resultString.toString();

    }

    // void scrollScreen(MainScreen screen, String className) {
    // screen.verticalScrollValue = 245 * mainClass.getRecognizer().getClassIndex(className);
    //
    // }

    // round 'n' to 'd' decimals
    private static double round(double n, double d) {
        final double de = Math.pow(10, d);
        return Math.round(n * de) / de;
    }

    public JTable resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            // Min width
            int width = 15;
            for (int row = 0; row < table.getRowCount(); row++) {
                final TableCellRenderer renderer = table.getCellRenderer(row, column);
                final Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }
            if (width > 300) {
                width = 300;
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
        return table;
    }

    // Se è il parametro className è impostato visualizza feature dei template
    // della classe, altrimenti tabella globale di tutti i tamplate del set
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:executablestatementcount", "checkstyle:javancss",
        "checkstyle:methodlength"})
    private void showFeaturesTable(String className) {
        System.out.println("Show features per classe" + className);

        final String[] classes = mainClass.getRecognizer().getClassNames().toArray(new String[0]);
        final Object[] columnHeaders = {"Class", "Template", "Polyline lines", /* "MYIsokoski", */ "Curviness",
            "Length", "Dist First->Last", "BBox Area", "BBox Diagonal", "BBox Angle", "Cos(StartAngle)",
            "Sin(StartAngle)", "Cos(EndAngle)", "GlobalOrientation(Angle First->Last)", "Cos(Angle First->Last)",
            "Sin(Angle First->Last)"};
        int rowNumbers = mainClass.getRecognizer().getTemplatesNumber();
        if (className != null) {
            rowNumbers = mainClass.getRecognizer().getTemplate(className).size();
        }
        final Object[][] rowData = new Object[rowNumbers][columnHeaders.length];
        int rowcount = 0;
        for (int m = 0; m < classes.length; m++) {

            if (className == null || className.equals(classes[m])) {
                // System.out.println("OOOK");
                final List<Polyline> polylines = mainClass.getRecognizer().getTemplate(classes[m]);

                for (int p = 0; p < polylines.size(); p++) {
                    final Polyline polyline = polylines.get(p);
                    final Gesture normalizedGesture = GDTRecognizer.normalizeGesture(polyline.getGesture(),
                            150, 150, 0);

                    double sumangle = 0;

                    for (int i = 0; i < polyline.getNumVertexes(); i++) {

                        sumangle += Math.abs(polyline.getSlopeChange(i));
                    }

                    // Distance between first and last point (f5);
                    final double f5 = Math.sqrt(Math
                            .pow(normalizedGesture.getPoints().get(normalizedGesture.getPoints().size() - 1).getX()
                                    - normalizedGesture.getPoints().get(0).getX(), 2)
                            + Math.pow(
                                    normalizedGesture.getPoints().get(normalizedGesture.getPoints().size() - 1).getY()
                                            - normalizedGesture.getPoints().get(0).getY(),
                                    2));
                    // angle between first and last point
                    final double globalOrientation = Polyline.getLineAngle(
                            new TPoint((int) normalizedGesture.getBoundingBox().x,
                                    (int) normalizedGesture.getBoundingBox().y
                                            + (int) normalizedGesture.getBoundingBox().height,
                                    0),
                            new TPoint(
                                    (int) normalizedGesture.getBoundingBox().x
                                            + (int) normalizedGesture.getBoundingBox().width,
                                    (int) normalizedGesture.getBoundingBox().y, 0));
                    final Object[] row = {classes[m], String.valueOf(p), polyline.getNumLines(), round(sumangle, 2),
                        round(normalizedGesture.getLength(), 2), round(f5, 2),
                        round(normalizedGesture.getBoundingBox().height * normalizedGesture.getBoundingBox().width, 2),
                        round(normalizedGesture.getDiagonal(), 2),
                        round(Math.atan(
                                normalizedGesture.getBoundingBox().height / normalizedGesture.getBoundingBox().width),
                                2),
                        // verificare problema linea punti
                        round(Math.cos(polyline.getLineSlope(0)), 2),
                        // verificare problema linea punti
                        round(Math.sin(polyline.getLineSlope(0)), 2),
                        // verificare problema linea punti
                        round(Math.cos(polyline.getLineSlope(polyline.getNumLines() - 1)), 2),
                        round(globalOrientation, 2), round(Math.cos(globalOrientation), 2),
                        round(Math.sin(globalOrientation), 2)};
                    rowData[rowcount++] = row;

                }
            }
        }

        final FeaturesTableModel mod = new FeaturesTableModel(rowData, columnHeaders);

        final JTable table = new JTable(mod);

        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);
        table.setOpaque(false);
        table.setSelectionBackground(Color.lightGray);

        table.setDefaultRenderer(String.class, new TableRenderer());
        final TableColumnModel tcm = table.getColumnModel();
        final FeaturesTableHeader header = new FeaturesTableHeader(tcm);
        table.setTableHeader(header);

        final DefaultRowSorter<?, ?> sorter = (DefaultRowSorter<?, ?>) table.getRowSorter();
        final ArrayList<SortKey> list = new ArrayList<SortKey>();
        list.add(new RowSorter.SortKey(2, SortOrder.DESCENDING));
        sorter.setSortKeys(list);
        sorter.sort();

        final JPanel dialogPanel = new JPanel(new BorderLayout());

        final JScrollPane scrollPane = new JScrollPane(resizeColumnWidth(table));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        scrollPane.setPreferredSize(new Dimension(800, Math.min(50 + rowcount * 16, 300)));

        dialogPanel.add(scrollPane, BorderLayout.CENTER);
        final JPanel southPanel = new JPanel(new FlowLayout());
        final JButton exportCSV = new JButton("Export to CSV");
        southPanel.add(exportCSV);
        exportCSV.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final TableToCsv converter = new TableToCsv(table);
                try {
                    converter.convert();
                    dashboardScreen.display.set("File Saved", 0);
                } catch (final IOException e1) {
                    // custom title, error icon
                    JOptionPane.showMessageDialog(mainClass, "Error saving file. \n(" + e1.getMessage() + ")",
                            "Export to CSV", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        if (className != null || dashboardScreen.templatesNum < 30) {
            final JButton chart = new JButton("Show Chart");
            chart.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        final Chart ex = new Chart(table, "Feature Chart", 2, table.getModel().getColumnCount(), 2,
                                Chart.LINE, SortOrder.DESCENDING);
                        ex.setVisible(true);
                    });

                }
            });
            southPanel.add(chart);
        }
        dialogPanel.add(southPanel, BorderLayout.SOUTH);

        final JOptionPane optionPane = new JOptionPane(dialogPanel);

        String dialogTitle = "Features Table";
        if (className != null) {
            dialogTitle = dialogTitle + " - Class" + className;
        }
        final JDialog dialog = optionPane.createDialog(dialogTitle);

        // Makes the dialog not modal
        dialog.setModal(false);
        dialog.setVisible(true);
        dialog.setResizable(true);

    }

}
