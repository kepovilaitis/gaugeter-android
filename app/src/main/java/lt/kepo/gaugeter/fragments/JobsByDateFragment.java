package lt.kepo.gaugeter.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import lombok.AllArgsConstructor;
import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.adapters.JobsListAdapter;
import lt.kepo.gaugeter.holders.JobHolder;
import lt.kepo.gaugeter.interfaces.OnItemClickListener;
import lt.kepo.gaugeter.network.BaseResponse;
import lt.kepo.gaugeter.tools.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JobsByDateFragment extends BaseFragment {

    private JobsListAdapter _jobsListAdapter;
    private long _dateStartMillis;
    private long _dateEndMillis;

    private List<JobHolder> _jobs = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_jobs_by_date, container, false);

        setTitle(R.string.title_jobs_by_date);

        TextView textStart = main.findViewById(R.id.textStart);
        TextView textEnd = main.findViewById(R.id.textEnd);
        LinearLayout dateStartButton = main.findViewById(R.id.btnDateStart);
        LinearLayout dateEndButton = main.findViewById(R.id.btnDateEnd);
        RecyclerView jobsListView = main.findViewById(R.id.recyclerView);

        _dateStartMillis = System.currentTimeMillis() - 259200000;
        _dateEndMillis = System.currentTimeMillis();

        textStart.setText(Utils.getFormattedDate(_dateStartMillis));
        textEnd.setText(Utils.getFormattedDate(_dateEndMillis));

        dateStartButton.setOnClickListener(new StartDatePickerClickListener(textStart));
        dateEndButton.setOnClickListener(new EndDatePickerClickListener(textEnd));

        jobsListView.setLayoutManager(new LinearLayoutManager(_context));
        _jobsListAdapter = new JobsListAdapter(_jobs, _context, _getJobClickListener);
        jobsListView.setAdapter(_jobsListAdapter);

        _fab = getFab();
        _fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_search, null));
        _fab.setOnClickListener(_fabClickListener);
        _fab.show();

        executeGetJobsRequest();

        return main;
    }

    private void executeGetJobsRequest() {
        _httpClient.getJobsByDate(_dateStartMillis, _dateEndMillis)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetJobsResponse());
    }

    private OnItemClickListener _getJobClickListener = new OnItemClickListener<JobHolder>() {
        @Override
        public void execute(JobHolder job) {
            _httpClient.getJob(job.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new GetJobResponse());
        }
    };

    private OnClickListener _fabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            executeGetJobsRequest();
        }
    };

    private class GetJobsResponse extends BaseResponse<List<JobHolder>> {
        GetJobsResponse() {
            super(JobsByDateFragment.this);
        }

        @Override
        public void onSuccess(List<JobHolder> jobs) {
            super.onSuccess(jobs);

            _jobs.clear();
            _jobs.addAll(jobs);
            _jobsListAdapter.notifyDataSetChanged();
        }
    }

    private class GetJobResponse extends BaseResponse<JobHolder> {
        GetJobResponse() {
            super(JobsByDateFragment.this);
        }

        @Override
        public void onSubscribe(Disposable d) { }

        @Override
        public void onSuccess(JobHolder job) {
            super.onSuccess(job);

            FragmentManager fragmentManager = getFragmentManager();

            if (fragmentManager != null) {

                Bundle args = new Bundle();
                args.putSerializable(JobHolder.class.getSimpleName(), job);

                CompletedJobFragment completedJobFragment = new CompletedJobFragment();
                completedJobFragment.setArguments(args);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainContent, completedJobFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }

    @AllArgsConstructor
    private class StartDatePickerClickListener implements OnClickListener {
        private TextView _text;

        @Override
        public void onClick(View v) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(_dateStartMillis);

            new DatePickerDialog(
                    _context,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            calendar.set(year, monthOfYear, dayOfMonth);

                            _dateStartMillis = calendar.getTimeInMillis();
                            _text.setText(Utils.getFormattedDate(_dateStartMillis));
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        }
    }

    @AllArgsConstructor
    private class EndDatePickerClickListener implements OnClickListener {
        private TextView _text;

        @Override
        public void onClick(View v) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(_dateEndMillis);

            new DatePickerDialog(
                    _context,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            calendar.set(year, monthOfYear, dayOfMonth);

                            _dateEndMillis = calendar.getTimeInMillis();
                            _text.setText(Utils.getFormattedDate(_dateEndMillis));
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        }
    }
}
