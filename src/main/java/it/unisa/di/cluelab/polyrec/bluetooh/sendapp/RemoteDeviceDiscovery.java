package it.unisa.di.cluelab.polyrec.bluetooh.sendapp;

import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.*;
import javax.swing.JOptionPane;

/**
 * Device Discovery .
 */
public class RemoteDeviceDiscovery {

    private Vector<AvailableDevice> devicesDiscovered = new Vector<AvailableDevice>();

    private static final UUID OBEX_OBJECT_PUSH = new UUID(0x1105);

    public static final Vector/* <String> */ serviceFound = new Vector();

    public RemoteDeviceDiscovery() throws InterruptedException, BluetoothStateException {

        final Object inquiryCompletedEvent = new Object();
        final Object serviceSearchCompletedEvent = new Object();

        devicesDiscovered.clear();

        UUID serviceUUID = OBEX_OBJECT_PUSH;

        UUID[] searchUuidSet = new UUID[] {serviceUUID};
        int[] attrIDs = new int[] {0x0100 // Service name
        };

        DiscoveryListener listener = new DiscoveryListener() {

            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
                try {
                    System.out.println("     name " + btDevice.getFriendlyName(false));
                } catch (IOException cantGetDeviceName) {
                }
                try {
                    synchronized (serviceSearchCompletedEvent) {
                        serviceFound.clear();
                        System.out.println("search services on " + btDevice.getBluetoothAddress() + " "
                                + btDevice.getFriendlyName(false));
                        LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet,
                                btDevice, this);

                        serviceSearchCompletedEvent.wait();
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }

            }

            public void inquiryCompleted(int discType) {
                System.out.println("Device Inquiry completed!");
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.notifyAll();
                }
            }

            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                System.out.println("servizio trovato");
                for (int i = 0; i < servRecord.length; i++) {
                    String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    if (url == null) {
                        continue;
                    }
                    // serviceFound.add(url);
                    devicesDiscovered.addElement(new AvailableDevice(servRecord[i].getHostDevice(), url));
                    DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
                    if (serviceName != null) {
                        System.out.println("service " + serviceName.getValue() + " found " + url);
                    } else {
                        System.out.println("service found " + url);
                    }
                }
            }

            public void serviceSearchCompleted(int transID, int respCode) {
                System.out.println("service search completed!");
                synchronized (serviceSearchCompletedEvent) {
                    serviceSearchCompletedEvent.notifyAll();
                }
            }
        };

        synchronized (inquiryCompletedEvent) {
            boolean started = false;

            DiscoveryAgent discoveryAgent = LocalDevice.getLocalDevice().getDiscoveryAgent();

            started = discoveryAgent.startInquiry(DiscoveryAgent.GIAC, listener);
            System.out.println(started);

            if (started) {
                System.out.println("wait for device inquiry to complete...");
                inquiryCompletedEvent.wait();

                System.out.println(devicesDiscovered.size() + " device(s) found");
            }
        }
    }

    public Vector<AvailableDevice> getDevicesDiscovered() {
        return devicesDiscovered;
    }

}
