package it.unisa.di.cluelab.polyrec.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.microedition.io.StreamConnection;

import it.unisa.di.cluelab.polyrec.TPoint;
import it.unisa.di.cluelab.polyrec.gdt.TemplateScreen;

/**
 * not used.
 */
public class ProcessConnectionThread implements Runnable {

    private final StreamConnection connection;
    // ProcessConnectionThread thread;

    // Constant that indicate command from devices
    // private static final int EXIT_CMD = -1;
    // private static final int KEY_RIGHT = 1;
    // private static final int KEY_LEFT = 2;
    // TestApplet3Swing gui;
    private final TemplateScreen gui;

    // public ProcessConnectionThread(StreamConnection connection, TestApplet3Swing gui) {
    // mConnection = connection;
    // this.gui = gui;
    // }
    public ProcessConnectionThread(StreamConnection connection, TemplateScreen gui) {
        this.connection = connection;
        this.gui = gui;
    }

    @Override
    public void run() {
        try {

            final InputStream inputStream = connection.openInputStream();

            boolean first = true;
            ObjectInputStream ois = null;
            while (true) {
                ois = new ObjectInputStream(inputStream);

                final TPoint tpoint = (TPoint) ois.readObject();

                if (tpoint.getX() == -1) {
                    gui.strokeCompleted();
                    first = true;
                    System.out.println("last");
                } else if (tpoint.getX() == -2) {
                    gui.clearCanvas();
                    System.out.println("clear canvas");
                } else if (tpoint.getX() == -3) {
                    connection.close();
                    Thread.currentThread().interrupt();
                    System.out.println("stop processthread");
                    ois.close();
                    // this.state = "0";
                    return;
                } else if (first) {
                    System.out.println("first");
                    gui.startStroke();
                    first = false;
                } else {
                    System.out.println("move");
                    gui.getCurrentGesture().addPoint(tpoint);
                    System.out.println(tpoint);
                    gui.setState(TemplateScreen.STROKE_IN_PROGRESS);
                    gui.setMode(TemplateScreen.CURRENT);
                    gui.repaint();
                    gui.repaintCanvas();

                    first = false;
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // ProcessConnectionThread connectionThread = new ProcessConnectionThread(connection,gui);
        // Thread processThread = new Thread(connectionThread);
        // processThread.start();
    }
}
