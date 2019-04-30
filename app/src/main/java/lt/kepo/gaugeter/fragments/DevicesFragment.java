package lt.kepo.gaugeter.fragments;

import android.content.Context;
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

import io.reactivex.SingleObserver;
import lombok.AllArgsConstructor;
import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.adapters.FoundDevicesAdapter;
import lt.kepo.gaugeter.constants.Enums.CONNECTION_STATUS;
import lt.kepo.gaugeter.controllers.BluetoothController;
import lt.kepo.gaugeter.adapters.DevicesListAdapter;
import lt.kepo.gaugeter.holders.DeviceInfoHolder;
import lt.kepo.gaugeter.interfaces.OnDeviceAction;
import lt.kepo.gaugeter.network.BaseResponse;
import lt.kepo.gaugeter.network.HttpClient;
import lt.kepo.gaugeter.tools.ToastNotifier;
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
    private Context _context;
    private Disposable _statusDisposable;

    private List<DeviceInfoHolder> _devices = new ArrayList<>();
    private List<DeviceInfoHolder> _foundDevices = new ArrayList<>();
    private HttpClient _httpClient = HttpClient.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        _context = getContext();
        _bluetoothController = BluetoothController.getInstance();
        _bluetoothController.setDevice(null);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_devices, container, false);

        setHasOptionsMenu(true);

        main.<EditText>findViewById(R.id.search).addTextChangedListener(_queryTextListener);
        RecyclerView devicesList = main.findViewById(R.id.recyclerViewPairedDevices);
        RecyclerView foundDevicesList = main.findViewById(R.id.recyclerViewFoundDevices);

        devicesList.setLayoutManager(new LinearLayoutManager(_context));
        _devicesListAdapter = new DevicesListAdapter(_devices, _context, _connectToDeviceAction);
        devicesList.setAdapter(_devicesListAdapter);

        foundDevicesList.setLayoutManager(new LinearLayoutManager(_context));
        _foundDevicesAdapter = new FoundDevicesAdapter(_foundDevices, _context, _bondWithDeviceAction);
        foundDevicesList.setAdapter(_foundDevicesAdapter);

        main.findViewById(R.id.btnSearch).setOnClickListener(_discoverDevicesClickListener);

        _httpClient
                .getUserDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetDeviceListResponse(_context));

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

    private OnDeviceAction _connectToDeviceAction = new OnDeviceAction() {
        @Override
        public void execute(DeviceInfoHolder device) {
            _bluetoothController.setDevice(device);
            _bluetoothController.connectToDevice(_context, device.getBluetoothAddress(), null);
        }
    };

    private OnDeviceAction _bondWithDeviceAction = new OnDeviceAction() {
        @Override
        public void execute(DeviceInfoHolder device) {
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
                    ToastNotifier.showBluetoothError(_context, R.string.message_connection_closed);

                    break;

                case CONNECTED:
                    FragmentManager fragmentManager = getFragmentManager();

                    if (fragmentManager != null) {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.mainContent, new LiveDataFragment_());
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

    private class GetDeviceListResponse extends BaseResponse<List<DeviceInfoHolder>> {
        GetDeviceListResponse(Context context) {
            super(context);
        }

        @Override
        public void onSubscribe(Disposable d) {
            startProgress();

            _foundDevices.clear();
            _foundDevicesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSuccess(List<DeviceInfoHolder> devices) {
            stopProgress();
            _foundDevicesAdapter.stopProgress();

            _devices.clear();
            _devices.addAll(devices);
            _devicesListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Throwable e) {
            stopProgress();

            super.onError(e);
        }
    }

    private class FoundDevicesObserver implements Observer<DeviceInfoHolder> {
        @Override
        public void onSubscribe(Disposable d) {
            startProgress();

            _foundDevices.clear();
            _foundDevicesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNext(DeviceInfoHolder deviceInfoHolder) {
            boolean shouldAdd = false;

            for (DeviceInfoHolder device : _devices) {
                if (!device.getBluetoothAddress().equals(deviceInfoHolder.getBluetoothAddress())) {
                    shouldAdd = true;
                }
            }

            if (shouldAdd || _devices.isEmpty()) {
                _foundDevices.add(deviceInfoHolder);
                _foundDevicesAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onError(Throwable e) {
            stopProgress();
            ToastNotifier.showBluetoothError(_context, R.string.error_bluetooth);
        }

        @Override
        public void onComplete() {
            stopProgress();
        }
    }

    @AllArgsConstructor
    private class BondWithNewDeviceObserver implements SingleObserver<String> {
        private DeviceInfoHolder _device;

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(final String bluetoothAddress) {
            _httpClient
                    .addDeviceToUser(_device)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new GetDeviceListResponse(_context));

            _foundDevices.remove(_device);
            _foundDevicesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Throwable e) {
            _devicesListAdapter.stopProgress();
            ToastNotifier.showBluetoothError(_context, R.string.error_bluetooth_could_not_bond);
        }
    }
}
