package com.example.kestutis.cargauges.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.constants.Constants;
import com.example.kestutis.cargauges.constants.Enums.MEASUREMENT_SYSTEM;
import com.example.kestutis.cargauges.controllers.PreferenceController;
import com.example.kestutis.cargauges.helpers.HttpHelper;
import com.example.kestutis.cargauges.holders.UserInfoHolder;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private EditText _userId;
    private EditText _password;
    private PreferenceController _preferenceController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _preferenceController = new PreferenceController(getApplicationContext());

        if (_preferenceController.getIsLoggedIn()) {
            startMainActivity();
        } else {
            _userId = findViewById(R.id.editUsername);
            _password = findViewById(R.id.editPassword);
            findViewById(R.id.btnSubmit).setOnClickListener(new LoginClickListener());
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(getIntent());
        startActivity(intent);
    }

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            new RetrieveFeedTask().execute();
        }
    }

    private class RetrieveFeedTask extends AsyncTask<String, Void, UserInfoHolder> {

        @Override
        protected UserInfoHolder doInBackground(String... strings) {
            UserInfoHolder answer = new UserInfoHolder(1, "a", "a",  "a", MEASUREMENT_SYSTEM.METRIC, "a", "a");
            try {
                answer = new HttpHelper().loginRequest(_userId.getText().toString(), _password.getText().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return answer;
        }

        protected void onPostExecute(UserInfoHolder answer) {
            _preferenceController.setEditorValue(Constants.LOGGED_IN, true);
            _preferenceController.setEditorValue(Constants.USER_ID, answer._username);
            _preferenceController.setEditorValue(Constants.TOKEN, answer._token);

            startMainActivity();
        }
    }
}
