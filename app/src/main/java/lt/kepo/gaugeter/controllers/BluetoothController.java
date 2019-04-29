package lt.kepo.gaugeter.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass.Device.Major;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.*;

import lt.kepo.gaugeter.constants.Enums.CONNECTION_STATUS;
import lt.kepo.gaugeter.holders.DeviceInfoHolder;
import lt.kepo.gaugeter.holders.LiveDataHolder;

import lt.kepo.gaugeter.tools.ByteParser;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.subjects.PublishSubject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//https://stackoverflow.com/questions/35647767/android-bluetooth-wake-up-device

@NoArgsConstructor
public class BluetoothController {
    @Getter private static BluetoothController _instance;
    @Getter private PublishSubject<CONNECTION_STATUS> _stateSubject = PublishSubject.create();
    @Getter private PublishSubject<LiveDataHolder> _liveDataSubject = PublishSubject.create();
    @Getter private ReadLiveDataThread _liveDataThread;

    private static BluetoothAdapter _adapter;

    public static void setInstance() {
        _instance = new BluetoothController();
        _adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothOn() {
        return _adapter.isEnabled();
    }

    public void startDiscovery(Context context, final Observer<DeviceInfoHolder> observer) {
        if (_adapter.isDiscovering()) {
            _adapter.cancelDiscovery();
        }

        IntentFilter filter = new IntentFilter();

        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(BluetoothDevice.EXTRA_BOND_STATE);
        filter.addAction(BluetoothDevice.EXTRA_CLASS);
        filter.addAction(BluetoothDevice.EXTRA_DEVICE);
        filter.addAction(BluetoothDevice.EXTRA_NAME);
        filter.addAction(BluetoothDevice.EXTRA_PAIRING_VARIANT);
        filter.addAction(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE);
        filter.addAction(BluetoothDevice.EXTRA_RSSI);
        filter.addAction(BluetoothDevice.EXTRA_UUID);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.EXTRA_CONNECTION_STATE);
        filter.addAction(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION);
        filter.addAction(BluetoothAdapter.EXTRA_LOCAL_NAME);
        filter.addAction(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE);
        filter.addAction(BluetoothAdapter.EXTRA_PREVIOUS_STATE);
        filter.addAction(BluetoothAdapter.EXTRA_SCAN_MODE);
        filter.addAction(BluetoothAdapter.EXTRA_STATE);

        context.registerReceiver(new SearchDevicesReceiver(observer), filter);

        Observable.create(new ObservableOnSubscribe<DeviceInfoHolder>() {
            @Override
            public void subscribe(ObservableEmitter<DeviceInfoHolder> emitter) {
                _adapter.startDiscovery();
            }
        }).subscribe(observer);

    }

    public void connectToDevice(String deviceAddress){
        _stateSubject.onNext(CONNECTION_STATUS.CONNECTING);
        _liveDataThread = new ReadLiveDataThread(deviceAddress);
        _liveDataThread.start();
    }

    @AllArgsConstructor
    private class SearchDevicesReceiver extends BroadcastReceiver {
        Observer<DeviceInfoHolder> _observer;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    Log.d("ACTION_STATE_CHANGED: ", "STATE_ON");
                }
            }


            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("ACTION", "BLA Found");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBluetoothClass().getMajorDeviceClass() == Major.UNCATEGORIZED && device.getName() != null) {
                    _observer.onNext(
                            new DeviceInfoHolder(
                                    device.getName(),
                                    device.getAddress()
                            )
                    );
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                context.unregisterReceiver(this);
                _observer.onComplete();
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                Log.d("ACTION", "BLA ACTION_PAIRING_REQUEST");
            }
        }
    }



    public class ReadLiveDataThread extends Thread {
        private BluetoothSocket _socket = null;
        private BluetoothDevice _device;

        ReadLiveDataThread(String deviceAddress) {
            _device = _adapter.getRemoteDevice(deviceAddress);
        }

        public void run() {
            _adapter.cancelDiscovery();

            try {
                ParcelUuid[] uuids = _device.getUuids();

                if (uuids == null){
                    _stateSubject.onNext(CONNECTION_STATUS.DISCONNECTED);

                    return;
                } else {
                    _socket = _device.createRfcommSocketToServiceRecord(_device.getUuids()[0].getUuid());
                    _socket.connect();
                }
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

                    Log.d("Thread status", getState().name());

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
