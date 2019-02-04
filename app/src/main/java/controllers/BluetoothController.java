package controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import interfaces.InputDataUpdateListener;
import interfaces.SocketConnectedListener;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//https://stackoverflow.com/questions/35647767/android-bluetooth-wake-up-device

import static android.content.ContentValues.TAG;

@NoArgsConstructor
public class BluetoothController {
    @Getter private static BluetoothController _instance;
    private BluetoothAdapter _adapter;
    @Setter private InputDataUpdateListener _dataUpdateListener = null;
    private Set<BluetoothDevice> _foundDevices;
    private Set<BluetoothDevice> _bondedDevices;

    public static void setInstance(Context base) {
        _instance = new BluetoothController(base);
    }

    private BluetoothController(Context base) {
        _adapter = BluetoothAdapter.getDefaultAdapter();
        base.registerReceiver(_btReceiver, getFilter());
        _bondedDevices = _adapter.getBondedDevices();
    }

    public List<BluetoothDevice> getFoundDevices() {
        return new ArrayList<>(_foundDevices);
    }

    public List<BluetoothDevice> getBondedDevices() {
        return new ArrayList<>(_bondedDevices);
    }

    public boolean isBluetoothOn() {
        return _adapter.isEnabled();
    }


    public void startDiscovery(){
        _foundDevices.clear();

        if (_adapter.isDiscovering()) {
            _adapter.cancelDiscovery();
            _adapter.startDiscovery();
        } else {
            _adapter.startDiscovery();
        }
    }

    private void addDevice(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (device.getName().contains("car-gauges")) {
            _foundDevices.add(device);
        }
    }

    public void delete(BluetoothDevice device){

    }

    public void connectToDevice(BluetoothDevice device, SocketConnectedListener listener){
        new ReadParamsThread(device, listener).start();
    }

    private IntentFilter getFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        return filter;
    }

    private final BroadcastReceiver _btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        Log.d("ACTION","BLA State changed");

                        //setState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));
                        //_btStateListener.setFAB(intent);
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        Log.d("ACTION","BLA Found");

                        addDevice(intent);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        //_btStateListener.setFAB(R.drawable.ic_radar, R.anim.rotate);
                        Log.d("ACTION","BLA Discovery started");
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        //_btStateListener.setFAB(R.drawable.ic_bluetooth_white_48dp, R.anim.stay_still);
                        Log.d("ACTION"," BLA Discovery finished");
                        break;
                }
            }
        }
    };

    private class ReadParamsThread extends Thread {
        private final BluetoothSocket _socket;
        private final BluetoothDevice _device;
        private SocketConnectedListener _listener;

        ReadParamsThread(BluetoothDevice device, SocketConnectedListener listener) {
            _device = device;
            _listener = listener;
            BluetoothSocket temporarySocket = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                temporarySocket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            _socket = temporarySocket;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            _adapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                _socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    _socket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            if (_socket.isConnected()) {
                _listener.hasConnected();
                try {
                    _socket.getOutputStream().write(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            byte[] buffer = new byte[256];

            while (_socket.isConnected()){
                try {
                    ByteArrayInputStream input = new ByteArrayInputStream(buffer);
                    InputStream inputStream = _socket.getInputStream();
                    inputStream.read(buffer);

                    int data = input.read();

                    Log.i("logging", String.valueOf(data));

                    if (_dataUpdateListener != null){
                        _dataUpdateListener.update(data);
                    }
                } catch (IOException e) {
                    e.getMessage();
                }
            }

            //manageMyConnectedSocket(mmSocket);
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                _socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
