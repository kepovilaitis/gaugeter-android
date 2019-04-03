package com.example.kestutis.cargauges.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import com.example.kestutis.cargauges.constants.Enums.CONNECTION_STATE;
import com.example.kestutis.cargauges.holders.LiveDataHolder;

import com.example.kestutis.cargauges.tools.ByteParser;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import lombok.Getter;
import lombok.NoArgsConstructor;

//https://stackoverflow.com/questions/35647767/android-bluetooth-wake-up-device

@NoArgsConstructor
public class BluetoothController {
    @Getter private static BluetoothController _instance;
    @Getter private PublishSubject<CONNECTION_STATE> _publishSubjectState = PublishSubject.create();
    @Getter private PublishSubject<LiveDataHolder> _publishSubjectLiveData = PublishSubject.create();
    @Getter private ReadLiveDataThread _liveDataThread;
    @Getter private BluetoothDevice _device;

    private BluetoothAdapter _adapter;
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

    public void delete(BluetoothDevice device){ }

    public void reconnectToDevice(){
        if (_device != null){
            connectToDevice(_device);
        }
    }

    public void connectToDevice(BluetoothDevice device){

        _liveDataThread = new ReadLiveDataThread(device);
        _liveDataThread.start();
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

    public class ReadLiveDataThread extends Thread {
        private BluetoothSocket _socket = null;

        ReadLiveDataThread(BluetoothDevice device) {
            _device = device;

            try {
                _publishSubjectState.onNext(CONNECTION_STATE.IS_CONNECTING);
                _socket = _device.createRfcommSocketToServiceRecord(_device.getUuids()[0].getUuid());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            _adapter.cancelDiscovery();

            try {
                _socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                cancel();

                return;
            }

            if (_socket.isConnected()) {
                try {
                    _socket.getOutputStream().write(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                _publishSubjectState.onNext(CONNECTION_STATE.HAS_CONNECTED);
                Log.d("LiveDataThread", "Has connected");
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

                            _publishSubjectLiveData.onNext(new LiveDataHolder(
                                    oilTemperature,
                                    oilPressure,
                                    waterTemperature,
                                    charge
                            ));
                        } else {
                            Log.d("Incorrect checksum", "!");
                            return;
                        }
                    }
                } catch (IOException e) {

                    _publishSubjectState.onNext(CONNECTION_STATE.HAS_DISCONNECTED);

                    e.printStackTrace();
                    cancel();
                }
            }
        }

        public void cancel() {
            try {
                _socket.close();
                _publishSubjectState.onNext(CONNECTION_STATE.HAS_DISCONNECTED);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
