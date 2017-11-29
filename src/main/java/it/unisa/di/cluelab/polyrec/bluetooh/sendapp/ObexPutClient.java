package it.unisa.di.cluelab.polyrec.bluetooh.sendapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.obex.*;

public class ObexPutClient {

    private String serverURL;
    private String apkfile = "BluetoothGestures.apk";

    /**
     * send apk to mobile device
     * 
     * @throws IOException
     */
    public void send() throws IOException {

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

        // convert file into array of bytes
        final byte[] bFile;
        try (InputStream bInputStream = getClass().getResourceAsStream("/BluetoothGestures.apk")) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] buffer = new byte[4096];
            for (int read; (read = bInputStream.read(buffer, 0, buffer.length)) != -1;) {
                baos.write(buffer, 0, read);
            }
            bFile = baos.toByteArray();
        }

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
