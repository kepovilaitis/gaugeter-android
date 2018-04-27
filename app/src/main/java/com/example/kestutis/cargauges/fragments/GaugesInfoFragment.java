package com.example.kestutis.cargauges.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.example.kestutis.cargauges.controllers.AnimationController;
import com.example.kestutis.cargauges.controllers.BluetoothController;
import com.example.kestutis.cargauges.interfaces.BluetoothStateListener;
import com.example.kestutis.cargauges.constants.Constants;
import com.example.kestutis.cargauges.adapters.DeviceListAdapter;
import com.example.kestutis.cargauges.R;

import lombok.AllArgsConstructor;

public class GaugesInfoFragment extends Fragment {
    private BluetoothController _bluetooth;
    private AnimationController _animation;
    private FloatingActionButton _fab;
    private DeviceListAdapter _adapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        _bluetooth = BluetoothController.getInstance();
        _bluetooth.setBtStateListener(_btStateListener);
        _animation = AnimationController.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_gauges_info, container, false);
        ListView deviceList = main.findViewById(R.id.list_view);

        _fab = main.findViewById(R.id.fab);
        _fab.setImageResource(_bluetooth.isBluetoothOn() ? R.drawable.ic_radar : R.drawable.ic_bluetooth_off_white_48dp);
        _fab.setOnClickListener(_fabOnClickListener);

        deviceList.setEmptyView(main.findViewById(R.id.text_empty));
        _adapter = new DeviceListAdapter(_bluetooth.getFoundDevices(), getActivity());
        deviceList.setAdapter(_adapter);
        deviceList.setOnItemLongClickListener(_itemLongClickListener);

        return main;
    }

    private OnClickListener _fabOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (_bluetooth.isBluetoothOn())
            {
                _bluetooth.startDiscovery();

                Snackbar.make(v, "Bluetooth is On, Scanning", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            }
        }
    };

    private BluetoothStateListener _btStateListener = new BluetoothStateListener() {
        @Override
        public void setFoundDevices() {
            _adapter.notifyDataSetChanged();
        }

        @Override
        public void setDevice() {

        }

        @Override
        public void setFAB(Intent intent) {
            _animation.setFAB(intent, _fab, getActivity());
        }
    };

    private OnItemLongClickListener _itemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Delete");
            alertDialog.setMessage("Are you sure you want to delete");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new ConfirmDeleteBtnClick(_bluetooth.getDevices().get(position)));
            alertDialog.show();

            return true;
        }
    };

    @AllArgsConstructor
    private class ConfirmDeleteBtnClick implements DialogInterface.OnClickListener {
        private BluetoothDevice device;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    _bluetooth.delete(device);
                    _adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
}
