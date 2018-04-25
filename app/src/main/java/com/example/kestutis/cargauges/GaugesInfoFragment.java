package com.example.kestutis.cargauges;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class GaugesInfoFragment extends Fragment {
    private View _main = null;
    private BluetoothController _bluetooth;
    private boolean _isBluetoothOn;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _main = inflater.inflate(R.layout.fragment_gauges_info, container, false);

        _bluetooth = BluetoothController.getInstance();
        _isBluetoothOn = _bluetooth.isBluetoothOn();

        FloatingActionButton fab = _main.findViewById(R.id.fab);
        fab.setImageResource(_isBluetoothOn ? R.drawable.ic_bluetooth_connect_white_48dp : R.drawable.ic_bluetooth_white_48dp);
        fab.setOnClickListener(_fabOnClickListener);

        return _main;
    }

    private OnClickListener _fabOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (_isBluetoothOn)
            {
                Snackbar.make(v, "Bluetooth is On", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            }
        }
    };
}
