package com.example.kestutis.cargauges.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.controllers.PreferenceController;

public class LoginActivity extends AppCompatActivity {
    private EditText _username;
    private EditText _password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        PreferenceController preferenceController = new PreferenceController(getApplicationContext());

        if (preferenceController.getIsLoggedIn()) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtras(getIntent());
            startActivity(intent);
        } else {

        }

        _username = findViewById(R.id.editUsername);
        _password = findViewById(R.id.editPassword);
        findViewById(R.id.btnSubmit).setOnClickListener(new LoginClickListener());
    }

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

        }
    }
}
