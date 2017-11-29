package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLayeredPane;

import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.TPoint;
import it.unisa.di.cluelab.polyrec.geom.Rectangle2D.Double;

/**
 * @author roberto
 *
 */
public class Thumbnail extends JLayeredPane {

    private final Gesture gesture;
    // private Polyline polyline;

    /**
     * @param gesture
     */
    public Thumbnail(Gesture gesture) {
        this.gesture = gesture;
        this.setBackground(Color.lightGray);
        this.setOpaque(true);
        this.setPreferredSize(new Dimension(150, 150));

        repaint();

    }

    // public Thumbnail(Polyline polyline) {
    // this.polyline = polyline;
    // this.setBackground(Color.lightGray);
    // this.setOpaque(true);
    // this.setPreferredSize(new Dimension(150, 150));
    //
    // repaint();
    //
    // }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        final Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(Color.red);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final Gesture normalizedGesture = ExtendedPolyRecognizerGSS.normalizeGesture(gesture, this.getWidth(),
                this.getHeight(), 20);

        if (normalizedGesture.getPoints().size() < 2) {
            return;
        }

        final Double r = normalizedGesture.getBoundingBox();
        // per centrare il template
        final double fattorex = getWidth() / 2 - ((r.getX()) + ((r.getWidth()) / 2));
        final double fattorey = getHeight() / 2 - ((r.getY()) + ((r.getHeight()) / 2));

        // primo punto
        g2.setStroke(new BasicStroke(5));
        g2.drawLine((int) (normalizedGesture.getPoints().get(0).getX() + fattorex),
                (int) (normalizedGesture.getPoints().get(0).getY() + fattorey),
                (int) (normalizedGesture.getPoints().get(0).getX() + fattorex),
                (int) (normalizedGesture.getPoints().get(0).getY() + fattorey));

        g2.setStroke(new BasicStroke(1));
        for (int i = 0; i < normalizedGesture.getPoints().size() - 1; i++) {

            final TPoint p1 = normalizedGesture.getPoints().get(i);
            final TPoint p2 = normalizedGesture.getPoints().get(i + 1);
            g2.drawLine((int) (p1.getX() + fattorex), (int) (p1.getY() + fattorey), (int) (p2.getX() + fattorex),
                    (int) (p2.getY() + fattorey));

            for (int pointer = 2; pointer <= normalizedGesture.getPointers(); pointer++) {
                final int strokesdistance = 10;

                if (pointer % 2 == 1) {
                    g2.drawLine((int) p1.getX() + (int) fattorex + strokesdistance * (pointer / 2),
                            (int) p1.getY() + (int) fattorey + strokesdistance * (pointer / 2),
                            (int) p2.getX() + (int) fattorex + strokesdistance * (pointer / 2),
                            (int) p2.getY() + (int) fattorey + strokesdistance * (pointer / 2));
                } else {
                    g2.drawLine((int) p1.getX() + (int) fattorex - strokesdistance * (pointer / 2),
                            (int) p1.getY() + (int) fattorey - strokesdistance * (pointer / 2),
                            (int) p2.getX() + (int) fattorex - strokesdistance * (pointer / 2),
                            (int) p2.getY() + (int) fattorey + -strokesdistance * (pointer / 2));
                }
            }

        }

        g2.dispose();
    }

}
