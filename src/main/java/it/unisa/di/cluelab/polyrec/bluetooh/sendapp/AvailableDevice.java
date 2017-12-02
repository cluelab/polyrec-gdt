package it.unisa.di.cluelab.polyrec.bluetooh.sendapp;

import javax.bluetooth.RemoteDevice;

/**
 * Available device.
 */
public class AvailableDevice {
    private RemoteDevice device;
    private String obexServiceURL;

    public AvailableDevice(RemoteDevice device, String obexServiceURL) {
        this.device = device;
        this.obexServiceURL = obexServiceURL;
    }

    public RemoteDevice getDevice() {
        return device;
    }

    public void setDevice(RemoteDevice device) {
        this.device = device;

    }

    public String getObexServiceURL() {
        return obexServiceURL;
    }

    public void setObexServiceURL(String obexServiceURL) {
        this.obexServiceURL = obexServiceURL;
    }

    @Override
    public String toString() {
        return device.getBluetoothAddress() + " " + obexServiceURL;

    }

}
