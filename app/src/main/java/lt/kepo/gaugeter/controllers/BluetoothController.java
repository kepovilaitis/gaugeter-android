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
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import lombok.*;
import lt.kepo.gaugeter.constants.Enums.CONNECTION_STATUS;
import lt.kepo.gaugeter.holders.DeviceInfoHolder;
import lt.kepo.gaugeter.holders.LiveDataHolder;
import lt.kepo.gaugeter.tools.ByteParser;

//https://stackoverflow.com/questions/35647767/android-bluetooth-wake-up-device

@NoArgsConstructor
public class BluetoothController {
    private static final String HC_05_PIN = "1234";
    private static final String HC_05_DEFAULT_NAME = "HC-05";

    @Getter private static BluetoothController _instance;
    @Getter private PublishSubject<CONNECTION_STATUS> _stateSubject = PublishSubject.create();
    @Getter private PublishSubject<LiveDataHolder> _liveDataSubject = PublishSubject.create();
    @Getter private ReadLiveDataThread _liveDataThread;
    @Getter @Setter DeviceInfoHolder _device;

    private static BluetoothAdapter _adapter;

    public static void setInstance() {
        _instance = new BluetoothController();
        _adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothOn() {
        return _adapter.isEnabled();
    }

    public void discoverDevices(Context context, final Observer<DeviceInfoHolder> observer) {
        if (_adapter.isDiscovering()) {
            _adapter.cancelDiscovery();
        }

        IntentFilter filter = new IntentFilter();

        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        context.registerReceiver(new SearchDevicesReceiver(observer), filter);

        Observable.create(new ObservableOnSubscribe<DeviceInfoHolder>() {
            @Override
            public void subscribe(ObservableEmitter<DeviceInfoHolder> emitter) {
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

        Single.create(new SingleOnSubscribe<String>() {
                    @Override
                    public void subscribe(SingleEmitter<String> emitter){
                        device.createBond();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(observer);
    }

    public void connectToDevice(Context context, String bluetoothAddress){
        BluetoothDevice device = _adapter.getRemoteDevice(bluetoothAddress);

        if (context != null && !(device.getBondState() == BluetoothDevice.BOND_BONDED)) {
            bondWithDevice(context, bluetoothAddress, new BondWithExistingDeviceObserver());
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

    @AllArgsConstructor
    private class SearchDevicesReceiver extends BroadcastReceiver {
        Observer<DeviceInfoHolder> _observer;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBluetoothClass().getMajorDeviceClass() == Major.UNCATEGORIZED
                        && device.getName() != null
                        && device.getName().equals(HC_05_DEFAULT_NAME)) {

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
                if (_device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    _observer.onSuccess(_device.getAddress());

                    context.unregisterReceiver(this);

                } else if (!(_device.getBondState() == BluetoothDevice.BOND_BONDED && _device.getBondState() == BluetoothDevice.BOND_BONDING)){
                    _observer.onError(null);
                }
            }
        }
    }

    private class BondWithExistingDeviceObserver implements SingleObserver<String> {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(String bluetoothAddress) {
            connectToDevice(_adapter.getRemoteDevice(bluetoothAddress));
        }

        @Override
        public void onError(Throwable e) {
            _stateSubject.onNext(CONNECTION_STATUS.DISCONNECTED);
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
