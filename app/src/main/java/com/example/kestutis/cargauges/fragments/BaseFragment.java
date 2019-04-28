package com.example.kestutis.cargauges.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.example.kestutis.cargauges.activities.BaseActivity;

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
