package lt.kepo.gaugeter.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.*;

import lombok.RequiredArgsConstructor;
import lt.kepo.gaugeter.R;

import java.util.List;
import java.util.ArrayList;

import lt.kepo.gaugeter.controllers.BluetoothController;
import lt.kepo.gaugeter.holders.DeviceInfoHolder;
import lt.kepo.gaugeter.interfaces.OnDeviceAction;
import lt.kepo.gaugeter.network.HttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DevicesListAdapter extends RecyclerView.Adapter<DevicesListAdapter.ViewHolder> implements Filterable {
    private List<DeviceInfoHolder> _devices, _filteredDevices;
    private Context _context;
    private OnDeviceAction _connectToDeviceAction;

    private int _selectedPosition = -1;
    private boolean _isClickable = true;

    public DevicesListAdapter(List<DeviceInfoHolder> devices, Context context, OnDeviceAction onDeviceAction) {
        _devices = devices;
        _filteredDevices = devices;
        _context = context;
        _connectToDeviceAction = onDeviceAction;
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
        viewHolder.progressBar.setVisibility(_selectedPosition == position ? View.VISIBLE : View.GONE);
        viewHolder.textName.setText(device.getName());
        viewHolder.textAddress.setText(device.getBluetoothAddress());
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

    public void stopProgress() {
        _selectedPosition = -1;
        _isClickable = true;

        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textName;
        private TextView textAddress;
        private FrameLayout progressBar;

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

                    _connectToDeviceAction.execute(_devices.get(_selectedPosition));
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
                            .setNegativeButton(R.string.no, null)
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
            final String bluetoothAddress = _devices.get(_selectedPos).getBluetoothAddress();

            HttpClient.getInstance()
                    .removeDeviceFromUser(bluetoothAddress)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            BluetoothController.getInstance().removeBondedDevice(bluetoothAddress);
                            _devices.remove(_selectedPos);
                            notifyDataSetChanged();
                            stopProgress();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {

                        }
                    });
        }
    }
}
