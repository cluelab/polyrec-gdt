package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * @author rbufano
 *
 */
public class Display extends JLabel {

    private static final long serialVersionUID = 1L;
    private final Color okColor = new Color(145, 220, 90);
    private final Color warningColor = new Color(255, 218, 68);

    public final int DISPLAY_OK = 0;
    public final int DISPLAY_WARNING = 1;

    /**
     * 
     */
    public Display() {
        super();

        setFont(new Font("Arial", Font.PLAIN, 20));
        setVerticalAlignment(SwingConstants.CENTER);

    }

    /**
     * @param arg0
     */
    public Display(String arg0) {
        super(arg0);
        setFont(new Font("Arial", Font.PLAIN, 20));
        setVerticalAlignment(SwingConstants.CENTER);

    }

    /**
     * @param text
     * @param type
     */
    public void set(String text, int type) {
        if (type == DISPLAY_OK) {
            getParent().setBackground(okColor);

            try {

                setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/info.png"))));
            } catch (final IOException e2) {

                e2.printStackTrace();
            }
        }
        if (type == DISPLAY_WARNING) {
            getParent().setBackground(warningColor);
            try {

                setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/img/warning.png"))));
            } catch (final IOException e2) {

                e2.printStackTrace();
            }
        }
        setText(text);
    }

}
