package lt.kepo.gaugeter.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;

import lombok.AllArgsConstructor;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.adapters.FoundDevicesAdapter;
import lt.kepo.gaugeter.constants.Enums.CONNECTION_STATUS;
import lt.kepo.gaugeter.controllers.BluetoothController;
import lt.kepo.gaugeter.adapters.DevicesListAdapter;
import lt.kepo.gaugeter.holders.DeviceHolder;
import lt.kepo.gaugeter.interfaces.OnItemClickListener;
import lt.kepo.gaugeter.network.BaseResponse;
import lt.kepo.gaugeter.tools.ToastNotifier;

import io.reactivex.SingleObserver;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.List;
import java.util.ArrayList;

public class DevicesFragment extends BaseFragment {
    private BluetoothController _bluetoothController;
    private DevicesListAdapter _devicesListAdapter;
    private FoundDevicesAdapter _foundDevicesAdapter;
    private Disposable _statusDisposable;

    private List<DeviceHolder> _devices = new ArrayList<>();
    private List<DeviceHolder> _foundDevices = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        _bluetoothController = BluetoothController.getInstance();
        _bluetoothController.setDevice(null);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_devices, container, false);

        setTitle(R.string.app_name);

        main.<EditText>findViewById(R.id.filter).addTextChangedListener(_queryTextListener);
        RecyclerView devicesList = main.findViewById(R.id.recyclerViewPairedDevices);
        RecyclerView foundDevicesList = main.findViewById(R.id.recyclerViewFoundDevices);

        devicesList.setLayoutManager(new LinearLayoutManager(_context));
        _devicesListAdapter = new DevicesListAdapter(_devices, _context, _connectToDeviceClickListener);
        devicesList.setAdapter(_devicesListAdapter);

        foundDevicesList.setLayoutManager(new LinearLayoutManager(_context));
        _foundDevicesAdapter = new FoundDevicesAdapter(_foundDevices, _context, _bondWithDeviceClickListener);
        foundDevicesList.setAdapter(_foundDevicesAdapter);

        _fab = getFab();
        _fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_scan, null));
        _fab.setOnClickListener(_discoverDevicesClickListener);
        _fab.show();

        _httpClient
                .getUserDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetDeviceListResponse());

        return main;
    }

    @Override
    public void onStart() {
        super.onStart();

        _bluetoothController
                .getStateSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_statusObserver);
    }

    @Override
    public void onStop() {
        if (_statusDisposable != null && !_statusDisposable.isDisposed()) {
            _statusDisposable.dispose();
        }

        super.onStop();
    }

    private OnClickListener _discoverDevicesClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            _bluetoothController.discoverDevices(_context, new FoundDevicesObserver());
        }
    };

    private OnItemClickListener _connectToDeviceClickListener = new OnItemClickListener<DeviceHolder>() {
        @Override
        public void execute(DeviceHolder device) {
            _bluetoothController.setDevice(device);
            _bluetoothController.connectToDevice(_context, device.getBluetoothAddress(), null);
        }
    };

    private OnItemClickListener _bondWithDeviceClickListener = new OnItemClickListener<DeviceHolder>() {
        @Override
        public void execute(DeviceHolder device) {
            _bluetoothController.bondWithDevice(_context, device.getBluetoothAddress(), new BondWithNewDeviceObserver(device));
        }
    };

    private TextWatcher _queryTextListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            _devicesListAdapter.getFilter().filter(s);
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    private Observer<CONNECTION_STATUS> _statusObserver = new Observer<CONNECTION_STATUS>() {
        @Override
        public void onSubscribe(Disposable d) {
            _statusDisposable = d;
        }

        @Override
        public void onNext(CONNECTION_STATUS connection_status) {
            switch (connection_status) {
                case DISCONNECTED:
                    stopProgress();
                    _devicesListAdapter.stopProgress();
                    ToastNotifier.showError(_context, R.string.message_connection_closed);

                    break;

                case CONNECTED:
                    FragmentManager fragmentManager = getFragmentManager();

                    if (fragmentManager != null) {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.mainContent, new TelemDataFragment());
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }

                    _devicesListAdapter.stopProgress();

                    break;
            }
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
        }
    };

    private class GetDeviceListResponse extends BaseResponse<List<DeviceHolder>> {
        GetDeviceListResponse() {
            super(DevicesFragment.this);
        }

        @Override
        public void onSubscribe(Disposable d) {
            super.onSubscribe(d);

            _foundDevices.clear();
            _foundDevicesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSuccess(List<DeviceHolder> devices) {
            super.onSuccess(devices);
            _foundDevicesAdapter.stopProgress();

            _devices.clear();
            _devices.addAll(devices);
            _devicesListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Throwable e) {
            _foundDevicesAdapter.stopProgress();

            super.onError(e);
        }
    }

    private class FoundDevicesObserver implements Observer<DeviceHolder> {
        @Override
        public void onSubscribe(Disposable d) {
            startProgress();

            _foundDevices.clear();
            _foundDevicesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNext(DeviceHolder deviceHolder) {
            boolean shouldAdd = false;

            for (DeviceHolder device : _devices) {
                if (!device.getBluetoothAddress().equals(deviceHolder.getBluetoothAddress())) {
                    shouldAdd = true;
                }
            }

            if (shouldAdd || _devices.isEmpty()) {
                _foundDevices.add(deviceHolder);
                _foundDevicesAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onComplete() {
            stopProgress();
        }

        @Override
        public void onError(Throwable e) {
            stopProgress();
            ToastNotifier.showError(_context, R.string.error_bluetooth);
        }
    }

    @AllArgsConstructor
    private class BondWithNewDeviceObserver implements SingleObserver<String> {
        private DeviceHolder _device;

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(final String bluetoothAddress) {
            _httpClient
                    .addDeviceToUser(_device)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new GetDeviceListResponse());

            _foundDevices.remove(_device);
            _foundDevicesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Throwable e) {
            _devicesListAdapter.stopProgress();
            ToastNotifier.showError(_context, R.string.error_bluetooth_could_not_bond);
        }
    }
}
