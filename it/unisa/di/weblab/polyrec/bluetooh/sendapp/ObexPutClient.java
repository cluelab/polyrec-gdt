package it.unisa.di.weblab.polyrec.bluetooh.sendapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.obex.*;

public class ObexPutClient {

	private String serverURL;
	private String apkfile = "BluetoothGestures.apk";

	/**
	 * send apk to mobile device
	 * @throws IOException
	 */
	public void send() throws IOException{

		
		System.out.println("Connecting to " + serverURL);

		ClientSession clientSession;
		
			clientSession = (ClientSession) Connector.open(serverURL);
			System.out.println(clientSession);

			HeaderSet hsConnectReply;

			hsConnectReply = clientSession.connect(null);

			if (hsConnectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
				System.out.println("Failed to connect");
				return;
			}

			FileInputStream fileInputStream = null;

			File file = new File("BluetoothGestures.apk");

			byte[] bFile = new byte[(int) file.length()];

			// convert file into array of bytes
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bFile);

			fileInputStream.close();

			HeaderSet hsOperation = clientSession.createHeaderSet();
			hsOperation.setHeader(HeaderSet.NAME, apkfile);
			hsOperation.setHeader(HeaderSet.TYPE, "application/vnd.android.package-archive");

			long lenght = bFile.length;

			hsOperation.setHeader(HeaderSet.LENGTH, lenght);

			// Create PUT Operation
			Operation putOperation = clientSession.put(hsOperation);

			OutputStream os = putOperation.openOutputStream();
			os.write(bFile);
			os.close();

			putOperation.close();

			clientSession.disconnect(null);

			clientSession.close();
		
	}

	public ObexPutClient(String obexServiceURL) {
		this.serverURL = obexServiceURL;
	}
}