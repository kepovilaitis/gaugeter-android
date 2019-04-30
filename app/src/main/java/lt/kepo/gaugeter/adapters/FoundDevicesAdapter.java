package lt.kepo.gaugeter.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import lombok.RequiredArgsConstructor;
import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.holders.DeviceInfoHolder;
import lt.kepo.gaugeter.interfaces.OnDeviceAction;

import java.util.List;

@RequiredArgsConstructor
public class FoundDevicesAdapter extends RecyclerView.Adapter<FoundDevicesAdapter.ViewHolder> {
    final private List<DeviceInfoHolder> _foundDevices;
    final private Context _context;
    final private OnDeviceAction _onDeviceAction;

    private int _selectedPosition = -1;
    private boolean _isClickable = true;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        DeviceInfoHolder device = _foundDevices.get(position);

        viewHolder.itemView.setSelected(_selectedPosition == position);
        viewHolder.progressBar.setVisibility(_selectedPosition == position ? View.VISIBLE : View.GONE);
        viewHolder.textName.setText(device.getName());
        viewHolder.textAddress.setText(device.getBluetoothAddress());
    }

    @Override
    public int getItemCount() {
        return _foundDevices.size();
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

                    new AlertDialog.Builder(_context)
                            .setTitle(R.string.title_add_device)
                            .setPositiveButton(R.string.yes, new DialogPositiveClickListener())
                            .setNegativeButton(R.string.no, null)
                            .setMessage(_context.getResources().getString(R.string.dialog_add_device, _foundDevices.get(_selectedPosition).getName()))
                            .create()
                            .show();
                }
            });
        }
    }

    private class DialogPositiveClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            startProgress();

            _onDeviceAction.execute(_foundDevices.get(_selectedPosition));
        }
    }
}
