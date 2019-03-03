package com.example.kestutis.cargauges.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Button;

import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.activities.MainActivity;
import com.example.kestutis.cargauges.controllers.BluetoothController;
import com.example.kestutis.cargauges.helpers.TouchHelperCallback;
import com.example.kestutis.cargauges.constants.Constants;
import com.example.kestutis.cargauges.adapters.DeviceListAdapter;

import java.util.List;
import java.util.ArrayList;

public class DevicesFragment extends Fragment {
    private BluetoothController _bluetooth;
    private DeviceListAdapter _adapter;

    private List<BluetoothDevice> _devices = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        _bluetooth = BluetoothController.getInstance();
        _devices = _bluetooth.getBondedDevices();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_devices, container, false);

        RecyclerView _deviceList = main.findViewById(R.id.listViewPairedDevice);
        Button searchButton = main.findViewById(R.id.btnSearch);
        EditText searchEditText = main.findViewById(R.id.search);

        setHasOptionsMenu(true);
        searchButton.setOnClickListener(_searchListener);
        _deviceList.setLayoutManager(new LinearLayoutManager(getContext()));

        _adapter = new DeviceListAdapter(_devices, ((MainActivity) getActivity()).getFab());
        _deviceList.setAdapter(_adapter);
        ItemTouchHelper.Callback callback = new TouchHelperCallback(_adapter.getItemTouchMoveListener());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(_deviceList);

        searchEditText.addTextChangedListener(_queryTextListener);

        return main;
    }

    private OnClickListener _searchListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (_bluetooth.isBluetoothOn()) {
                _bluetooth.startDiscovery();

                Snackbar.make(v, "Bluetooth is On, Scanning", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            }
        }
    };

    private TextWatcher _queryTextListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            _adapter.getFilter().filter(s);
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };
}
