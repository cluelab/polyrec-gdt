package it.unisa.di.cluelab.polyrec.gdt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.bluetooh.sendapp.AvailableDevice;
import it.unisa.di.cluelab.polyrec.bluetooh.sendapp.ObexPutClient;
import it.unisa.di.cluelab.polyrec.bluetooh.sendapp.RemoteDeviceDiscovery;


public class MenuListener implements ActionListener {

	private MainFrame mainFrame;

	public MenuListener(MainFrame mainFrame) {

		this.mainFrame = mainFrame;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == mainFrame.getMenu().exit) {

			mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
			return;
		}
		if (e.getSource() == mainFrame.getMenu().settings) {
			new Settings();
			return;
		}
		//samples
		if (e.getSource() == mainFrame.getMenu().replaceClassesSamples) {
			mainFrame.getRecognizer().removeClasses();
			mainFrame.getRecognizer().addSamples();
			mainFrame.setScreen(new DashboardScreen(mainFrame, true));
			mainFrame.getMenu().updateMenu();
			return;
		}
		if (e.getSource() == mainFrame.getMenu().addClassesSamples) {
			mainFrame.getRecognizer().addSamples();
			mainFrame.setScreen(new DashboardScreen(mainFrame, true));
			mainFrame.getMenu().updateMenu();
			return;
		}
		//open from file
		if (e.getSource() == mainFrame.getMenu().replaceClasses) {
			
			DashboardScreen dashboardScreen = new DashboardScreen(mainFrame, true);
			
			CursorToolkit.startWaitCursor(mainFrame.getRootPane());
			//if (importsKB(ExtendedPolyRecognizerGSS.REPLACE)) {
			 if(imports(ExtendedPolyRecognizerGSS.REPLACE)){
				dashboardScreen.display.set("Class and Templates imported from selected file", 0);
			
				mainFrame.setScreen(new DashboardScreen(mainFrame, true));
				if ( mainFrame.getOpenedFile()!=null)
				mainFrame.setTitle("PolyRec GDT (" + mainFrame.getOpenedFile() + ")");
				
			} else
				dashboardScreen.display.set("Error importing gesture set from selected file", 1);
			CursorToolkit.stopWaitCursor(mainFrame.getRootPane());
			mainFrame.getMenu().updateMenu();
			
			return;
		}
		if (e.getSource() == mainFrame.getMenu().addTemplates) {
			
			DashboardScreen dashboardScreen = new DashboardScreen(mainFrame, true);
			CursorToolkit.startWaitCursor(mainFrame.getRootPane());
			try {
				if (imports(ExtendedPolyRecognizerGSS.ADD_TEMPLATES)) {

					dashboardScreen.display.set("Class and Templates imported from selected file", 0);
					mainFrame.setScreen(new DashboardScreen(mainFrame, true));
					mainFrame.setTitle("PolyRec GDT (" + mainFrame.getOpenedFile() + ")");

				} else
					dashboardScreen.display.set("Error importing gesture set from selected file", 1);
			} catch (Exception e1) {

				e1.printStackTrace();
				CursorToolkit.stopWaitCursor(mainFrame.getRootPane());
			}
			CursorToolkit.stopWaitCursor(mainFrame.getRootPane());
			mainFrame.getMenu().updateMenu();
			return;
		}
		//export recognizer
		if (e.getSource() == mainFrame.getMenu().exportRecognizer) {
			
			JFileChooser saveFile = new JFileChooser();
			saveFile.setSelectedFile(new File("polyrec-recognizer2.jar"));
			FileNameExtensionFilter filter1 = new FileNameExtensionFilter(MainFrame.EXTENSION_JAR, "jar");
			saveFile.setFileFilter(filter1);
			int retrival = saveFile.showSaveDialog(null);

			if (retrival == JFileChooser.APPROVE_OPTION) {

				File f1 = new File("polyrec-recognizer2.jar");
				File f2;
				if (saveFile.getSelectedFile().toString().endsWith(".jar"))
					f2 = new File(saveFile.getSelectedFile().toString());
				else
					f2 = new File(saveFile.getSelectedFile().toString() + ".jar");

				try {
					copyFile(f1, f2);
				} catch (IOException e1) {

					JOptionPane.showMessageDialog(mainFrame, "Error exporting recognizer\n(" + e1.getMessage() + ")",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				if (mainFrame.getScreen() instanceof DashboardScreen) {
					DashboardScreen screen = (DashboardScreen) mainFrame.getScreen();
					screen.display.set("Reconignizer exported", 0);
				} else if (mainFrame.getScreen() instanceof TemplateScreen) {
					TemplateScreen screen = (TemplateScreen) mainFrame.getScreen();
					screen.display.set("Reconignizer exported", 0);
				}
			}
			return;
		}
		//new Gesture Set
		if (e.getSource() == mainFrame.getMenu().newGS) {
			int result = JOptionPane.CLOSED_OPTION;
			if (mainFrame.getMenu().save.isEnabled() || mainFrame.getMenu().saveas.isEnabled()) {
				result = JOptionPane.showConfirmDialog(null, "Save Gesture Set before closing?", "Confirm",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
				if (result == JOptionPane.YES_OPTION) {
					if (mainFrame.getMenu().save.isEnabled())
						mainFrame.getMenu().save.doClick();
					else if (mainFrame.getMenu().saveas.isEnabled())
						mainFrame.getMenu().saveas.doClick();

				}
			}
			mainFrame.getRecognizer().removeClasses();
			mainFrame.setOpenedFile("");
			DashboardScreen dashboardScreen = new DashboardScreen(mainFrame,false);
			mainFrame.setScreen(dashboardScreen);
			mainFrame.setTitle("PolyRec GDT");

			mainFrame.getMenu().updateMenu();

		}
		if (e.getSource() == mainFrame.getMenu().saveas) {
			try {
				exports(true);
				mainFrame.setTitle("PolyRec GDT (" + mainFrame.getOpenedFile() + ")");

				if (mainFrame.getScreen() instanceof DashboardScreen) {
					DashboardScreen screen = (DashboardScreen) mainFrame.getScreen();
					screen.display.set("File Saved", 0);
				} else if (mainFrame.getScreen() instanceof TemplateScreen) {
					TemplateScreen screen = (TemplateScreen) mainFrame.getScreen();
					screen.display.set("File Saved", 0);
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(mainFrame, "Error saving gesture set\n(" + e1.getMessage() + ")", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			mainFrame.getMenu().updateMenu();
		}
		//save gesture set
		if (e.getSource() == mainFrame.getMenu().save) {

			try {
				System.out.println(exports(false));
				if (mainFrame.getScreen() instanceof DashboardScreen) {
					DashboardScreen screen = (DashboardScreen) mainFrame.getScreen();
					screen.display.set("File Saved", 0);
				} else if (mainFrame.getScreen() instanceof TemplateScreen) {
					TemplateScreen screen = (TemplateScreen) mainFrame.getScreen();
					screen.display.set("File Saved", 0);
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(mainFrame, "Error saving gesture set\n(" + e1.getMessage() + ")", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		//send app to mobile device to draw gestures
		if (e.getSource() == mainFrame.getMenu().send) {
			
			JOptionPane messagePane = new JOptionPane("Searching for Devices...Please Wait...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
			
			final JDialog dialog = messagePane.createDialog(mainFrame, "Send App");
			
			
			CursorToolkit.startWaitCursor(messagePane);
			
			new SwingWorker<Void, Void>() {
				
				@Override
				protected Void doInBackground() {
					

					Vector<AvailableDevice> discoveredDevices = null;
					try {

						RemoteDeviceDiscovery deviceDiscovery = new RemoteDeviceDiscovery();
						discoveredDevices = deviceDiscovery.getDevicesDiscovered();
						CursorToolkit.stopWaitCursor(messagePane);
					} catch (InterruptedException e) {

						e.printStackTrace();
					} catch (BluetoothStateException e) {
						dialog.dispose();
						JOptionPane.showMessageDialog(null, "Attiva il Bluetooth del PC", "Bluetooth",
								JOptionPane.WARNING_MESSAGE);

					}
					dialog.dispose();
					if (discoveredDevices.size() > 0) {
						String[] possibilities = new String[discoveredDevices.size()];

						for (int i = 0; i < discoveredDevices.size(); i++) {
							AvailableDevice availableDevice = discoveredDevices.elementAt(i);
							try {
								possibilities[i] = i + "." + availableDevice.getDevice().getFriendlyName(false) + " ("
										+ availableDevice.getDevice().getBluetoothAddress() + ")";
							} catch (IOException e) {
							
								e.printStackTrace();
							}
						}

						String selected = (String) JOptionPane.showInputDialog(mainFrame, "Send App to:",
								"Devices Found", JOptionPane.WARNING_MESSAGE, null, possibilities, null);
						String element = selected.substring(0, selected.indexOf("."));
						ObexPutClient obexClient = new ObexPutClient(discoveredDevices.elementAt(Integer.parseInt(element)).getObexServiceURL());
						try {
							obexClient.send();
						} catch (IOException e) {
							JOptionPane.showMessageDialog(mainFrame, "Error Sending apk");
						}
					} else {
						JOptionPane.showMessageDialog(mainFrame, "No device Found");
					}

					return null;
				}

				// this is called when background thread above has
				// completed.
				protected void done() {
					// dialog.dispose();
				};
			}.execute();

			dialog.setVisible(true);

			return;
		}
	}

	/**
	 * 
	 * Save gesture set
	 * @param saveas
	 * @return
	 * @throws Exception
	 */
	boolean exports(boolean saveas) throws Exception {
		String choosedFile = "";
		String fileExt = "";
		if (saveas) {
			JFileChooser saveFile = new JFileChooser();
			
			FileNameExtensionFilter filter1 = new FileNameExtensionFilter(MainFrame.EXTENSION_XML, "xml");
			FileNameExtensionFilter filter2 = new FileNameExtensionFilter(MainFrame.EXTENSION_PGS, "pgs");
			saveFile.setFileFilter(filter1);
			saveFile.setFileFilter(filter2);
			int retrival = saveFile.showSaveDialog(null);

			if (retrival == JFileChooser.APPROVE_OPTION) {
				choosedFile = saveFile.getSelectedFile().toString();
				System.out.println("file selezionato" + saveFile.getSelectedFile().toString());
				fileExt = saveFile.getFileFilter().getDescription();
			} else
				return false;
		} else {
			choosedFile = mainFrame.getOpenedFile();
			fileExt = mainFrame.getExtOpenedFile();
		}

		if (fileExt != null && fileExt.equals(MainFrame.EXTENSION_XML)) {

			if (!choosedFile.endsWith(".xml"))
				choosedFile = choosedFile + ".xml";
			mainFrame.getRecognizer().saveTemplatesXML(new File(choosedFile));

		} else if (fileExt != null && fileExt.equals(MainFrame.EXTENSION_PGS)) {

			File f1 = new File("gestures.pgs");
			mainFrame.getRecognizer().saveTemplatesPGS(f1);
		
			if (!choosedFile.endsWith(".pgs"))
				choosedFile = choosedFile + ".pgs";
			File f2 = new File(choosedFile);
			

			copyFile(f1, f2);

		}

		if (saveas) {
			mainFrame.setOpenedFile(choosedFile);
			mainFrame.setExtOpenedFile(fileExt);
			
		}

		return true;

	}

	// aggiunto temporaneamente per importare dataset KB per valutazione tesi
	/*boolean importsKB(int method) {

		// this.rInvariant = method2;

		// store = new GestureStore();

		// load gestures from logs
		mainFrame.getRecognizer().removeClasses();
		FileListManager flm = new FileListManager("datasets/mykb", "xml");
		ArrayList<File> files = flm.getFileList();
		System.out.println("Adding " + files.size() + " gestures to store");
		for (int i = 0; i < files.size(); i++) {
			FileImporter fi = new FileImporter(files.get(i));
			Gesture gesture = fi.importInfo(true);

			//if (gesture.getInfo().getSubject() != 5)
				mainFrame.getRecognizer().addTemplate(gesture.getInfo().getName(), gesture);

		}
		System.out.println("Gestures added to store");
		return true;
	}*/

	boolean imports(int method) {

		JFileChooser openFile = new JFileChooser();
		
		FileNameExtensionFilter filter1 = new FileNameExtensionFilter(MainFrame.EXTENSION_XML, "xml");
		FileNameExtensionFilter filter2 = new FileNameExtensionFilter(MainFrame.EXTENSION_PGS, "pgs");
		
		openFile.setFileFilter(filter1);
		openFile.setFileFilter(filter2);
		
		int retrival = openFile.showOpenDialog(null);
		try {
			if (retrival == JFileChooser.APPROVE_OPTION) {

				String selectedFile = openFile.getSelectedFile().toString();
				String fileExt = openFile.getFileFilter().getDescription();

				CursorToolkit.startWaitCursor(mainFrame.getRootPane());
				if (fileExt.equals(MainFrame.EXTENSION_XML)) {

					File schemaFile = new File("schema-validator.xsd");
					Source xmlFile = new StreamSource(new File(selectedFile));
					SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
					Schema schema = schemaFactory.newSchema(schemaFile);
					Validator validator = schema.newValidator();
					try {
						validator.validate(xmlFile);
						System.out.println(xmlFile.getSystemId() + " is valid");
					} catch (SAXException e) {
						JOptionPane.showMessageDialog(mainFrame,
								xmlFile.getSystemId() + " is NOT valid reason:" + e.getLocalizedMessage());
						return false;
					}

					mainFrame.getRecognizer().loadTemplatesXML(openFile.getSelectedFile(),
							method == ExtendedPolyRecognizerGSS.REPLACE);
					System.out.println(mainFrame.getRecognizer().getTamplatesNumber());

				} else if (fileExt.equals(MainFrame.EXTENSION_PGS)) {

					File f1 = new File(selectedFile);

					File f2 = new File("gestures.pgs");

					InputStream in = new FileInputStream(f1);

					OutputStream out = new FileOutputStream(f2);

					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					in.close();
					out.close();

					mainFrame.getRecognizer().loadTemplatesPGS(f1, method == ExtendedPolyRecognizerGSS.REPLACE);

				}
				mainFrame.setExtOpenedFile(fileExt);
				mainFrame.setOpenedFile(selectedFile);

			}
		} catch (Exception e) {
			CursorToolkit.stopWaitCursor(mainFrame.getRootPane());
			System.out.println("errore");
			JOptionPane.showMessageDialog(mainFrame, "Errore nell'importazione del File (" + e.getMessage() + ")");
			return false;
		}
		return true;

	}

	
	private void copyFile(File f1, File f2) throws IOException {

		InputStream in = new FileInputStream(f1);

		OutputStream out = new FileOutputStream(f2);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();

	}

}
