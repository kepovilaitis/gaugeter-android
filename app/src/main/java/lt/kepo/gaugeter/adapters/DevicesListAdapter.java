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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import io.reactivex.SingleObserver;
import lombok.AllArgsConstructor;
import lt.kepo.gaugeter.R;

import java.util.List;
import java.util.ArrayList;

import lt.kepo.gaugeter.holders.DeviceInfoHolder;
import lt.kepo.gaugeter.interfaces.OnDeviceAction;
import lt.kepo.gaugeter.network.HttpClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@AllArgsConstructor
public class DevicesListAdapter extends RecyclerView.Adapter<DevicesListAdapter.ViewHolder> implements Filterable {
    private List<DeviceInfoHolder> _devices, _filteredDevices;
    private Context _context;
    private SingleObserver<List<DeviceInfoHolder>> _removeDeviceObserver;
    private OnDeviceAction _connectToDeviceAction;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        DeviceInfoHolder device = _filteredDevices.get(position);

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

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textName;
        private TextView textAddress;

        ViewHolder(final View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _connectToDeviceAction.execute(_devices.get(getLayoutPosition()));
                }
            });

            itemView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(_context)
                            .setTitle(R.string.title_remove_device)
                            .setPositiveButton(R.string.yes, new DialogPositiveClickListener(getLayoutPosition()))
                            .setNegativeButton(R.string.no, null)
                            .setMessage(R.string.dialog_remove_device)
                            .create()
                            .show();

                    return true;
                }
            });
        }
    }

    @AllArgsConstructor
    private class DialogPositiveClickListener implements DialogInterface.OnClickListener {
        int _selectedPos;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            HttpClient.getInstance()
                    .removeDeviceFromUser(_devices.get(_selectedPos).getBluetoothAddress())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(_removeDeviceObserver);
        }
    }
}
