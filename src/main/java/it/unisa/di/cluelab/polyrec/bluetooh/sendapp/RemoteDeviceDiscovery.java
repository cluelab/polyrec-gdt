package it.unisa.di.cluelab.polyrec.bluetooh.sendapp;

import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

/**
 * Device Discovery .
 */
public class RemoteDeviceDiscovery {

    private static final UUID OBEX_OBJECT_PUSH = new UUID(0x1105);

    private static final Vector<String> SERVICE_FOUND = new Vector<String>();

    private final Vector<AvailableDevice> devicesDiscovered = new Vector<AvailableDevice>();

    public RemoteDeviceDiscovery() throws InterruptedException, BluetoothStateException {

        final Object inquiryCompletedEvent = new Object();
        final Object serviceSearchCompletedEvent = new Object();

        devicesDiscovered.clear();

        final UUID serviceUUID = OBEX_OBJECT_PUSH;

        final UUID[] searchUuidSet = new UUID[] {serviceUUID};
        // Service name
        final int[] attrIDs = new int[] {0x0100};

        @SuppressWarnings("checkstyle:anoninnerlength")
        final DiscoveryListener listener = new DiscoveryListener() {

            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
                try {
                    System.out.println("     name " + btDevice.getFriendlyName(false));
                } catch (final IOException cantGetDeviceName) {
                    //
                }
                try {
                    synchronized (serviceSearchCompletedEvent) {
                        SERVICE_FOUND.clear();
                        System.out.println("search services on " + btDevice.getBluetoothAddress() + " "
                                + btDevice.getFriendlyName(false));
                        LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet,
                                btDevice, this);

                        serviceSearchCompletedEvent.wait();
                    }
                } catch (final Exception e) {

                    e.printStackTrace();
                }

            }

            @Override
            public void inquiryCompleted(int discType) {
                System.out.println("Device Inquiry completed!");
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.notifyAll();
                }
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                System.out.println("servizio trovato");
                for (int i = 0; i < servRecord.length; i++) {
                    final String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    if (url == null) {
                        continue;
                    }
                    // serviceFound.add(url);
                    devicesDiscovered.addElement(new AvailableDevice(servRecord[i].getHostDevice(), url));
                    final DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
                    if (serviceName != null) {
                        System.out.println("service " + serviceName.getValue() + " found " + url);
                    } else {
                        System.out.println("service found " + url);
                    }
                }
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
                System.out.println("service search completed!");
                synchronized (serviceSearchCompletedEvent) {
                    serviceSearchCompletedEvent.notifyAll();
                }
            }
        };

        synchronized (inquiryCompletedEvent) {
            boolean started = false;

            final DiscoveryAgent discoveryAgent = LocalDevice.getLocalDevice().getDiscoveryAgent();

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
