package fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


import controllers.BluetoothController;
import helper.TouchHelperCallback;
import holders.DeviceInfoHolder;
import constants.Constants;
import interfaces.BluetoothStateListener;

import adapters.DeviceListAdapter;
import com.example.kestutis.cargauges.R;

import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends Fragment{
    private BluetoothController _bluetooth;
    private FloatingActionButton _fab;
    private DeviceListAdapter _adapter;
    private RecyclerView _deviceList;


    DeviceInfoHolder device = new DeviceInfoHolder("Toyota 80", "00:11:22:33:44", BluetoothDevice.BOND_BONDING);
    DeviceInfoHolder device2 = new DeviceInfoHolder("BMW 320i", "25:65:65:44:77", BluetoothDevice.BOND_BONDING);
    DeviceInfoHolder device3 = new DeviceInfoHolder("BMW 328i", "00:01:87:34:31", BluetoothDevice.BOND_BONDING);

    private List<DeviceInfoHolder> _devices = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        _bluetooth = BluetoothController.getInstance();
        _bluetooth.setBtStateListener(_btStateListener);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_devices, container, false);
        _deviceList = main.findViewById(R.id.paired_device_list_view);
        Button searchButton = main.findViewById(R.id.search_button);
        EditText searchEditText = main.findViewById(R.id.search);

        searchButton.setOnClickListener(_searchListener);

        _fab = main.findViewById(R.id.fab);
        _fab.setImageResource(R.drawable.ic_bluetooth_connect_white_48dp);
        _fab.setOnClickListener(_fabOnClickListener);
        _fab.hide();

        if (isAdded()) {
            _deviceList.setLayoutManager(new LinearLayoutManager(getActivity()));
            _deviceList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        }

        _devices.add(device);
        _devices.add(device2);
        _devices.add(device3);

        _adapter = new DeviceListAdapter(/*_bluetooth.getBondedDevices()*/_devices, _fab);
        _deviceList.setAdapter(_adapter);
        ItemTouchHelper.Callback callback = new TouchHelperCallback(_adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(_deviceList);

        searchEditText.addTextChangedListener(_queryTextListener);

        return main;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    private OnClickListener _fabOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.main_content, new GaugesFragment());
            fragmentTransaction.commit();
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
            //_animation.setFAB(intent, _fab, getActivity());
        }
    };

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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            _adapter.getFilter().filter(s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
