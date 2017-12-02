package it.unisa.di.cluelab.polyrec.bluetooh.sendapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

/**
 * Obex put client.
 */
public class ObexPutClient {

    private static final String APK_FILE = "BluetoothGestures.apk";
    private final String serverURL;

    public ObexPutClient(String obexServiceURL) {
        this.serverURL = obexServiceURL;
    }

    /**
     * send apk to mobile device.
     * 
     * @throws IOException
     *             if a connection error occurs
     */
    @SuppressWarnings("checkstyle:innerassignment")
    public void send() throws IOException {

        System.out.println("Connecting to " + serverURL);

        final ClientSession clientSession;

        clientSession = (ClientSession) Connector.open(serverURL);
        System.out.println(clientSession);

        final HeaderSet hsConnectReply;

        hsConnectReply = clientSession.connect(null);

        if (hsConnectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
            System.out.println("Failed to connect");
            return;
        }

        // convert file into array of bytes
        final byte[] bFile;
        try (InputStream bInputStream = getClass().getResourceAsStream("/" + APK_FILE)) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] buffer = new byte[4096];
            for (int read; (read = bInputStream.read(buffer, 0, buffer.length)) != -1;) {
                baos.write(buffer, 0, read);
            }
            bFile = baos.toByteArray();
        }

        final HeaderSet hsOperation = clientSession.createHeaderSet();
        hsOperation.setHeader(HeaderSet.NAME, APK_FILE);
        hsOperation.setHeader(HeaderSet.TYPE, "application/vnd.android.package-archive");

        final long lenght = bFile.length;

        hsOperation.setHeader(HeaderSet.LENGTH, lenght);

        // Create PUT Operation
        final Operation putOperation = clientSession.put(hsOperation);

        final OutputStream os = putOperation.openOutputStream();
        os.write(bFile);
        os.close();

        putOperation.close();

        clientSession.disconnect(null);

        clientSession.close();

    }
}
