package lt.kepo.gaugeter.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.interfaces.OnItemClickListener;

import java.util.List;

public abstract class BaseListAdapter<T> extends RecyclerView.Adapter<BaseListAdapter.ViewHolder>{
    final List<T> _list;
    final Context _context;
    final OnItemClickListener _onItemClickListener;

    int _selectedPosition = -1;
    boolean _isClickable = true;

    BaseListAdapter(List<T> list, Context context, OnItemClickListener onItemClickListener){
        _list = list;
        _context = context;
        _onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return _list.size();
    }

    void startProgress() {
        notifyItemChanged(_selectedPosition);

        _isClickable = false;
    }

    public void stopProgress() {
        _selectedPosition = -1;
        _isClickable = true;

        notifyDataSetChanged();
    }


    abstract class ViewHolder extends RecyclerView.ViewHolder {
        TextView textPrimary;
        TextView textSecondary;
        FrameLayout progressBar;

        ViewHolder(final View itemView) {
            super(itemView);

            textPrimary = itemView.findViewById(R.id.textPrimary);
            textSecondary = itemView.findViewById(R.id.textSecondary);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
