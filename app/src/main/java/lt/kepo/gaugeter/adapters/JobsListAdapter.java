package lt.kepo.gaugeter.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import lombok.RequiredArgsConstructor;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.constants.ErrorCodes;
import lt.kepo.gaugeter.holders.JobHolder;
import lt.kepo.gaugeter.interfaces.OnItemClickListener;
import lt.kepo.gaugeter.network.HttpClient;
import lt.kepo.gaugeter.tools.ToastNotifier;
import lt.kepo.gaugeter.tools.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class JobsListAdapter extends BaseListAdapter<JobHolder> {
    public JobsListAdapter(List<JobHolder> jobs, Context context, OnItemClickListener<JobHolder> onItemClickListener){
        super(jobs, context, onItemClickListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseListAdapter.ViewHolder holder, int position) {
        JobHolder job = _list.get(position);

        holder.itemView.setSelected(_selectedPosition == position);
        holder.progressBar.setVisibility(_selectedPosition == position ? View.VISIBLE : View.GONE);
        holder.textPrimary.setText(Utils.getFormattedDate(job.getDateCreated()));
        holder.textSecondary.setText(job.getDevice().getName());
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
                            .setTitle(R.string.title_remove_job)
                            .setPositiveButton(R.string.yes, new DialogPositiveClickListener(_selectedPosition))
                            .setNegativeButton(R.string.no, null)
                            .setMessage(R.string.dialog_remove_job)
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
            final int jobId = _list.get(_selectedPos).getId();

            HttpClient.getInstance()
                    .deleteJob(jobId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            stopProgress();

                            if (response.isSuccessful() && response.code() != ErrorCodes.HTTP_NO_CONTENT) {
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