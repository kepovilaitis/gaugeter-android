package com.example.kestutis.cargauges.network.responses;

import android.view.View;
import com.example.kestutis.cargauges.activities.LoginActivity;
import com.example.kestutis.cargauges.constants.Constants;
import com.example.kestutis.cargauges.controllers.PreferencesController;
import com.example.kestutis.cargauges.holders.UserInfoHolder;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginResponse implements SingleObserver<UserInfoHolder> {
    private LoginActivity _activity;

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onSuccess(UserInfoHolder userInfoHolder) {
        _activity.getProgressBar().setVisibility(View.GONE);

        PreferencesController _preferencesController = new PreferencesController(_activity);

        _preferencesController.setEditorValue(Constants.LOGGED_IN, true);
        _preferencesController.setEditorValue(Constants.USER_ID, userInfoHolder.getUsername());
        _preferencesController.setEditorValue(Constants.TOKEN, userInfoHolder.getToken());

        _activity.startMainActivity();
    }

    @Override public void onError(Throwable e) {
        e.printStackTrace();
    }
}
