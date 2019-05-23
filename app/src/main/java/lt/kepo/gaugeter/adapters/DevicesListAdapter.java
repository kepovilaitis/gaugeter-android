package lt.kepo.gaugeter.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.*;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.ArrayList;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.constants.ErrorCodes;
import lt.kepo.gaugeter.controllers.BluetoothController;
import lt.kepo.gaugeter.holders.DeviceHolder;
import lt.kepo.gaugeter.interfaces.OnItemClickListener;
import lt.kepo.gaugeter.network.HttpClient;
import lt.kepo.gaugeter.tools.ToastNotifier;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DevicesListAdapter extends BaseListAdapter<DeviceHolder> implements Filterable {
    private List<DeviceHolder> _filteredDevices;

    public DevicesListAdapter(List<DeviceHolder> devices, Context context, OnItemClickListener onItemClickListener) {
        super(devices, context, onItemClickListener);
        _filteredDevices = devices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseListAdapter.ViewHolder holder, int position) {
        DeviceHolder device = _filteredDevices.get(position);

        holder.itemView.setSelected(_selectedPosition == position);
        holder.progressBar.setVisibility(_selectedPosition == position ? View.VISIBLE : View.GONE);
        holder.textPrimary.setText(device.getName());
        holder.textSecondary.setText(device.getBluetoothAddress());
    }

    @Override
    public int getItemCount() {
        return _filteredDevices.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<DeviceHolder> deviceList = getDeviceList(constraint);

                filterResults.values = deviceList;
                filterResults.count = deviceList.size();

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                _filteredDevices = (List<DeviceHolder>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private List<DeviceHolder> getDeviceList(CharSequence input) {
        List<DeviceHolder> bondedDeviceList = _list;

        if (input == null || input.length() == 0) {
            return bondedDeviceList;
        }

        List<DeviceHolder> list = new ArrayList<>();
        String filter = input.toString().toLowerCase();

        for (DeviceHolder item: bondedDeviceList) {
            if (item.getName().toLowerCase().contains(filter)) {
                list.add(item);
            }
        }
        return list;
    }

    class ViewHolder extends BaseListAdapter.ViewHolder {

        ViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!_isClickable)
                        return;

                    _selectedPosition = getLayoutPosition();
                    startProgress();

                    _onItemClickListener.execute(_list.get(_selectedPosition));
                }
            });

            itemView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!_isClickable)
                        return false;

                    _selectedPosition = getLayoutPosition();

                    startProgress();

                    new AlertDialog.Builder(_context)
                            .setTitle(R.string.title_remove_device)
                            .setPositiveButton(R.string.yes, new DialogPositiveClickListener(_selectedPosition))
                            .setNegativeButton(R.string.no, _dialogNegativeClickListener)
                            .setMessage(R.string.dialog_remove_device)
                            .create()
                            .show();

                    return true;
                }
            });
        }
    }

    @RequiredArgsConstructor
    private class DialogPositiveClickListener implements DialogInterface.OnClickListener {
        final int _selectedPos;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            final String bluetoothAddress = _list.get(_selectedPos).getBluetoothAddress();

            HttpClient.getInstance()
                    .removeDeviceFromUser(bluetoothAddress)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            stopProgress();

                            if (response.isSuccessful() && response.code() != ErrorCodes.HTTP_NO_CONTENT) {

                                BluetoothController.getInstance().removeBondedDevice(bluetoothAddress);
                                _list.remove(_selectedPos);
                                notifyDataSetChanged();

                            } else {
                                ToastNotifier.showHttpError(_context, response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            stopProgress();
                            ToastNotifier.showHttpError(_context, ErrorCodes.HTTP_SERVER);
                        }
                    });
        }
    }
}
