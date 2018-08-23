package adapters;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.Filter;
import android.widget.TextView;

import com.example.kestutis.cargauges.R;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import controllers.BluetoothController;
import holders.DeviceInfoHolder;
import interfaces.ItemTouchMoveListener;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> implements Filterable, ItemTouchMoveListener {
    private List<DeviceInfoHolder> _devices, filteredDevices;
    private FloatingActionButton _fab;
    private int _selectedPos = -1;
    private BluetoothController _bluetoothController;
    private View _view;

    public DeviceListAdapter(List<DeviceInfoHolder> devices, FloatingActionButton fab){
        _devices = devices;
        filteredDevices = devices;
        _fab = fab;
        _bluetoothController = BluetoothController.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        _view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false);
        return new ViewHolder(_view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        //BluetoothDevice device = filteredDevices.get(position);
        DeviceInfoHolder device = filteredDevices.get(position);

        viewHolder.itemView.setSelected(_selectedPos == position);
        viewHolder._textName.setText(device.getName());
        viewHolder._textAddress.setText(device.getAddress());
        viewHolder._textStatus.setText("Bonding");
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
                List<DeviceInfoHolder> deviceList = getDeviceList(constraint);

                filterResults.values = deviceList;
                filterResults.count = deviceList.size();

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredDevices = (List<DeviceInfoHolder>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private List<DeviceInfoHolder> getDeviceList(CharSequence input) {
//        List<BluetoothDevice> bondedDeviceList = _bluetoothController.getBondedDevices();
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

    /*public BluetoothDevice getDevice(){
        return _devices.get(_selectedPos);
    }*/

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(_devices, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(final int position) {
//        BluetoothDevice device = _devices.get(position);
        final DeviceInfoHolder device = _devices.get(position);

        if (_selectedPos == position){
            _selectedPos = -1;
            showFAB();
        } else if ( _selectedPos > position){
            _selectedPos -= 1;
        }

        _devices.remove(position);
        notifyItemRemoved(position);

        Snackbar.make(_view, "Deleted the device", Snackbar.LENGTH_INDEFINITE)
        .setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _devices.add(position, device);
                notifyDataSetChanged();
                Snackbar.make(view, "Device is restored!", Snackbar.LENGTH_SHORT).show();
            }
        })
        .setDuration(5000)
        .show();
    }

    private void showFAB(){
        if (_selectedPos >= 0) {
            _fab.show();
        } else {
            _fab.hide();
        }
    }

    @Getter
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView _textName;
        TextView _textAddress;
        TextView _textStatus;

        ViewHolder(final View itemView) {
            super(itemView);

            _textName = itemView.findViewById(R.id.text_name);
            _textAddress = itemView.findViewById(R.id.text_address);
            _textStatus = itemView.findViewById(R.id.text_status);

            // Handle item click and set the selection
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getLayoutPosition();

                    notifyItemChanged(_selectedPos);

                    if (_selectedPos == position) {
                        _selectedPos = -1;
                    } else {
                        _selectedPos = position;
                    }
                    notifyItemChanged(_selectedPos);

                    showFAB();
                }
            });
        }
    }
}
