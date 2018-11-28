package controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import holders.RealTimeDataHolder;
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
    private ReadParamsThread _readThread;

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

    public void connectToDevice(BluetoothDevice device, SocketConnectedListener listener){
        _readThread = new ReadParamsThread(device, listener);
        _readThread.start();
    }

    public void delete(BluetoothDevice device){

    }

    public void closeSocket(){
        _readThread.closeConnection();
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
        private SocketConnectedListener _listener;
        private final Handler _handler;

        ReadParamsThread(BluetoothDevice device, SocketConnectedListener listener) {
            _listener = listener;
            BluetoothSocket temporarySocket = null;

            try {
                temporarySocket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            } catch (IOException e) {
                Log.e(TAG, "Socket's createRfcommSocketToServiceRecord() method failed", e);
            }
            _socket = temporarySocket;

            _handler = new Handler(Looper.getMainLooper());
        }

        public void run() {
            _adapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks until it succeeds or throws an exception.
                _socket.connect();
                _socket.getOutputStream().write(0);
                _listener.hasConnected();

            } catch (IOException connectException) {

                try {
                    _socket.close();

                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            _listener.connectionAttemptHasFailed();
                        }
                    });

                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            while (_socket.isConnected()){
                try {

                    BufferedReader r = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

                    for (String line; (line = r.readLine()) != null; ) {
                        String[] numbers = line.split(";");

                        if (numbers.length == 4) {
                            final RealTimeDataHolder data = new RealTimeDataHolder(
                                    Integer.parseInt(numbers[0]),
                                    Integer.parseInt(numbers[1]),
                                    Integer.parseInt(numbers[2]),
                                    Integer.parseInt(numbers[3])
                            );

                            _handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (_dataUpdateListener != null) {
                                        _dataUpdateListener.update(data);
                                    }
                                }
                            });
                        }
                        Log.d("reading ", "values");
                    }
                } catch (IOException e) {
                    Log.d("entered  ", "exception");

                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (_dataUpdateListener != null) {
                                Log.d("connectionWasLost  ", "within handler");
                                _dataUpdateListener.connectionWasLost();
                            }
                        }
                    });
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        void closeConnection() {
            try {
                _socket.close();
                Log.d("isConnected", String.valueOf(_socket.isConnected()));
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
