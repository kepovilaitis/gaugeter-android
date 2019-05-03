package lt.kepo.gaugeter.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import lt.kepo.gaugeter.activities.BaseActivity;

public abstract class BaseFragment extends Fragment {

    public void startProgress() {
        FragmentActivity activity = getActivity();

        if (activity instanceof BaseActivity){
            ((BaseActivity) activity).startProgress();
        }
    }

    public void stopProgress() {
        FragmentActivity activity = getActivity();

        if (activity instanceof BaseActivity){
            ((BaseActivity) activity).stopProgress();
        }
    }
}
