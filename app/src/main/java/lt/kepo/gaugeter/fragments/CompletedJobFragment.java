package lt.kepo.gaugeter.fragments;

import android.os.Bundle;
import lt.kepo.gaugeter.R;

import lt.kepo.gaugeter.activities.MainActivity;
import lt.kepo.gaugeter.holders.JobHolder;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_completed_job)
public class CompletedJobFragment extends BaseFragment {
    private JobHolder _job = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args != null){
            _job = (JobHolder) args.getSerializable(JobHolder.class.getSimpleName());
        }
    }

    @AfterViews
    void setUpViews() {
        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null && mainActivity.isActive()) {
            mainActivity.setTitle(R.string.title_completed_jobs);
        }
    }
}
