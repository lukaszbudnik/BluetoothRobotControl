package com.github.lukaszbudnik.bluetoothrobotcontrol;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;

public class BluetoothConnectionThread extends Thread {

    private static final String TAG = "BluetoothConnection";

    private BluetoothDevice peer;
    private BluetoothSocket socket;

    public BluetoothConnectionThread(BluetoothDevice peer) {
            this.peer = peer;
    }

    public void run() {
        createSocket();
        while (true) {
            if (socket.isConnected()) {
                sleep(2);
                continue;
            }
            try {
                // attempt to re-create socket
                createSocket();
                socket.connect();
            } catch (IOException e) {
                Log.e(TAG, "Could not connect to device = " + socket.getRemoteDevice().getName(), e);
            }
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void write(Integer code) throws IOException {
        if (isConnected()) {
            String data = code.toString();
            char EOT = (char) 3;
            // Get the message bytes and send it
            byte[] message = (data + EOT).getBytes();
            socket.getOutputStream().write(message);
            Log.i(TAG, "Sent => " + data);
        }
    }

    public void cancel() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket = " + socket.getRemoteDevice().getName(), e);
        }
    }

    public void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
        }
    }

    private void createSocket() {
        try {
            cancel();
            ParcelUuid[] uuids = peer.getUuids();
            socket = peer.createRfcommSocketToServiceRecord(uuids[0].getUuid());
        } catch (IOException e) {
            Log.e(TAG, "Could not create socket to device = " + peer.getName(), e);
            sleep(5);
        }
    }

}
