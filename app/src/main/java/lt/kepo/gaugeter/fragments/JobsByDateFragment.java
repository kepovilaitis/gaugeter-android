package lt.kepo.gaugeter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.activities.MainActivity;
import lt.kepo.gaugeter.adapters.JobsListAdapter;
import lt.kepo.gaugeter.holders.JobHolder;
import lt.kepo.gaugeter.interfaces.OnItemClickListener;
import lt.kepo.gaugeter.network.BaseResponse;
import lt.kepo.gaugeter.network.HttpClient;
import lt.kepo.gaugeter.tools.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_jobs_by_date)
public class JobsByDateFragment extends BaseFragment {

    @ViewById(R.id.textStart) TextView _textStart;
    @ViewById(R.id.textEnd) TextView _textEnd;
    @ViewById(R.id.recyclerView) RecyclerView _jobsListView;
    @ViewById(R.id.btn) Button _btnGetJobs;

    private Context _context;
    private JobsListAdapter _jobsListAdapter;

    private List<JobHolder> _jobs = new ArrayList<>();
    private HttpClient _httpClient = HttpClient.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _context = getContext();
    }

    @AfterViews
    void setUpViews() {
        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null && mainActivity.isActive()) {
            mainActivity.setTitle(R.string.title_jobs_by_date);
        }

        long searchDateStart = System.currentTimeMillis() - 259200000;
        long searchDateEnd = System.currentTimeMillis();

        _textStart.setText(Utils.getFormattedDate(searchDateStart));
        _textEnd.setText(Utils.getFormattedDate(searchDateEnd));

        _httpClient.getJobsByDate(searchDateStart, searchDateEnd)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetJobsResponse());

        _jobsListView.setLayoutManager(new LinearLayoutManager(_context));
        _jobsListAdapter = new JobsListAdapter(_jobs, _context, _getJobClickListener);
        _jobsListView.setAdapter(_jobsListAdapter);
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

                CompletedJobFragment_ completedJobFragment = new CompletedJobFragment_();
                completedJobFragment.setArguments(args);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainContent, completedJobFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }

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
}
