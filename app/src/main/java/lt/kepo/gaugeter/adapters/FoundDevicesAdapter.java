package lt.kepo.gaugeter.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import lombok.AllArgsConstructor;
import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.holders.DeviceInfoHolder;
import lt.kepo.gaugeter.interfaces.OnDeviceAction;

import java.util.List;

@AllArgsConstructor
public class FoundDevicesAdapter extends RecyclerView.Adapter<FoundDevicesAdapter.ViewHolder> {
    private List<DeviceInfoHolder> _foundDevices;
    private Context _context;
    private OnDeviceAction _onDeviceAction;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        DeviceInfoHolder device = _foundDevices.get(position);

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

        ViewHolder(final View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(_context)
                            .setTitle(R.string.title_add_device)
                            .setPositiveButton(R.string.yes, new DialogPositiveClickListener(getLayoutPosition()))
                            .setNegativeButton(R.string.no, null)
                            .setMessage(R.string.dialog_add_device)
                            .create()
                            .show();
                }
            });
        }
    }

    @AllArgsConstructor
    private class DialogPositiveClickListener implements DialogInterface.OnClickListener {
        int _selectedPos;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            _onDeviceAction.execute(_foundDevices.get(_selectedPos));
        }
    }
}
