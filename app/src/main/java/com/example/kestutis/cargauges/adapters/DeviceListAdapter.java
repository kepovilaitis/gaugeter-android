package com.example.kestutis.cargauges.adapters;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.constants.Enums.CONNECTION_STATUS;
import com.example.kestutis.cargauges.controllers.BluetoothController;

import java.util.List;
import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> implements Filterable {
    private List<BluetoothDevice> _devices, filteredDevices;
    private int _selectedPos = -1;
    private boolean _isClickable = true;
    private Disposable _statusDisposable;

    public DeviceListAdapter(List<BluetoothDevice> devices){
        _devices = devices;
        filteredDevices = devices;

        BluetoothController.getInstance().getStateSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_statusObserver);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        BluetoothDevice device = filteredDevices.get(position);

        if(_selectedPos == position){
            viewHolder.itemView.setSelected(true);
            viewHolder.progressBar.setVisibility(View.VISIBLE);
        } else {
            viewHolder.itemView.setSelected(false);
            viewHolder.progressBar.setVisibility(View.INVISIBLE);
        }

        viewHolder.textName.setText(device.getName());
        viewHolder.textAddress.setText(device.getAddress());
        viewHolder.textStatus.setText(/*device.getBondState()*/"kantakt est");
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (_statusDisposable != null && !_statusDisposable.isDisposed()) {
            _statusDisposable.dispose();
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return filteredDevices.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<BluetoothDevice> deviceList = getDeviceList(constraint);

                filterResults.values = deviceList;
                filterResults.count = deviceList.size();

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredDevices = (List<BluetoothDevice>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private List<BluetoothDevice> getDeviceList(CharSequence input) {
        List<BluetoothDevice> bondedDeviceList = _devices;

        if (input == null || input.length() == 0) {
            return bondedDeviceList;
        }

        List<BluetoothDevice> list = new ArrayList<>();
        String filter = input.toString().toLowerCase();

        for (BluetoothDevice item: bondedDeviceList) {
            if (item.getName().toLowerCase().contains(filter)) {
                list.add(item);
            }
        }
        return list;
    }

    private void startProgress() {
        notifyItemChanged(_selectedPos);

        _isClickable = false;
    }

    private void stopProgress() {
        _selectedPos = -1;
        _isClickable = true;

        notifyDataSetChanged();
    }

    private Observer<CONNECTION_STATUS> _statusObserver = new Observer<CONNECTION_STATUS>() {
        @Override
        public void onSubscribe(Disposable d) {
            _statusDisposable = d;
        }

        @Override
        public void onNext(CONNECTION_STATUS connection_STATUS) {
            if (connection_STATUS == CONNECTION_STATUS.CONNECTING) {
                startProgress();
            } else {
                stopProgress();
            }
        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onError(Throwable e) {
            stopProgress();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textName;
        private TextView textAddress;
        private TextView textStatus;
        private ProgressBar progressBar;

        ViewHolder(final View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);
            textStatus = itemView.findViewById(R.id.textStatus);
            progressBar = itemView.findViewById(R.id.progressBar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!_isClickable)
                        return;

                    _selectedPos = getLayoutPosition();

                    startProgress();
                    BluetoothController.getInstance().connectToDevice(_devices.get(_selectedPos));
                }
            });
        }
    }
}
