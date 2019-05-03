package lt.kepo.gaugeter.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.holders.DeviceHolder;
import lt.kepo.gaugeter.interfaces.OnItemClickListener;

import java.util.List;

public class FoundDevicesAdapter extends BaseListAdapter<DeviceHolder> {

    public FoundDevicesAdapter(List<DeviceHolder> jobs, Context context, OnItemClickListener<DeviceHolder> onItemClickListener){
        super(jobs, context, onItemClickListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseListAdapter.ViewHolder viewHolder, int position) {
        DeviceHolder device = _list.get(position);

        viewHolder.itemView.setSelected(_selectedPosition == position);
        viewHolder.progressBar.setVisibility(_selectedPosition == position ? View.VISIBLE : View.GONE);
        viewHolder.textPrimary.setText(device.getName());
        viewHolder.textSecondary.setText(device.getBluetoothAddress());
    }

    @Override
    public int getItemCount() {
        return _list.size();
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

                    new AlertDialog.Builder(_context)
                            .setTitle(R.string.title_add_device)
                            .setPositiveButton(R.string.yes, new DialogPositiveClickListener())
                            .setNegativeButton(R.string.no, null)
                            .setMessage(_context.getResources().getString(R.string.dialog_add_device, _list.get(_selectedPosition).getName()))
                            .create()
                            .show();
                }
            });
        }
    }

    private class DialogPositiveClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            _onItemClickListener.execute(_list.get(_selectedPosition));
        }
    }
}
