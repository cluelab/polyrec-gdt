package it.unisa.di.cluelab.polyrec.bluetooth;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import it.unisa.di.cluelab.polyrec.TPoint;
import it.unisa.di.cluelab.polyrec.gdt.TemplateScreen;

/**
 * Wait thread.
 */
public class WaitThread implements Runnable {

    public static final String STATE_ZERO = "0";
    private static StreamConnectionNotifier notifier;
    private static StreamConnection connection;
    // TestApplet3Swing gui;
    private String state = STATE_ZERO;
    private TemplateScreen gui;
    private boolean draw = true;
    private final LocalDevice local;
    private RemoteDevice dev;

    public WaitThread(TemplateScreen gui) throws BluetoothStateException {

        this.gui = gui;

        local = LocalDevice.getLocalDevice();
        local.setDiscoverable(DiscoveryAgent.GIAC);

    }

    @Override
    public void run() {

        waitForConnection();

    }

    public void setDraw(boolean draw) {
        this.draw = draw;

    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Waiting for connection from devices.
     */
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:executablestatementcount", "checkstyle:javancss"})
    private void waitForConnection() {

        if (notifier == null && STATE_ZERO.equals(state) && local != null) {

            // "04c6093b-0000-1000-8000-00805f9b34fb"
            final UUID uuid = new UUID(80087355);
            final String url = "btspp://localhost:" + uuid.toString() + ";name=polyrecGDT";
            try {
                notifier = (StreamConnectionNotifier) Connector.open(url);

                gui.getDisplay().set("BT Server Started - Connect your device using 'Blueotooth Gestures' App", 0);
                state = "1_0";
            } catch (final Exception e) {
                state = STATE_ZERO;
                e.printStackTrace();
                return;
            }

            while (true) {
                System.out.println("waiting for connection...");
                this.state = "1_1";

                try {
                    connection = notifier.acceptAndOpen();

                    (new Thread() {
                        @Override
                        public void run() {
                            try {
                                dev = RemoteDevice.getRemoteDevice(connection);
                                gui.getDisplay().set("Device Connected: " + dev.getFriendlyName(false) + "("
                                        + dev.getBluetoothAddress() + ")", 0);
                            } catch (final IOException e) {

                                e.printStackTrace();
                            }
                        }
                    }).start();

                    // ProcessConnectionThread processConnectionThread = new ProcessConnectionThread(connection, gui);
                    // Thread connectionThread = new Thread(processConnectionThread);
                    // connectionThread.start();

                    final InputStream inputStream = connection.openInputStream();
                    gui.getDisplay().set("Device Connected", 0);

                    draw = true;

                    boolean first = true;
                    ObjectInputStream ois = null;

                    while (true) {

                        ois = new ObjectInputStream(inputStream);

                        final TPoint tpoint = (TPoint) ois.readObject();
                        if (draw) {

                            if (tpoint.getX() == -1) {

                                gui.strokeCompleted();
                                first = true;
                                System.out.println("received -1: stroke completed - numero puntatori:"
                                        + gui.getCurrentGesture().getPointers());

                            } else if (tpoint.getX() == -2) {

                                gui.clearCanvas();
                                System.out.println("received -2: clear canvas " + tpoint);

                            } else if (tpoint.getX() == -3) {
                                gui.getCurrentGesture().setPointers((int) tpoint.getY());
                                // System.out.println("POINTERS RICEVUTI: "+(int)tpoint.getY());

                            } else if (first) {

                                gui.startStroke();
                                first = false;
                            } else {
                                System.out.println("add point");
                                gui.getCurrentGesture().addPoint(tpoint);
                                gui.setState(TemplateScreen.STROKE_IN_PROGRESS);
                                gui.setMode(TemplateScreen.CURRENT);
                                gui.repaint();
                                gui.repaintCanvas();

                                first = false;
                            }

                        }
                    }
                } catch (final EOFException e) {
                    e.printStackTrace();
                    gui.getDisplay().set("Device Connection Lost", 1);
                    e.printStackTrace();
                    this.state = STATE_ZERO;
                    dev = null;

                } catch (final ClassNotFoundException e) {

                    e.printStackTrace();

                    this.state = STATE_ZERO;
                    dev = null;
                } catch (final IOException e) {

                    e.printStackTrace();

                    this.state = STATE_ZERO;
                    dev = null;
                }

            }
        }
    }

    public static StreamConnectionNotifier getNotifier() {
        return notifier;
    }

    public void setGui(TemplateScreen gui) {
        this.gui = gui;
    }

    public RemoteDevice getDev() {
        return dev;
    }

}
