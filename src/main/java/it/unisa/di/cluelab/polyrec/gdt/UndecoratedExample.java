package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import it.unisa.di.cluelab.polyrec.Gesture;

public class UndecoratedExample {

    private final JFrame frame = new JFrame();

    class BorderPanel extends JPanel {

        private JButton label;
        int pX, pY;

        public BorderPanel() {
            try {
                label = new JButton(new ImageIcon(ImageIO.read(getClass().getResource("/img/multiply-white-16.png"))));
            } catch (final IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            label.setBorder(BorderFactory.createEmptyBorder());
            label.setContentAreaFilled(false);
            label.setCursor(new Cursor(Cursor.HAND_CURSOR));

            setBackground(Color.gray);
            setLayout(new FlowLayout(FlowLayout.RIGHT));

            add(label);

            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    frame.dispose();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent me) {
                    // Get x,y and store them
                    pX = me.getX();
                    pY = me.getY();

                }

                @Override
                public void mouseDragged(MouseEvent me) {

                    frame.setLocation(frame.getLocation().x + me.getX() - pX, frame.getLocation().y + me.getY() - pY);
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent me) {

                    frame.setLocation(frame.getLocation().x + me.getX() - pX, frame.getLocation().y + me.getY() - pY);
                }
            });
        }
    }

    class OutsidePanel extends JPanel {

        public OutsidePanel(Gesture gesture) {
            setLayout(new BorderLayout());
            add(new Thumbnail(gesture), BorderLayout.CENTER);
            add(new BorderPanel(), BorderLayout.PAGE_START);
            setBorder(new LineBorder(Color.BLACK, 2));
        }
    }

    void createAnsShowGui(Gesture gesture) {
        final ComponentResizer cr = new ComponentResizer();
        cr.setMinimumSize(new Dimension(150, 150));
        cr.setMaximumSize(new Dimension(800, 800));
        cr.registerComponent(frame);
        cr.setSnapSize(new Dimension(10, 10));
        // frame.setUndecorated(true);

        // frame.add(new OutsidePanel(gesture));
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.pack();
        frame.setSize(200, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.add(new Thumbnail(gesture), BorderLayout.CENTER);
        System.setProperty("sun.java2d.noddraw", "true");

        // WindowUtils.setWindowTransparent(this.getFrames()[0], true);

    }

    // public static void main(String[] args) {
    // SwingUtilities.invokeLater(new Runnable() {
    // public void run() {
    // new UndecoratedExample().createAnsShowGui();
    // }
    // });
    // }
}
