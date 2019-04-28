package com.example.kestutis.cargauges.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.holders.DeviceInfoHolder;

import java.util.List;

public class FoundDevicesAdapter extends RecyclerView.Adapter<FoundDevicesAdapter.ViewHolder> {
    private List<DeviceInfoHolder> _foundDevices;
    private Context _context;
    private int _selectedPosition = -1;

    public FoundDevicesAdapter(List<DeviceInfoHolder> devices, Context context){
        _foundDevices = devices;
        _context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        DeviceInfoHolder device = _foundDevices.get(position);

        viewHolder.itemView.setSelected(_selectedPosition == position);
        viewHolder.progressBar.setVisibility(_selectedPosition == position ? View.VISIBLE : View.INVISIBLE);
        viewHolder.textName.setText(device.getName());
        viewHolder.textAddress.setText(device.getBluetoothAddress());
    }

    @Override
    public int getItemCount() {
        return _foundDevices.size();
    }

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
                    Toast.makeText(_context, "connect", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
