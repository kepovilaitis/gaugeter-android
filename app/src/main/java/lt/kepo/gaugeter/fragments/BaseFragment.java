package lt.kepo.gaugeter.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import lt.kepo.gaugeter.activities.BaseActivity;

abstract class BaseFragment extends Fragment {

    void startProgress() {
        FragmentActivity activity = getActivity();

        if (activity instanceof BaseActivity){
            ((BaseActivity) activity).startProgress();
        }
    }

    void stopProgress() {
        FragmentActivity activity = getActivity();

        if (activity instanceof BaseActivity){
            ((BaseActivity) activity).stopProgress();
        }
    }
}
