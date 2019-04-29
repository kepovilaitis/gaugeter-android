package lt.kepo.gaugeter.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.adapters.FoundDevicesAdapter;
import lt.kepo.gaugeter.controllers.BluetoothController;
import lt.kepo.gaugeter.constants.Constants;
import lt.kepo.gaugeter.adapters.DevicesListAdapter;
import lt.kepo.gaugeter.holders.DeviceInfoHolder;
import lt.kepo.gaugeter.network.BaseResponse;
import lt.kepo.gaugeter.network.GaugeterClient;
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

    private List<DeviceInfoHolder> _devices = new ArrayList<>();
    private List<DeviceInfoHolder> _foundDevices = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        _bluetoothController = BluetoothController.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_devices, container, false);

        setHasOptionsMenu(true);

        EditText searchEditText = main.findViewById(R.id.search);
        RecyclerView devicesList = main.findViewById(R.id.recyclerViewPairedDevices);
        RecyclerView foundDevicesList = main.findViewById(R.id.recyclerViewFoundDevices);

        searchEditText.addTextChangedListener(_queryTextListener);

        devicesList.setLayoutManager(new LinearLayoutManager(getContext()));
        _devicesListAdapter = new DevicesListAdapter(_devices, getContext());
        devicesList.setAdapter(_devicesListAdapter);

        foundDevicesList.setLayoutManager(new LinearLayoutManager(getContext()));
        _foundDevicesAdapter = new FoundDevicesAdapter(_foundDevices, getContext());
        foundDevicesList.setAdapter(_foundDevicesAdapter);

        main.findViewById(R.id.btnSearch).setOnClickListener(_findDevicesClickListener);

        GaugeterClient.getInstance()
                .getUserDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetDeviceListResponse(getContext()));

        return main;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (_bluetoothController.isBluetoothOn()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }
    }

    private OnClickListener _findDevicesClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            _bluetoothController.startDiscovery(getContext(), new FoundDevicesObserver());
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

    private class GetDeviceListResponse extends BaseResponse<List<DeviceInfoHolder>> {
        GetDeviceListResponse(Context context) {
            super(context);
        }

        @Override
        public void onSubscribe(Disposable d) {
            startProgress();
        }

        @Override
        public void onSuccess(List<DeviceInfoHolder> devices) {
            stopProgress();

            _devices.clear();
            _devices.addAll(devices);
            _devicesListAdapter.notifyDataSetChanged();
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
            _foundDevices.add(deviceInfoHolder);
            _foundDevicesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Throwable e) {
            stopProgress();
            ToastNotifier.showBluetoothError(getContext(), R.string.error_bluetooth);
        }

        @Override
        public void onComplete() {
            stopProgress();
        }
    }
}
