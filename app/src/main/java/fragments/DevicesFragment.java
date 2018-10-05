package fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Button;


import controllers.BluetoothController;
import helper.TouchHelperCallback;
import holders.DeviceInfoHolder;
import constants.Constants;
import interfaces.BluetoothStateListener;

import adapters.DeviceListAdapter;
import lombok.*;

import com.example.kestutis.cargauges.R;

import java.util.List;
import java.util.ArrayList;

public class DevicesFragment extends Fragment{
    private BluetoothController _bluetooth;
    private DeviceListAdapter _adapter;
    private Context _context;

    DeviceInfoHolder device = new DeviceInfoHolder("BMW 323i", "25:65:65:66:77", BluetoothDevice.BOND_BONDED);
    DeviceInfoHolder device2 = new DeviceInfoHolder("BMW 320i", "25:65:65:44:77", BluetoothDevice.BOND_BONDING);
    DeviceInfoHolder device3 = new DeviceInfoHolder("BMW 328i", "00:01:87:34:31", BluetoothDevice.BOND_BONDING);
    DeviceInfoHolder device4 = new DeviceInfoHolder("BMW 318i", "00:01:87:34:31", BluetoothDevice.BOND_BONDING);
    DeviceInfoHolder device5 = new DeviceInfoHolder("BMW 323i", "00:01:87:34:31", BluetoothDevice.BOND_BONDING);
    DeviceInfoHolder device6 = new DeviceInfoHolder("VW Sharan", "00:01:87:34:31", BluetoothDevice.BOND_BONDING);
    DeviceInfoHolder device7 = new DeviceInfoHolder("BMW 328i", "00:01:87:34:31", BluetoothDevice.BOND_BONDING);
    DeviceInfoHolder device8 = new DeviceInfoHolder("BMW 328i", "00:01:87:34:31", BluetoothDevice.BOND_BONDING);
    DeviceInfoHolder device9 = new DeviceInfoHolder("Galas", "00:01:87:34:31", BluetoothDevice.BOND_BONDING);


    private List<DeviceInfoHolder> _devices = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

//        _bluetooth = BluetoothController.getInstance();
        //_bluetooth.setBtStateListener(_btStateListener);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_devices, container, false);
        RecyclerView _deviceList = main.findViewById(R.id.paired_device_list_view);
        Button searchButton = main.findViewById(R.id.search_button);
        EditText searchEditText = main.findViewById(R.id.search);
        FloatingActionButton fab = main.findViewById(R.id.fab);

        if (isAdded()) {
            _context = getContext();
        }

        setHasOptionsMenu(true);

        searchButton.setOnClickListener(_searchListener);

        _deviceList.setLayoutManager(new LinearLayoutManager(_context));

        fab.setOnClickListener(_fabOnClickListener);
        fab.hide();

        _devices.add(device);
        _devices.add(device2);
        _devices.add(device3);
        _devices.add(device4);
        _devices.add(device5);
        _devices.add(device6);
        _devices.add(device7);
        _devices.add(device8);
        _devices.add(device9);

        _adapter = new DeviceListAdapter(/*_bluetooth.getBondedDevices()*/_devices, fab);
        _deviceList.setAdapter(_adapter);
        ItemTouchHelper.Callback callback = new TouchHelperCallback(_adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(_deviceList);

        searchEditText.addTextChangedListener(_queryTextListener);

        return main;
    }

    private final CountDownTimer mTimer = new CountDownTimer(5000, 1500) {
        @Override
        public void onTick(final long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.main_content, new GaugesFragment_());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            _adapter._isClickable = true;
            _adapter.stopProgressBar();

        }
    };

    private OnClickListener _fabOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            _adapter.startProgressBar();
            _adapter._isClickable = false;
            mTimer.start();
            /*FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.main_content, new GaugesFragment());
            fragmentTransaction.commit();*/
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
