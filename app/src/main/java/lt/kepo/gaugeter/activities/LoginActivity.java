package lt.kepo.gaugeter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.constants.Constants;
import lt.kepo.gaugeter.controllers.PreferencesController;
import lt.kepo.gaugeter.helpers.KeyboardHelper;
import lt.kepo.gaugeter.holders.LoginHolder;
import lt.kepo.gaugeter.holders.UserInfoHolder;
import lt.kepo.gaugeter.network.BaseResponse;
import lt.kepo.gaugeter.network.GaugeterClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends BaseActivity {
    private EditText _userId;
    private EditText _password;
    private PreferencesController _preferencesController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _preferencesController = new PreferencesController(getApplicationContext());

        if (_preferencesController.getIsLoggedIn()) {
            GaugeterClient.getInstance().setUserToken(_preferencesController.getUserToken());
            startMainActivity();
        } else {
            setContentView(R.layout.activity_login);

            _progressBar = findViewById(R.id.progressBar);
            _userId = findViewById(R.id.editUsername);
            _password = findViewById(R.id.editPassword);

            findViewById(R.id.btnSubmit).setOnClickListener(_loginClickListener);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(getIntent());
        startActivity(intent);
    }

    private void login(LoginHolder loginHolder) {
        KeyboardHelper.hideKeyboard(this);

        GaugeterClient.getInstance()
                .login(loginHolder)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoginResponse());
    }

    private OnClickListener _loginClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            LoginHolder loginHolder = new LoginHolder();
            UserInfoHolder user = new UserInfoHolder();

            user.setUserId(_userId.getText().toString());
            user.setPassword(_password.getText().toString());

            loginHolder.setUser(user);
            login(loginHolder);
        }
    };

    private class LoginResponse extends BaseResponse<LoginHolder> {
        LoginResponse() {
            super(LoginActivity.this);
        }

        @Override
        public void onSubscribe(Disposable d) {
            startProgress();
        }

        @Override
        public void onSuccess(LoginHolder loginHolder) {
            _preferencesController.setEditorValue(Constants.LOGGED_IN, true);
            _preferencesController.setEditorValue(Constants.USER_ID, loginHolder.getUser().getUserId());
            _preferencesController.setEditorValue(Constants.USER_TOKEN, loginHolder.getToken());

            GaugeterClient.getInstance().setUserToken(loginHolder.getToken());

            stopProgress();
            startMainActivity();
        }
    }
}
