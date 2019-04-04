package com.example.kestutis.cargauges.network.responses;

import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.activities.LoginActivity;
import com.example.kestutis.cargauges.constants.Constants;
import com.example.kestutis.cargauges.controllers.PreferencesController;
import com.example.kestutis.cargauges.holders.UserInfoHolder;
import com.example.kestutis.cargauges.tools.SnackbarNotifier;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import lombok.AllArgsConstructor;
import retrofit2.HttpException;

@AllArgsConstructor
public class LoginResponse implements SingleObserver<UserInfoHolder> {
    private LoginActivity _activity;

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onSuccess(UserInfoHolder userInfoHolder) {
        _activity.stopProgressBar();

        PreferencesController _preferencesController = new PreferencesController(_activity);

        _preferencesController.setEditorValue(Constants.LOGGED_IN, true);
        _preferencesController.setEditorValue(Constants.USER_ID, userInfoHolder.getUsername());
        _preferencesController.setEditorValue(Constants.TOKEN, userInfoHolder.getToken());

        _activity.startMainActivity();
    }

    @Override public void onError(Throwable e) {
        _activity.stopProgressBar();

        if (((HttpException)e).code() == 400) {
            SnackbarNotifier.showMessage(_activity.findViewById(R.id.mainContent), R.string.error_bad_credentials);
        } else {
            SnackbarNotifier.showMessage(_activity.findViewById(R.id.mainContent), R.string.error_internal_server);
        }
    }
}
