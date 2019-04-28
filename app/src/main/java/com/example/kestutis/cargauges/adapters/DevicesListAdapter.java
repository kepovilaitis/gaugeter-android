package com.example.kestutis.cargauges.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
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

import com.example.kestutis.cargauges.holders.DeviceInfoHolder;
import com.example.kestutis.cargauges.network.BaseEmptyResponse;
import com.example.kestutis.cargauges.network.GaugeterClient;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

public class DevicesListAdapter extends RecyclerView.Adapter<DevicesListAdapter.ViewHolder> implements Filterable {
    private List<DeviceInfoHolder> _devices, _filteredDevices;
    private int _selectedPosition = -1;
    private boolean _isClickable = true;
    private Disposable _statusDisposable;
    private Context _context;

    public DevicesListAdapter(List<DeviceInfoHolder> devices, Context context){
        _devices = devices;
        _filteredDevices = devices;
        _context = context;

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
        DeviceInfoHolder device = _filteredDevices.get(position);

        viewHolder.itemView.setSelected(_selectedPosition == position);
        viewHolder.progressBar.setVisibility(_selectedPosition == position ? View.VISIBLE : View.INVISIBLE);
        viewHolder.textName.setText(device.getName());
        viewHolder.textAddress.setText(device.getBluetoothAddress());
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if (_statusDisposable != null && !_statusDisposable.isDisposed()) {
            _statusDisposable.dispose();
        }
    }

    @Override
    public int getItemCount() {
        return _devices.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<DeviceInfoHolder> deviceList = getDeviceList(constraint);

                filterResults.values = deviceList;
                filterResults.count = deviceList.size();

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                _filteredDevices = (List<DeviceInfoHolder>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private List<DeviceInfoHolder> getDeviceList(CharSequence input) {
        List<DeviceInfoHolder> bondedDeviceList = _devices;

        if (input == null || input.length() == 0) {
            return bondedDeviceList;
        }

        List<DeviceInfoHolder> list = new ArrayList<>();
        String filter = input.toString().toLowerCase();

        for (DeviceInfoHolder item: bondedDeviceList) {
            if (item.getName().toLowerCase().contains(filter)) {
                list.add(item);
            }
        }
        return list;
    }

    private void startProgress() {
        notifyItemChanged(_selectedPosition);

        _isClickable = false;
    }

    private void stopProgress() {
        _selectedPosition = -1;
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
            stopProgress();
        }

        @Override
        public void onError(Throwable e) {
            stopProgress();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textName;
        private TextView textAddress;
        private ProgressBar progressBar;

        ViewHolder(final View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);
            progressBar = itemView.findViewById(R.id.progressBar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!_isClickable)
                        return;

                    _selectedPosition = getLayoutPosition();

                    startProgress();
                    BluetoothController.getInstance().connectToDevice(_devices.get(_selectedPosition).getBluetoothAddress());
                }
            });

            itemView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int selectedPosition = getLayoutPosition();

                    startProgress();

                    GaugeterClient.getInstance()
                            .removeDeviceFromUser(_devices.get(selectedPosition).getBluetoothAddress())
                            .enqueue(new RemoveDeviceFromUserResponse(selectedPosition));

                    return true;
                }
            });
        }
    }

    private class RemoveDeviceFromUserResponse extends BaseEmptyResponse {
        private int _selectedPos;

        RemoveDeviceFromUserResponse(int selectedPosition) {
            super(_context);
            _selectedPos = selectedPosition;
        }

        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            _devices.remove(_selectedPos);
            notifyDataSetChanged();
            stopProgress();
        }
    }
}
