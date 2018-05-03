package fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import controllers.AnimationController;
import controllers.BluetoothController;
import holders.DeviceInfoHolder;
import interfaces.BluetoothStateListener;
import constants.Constants;
import adapters.DeviceListAdapter;
import com.example.kestutis.cargauges.R;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;

public class DevicesFragment extends Fragment {
    private BluetoothController _bluetooth;
    private AnimationController _animation;
    private FloatingActionButton _fab;
    private DeviceListAdapter _adapter;
    private DeviceInfoHolder _selectedDevice;

    DeviceInfoHolder device = new DeviceInfoHolder("Device1", "00:11:22:33:44", BluetoothDevice.BOND_BONDING);
    private List<DeviceInfoHolder> _devices = new LinkedList<>(Arrays.asList(device));

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        _bluetooth = BluetoothController.getInstance();
        _bluetooth.setBtStateListener(_btStateListener);
        _animation = AnimationController.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_devices, container, false);
        ListView deviceList = main.findViewById(R.id.list_view);

        _fab = main.findViewById(R.id.fab);
        _fab.setImageResource(_bluetooth.isBluetoothOn() ? R.drawable.ic_radar : R.drawable.ic_bluetooth_off_white_48dp);
        _fab.setOnClickListener(_fabOnClickListener);

        deviceList.setEmptyView(main.findViewById(R.id.text_empty));
        _adapter = new DeviceListAdapter(/*_bluetooth.getFoundDevices()*/ _devices, getActivity());
        deviceList.setAdapter(_adapter);
        deviceList.setOnItemLongClickListener(_itemLongClickListener);
        deviceList.setOnItemClickListener(_itemClickListener);

        return main;
    }

    private OnClickListener _fabOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (_selectedDevice != null){
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.main_content, new GaugesFragment());
                fragmentTransaction.commit();
            } else if (_bluetooth.isBluetoothOn()) {
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

            AlertDialog alertDialog = new Builder(getActivity()).create();
            alertDialog.setTitle("Delete");
            alertDialog.setMessage("Are you sure you want to delete");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new ConfirmDeleteBtnClick(/*_bluetooth.getDevices()*/_devices.get(position)));
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new ConfirmDeleteBtnClick(/*_bluetooth.getDevices()*/_devices.get(position)));
            alertDialog.show();

            return true;
        }
    };

    private OnItemClickListener _itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            _selectedDevice = _devices.get(position);
            _fab.setImageResource(R.drawable.ic_bluetooth_connect_white_48dp);
            parent.setSelected(!parent.isSelected());

        }
    };

    @AllArgsConstructor
    private class ConfirmDeleteBtnClick implements DialogInterface.OnClickListener {
        //private BluetoothDevice device;
        private DeviceInfoHolder device;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    /*_bluetooth.delete(device);
                    _adapter.notifyDataSetChanged();*/

                    _devices.remove(device);
                    _adapter.notifyDataSetChanged();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:

                    break;
            }
        }
    }
}
