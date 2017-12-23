package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.bluetooth.BluetoothStateException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.Polyline;
import it.unisa.di.cluelab.polyrec.TPoint;
import it.unisa.di.cluelab.polyrec.bluetooth.WaitThread;
import it.unisa.di.cluelab.polyrec.geom.Rectangle2D.Double;

/**
 * Template screen.
 */
@SuppressWarnings({"checkstyle:classfanoutcomplexity", "checkstyle:classdataabstractioncoupling",
    "checkstyle:multiplestringliterals"})
public class TemplateScreen extends JPanel
        implements MouseWheelListener, ChangeListener, MouseListener, MouseMotionListener {
    // mode values
    public static final int POLYLINE = 0;
    public static final int GESTURE = 1;
    public static final int VERTEX = 2;
    public static final int GESTURE_TIMED = 3;
    public static final int CURRENT = 4;

    // state values
    public static final int GESTURE_PROCESSED = 0;
    public static final int STROKE_COMPLETE = 2;
    public static final int STROKE_IN_PROGRESS = 1;

    // draw mode values
    public static final int MOUSE = 0;
    public static final int SMARTPHONE = 1;

    private static int drawMode;
    private static WaitThread waitThread;

    private static final long serialVersionUID = 2059513571482634664L;

    protected Display display;

    protected Canvas canvas = new Canvas(this);

    protected int mode = POLYLINE;

    protected boolean testing;

    protected HashMap<Integer, Gesture> canvasGestures = new HashMap<Integer, Gesture>();
    protected Gesture currentGesture = new Gesture();

    // options
    protected JCheckBox showPolyline = new JCheckBox("<html><font color='white'>Show Polyline</font></html>");
    protected JCheckBox showGesture = new JCheckBox("<html><font color='white'>Show Gesture</font></html>");
    protected JCheckBox showVertex = new JCheckBox("<html><font color='white'>Show Vertices</font></html>");
    protected JButton showTimedGesture = new JButton("<html><font color='white'>Show timed gesture</font></html>");
    protected JButton features = new JButton("<html><font color='white'>Features</font></html>");
    protected JButton rotInvCommand = new JButton("<html><font color='white'>Rotation Invariant</font></html>");
    protected JSpinner pointersCommand;
    // draw gesture
    protected JButton drawGesture = new JButton();
    protected JButton drawGestureBluetooth = new JButton();

    protected JButton clearCanvas = new JButton("Clear Canvas");
    protected JCheckBox rotInv = new JCheckBox();

    // toolbar commands
    protected JButton zoomIn = new JButton();
    protected JButton zoomOut = new JButton();
    protected JButton left = new JButton();
    protected JButton right = new JButton();
    protected JButton up = new JButton();
    protected JButton down = new JButton();
    protected JButton rotateright = new JButton();
    protected JButton rotateleft = new JButton();
    protected JSlider slider = new JSlider(1, 3, 1);
    protected JButton saveGesture = new JButton();

    protected double zoom = 1.0;

    // protected JPanel gesturesPanel;

    protected Box showOptions = Box.createVerticalBox();

    protected String className = "";
    protected int item;
    protected int zoomLevel = 1;
    protected int rotationAngle;

    protected JButton scoreTableButton;
    protected JSpinner pointersNum;
    private JLabel pointerNumLabel = new JLabel();

    private int state = GESTURE_PROCESSED;

    // private Thread bluetoothServer;

    private double score;

    // style
    private final Color lineColor = Color.red;
    private final Color defaultColor = new Color(0f, 0f, 0f);
    private final Font fontButtons = new Font("Arial", Font.PLAIN, 16);

    // JButton[] gestureButtons;

    private final MainFrame mainClass;
    private final TemplateScreenListener templateScreenListener;

    private JToolBar controlTools;
    private JLabel title2;
    private JPanel commands;
    private final JPanel thumbPanel = new JPanel();
    private JPanel main;
    private JPanel panelTop;

    // private JScrollPane classScrollPane;

    public TemplateScreen(MainFrame mainClass) throws IOException {
        System.out.println("costruttore name" + className);
        this.mainClass = mainClass;
        templateScreenListener = new TemplateScreenListener(this, mainClass);
        setLayout(new BorderLayout());
        setBackground(Color.gray);
        mainClass.screenMode = MainFrame.DETAILSCREEN;

        initComponents();

    }

    public Display getDisplay() {
        return display;
    }

    public void repaintCanvas() {
        canvas.repaint();
    }

    static WaitThread getWaitThread() {
        return waitThread;
    }

    static int getDrawMode() {
        return drawMode;
    }

    @SuppressWarnings({"checkstyle:executablestatementcount", "checkstyle:javancss", "checkstyle:methodlength"})
    public void initComponents() throws IOException {

        // pannello (NORTH)
        final JPanel northContainer = new JPanel();
        northContainer.setLayout(new BorderLayout());

        // pannello classi

        // pannello informativo
        final Box top = Box.createVerticalBox();

        panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panelTop.setBackground(Color.black);
        final JLabel title = new JLabel("<html><font color='white'>Dashboard </font></html>");
        title.setFont(new Font("Arial", Font.PLAIN, 34));
        try {

            title.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/back.png"))));
        } catch (final IOException e2) {

            e2.printStackTrace();
        }
        title.setToolTipText("Show Dashboard");
        @SuppressWarnings("checkstyle:anoninnerlength")
        final MouseListener mListener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {

                mainClass.setScreen(new DashboardScreen(mainClass, true));

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                title.setCursor(new Cursor(Cursor.HAND_CURSOR));
                title.setText("<html><font color='#FFDA44'>Dashboard </font></html>");

            }

            @Override
            public void mouseExited(MouseEvent e) {
                title.setText("<html><font color='white'>Dashboard </font></html>");

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

        };
        title.addMouseListener(mListener);
        panelTop.add(title);

        title2 = new JLabel();
        title2.setFont(new Font("Arial", Font.PLAIN, 34));

        panelTop.add(title2);

        top.add(panelTop);

        panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        display = new Display();
        panelTop.add(display);

        display.set("", 0);

        top.add(panelTop);
        northContainer.add(top, BorderLayout.NORTH);

        commands = new JPanel(new FlowLayout());
        commands.setBackground(new Color(28, 28, 28));

        rotInvCommand.setVisible(true);

        // saving footer
        rotInv.addItemListener(templateScreenListener);
        saveGesture.addActionListener(templateScreenListener);

        // draw with smartphone

        drawGestureBluetooth.addActionListener(templateScreenListener);

        drawGestureBluetooth.setOpaque(false);
        drawGestureBluetooth.setText("<html><font color='white'>Draw With Smartphone</font></html>");
        drawGestureBluetooth.setToolTipText("Activate BT Server to Draw With Smartphone");
        drawGestureBluetooth.setContentAreaFilled(false);
        drawGestureBluetooth.setBorderPainted(false);
        drawGestureBluetooth.setCursor(new Cursor(Cursor.HAND_CURSOR));
        drawGestureBluetooth.setFont(fontButtons);

        // draw with mouse
        drawGesture.setText("<html><font color='white'>Draw With Mouse</font></html>");
        drawGesture.setContentAreaFilled(false);
        drawGesture.setBorderPainted(false);
        drawGesture.setCursor(new Cursor(Cursor.HAND_CURSOR));
        drawGesture.setFont(fontButtons);
        drawGesture.addActionListener(templateScreenListener);

        drawGesture.setOpaque(false);

        clearCanvas.addActionListener(templateScreenListener);

        add(northContainer, BorderLayout.NORTH);

        // canvas
        main = new JPanel(new BorderLayout());

        final JPanel canvasContainer = new JPanel();
        canvasContainer.setBackground(Color.gray);
        canvasContainer.setLayout(new BoxLayout(canvasContainer, BoxLayout.Y_AXIS));
        main.add(canvasContainer, BorderLayout.CENTER);

        main.add(commands, BorderLayout.NORTH);

        canvas.addMouseWheelListener(this);
        canvasContainer.add(canvas);

        add(main, BorderLayout.CENTER);

        // pannello opzioni gesto (EAST)

        showOptions.setBackground(Color.darkGray);
        showOptions.setOpaque(true);

        showOptions.setPreferredSize(new Dimension(160, 0));

        // aggiungi checkbox show gesture
        showGesture.addItemListener(templateScreenListener);
        showGesture.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showGesture.setFont(fontButtons);
        showGesture.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/checkbox.png"))));
        showGesture.setOpaque(false);
        showGesture.setSelectedIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/checked.png"))));

        showPolyline.addItemListener(templateScreenListener);
        showPolyline.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPolyline.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/checkbox.png"))));
        showPolyline.setSelectedIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/checked.png"))));
        showPolyline.setFont(fontButtons);
        showPolyline.setOpaque(false);

        // aggiungi checkbox show vertex

        showVertex.addItemListener(templateScreenListener);
        showVertex.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showVertex.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/checkbox.png"))));
        showVertex.setSelectedIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/checked.png"))));
        showVertex.setFont(fontButtons);
        showVertex.setOpaque(false);

        showTimedGesture.addActionListener(templateScreenListener);

        showTimedGesture.setContentAreaFilled(false);
        showTimedGesture.setBorderPainted(false);
        showTimedGesture.setOpaque(false);
        showTimedGesture.setFont(fontButtons);
        showTimedGesture.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showTimedGesture.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/stopwatch.png"))));

        features.addActionListener(templateScreenListener);

        features.setContentAreaFilled(false);
        features.setBorderPainted(false);
        features.setOpaque(false);
        features.setFont(fontButtons);
        features.setCursor(new Cursor(Cursor.HAND_CURSOR));
        features.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/table.png"))));

        main.add(showOptions, BorderLayout.EAST);

        // pannello comandi inferiore (SOUTH)

        controlTools = new JToolBar() {
            private static final long serialVersionUID = 7811060300185516376L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // linux fix
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        controlTools.setBackground(new Color(28, 28, 28));
        controlTools.setPreferredSize(new Dimension(0, 50));
        controlTools.setLayout(new FlowLayout());

        // posiziona pannello comandi in basso
        add(controlTools, BorderLayout.SOUTH);

    }

    void draw(int mode) {

        final ArrayList<Polyline> polylines = mainClass.getRecognizer().getTemplate(className);
        canvasGestures.put(mode, adapt(polylines.get(item), mode));
        if (this.mode == GESTURE_TIMED) {
            canvas.paintTimedGesture();
        } else {

            canvas.paintGestures(null);
        }

    }

    private Gesture adapt(Polyline polyline, int mode) {
        ArrayList<TPoint> points = null;

        if (mode == POLYLINE || mode == VERTEX) {
            points = (ArrayList<TPoint>) polyline.getPoints();

        } else {
            // gesture
            points = polyline.getGesture().getPoints();
        }

        final Double r = polyline.getGesture().getBoundingBox();

        final double fattorex = canvas.getPreferredSize().getWidth() / 2
                - ((r.getX() * zoom) + ((r.getWidth() * zoom) / 2));
        final double fattorey = canvas.getPreferredSize().getHeight() / 2
                - ((r.getY() * zoom) + ((r.getHeight() * zoom) / 2));

        final Gesture adapted = new Gesture();
        adapted.setInfo(polyline.getGesture().getInfo());
        adapted.setRotInv(polyline.getGesture().isRotInv());
        adapted.setPointers(polyline.getGesture().getPointers());
        // int angle = 120;
        for (final TPoint point : points) {
            final TPoint temppoint = new TPoint(point.getX() * zoom + fattorex, point.getY() * zoom + fattorey,
                    point.getTime());

            adapted.addPoint(temppoint);

        }

        return adapted;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            zoomIn.doClick();
        } else {
            zoomOut.doClick();
        }

    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        System.out.println("state changed");
        zoom = slider.getValue();
        if (showGesture.isSelected()) {
            draw(GESTURE);
        }

        if (showPolyline.isSelected()) {
            draw(POLYLINE);
        }

        if (showVertex.isSelected()) {
            draw(VERTEX);
        }
    }

    public void clearCanvas() {
        canvasGestures.clear();
        currentGesture = new Gesture();
        display.set("", 0);

        controlTools.removeAll();

        canvas.repaint();
        repaint();

    }

    // mouse entered canvas
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    // mouse left canvas
    @Override
    public void mouseExited(MouseEvent e) {
    }

    // mouse pressed-depressed (no motion
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    public void update(MouseEvent e) {

        final TPoint p = new TPoint(e.getX(), e.getY(), e.getWhen());
        currentGesture.addPoint(p);

        // canvas.paintCurrentGesture();
        canvas.repaint();
        // repaint();
        e.consume();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        final int button = e.getButton();

        switch (button) {
            case MouseEvent.BUTTON1:

                startStroke();
                update(e);
                return;
            default:
        }
    }

    public void startStroke() {

        System.out.println("START STROKE - STATE " + state);
        if (state == GESTURE_PROCESSED || state == STROKE_COMPLETE) {
            clearCanvas();

        }

        state = STROKE_IN_PROGRESS;
        // display.setForeground(lineColor);
        display.set("Capturing stroke", 0);

        // repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        final int button = e.getButton();

        switch (button) {
            case MouseEvent.BUTTON1:

                strokeCompleted();
                return;
            default:
        }
    }

    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:executablestatementcount", "checkstyle:javancss",
        "checkstyle:npathcomplexity"})
    public void strokeCompleted() {

        state = STROKE_COMPLETE;
        display.setForeground(lineColor);
        display.set("Gesture recorded", 0);

        boolean empty = true;
        final String[] classes = mainClass.getRecognizer().getClassNames().toArray(new String[0]);
        // conta numero classi non vuote
        for (int i = 0; i < classes.length; i++) {
            if (mainClass.getRecognizer().getTemplate(classes[i]).size() > 0) {
                empty = false;
            }

        }
        ExtendedResult r = null;
        String recognizedName = null;

        // almeno una classe non vuota
        if (!empty) {
            if (testing) {
                if (currentGesture.getPointers() == 1) {
                    pointerNumLabel.setText(
                            "<html><font color='white' >" + currentGesture.getPointers() + " Pointer</font></html>");
                } else {
                    pointerNumLabel.setText(
                            "<html><font color='white' >" + currentGesture.getPointers() + " Pointers</font></html>");
                }

                pointerNumLabel.setFont(fontButtons);
                controlTools.add(pointerNumLabel);
            }
            r = mainClass.getRecognizer().recognizeExt(currentGesture);

            if (r == null || r.getScore() == 0) {
                score = 0;
                final String captiontext = "Not Recognized";
                display.set(captiontext, 0);
            } else {
                score = r.getScore();
                recognizedName = r.getName();

                display.setForeground(defaultColor);
                final String captiontext = "Recognized as " + recognizedName + " (" + round(score, 2) + ")";
                display.set(captiontext, 0);

                if (!this.testing && recognizedName != null && !this.className.equals(recognizedName)
                        && score > java.lang.Double.parseDouble(Settings.APPLICATION_PROPS.getProperty("scorelimit"))) {
                    display.set(captiontext + " - Is not recommended adding drawn Template to another Class", 1);
                }

                // if (this.testing == true
                // && score > java.lang.Double.parseDouble(Settings.applicationProps.getProperty("scorelimit")))
                // setCaption(captiontext, 0);

                state = GESTURE_PROCESSED;
            }
        }

        // se in modalità add template predisponi interfaccia per salvataggio
        if (!"".equals(this.className)) {

            // pointers number
            // initial, min, max, step
            final SpinnerModel model = new SpinnerNumberModel(currentGesture.getPointers(), 0, 5, 1);
            pointersNum = new JSpinner(model);
            pointersNum = new JSpinner(model);
            pointersNum.setToolTipText("Set number of pointers");

            pointerNumLabel = new JLabel("<html><font color='white' > Pointers     </font></html>");
            pointerNumLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            pointerNumLabel.setOpaque(false);
            pointerNumLabel.setFont(fontButtons);
            rotInv.setOpaque(false);
            rotInv.setText("<html><font color='white' >Rotation Invariant Recognition     </font></html>");
            rotInv.setFont(fontButtons);
            try {
                rotInv.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/checkbox.png"))));

                rotInv.setSelectedIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/checked.png"))));
            } catch (final IOException e) {

                e.printStackTrace();
            }
            rotInv.setVisible(true);
            saveGesture.setCursor(new Cursor(Cursor.HAND_CURSOR));
            saveGesture.setOpaque(false);
            saveGesture.setBorderPainted(false);
            try {

                saveGesture.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/save-24.png"))));

            } catch (final IOException e) {

                e.printStackTrace();
            }

            saveGesture.setName(className);
            saveGesture.setText(
                    "<html><font color='white' >Save to Class " + className.toUpperCase() + "     </font></html>");
            saveGesture.setFont(fontButtons);
            saveGesture.setVisible(true);

            controlTools.add(pointersNum);
            controlTools.add(pointerNumLabel);
            controlTools.add(rotInv);
            controlTools.add(saveGesture);

        }
        showOptions.removeAll();
        if (r != null) {
            showScoreTable(r);
        }

    }

    // tabella score e distanze
    private void showScoreTable(ExtendedResult r) {

        scoreTableButton = new JButton();
        scoreTableButton.setText("<html><font color='white' >Score Table</font></html>");
        scoreTableButton.setFont(new Font("Arial", Font.PLAIN, 16));
        scoreTableButton.setOpaque(false);
        scoreTableButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        scoreTableButton.setContentAreaFilled(false);
        scoreTableButton.setBorderPainted(false);
        try {

            scoreTableButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/file.png"))));
        } catch (final IOException e2) {

            e2.printStackTrace();
        }

        showOptions.add(scoreTableButton);
        showOptions.setVisible(true);

        mainClass.repaint();

        // tabella score
        final JTable scoreTable = r.getRankingTable();
        // visualizza tabella al click sul bottone
        @SuppressWarnings("checkstyle:anoninnerlength")
        final ActionListener aListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                final JPanel dialogPanel = new JPanel(new BorderLayout());

                final JButton chart = new JButton("Show Chart");
                chart.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SwingUtilities.invokeLater(() -> {

                            final Chart ex = new Chart(r.getRankingTable(), "Recognition Table", 2,
                                    scoreTable.getModel().getColumnCount(), 1, Chart.BAR, SortOrder.DESCENDING);
                            ex.setVisible(true);
                        });

                    }
                });
                dialogPanel.add(chart, BorderLayout.SOUTH);
                final JScrollPane scrollPane = new JScrollPane(scoreTable);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                scoreTable.setOpaque(false);
                scrollPane.setPreferredSize(new Dimension(500, 300));

                dialogPanel.add(scrollPane, BorderLayout.CENTER);

                final JOptionPane optionPane = new JOptionPane(dialogPanel);

                final JDialog k = optionPane.createDialog("Score Table");
                // Makes the dialog not modal
                k.setModal(false);
                k.setVisible(true);

            }
        };
        scoreTableButton.addActionListener(aListener);
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

    }

    // round 'n' to 'd' decimals
    private static double round(double n, double d) {
        final double de = Math.pow(10, d);
        return Math.round(n * de) / de;
    }

    void azzeraZoom() {
        zoom = 1;
        zoomLevel = 1;
        slider.setValue(zoomLevel);

    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        state = STROKE_IN_PROGRESS;
        update(e);
    }

    public Gesture getCurrentGesture() {
        return currentGesture;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setCurrentGesture(Gesture currentGesture) {
        this.currentGesture = currentGesture;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    // imposta interfaccia per mostrare dettagli template
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:executablestatementcount", "checkstyle:javancss",
        "checkstyle:methodlength", "checkstyle:npathcomplexity"})
    public void showTemplate() {
        final ArrayList<Polyline> polylines = mainClass.getRecognizer().getTemplate(className);
        title2.setText("<html><font color='white'> \u2022 " + className.substring(0, 1).toUpperCase()
                + className.substring(1) + "  \u2022 Detail</font></html>");
        // pointers number
        // initial, min, max, step
        final SpinnerModel model = new SpinnerNumberModel(polylines.get(item).getGesture().getPointers(), 0, 5, 1);
        pointersCommand = new JSpinner(model);
        pointersCommand.setToolTipText("Set number of pointers");
        pointersCommand.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                polylines.get(item).getGesture().setPointers((Integer) pointersCommand.getValue());
                if ((Integer) pointersCommand.getValue() == 1) {
                    display.set("Now the gesture consists of 1 pointer", 0);
                } else {
                    display.set("Now the gesture consists of " + pointersCommand.getValue() + " pointers", 0);
                }
            }
        });

        pointerNumLabel = new JLabel("<html><font color='white' > Pointers     </font></html>");
        pointerNumLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pointerNumLabel.setOpaque(false);
        pointerNumLabel.setFont(fontButtons);

        // rotation invariant command
        // aggiungi checkbox show gesture
        rotInvCommand.setOpaque(false);
        rotInvCommand.addActionListener(templateScreenListener);
        rotInvCommand.setText("<html><font color='white' >Rotation Invariant Recognition</font></html>");
        rotInvCommand.setFont(new Font("Arial", Font.PLAIN, 16));
        rotInvCommand.setContentAreaFilled(false);
        rotInvCommand.setBorderPainted(false);

        final ExtendedResult r = mainClass.getRecognizer().recognizeExt(polylines.get(item).getGesture());
        try {
            System.out.println("is rot inv? " + polylines.get(item).getGesture().isRotInv() + " checkbox is selected?"
                    + rotInvCommand.isSelected());
            if (!polylines.get(item).getGesture().isRotInv()) {

                rotInvCommand.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/checkbox.png"))));

            } else {

                rotInvCommand.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/checked.png"))));

            }
        } catch (final IOException e) {

            e.printStackTrace();
        }

        rotateleft.addActionListener(templateScreenListener);
        rotateleft.setOpaque(false);
        rotateleft.setBorderPainted(false);
        rotateleft.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rotateleft.setToolTipText("Rotate Left");
        try {
            rotateleft.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/replay-left.png"))));
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        controlTools.add(rotateleft);

        rotateright.addActionListener(templateScreenListener);
        rotateright.setOpaque(false);
        rotateright.setBorderPainted(false);
        rotateright.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rotateright.setToolTipText("Rotate Right");
        try {
            rotateright.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/replay.png"))));
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        controlTools.add(rotateright);

        final JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(1, controlTools.getHeight()));
        controlTools.add(separator);

        left.addActionListener(templateScreenListener);
        controlTools.add(left);
        left.setOpaque(false);
        left.setBorderPainted(false);
        left.setCursor(new Cursor(Cursor.HAND_CURSOR));
        left.setToolTipText("Move Left");
        try {
            left.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/left-chevron.png"))));
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        // right.setText("right");
        right.addActionListener(templateScreenListener);
        right.setOpaque(false);
        right.setBorderPainted(false);
        right.setCursor(new Cursor(Cursor.HAND_CURSOR));
        right.setToolTipText("Move Right");
        try {
            right.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/right-chevron.png"))));
        } catch (final IOException e1) {
            e1.printStackTrace();
        }

        controlTools.add(right);
        // up.setText("up");
        up.setOpaque(false);
        up.setBorderPainted(false);
        up.setCursor(new Cursor(Cursor.HAND_CURSOR));
        try {
            up.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/up-chevron.png"))));
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        up.addActionListener(templateScreenListener);
        up.setToolTipText("Move Up");
        controlTools.add(up);
        // down.setText("down");
        down.setOpaque(false);
        down.setBorderPainted(false);
        down.setCursor(new Cursor(Cursor.HAND_CURSOR));
        down.setToolTipText("Move Down");
        try {
            down.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/down-chevron.png"))));
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        down.addActionListener(templateScreenListener);
        controlTools.add(down);
        final JSeparator separator1 = new JSeparator(SwingConstants.VERTICAL);
        separator1.setPreferredSize(new Dimension(1, controlTools.getHeight()));
        controlTools.add(separator1);

        try {
            zoomOut.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/zoom-out.png"))));
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        zoomOut.setOpaque(false);
        zoomOut.setBorderPainted(false);
        zoomOut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        zoomOut.addActionListener(templateScreenListener);
        zoomOut.setToolTipText("Zoom Out");

        controlTools.add(zoomOut);
        slider.setMajorTickSpacing(1);
        slider.setOpaque(false);
        slider.setPaintTicks(true);
        slider.addChangeListener(this);

        controlTools.add(slider);

        // zoomIn.setText("ZoomIn");

        try {
            zoomIn.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/zoom-in.png"))));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        zoomIn.setOpaque(false);
        zoomIn.setBorderPainted(false);
        zoomIn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        zoomIn.addActionListener(templateScreenListener);
        zoomIn.setToolTipText("Zoom In");

        controlTools.add(zoomIn);
        controlTools.setVisible(true);

        rotInv.setVisible(false);
        saveGesture.setVisible(false);

        // commands
        commands.removeAll();
        commands.add(pointersCommand);
        commands.add(pointerNumLabel);
        commands.add(rotInvCommand);

        // options
        // commands.removeAll();
        showOptions.removeAll();
        showOptions.add(showGesture);
        showOptions.add(showPolyline);
        showOptions.add(showVertex);
        showOptions.add(showTimedGesture);
        showOptions.add(features);
        showScoreTable(r);

        showPolyline.setEnabled(true);
        showPolyline
                .setToolTipText(mainClass.getRecognizer().getTemplate(className).get(item).getNumLines() + " lines");
        showVertex.setEnabled(true);
        showVertex.setToolTipText(
                mainClass.getRecognizer().getTemplate(className).get(item).getNumVertexes() + " vertices");
        showTimedGesture.setEnabled(true);
        showTimedGesture.setToolTipText(
                mainClass.getRecognizer().getTemplate(className).get(item).getGesture().getMilliseconds() + " ms");
        showGesture.setEnabled(true);
        showGesture.setSelected(true);

        canvas.removeMouseListener(this);
        canvas.removeMouseMotionListener(this);
        // thumbnails panel of class

        main.add(createThumbsPanel(className, false), BorderLayout.WEST);

    }

    // predisponi gui per disegno template (add template)
    // TODO fix
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:executablestatementcount", "checkstyle:javancss",
        "checkstyle:npathcomplexity"})
    public void drawTemplate(int mode, int drawmode, boolean thumbnails) {

        this.mode = mode;
        drawMode = drawmode;
        if (this.mode == CURRENT && !this.testing) {
            title2.setText("<html><font color='white'> \u2022 " + className.substring(0, 1).toUpperCase()
                    + className.substring(1) + " \u2022 Add Template</font></html>");
        }
        if (this.mode == CURRENT && this.testing) {
            title2.setText("<html><font color='white'>  \u2022 Test Recognizer</font></html>");
        }

        System.out.println("drawTemplate mode:" + mode + " drawmode: " + drawMode);
        controlTools.removeAll();
        showOptions.removeAll();
        commands.removeAll();
        // pannello thumbnails classe gesto (WEST)

        clearCanvas();

        if (drawMode == SMARTPHONE) {

            try {
                drawGestureBluetooth
                        .setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/smartphone-green.png"))));

                drawGesture.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/mouse.png"))));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            if (waitThread != null && !WaitThread.STATE_ZERO.equals(waitThread.getState())) {

                waitThread.setGui(this);
                waitThread.setDraw(true);
                if (waitThread.getDev() != null) {
                    try {
                        display.set("Device Connected " + waitThread.getDev().getFriendlyName(false)
                                + " - Use 'Blueotooth Gestures' App to draw gesture", 0);
                    } catch (final IOException e) {

                        e.printStackTrace();
                    }
                } else {
                    display.set("BT Server Started - Connect your device using 'Blueotooth Gestures' App", 0);
                }
            } else {

                System.out.println("start thread");
                try {
                    final WaitThread wThread = new WaitThread(this);
                    waitThread = wThread;

                    final Thread bServer = new Thread(wThread);
                    // bluetoothServer = bServer;

                    bServer.start();

                } catch (final BluetoothStateException e) {
                    JOptionPane.showMessageDialog(null, "Attiva il Bluetooth del PC", "Bluetooth",
                            JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    if (waitThread != null) {
                        waitThread.setState(WaitThread.STATE_ZERO);
                    }
                    this.drawGesture.doClick();

                }
            }

        } else if (drawMode == MOUSE) {
            if (waitThread != null) {
                waitThread.setDraw(false);
            }
            try {
                drawGesture.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/mouse-green.png"))));

                drawGestureBluetooth
                        .setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/smartphone.png"))));
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            display.set("Draw Gesture on Canvas with Mouse", 0);

            canvas.addMouseListener(this);
            canvas.addMouseMotionListener(this);
        }
        repaint();

        commands.add(drawGesture);
        commands.add(drawGestureBluetooth);

        rotInv.setVisible(false);
        saveGesture.setVisible(false);
        controlTools.add(rotInv);
        controlTools.add(saveGesture);

        if (!this.testing && thumbnails) {

            main.add(createThumbsPanel(className, false), BorderLayout.WEST);
            main.repaint();
            repaint();
        }

    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    // crea le anteprime dei template della classe
    @SuppressWarnings({"checkstyle:executablestatementcount", "checkstyle:javancss", "checkstyle:methodlength"})
    private JScrollPane createThumbsPanel(String className, boolean commands) {
        // Box thumbPanel = Box.createVerticalBox();
        System.out.println("CREATE THUMBNAIL PANEL " + className);
        final ArrayList<Polyline> polylines = mainClass.getRecognizer().getTemplate(className);
        // final Box[] templateBoxes = new Box[polylines.size()];

        thumbPanel.setBackground(Color.darkGray);
        thumbPanel.setOpaque(true);

        thumbPanel.setPreferredSize(new Dimension(160, polylines.size() * 220 + 30));
        for (int p = 0; p < polylines.size(); p++) {
            System.out.println("thumb polyline " + p);

            // pannello del template
            final Box templatePanel = Box.createVerticalBox();
            // templateBoxes[p] = templatePanel;
            templatePanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 0)));

            // riga panel delete template
            final JPanel controlTemplate = new JPanel(new BorderLayout());
            // controlTemplate.setBorder(new EmptyBorder(3, 3, 3, 3));

            controlTemplate.setBackground(Color.gray);

            try {
                final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonsPanel.setOpaque(false);
                if (commands) {
                    final JButton move = new JButton(
                            new ImageIcon(ImageIO.read(getClass().getResource("/img/exit-16-white.png"))));
                    move.setBorder(BorderFactory.createEmptyBorder());
                    move.setContentAreaFilled(false);
                    move.setName("movetemplate_" + className + "_" + p);
                    move.setToolTipText("Move Template to another class");
                    move.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    // move.addActionListener(mainScreenListener);

                    final String imgDelete = "/img/multiply-white-16.png";
                    final JButton delete = new JButton(new ImageIcon(ImageIO.read(getClass().getResource(imgDelete))));
                    delete.setBorder(BorderFactory.createEmptyBorder());
                    delete.setContentAreaFilled(false);
                    delete.setName("deletetemplate_" + className + "_" + p);
                    delete.setToolTipText("Delete Template");
                    delete.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    // delete.addActionListener(mainScreenListener);
                    delete.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent evt) {
                            try {
                                delete.setIcon(new ImageIcon(
                                        ImageIO.read(getClass().getResource("/img/multiply-red-16.png"))));
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void mouseExited(java.awt.event.MouseEvent evt) {
                            try {
                                delete.setIcon(new ImageIcon(ImageIO.read(getClass().getResource(imgDelete))));
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    buttonsPanel.add(move);
                    buttonsPanel.add(delete);
                }
                final JLabel number = new JLabel("<html><font color='white'>&nbsp;" + p + "</font></html>");
                number.setFont(new Font("Arial", Font.PLAIN, 18));
                controlTemplate.add(number, BorderLayout.WEST);
                controlTemplate.add(buttonsPanel, BorderLayout.EAST);

            } catch (final IOException e) {

                e.printStackTrace();
            }

            templatePanel.add(controlTemplate);
            // template thumbnail
            // Thumbnail tempThumbnail = new
            // Thumbnail(polylines.get(p).getGesture());
            final Thumbnail tempThumbnail = new Thumbnail(polylines.get(p).getGesture());
            tempThumbnail.addMouseListener(new DashboardListener(null, mainClass));
            tempThumbnail.setName("thumbnail_" + className + "_" + p);
            tempThumbnail.setToolTipText("Show Template Detail");

            tempThumbnail.setCursor(new Cursor(Cursor.HAND_CURSOR));
            tempThumbnail.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {

                    if (!MainFrame.isModalDialogShowing()) {
                        templatePanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
                    }

                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {

                    if (!MainFrame.isModalDialogShowing()) {
                        templatePanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 0)));
                    }
                }
            });

            templatePanel.add(tempThumbnail);

            // panel opzioni (rotinv)
            // JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            // optionPanel.setBackground(Color.lightGray);
            // optionPanel.setPreferredSize(new Dimension(150, 35));
            // JButton rotInv = new JButton();
            // rotInv.setCursor(new Cursor(Cursor.HAND_CURSOR));
            // rotInv.setName("rotinv_" + className + "_" + p);
            //
            // rotInv.setContentAreaFilled(false);
            // rotInv.setBorder(BorderFactory.createEmptyBorder());
            // try {
            // if (polylines.get(p).getGesture().isRotInv()) {
            // rotInv.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/repeat-24-black.png"))));
            // rotInv.setToolTipText("Is Rotation Invariant (click to set not RI)");
            // } else {
            // rotInv.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/notrotinv-black.png"))));
            // rotInv.setToolTipText("Is Not Rotation Invariant (click to set RI)");
            // }
            // } catch (IOException e2) {
            //
            // e2.printStackTrace();
            // }
            // optionPanel.add(rotInv);
            // templatePanel.add(optionPanel);
            //
            // thumbPanel.add(templatePanel);

            // panel opzioni (rotinv)
            final JPanel optionPanel = new JPanel(new BorderLayout());
            optionPanel.setBackground(Color.lightGray);
            optionPanel.setPreferredSize(new Dimension(150, 35));

            final JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            rightPanel.setOpaque(false);
            final JButton rotInv = new JButton();

            rotInv.setName("rotinv_" + className + "_" + p);

            rotInv.setContentAreaFilled(false);
            rotInv.setBorder(BorderFactory.createEmptyBorder());
            try {
                if (polylines.get(p).getGesture().isRotInv()) {
                    rotInv.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/repeat-24-black.png"))));
                    rotInv.setToolTipText("Is Rotation Invariant (click to set not RI)");
                } else {
                    rotInv.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/notrotinv-black.png"))));
                    rotInv.setToolTipText("Is Not Rotation Invariant (click to set RI)");
                }
            } catch (final IOException e2) {

                e2.printStackTrace();
            }
            rightPanel.add(rotInv);
            optionPanel.add(rightPanel, BorderLayout.EAST);

            // if (polylines.get(p).getGesture().getPointers() > 1) {
            final JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            leftPanel.setOpaque(false);
            final JLabel pointers = new JLabel(polylines.get(p).getGesture().getPointers() + "P");
            pointers.setToolTipText(polylines.get(p).getGesture().getPointers() + " pointers");
            pointers.setFont(new Font("Arial", Font.PLAIN, 18));
            leftPanel.add(pointers);
            optionPanel.add(leftPanel, BorderLayout.WEST);
            // }

            templatePanel.add(optionPanel);
            thumbPanel.add(templatePanel);
        }

        try {

            final String iagb = "/img/plus-white-32.png";
            final JButton addGestureButton = new JButton(new ImageIcon(ImageIO.read(getClass().getResource(iagb))));
            addGestureButton.setContentAreaFilled(false);
            addGestureButton.setBorderPainted(false);

            addGestureButton.setName("addgesture_" + className);

            addGestureButton.setToolTipText("Add Template to " + className + " Class");
            addGestureButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            addGestureButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    try {
                        ((JButton) evt.getSource())
                                .setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/plus-green-32.png"))));
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    try {
                        ((JButton) evt.getSource()).setIcon(new ImageIcon(ImageIO.read(getClass().getResource(iagb))));
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            addGestureButton.addActionListener(new DashboardListener(null, mainClass));
            thumbPanel.add(addGestureButton);
        } catch (final IOException e) {

            e.printStackTrace();
        }

        final JScrollPane classScrollPane = new JScrollPane(thumbPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        classScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return classScrollPane;

    }

}
