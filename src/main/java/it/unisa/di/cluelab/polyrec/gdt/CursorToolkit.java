package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;
import javax.swing.RootPaneContainer;

/** Basic CursorToolkit that swallows mouseclicks */
public class CursorToolkit {
    private final static MouseAdapter mouseAdapter = new MouseAdapter() {
    };

    private CursorToolkit() {
    }

    /** Sets cursor for specified component to Wait cursor */
    public static void startWaitCursor(JComponent component) {
        final RootPaneContainer root = ((RootPaneContainer) component.getTopLevelAncestor());
        root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        root.getGlassPane().addMouseListener(mouseAdapter);
        root.getGlassPane().setVisible(true);

    }

    /** Sets cursor for specified component to normal cursor */
    public static void stopWaitCursor(JComponent component) {
        final RootPaneContainer root = ((RootPaneContainer) component.getTopLevelAncestor());
        root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        root.getGlassPane().removeMouseListener(mouseAdapter);
        root.getGlassPane().setVisible(false);
    }
}
