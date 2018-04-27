package com.example.kestutis.cargauges.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kestutis.cargauges.R;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DeviceListAdapter extends BaseAdapter {
    private List<BluetoothDevice> _devices;
    private Context _context;

    @Override

    public int getCount() {
        return _devices.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return _devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(_context).inflate(R.layout.list_item_device, null);
            view.setTag(new ViewHolder(view));
        }

        BluetoothDevice device = getItem(position);

        if (device == null) {
            return view;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.getTextName().setText(device.getName());
        holder.getTextAddress().setText(device.getAddress());
        holder.getTextStatus().setText(device.getBondState());

        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        if (_devices.size() > 0) {
            super.notifyDataSetChanged();
        } else {
            super.notifyDataSetInvalidated();
        }
    }

    @Getter
    private class ViewHolder {
        TextView _textName;
        TextView _textAddress;
        TextView _textStatus;

        ViewHolder(View view) {
            _textName = view.findViewById(R.id.text_name);
            _textAddress = view.findViewById(R.id.text_address);
            _textStatus = view.findViewById(R.id.text_status);
        }
    }
}
