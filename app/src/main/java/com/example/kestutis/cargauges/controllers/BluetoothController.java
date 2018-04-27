package com.example.kestutis.cargauges.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.kestutis.cargauges.interfaces.BluetoothStateListener;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class BluetoothController {
    @Getter private static BluetoothController _instance;
    private BluetoothAdapter _adapter;
    @Getter private List<BluetoothDevice> _foundDevices;
    @Setter private BluetoothStateListener _btStateListener;

    public static void setInstance(Context base) {
        _instance = new BluetoothController(base);
    }

    private BluetoothController(Context base) {
        _adapter = BluetoothAdapter.getDefaultAdapter();
        base.registerReceiver(_btReceiver, getFilter());
        _foundDevices = new ArrayList<>();
    }

    public boolean isBluetoothOn() {
        return _adapter.isEnabled();
    }

    public boolean isDeviceConnected(String address) {
        return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address) != null;
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

    public List<BluetoothDevice> getDevices(){
        if (_foundDevices.size() == 0){
            _foundDevices.addAll(_adapter.getBondedDevices());
        }
        return _foundDevices;
    }

    private void addDevice(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        _foundDevices.add(device);
    }

    public void delete(BluetoothDevice device){

    }

    /*private void setState(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                Log.d("STATE","BLA Off");

                _btStateListener.setFAB(R.drawable.ic_bluetooth_off_white_48dp, R.anim.stay_still);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Log.d("STATE","BLA Turning off");

                _btStateListener.setFAB(R.drawable.ic_bluetooth_off_white_48dp, R.anim.rotate);
                break;
            case BluetoothAdapter.STATE_ON:
                Log.d("STATE","BLA On");

                _btStateListener.setFAB(R.drawable.ic_radar, R.anim.stay_still);
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                Log.d("STATE","BLA Turning on");

                _btStateListener.setFAB(R.drawable.ic_bluetooth_white_48dp, R.anim.rotate);
                break;
            case BluetoothAdapter.STATE_CONNECTING:
                Log.d("STATE","BLA Connecting");

                _btStateListener.setFAB(R.drawable.ic_bluetooth_connect_white_48dp, R.anim.rotate);
                break;
            case BluetoothAdapter.STATE_CONNECTED:
                Log.d("STATE","BLA Connected");

                _btStateListener.setFAB(R.drawable.ic_bluetooth_connect_white_48dp, R.anim.stay_still);
                break;
            case BluetoothAdapter.STATE_DISCONNECTING:
                Log.d("STATE","BLA Disconnecting");

                _btStateListener.setFAB(R.drawable.ic_bluetooth_white_48dp, R.anim.rotate);
                break;
            case BluetoothAdapter.STATE_DISCONNECTED:
                Log.d("STATE","BLA Disconnected");

                _btStateListener.setFAB(R.drawable.ic_bluetooth_off_white_48dp, R.anim.stay_still);
                break;
        }
    }*/

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
                _btStateListener.setFAB(intent);
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
                        _btStateListener.setFoundDevices();
                        //_btStateListener.setFAB(R.drawable.ic_bluetooth_white_48dp, R.anim.stay_still);
                        Log.d("ACTION"," BLA Discovery finished");
                        break;
                }
            }
        }
    };
}
