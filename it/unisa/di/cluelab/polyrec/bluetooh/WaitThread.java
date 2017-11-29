package it.unisa.di.cluelab.polyrec.bluetooh;

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
import javax.swing.JOptionPane;

import it.unisa.di.cluelab.polyrec.TPoint;
import it.unisa.di.cluelab.polyrec.gdt.TemplateScreen;


public class WaitThread implements Runnable {

	/** Constructor */
	// TestApplet3Swing gui;
	TemplateScreen gui;
	private static StreamConnectionNotifier notifier;
	private static StreamConnection connection = null;
	public String state = "0";
	private boolean draw = true;
	LocalDevice local;
	RemoteDevice dev;

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

	/**
	 * Waiting for connection from devices
	 * 
	 * @throws BluetoothStateException
	 */
	private void waitForConnection() {

		if (notifier == null && state == "0" && local != null) {
			
			

			UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
			String url = "btspp://localhost:" + uuid.toString() + ";name=polyrecGDT";
			try {
				notifier = (StreamConnectionNotifier) Connector.open(url);

				gui.display.set("BT Server Started - Connect your device using 'Blueotooth Gestures' App", 0);
				state = "1_0";
			} catch (Exception e) {
				state = "0";
				e.printStackTrace();
				return;
			}

			while (true) {
				System.out.println("waiting for connection...");
				this.state = "1_1";

				try {
					connection = notifier.acceptAndOpen();
				
					
					(new Thread() {
						  public void run() {
							  try {
								dev = RemoteDevice.getRemoteDevice(connection);
								gui.display.set(
										"Device Connected: " + dev.getFriendlyName(false) + "(" + dev.getBluetoothAddress() + ")",0);
							} catch (IOException e) {
					
								e.printStackTrace();
							}
						  }
						 }).start();
					

					/*
					 * ProcessConnectionThread processConnectionThread = new
					 * ProcessConnectionThread(connection,gui); Thread
					 * connectionThread = new Thread(processConnectionThread);
					 * connectionThread.start();
					 */

					InputStream inputStream = connection.openInputStream();
					gui.display.set("Device Connected",0);
	
					draw = true;

					boolean first = true;
					ObjectInputStream ois = null;

					while (true) {

						ois = new ObjectInputStream(inputStream);

						TPoint tpoint = (TPoint) ois.readObject();
						if (draw) {

							if (tpoint.getX() == -1) {
								
								gui.strokeCompleted();
								first = true;
								 System.out.println("received -1: stroke completed - numero puntatori:"+gui.getCurrentGesture().getPointers());
								 
								 
							} else if (tpoint.getX() == -2) {

								gui.clearCanvas();
								System.out.println("received -2: clear canvas "+tpoint);

							} else if (tpoint.getX() == -3) {
								gui.getCurrentGesture().setPointers((int)tpoint.getY());
								//System.out.println("POINTERS RICEVUTI: "+(int)tpoint.getY());
							
							} else if (first) {

								gui.startStroke();
								first = false;
							} else {
								System.out.println("add point");
								gui.getCurrentGesture().addPoint(tpoint);
								gui.setState(TemplateScreen.STROKE_IN_PROGRESS);
								gui.setMode(TemplateScreen.CURRENT);
								gui.repaint();
								gui.canvas.repaint();

								first = false;
							}

						}
					}
				} catch (EOFException e) {
					e.printStackTrace();
					gui.display.set("Device Connection Lost",1);
					e.printStackTrace();
					this.state = "0";
					dev =null;

				} catch (ClassNotFoundException e) {
	
					e.printStackTrace();
		
					this.state = "0";
					dev =null;
				} catch (IOException e) {
		
					e.printStackTrace();

					this.state = "0";
					dev =null;
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