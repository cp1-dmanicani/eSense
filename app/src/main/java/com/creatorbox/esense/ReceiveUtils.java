/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/


package com.creatorbox.esense;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * OFFLINE MODE
 * An class to be used when receiving files via Bluetooth or CreatorBox sensors.
 */
public class ReceiveUtils {

    private final Context context;
    private final Handler handler;
    private int state;

    private final BluetoothAdapter bluetoothAdapter;

    private ConnectThread connectThread;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;

    private final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private final String APP_NAME = "eSense";

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public ReceiveUtils(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;

        state = STATE_NONE;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public int getState() {
        return state;
    }

    public synchronized void setState(int state) {
        this.state = state;
        handler.obtainMessage(BluetoothReceive.MESSAGE_STATE_CHANGED, state, -1).sendToTarget();
    }

    //Check instance of connectThread
    public synchronized void start() {
        if (connectThread != null) { connectThread.cancel(); connectThread = null; }
        if (acceptThread == null) { acceptThread = new ReceiveUtils.AcceptThread(); acceptThread.start(); }
        if (connectedThread != null) { connectedThread.cancel(); connectedThread = null; }
        setState(STATE_LISTEN);
    }

    //Close all threads
    public synchronized void stop() {
        if (connectThread != null) { connectThread.cancel(); connectThread = null; }
        if (acceptThread != null) { acceptThread.cancel(); acceptThread = null; }
        if (connectedThread != null) { connectedThread.cancel(); connectedThread = null; }
        setState(STATE_NONE);
    }

    public void connecting (BluetoothDevice device) {
        if (state == STATE_CONNECTING) { connectThread.cancel(); connectThread = null; }
        connectThread = new ReceiveUtils.ConnectThread(device);
        connectThread.start();
        if (connectedThread != null) { connectedThread.cancel(); connectedThread = null; }
        setState(STATE_CONNECTING);
    }

    private synchronized void connectionFailed() {
        Message message = handler.obtainMessage(BluetoothReceive.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothReceive.TOAST, "Can't connect to device.");
        message.setData(bundle);
        handler.sendMessage(message);

        ReceiveUtils.this.start();
    }

    //private synchronized void connected(BluetoothDevice device) Original
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (connectThread != null) { connectThread.cancel();connectThread = null; }
        if (connectedThread != null) { connectedThread.cancel(); connectedThread = null; }
        if (acceptThread != null) {acceptThread.cancel(); acceptThread = null;}

        connectedThread = new ReceiveUtils.ConnectedThread(socket);
        connectedThread.start();

        Message message = handler.obtainMessage(BluetoothReceive.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothReceive.DEVICE_NAME, device.getName());
        message.setData(bundle);
        handler.sendMessage(message);

        setState(STATE_CONNECTED);
    }

    public void write(byte[] out) {
        ReceiveUtils.ConnectedThread connThread;
        synchronized (this) {
            if (state!= STATE_CONNECTED) {
                return;
            }
            connThread = connectedThread;
        }
        connThread.write(out);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID);
            } catch (IOException e) {
                Log.e("Accept->Constructor", e.toString());
            }
            serverSocket = tmp;
        }
        public void run() {
            BluetoothSocket socket = null;
            //while(true)
            while (state != STATE_CONNECTED) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e("Accept->Run", e.toString());
                    try {
                        serverSocket.close();
                    } catch (IOException e1) {
                        Log.e("Accept->Close", e.toString());
                    }
                    break;
                }

                if (socket != null) {
                    switch (state) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            //connected(socket.getRemoteDevice());
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e("Accept->CloseSocket", e.toString());
                            }
                            break;
                    }
                }
            }
        }
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e("Accept->CloseServerSckt", e.toString());
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread (BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
                //tmp = createBluetoothSocket(device);
            } catch (IOException e) {
                Log.e("Connect->Constructor", e.toString());
                //Trying a fallback method by calling the createRfcommSocket again.
                try {
                    Log.e("Connect->Constructor","Trying fallback.");
                    tmp =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                    Log.e("Connect->Constructor","Connected.");
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                }
            }
            socket = tmp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                if (!socket.isConnected()) {
                    socket.connect();
                }
                else {
                    Log.e("Connect->Run", "Could not connect socket.");
                }
            } catch (IOException e) {
                Log.e("Connect->Run", e.toString());
                try {
                    socket.close();
                } catch (IOException e1) {
                    Log.e("Connect->CloseSocket", e.toString());
                }
                connectionFailed();
                return;
            }
            synchronized (ReceiveUtils.this)  {
                connectThread = null;
            }
            connected(socket, device);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("Connect->Cancel", e.toString());
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothsocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            bluetoothsocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = bluetoothsocket.getInputStream();
                tmpOut = bluetoothsocket.getOutputStream();
            } catch (IOException e) {
                Log.e("ConnectedThrd->Cons", "Socket not created.");
                e.printStackTrace();
            }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    try {
                        bytes = inputStream.read(buffer);
                        // Send the obtained bytes to the UI Activity
                        handler.obtainMessage(BluetoothReceive.MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    } catch (NullPointerException n) {
                        Log.e("ConnectedThrd->Run", n.getMessage());
                    }
                } catch (IOException e) {
                    Log.e("ConnectedThrd->Run", "Connection Lost.", e);
                    e.printStackTrace();
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {

                try {
                    outputStream.write(buffer);
                    handler.obtainMessage(BluetoothReceive.MESSAGE_WRITE, -1, -1, buffer)
                            .sendToTarget();
                } catch (NullPointerException n) {
                    Log.e("ConnectedThrd->Write", "Bluetooth Socket is null: " + n.getMessage());
                }
            } catch (IOException e) {
                Log.e("ConnectedThread->Write", "Empty write stream.");
            }
        }

        public void cancel() {
            try {
                bluetoothsocket.close();
            } catch (IOException e) {
                Log.e("ConnectedThread->Cancel", "Failed to close socket.");
            }
        }
    }

    private void connectionLost() {
        Message message = handler.obtainMessage(BluetoothReceive.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothReceive.TOAST, "Connection Lost.");
        message.setData(bundle);
        handler.sendMessage(message);

        ReceiveUtils.this.start();
    }
}