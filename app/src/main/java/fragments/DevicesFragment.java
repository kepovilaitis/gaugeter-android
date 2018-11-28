package fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import controllers.BluetoothController;
import helpers.TouchHelperCallback;
import constants.Constants;

import adapters.DeviceListAdapter;
import interfaces.BluetoothItemSelectListener;
import interfaces.SocketConnectedListener;

import com.example.kestutis.cargauges.R;

import java.util.List;
import java.util.ArrayList;

public class DevicesFragment extends Fragment{
    private BluetoothController _bluetooth;
    private DeviceListAdapter _adapter;
    private Context _context;

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
        fab.hide();

        _adapter = new DeviceListAdapter(_devices, fab, _bluetoothItemClickListener);
        _deviceList.setAdapter(_adapter);
        ItemTouchHelper.Callback callback = new TouchHelperCallback(_adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(_deviceList);

        searchEditText.addTextChangedListener(_queryTextListener);

        return main;
    }

    @Override
    public void onPause(){
        super.onPause();

        resetListItems();
    }

    private void resetListItems(){
        _adapter.resetListItems();
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

    private BluetoothItemSelectListener _bluetoothItemClickListener = new BluetoothItemSelectListener() {
        @Override
        public void onClick(BluetoothDevice device) {
            BluetoothController.getInstance().connectToDevice(device, _socketListener);
        }
    };

    private SocketConnectedListener _socketListener = new SocketConnectedListener() {
        @Override
        public void hasConnected() {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_content, new LiveDataFragment_());
            fragmentTransaction.addToBackStack(DevicesFragment.class.getName());
            fragmentTransaction.commit();
        }

        @Override
        public void connectionAttemptHasFailed() {
            if(isVisible()){
                resetListItems();
                Snackbar.make(getView(), "Could not connect to device", Snackbar.LENGTH_LONG).show();
            }
        }
    };
}
