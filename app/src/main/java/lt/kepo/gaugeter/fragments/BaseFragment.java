package lt.kepo.gaugeter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import lt.kepo.gaugeter.activities.BaseActivity;
import lt.kepo.gaugeter.activities.MainActivity;
import lt.kepo.gaugeter.network.HttpClient;
import lt.kepo.gaugeter.tools.Utils;

public abstract class BaseFragment extends Fragment {

    MainActivity _mainActivity;
    FloatingActionButton _fab;
    Context _context;
    HttpClient _httpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _context = getContext();
        _mainActivity = (MainActivity) getActivity();
        _httpClient = HttpClient.getInstance();
    }

    @Override
    public void onDestroyView(){
        Utils.hideKeyboard(_mainActivity);

        if(_fab != null){
            _fab.hide();
            _fab.setOnClickListener(null);
        }

        super.onDestroyView();
    }

    void setTitle(String title) {
        if (_mainActivity != null) {
            _mainActivity.setTitle(title);
        }
    }

    void setTitle(int titleResId) {
        if (_mainActivity != null) {
            _mainActivity.setTitle(titleResId);
        }
    }

    FloatingActionButton getFab(){
        if (_mainActivity != null) {
            return _mainActivity.getFab();
        }

        return null;
    }

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
