package com.example.kestutis.cargauges.network.responses;

import android.util.Log;
import android.view.View;
import com.example.kestutis.cargauges.activities.LoginActivity;
import com.example.kestutis.cargauges.constants.Constants;
import com.example.kestutis.cargauges.controllers.PreferenceController;
import com.example.kestutis.cargauges.holders.UserInfoHolder;
import lombok.AllArgsConstructor;
import rx.Observer;

import static android.content.ContentValues.TAG;

@AllArgsConstructor
public class LoginResponse implements Observer<UserInfoHolder> {
    private LoginActivity _activity;

    @Override
    public void onNext(UserInfoHolder userInfoHolder) {
        Log.d(TAG, "In onNext()");
        _activity.getProgressBar().setVisibility(View.GONE);

        PreferenceController _preferenceController = new PreferenceController(_activity);

        _preferenceController.setEditorValue(Constants.LOGGED_IN, true);
        _preferenceController.setEditorValue(Constants.USER_ID, userInfoHolder.getUsername());
        _preferenceController.setEditorValue(Constants.TOKEN, userInfoHolder.getToken());

        _activity.startMainActivity();
    }

    @Override
    public void onCompleted() {
        Log.d(TAG, "In onCompleted()");

    }

    @Override public void onError(Throwable e) {
        e.printStackTrace();
        Log.d(TAG, "In onError()");
    }
}
