package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import it.unisa.di.cluelab.polyrec.Polyline;

/**
 * Thread that handle table creation.
 */
@SuppressWarnings({"checkstyle:classdataabstractioncoupling", "checkstyle:multiplestringliterals"})
public class TableThread implements Runnable {

    private final DashboardScreen dashboardScreen;
    private final String[] classes;
    private final String currentClass;

    public TableThread(String[] classes, DashboardScreen dashboardScreen) {

        this.dashboardScreen = dashboardScreen;

        this.classes = Arrays.copyOf(classes, classes.length);

        if (dashboardScreen.mainClass.getScreen() instanceof TemplateScreen) {
            currentClass = ((TemplateScreen) dashboardScreen.mainClass.getScreen()).className;
        } else {
            currentClass = null;
        }
    }

    @Override
    @SuppressWarnings({"checkstyle:executablestatementcount", "checkstyle:javancss"})
    public void run() {
        final GridBagConstraints c = new GridBagConstraints();
        final GridBagConstraints last = new GridBagConstraints();
        Box selBox = null;
        Box lastBox = null;
        for (int m = 0; m < classes.length; m++) {
            final List<Polyline> polylines = dashboardScreen.mainClass.getRecognizer().getTemplate(classes[m]);

            dashboardScreen.templatesNum += polylines.size();

            if (polylines.size() > 0) {
                dashboardScreen.notEmptyClasses++;
            }

            // pannello nome classe (prima colonna)
            JPanel panel = new JPanel();

            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray));
            panel.setOpaque(false);

            c.gridx = 0;
            c.gridy = m;
            c.ipady = 20;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.PAGE_START;
            c.weighty = 1;

            lastBox = dashboardScreen.classPanel(classes[m], polylines.size());
            if (classes[m].equals(currentClass)) {
                selBox = lastBox;
            }
            panel.add(lastBox);
            dashboardScreen.table.add(panel, c);

            // pannello colonna dei templates
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            panel.setOpaque(false);

            // per ogni template nella classe
            final Box[] templateBoxes = new Box[polylines.size()];

            for (int p = 0; p < polylines.size(); p++) {

                // pannello del template
                // JToolBar toolbar = new JToolBar();//per poter spostare

                // aggiungi pannello del template
                // panel.add(toolbar);

                // MyThreadCallable task = new MyThreadCallable(classes[m], p, this, polylines.get(p));
                // templateBoxes[p] = task.call();

                templateBoxes[p] = dashboardScreen.templatePanel(classes[m], polylines.get(p), p);

                panel.add(templateBoxes[p]);

                // mainScreen.repaint();

            }

            dashboardScreen.panelsMap.put(classes[m], templateBoxes);
            // fine ciclo pannello anteprima dei template della classe

            last.gridy = m;
            last.ipady = 20;
            last.weightx = 1;
            // last.anchor = GridBagConstraints.WEST;
            // last.anchor = GridBagConstraints.NORTHEAST;
            last.fill = GridBagConstraints.BOTH;
            last.gridx = 1;
            // last.gridheight =0;

            // pannello pulsante 'add gesture' dopo i template
            dashboardScreen.addGesturePanel[m] = new JPanel(new GridBagLayout());
            dashboardScreen.addGesturePanel[m].setPreferredSize(new Dimension(100, 90));
            dashboardScreen.addGesturePanel[m].setOpaque(false);

            try {

                dashboardScreen.addGestureButtons[m] = new JButton(
                        new ImageIcon(ImageIO.read(getClass().getResource("/img/plus-white-32.png"))));
                dashboardScreen.addGestureButtons[m].setContentAreaFilled(false);
                dashboardScreen.addGestureButtons[m].setBorderPainted(false);

                dashboardScreen.addGestureButtons[m].setName("addgesture_" + classes[m]);

                dashboardScreen.addGestureButtons[m].setToolTipText("Add Template to " + classes[m] + " Class");
                dashboardScreen.addGestureButtons[m].setCursor(new Cursor(Cursor.HAND_CURSOR));

                @SuppressWarnings("checkstyle:anoninnerlength")
                final java.awt.event.MouseAdapter mListener = new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        try {
                            ((JButton) evt.getSource()).setIcon(
                                    new ImageIcon(ImageIO.read(getClass().getResource("/img/plus-green-32.png"))));

                        } catch (final IOException e) {

                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        try {
                            ((JButton) evt.getSource()).setIcon(
                                    new ImageIcon(ImageIO.read(getClass().getResource("/img/plus-white-32.png"))));
                        } catch (final IOException e) {

                            e.printStackTrace();
                        }
                    }
                };
                dashboardScreen.addGestureButtons[m].addMouseListener(mListener);
                dashboardScreen.addGestureButtons[m].addActionListener(dashboardScreen.dashboardListener);
            } catch (final IOException e) {

                e.printStackTrace();
            }

            dashboardScreen.addGesturePanel[m].add(dashboardScreen.addGestureButtons[m]);

            // table.add(addGesturePanel[m], c);
            panel.add(dashboardScreen.addGesturePanel[m]);
            dashboardScreen.table.add(panel, last);
            dashboardScreen.validate();

        }
        dashboardScreen.upadateCommandsButtons();
        dashboardScreen.statusMessage();
        dashboardScreen.repaint();
        if (selBox != null) {
            lastBox.scrollRectToVisible(lastBox.getBounds());
            selBox.scrollRectToVisible(selBox.getBounds());
        }
    }
}
