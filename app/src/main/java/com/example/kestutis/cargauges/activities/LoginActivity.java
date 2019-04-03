package com.example.kestutis.cargauges.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.controllers.PreferencesController;
import com.example.kestutis.cargauges.helpers.KeyboardHelper;
import com.example.kestutis.cargauges.holders.LoginHolder;
import com.example.kestutis.cargauges.network.GaugeterClient;

import com.example.kestutis.cargauges.network.responses.LoginResponse;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;

public class LoginActivity extends AppCompatActivity {
    private EditText _userId;
    private EditText _password;
    @Getter private FrameLayout _progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        PreferencesController preferencesController = new PreferencesController(getApplicationContext());

        if (preferencesController.getIsLoggedIn()) {
            startMainActivity();
        } else {
            _progressBar = findViewById(R.id.progressBar);
            _userId = findViewById(R.id.editUsername);
            _password = findViewById(R.id.editPassword);

            findViewById(R.id.btnSubmit).setOnClickListener(_loginClickListener);
        }
    }

    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(getIntent());
        startActivity(intent);
    }

    private void login(LoginHolder loginHolder) {
        KeyboardHelper.hideKeyboard(this);

        _progressBar.setVisibility(View.VISIBLE);

        GaugeterClient.getInstance()
                .login(loginHolder)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoginResponse(this));
    }

    private OnClickListener _loginClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            login(new LoginHolder(_userId.getText().toString(), _password.getText().toString()));
        }
    };
}
