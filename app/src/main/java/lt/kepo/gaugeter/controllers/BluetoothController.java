package lt.kepo.gaugeter.controllers;

import android.bluetooth.*;
import android.bluetooth.BluetoothClass.Device.Major;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.*;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import lombok.*;

import lt.kepo.gaugeter.constants.Enums.CONNECTION_STATUS;
import lt.kepo.gaugeter.holders.DeviceHolder;
import lt.kepo.gaugeter.holders.TelemDataHolder;
import lt.kepo.gaugeter.tools.ByteParser;

//https://stackoverflow.com/questions/35647767/android-bluetooth-wake-up-device

@NoArgsConstructor
public class BluetoothController {
    private static final String HC_05_PIN = "1234";
    private static final String HC_05_DEFAULT_NAME = "HC-05";
        private static final String REMOVE_BOND_METHOD_NAME = "removeBond";

    @Getter private static BluetoothController _instance;
    @Getter private ReadLiveDataThread _liveDataThread;
    @Getter @Setter DeviceHolder _device;

    @Getter private PublishSubject<CONNECTION_STATUS> _stateSubject = PublishSubject.create();
    @Getter private PublishSubject<TelemDataHolder> _telemDataSubject;

    private static BluetoothAdapter _adapter;

    public static void setInstance() {
        _instance = new BluetoothController();
        _adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothOn() {
        return _adapter.isEnabled();
    }

    public void discoverDevices(Context context, final Observer<DeviceHolder> observer) {
        if (_adapter.isDiscovering()) {
            _adapter.cancelDiscovery();
        }

        IntentFilter filter = new IntentFilter();

        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        context.registerReceiver(new SearchDevicesReceiver(observer), filter);

        Observable.create(new ObservableOnSubscribe<DeviceHolder>() {
            @Override
            public void subscribe(ObservableEmitter<DeviceHolder> emitter) {
                _adapter.startDiscovery();
            }
        })
                .subscribe(observer);
    }

    public void bondWithDevice(Context context, String bluetoothAddress, SingleObserver<String> observer) {
        final BluetoothDevice device = _adapter.getRemoteDevice(bluetoothAddress);

        IntentFilter filter = new IntentFilter();

        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        context.registerReceiver(new BondWithDeviceReceiver(device, observer), filter);

        if (observer != null){
            Single.create(new SingleOnSubscribe<String>() {
                        @Override
                        public void subscribe(SingleEmitter<String> emitter){
                            device.createBond();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(observer);
        } else {
            device.createBond();
        }
    }

    public void connectToDevice(Context context, String bluetoothAddress, SingleObserver<String> observer){
        BluetoothDevice device = _adapter.getRemoteDevice(bluetoothAddress);

        if (context != null && !(device.getBondState() == BluetoothDevice.BOND_BONDED)) {
            bondWithDevice(context, bluetoothAddress, observer);
        } else {
            connectToDevice(device);
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        _liveDataThread = new ReadLiveDataThread(device);
        _liveDataThread.start();
    }

    public void reconnectToDevice() {
        connectToDevice(_adapter.getRemoteDevice(_device.getBluetoothAddress()));
    }

    public void removeBondedDevice(String bluetoothAddress) {
        try {
            BluetoothDevice device = _adapter.getRemoteDevice(bluetoothAddress);

            device.getClass()
                    .getMethod(REMOVE_BOND_METHOD_NAME, (Class[]) null)
                    .invoke(device, (Object[]) null);
        } catch (Exception ignore) { }
    }

    @AllArgsConstructor
    private class SearchDevicesReceiver extends BroadcastReceiver {
        Observer<DeviceHolder> _observer;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBluetoothClass().getMajorDeviceClass() == Major.UNCATEGORIZED
                        && device.getName() != null
                        && device.getName().equals(HC_05_DEFAULT_NAME)) {

                    _observer.onNext(
                            new DeviceHolder(
                                    device.getName(),
                                    device.getAddress()
                            )
                    );
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                context.unregisterReceiver(this);
                _observer.onComplete();
            }
        }
    }

    @AllArgsConstructor
    private class BondWithDeviceReceiver extends BroadcastReceiver {
        BluetoothDevice _device;
        SingleObserver<String> _observer;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {

                if (intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR) == BluetoothDevice.PAIRING_VARIANT_PIN) {
                    ((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).setPin(HC_05_PIN.getBytes());
                    abortBroadcast();
                } else {
                    Log.d("ACTION", "Unexpected pairing type");
                }

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                int deviceBondState = _device.getBondState();

                if (_observer != null) {
                    if (deviceBondState == BluetoothDevice.BOND_BONDED) {
                        context.unregisterReceiver(this);

                        _observer.onSuccess(_device.getAddress());
                    } else if (deviceBondState == BluetoothDevice.BOND_NONE){

                        _observer.onError(null);
                    }
                } else {
                    if (deviceBondState == BluetoothDevice.BOND_BONDED) {
                        context.unregisterReceiver(this);
                        connectToDevice(_device);
                    }
                }
            }
        }
    }

    @RequiredArgsConstructor
    public class ReadLiveDataThread extends Thread {
        private BluetoothSocket _socket = null;
        @NonNull private BluetoothDevice _device;

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
                    _telemDataSubject = PublishSubject.create();
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

                            _telemDataSubject.onNext(
                                new TelemDataHolder(
                                    oilTemperature,
                                    oilPressure,
                                    waterTemperature,
                                    charge
                                )
                            );
                        } else {
                            _telemDataSubject.onError(new Throwable());
                            Log.d("Incorrect", " checksum!");
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel();

                    _telemDataSubject.onError(new Throwable());
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
