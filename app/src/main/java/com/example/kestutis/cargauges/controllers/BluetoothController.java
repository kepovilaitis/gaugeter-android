package com.example.kestutis.cargauges.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass.Device.Major;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.*;
import java.util.Set;
import java.util.ArrayList;

import com.example.kestutis.cargauges.constants.Enums.CONNECTION_STATUS;
import com.example.kestutis.cargauges.holders.LiveDataHolder;

import com.example.kestutis.cargauges.tools.ByteParser;
import io.reactivex.subjects.PublishSubject;
import lombok.Getter;

//https://stackoverflow.com/questions/35647767/android-bluetooth-wake-up-device

public class BluetoothController {
    @Getter private static BluetoothController _instance;
    @Getter private PublishSubject<CONNECTION_STATUS> _stateSubject = PublishSubject.create();
    @Getter private PublishSubject<LiveDataHolder> _liveDataSubject = PublishSubject.create();
    @Getter private ReadLiveDataThread _liveDataThread;
    @Getter private BluetoothDevice _device;
    @Getter private ArrayList<BluetoothDevice> _devices;

    private BluetoothAdapter _adapter;

    public static void setInstance() {
        _instance = new BluetoothController();
    }

    private BluetoothController() {
        _adapter = BluetoothAdapter.getDefaultAdapter();
        _devices = filterDevices(_adapter.getBondedDevices());
    }

    private ArrayList<BluetoothDevice> filterDevices(Set<BluetoothDevice> devices) {
        ArrayList<BluetoothDevice> filteredDevices = new ArrayList<>();

        for (BluetoothDevice device : devices) {
            if ( device.getBluetoothClass().getMajorDeviceClass() == Major.UNCATEGORIZED)
                filteredDevices.add(device);
        }

        return filteredDevices;
    }

    public boolean isBluetoothOn() {
        return _adapter.isEnabled();
    }

    public void startDiscovery(){
//        _foundDevices.clear();

        if (_adapter.isDiscovering()) {
            _adapter.cancelDiscovery();
            _adapter.startDiscovery();
        } else {
            _adapter.startDiscovery();
        }
    }

//    private void addDevice(Intent intent) {
//        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
//        if (device.getName().contains("car-gauges")) {
//            _foundDevices.add(device);
//        }
//    }
//
//    public void delete(BluetoothDevice device){ }

    public void connectToDevice(final BluetoothDevice device){
        _stateSubject.onNext(CONNECTION_STATUS.CONNECTING);
        _liveDataThread = new ReadLiveDataThread(device);
        _liveDataThread.start();
    }

//    private IntentFilter getFilter(){
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        filter.addAction(BluetoothDevice.ACTION_FOUND);
//        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        return filter;
//    }

//    private final BroadcastReceiver _btReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (action != null) {
//                switch (action) {
//                    case BluetoothAdapter.ACTION_STATE_CHANGED:
//                        Log.d("ACTION","BLA State changed");
//
//                        //setState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));
//                        //_btStateListener.setFAB(intent);
//                        break;
//                    case BluetoothDevice.ACTION_FOUND:
//                        Log.d("ACTION","BLA Found");
//
//                        addDevice(intent);
//                        break;
//                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
//                        //_btStateListener.setFAB(R.drawable.ic_radar, R.anim.rotate);
//                        Log.d("ACTION","BLA Discovery started");
//                        break;
//                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
//                        //_btStateListener.setFAB(R.drawable.ic_bluetooth_white_48dp, R.anim.stay_still);
//                        Log.d("ACTION"," BLA Discovery finished");
//                        break;
//                }
//            }
//        }
//    };

    public class ReadLiveDataThread extends Thread {
        private BluetoothSocket _socket = null;

        ReadLiveDataThread(BluetoothDevice device) {
            _device = device;
        }

        public void run() {
            _adapter.cancelDiscovery();

            try {
                _socket = _device.createRfcommSocketToServiceRecord(_device.getUuids()[0].getUuid());
                _socket.connect();
            } catch (IOException e) {
                _stateSubject.onNext(CONNECTION_STATUS.DISCONNECTED);

                e.printStackTrace();
            }

            if (_socket.isConnected()) {
                try {
                    _socket.getOutputStream().write(0);
                    _stateSubject.onNext(CONNECTION_STATUS.CONNECTED);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            while (_socket.isConnected()){
                try {
                    DataInputStream inputStream = new DataInputStream(_socket.getInputStream());
                    ByteParser parser = new ByteParser();

                    if (parser.parseHeader(inputStream, 0)){

                        float oilTemperature = parser.parseFloat(inputStream);
                        float oilPressure = parser.parseFloat(inputStream);
                        float waterTemperature = parser.parseFloat(inputStream);
                        float charge = parser.parseFloat(inputStream);
                        float checksum = parser.parseFloat(inputStream);

                        if (checksum == oilTemperature + oilPressure + waterTemperature + charge){

                            _liveDataSubject.onNext(
                                new LiveDataHolder(
                                    oilTemperature,
                                    oilPressure,
                                    waterTemperature,
                                    charge
                                )
                            );
                        } else {
                            Log.d("Incorrect", " checksum!");
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel();

                    _stateSubject.onNext(CONNECTION_STATUS.DISCONNECTED);
                }
            }
        }

        public void cancel() {
            try {
                _socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
