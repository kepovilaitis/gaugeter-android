package com.example.kestutis.cargauges.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.TextView;
import com.example.kestutis.cargauges.constants.Enums.CONNECTION_STATE;
import com.example.kestutis.cargauges.controllers.BluetoothController;
import com.example.kestutis.cargauges.controllers.PreferencesController;
import com.example.kestutis.cargauges.fragments.DevicesFragment;

import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.fragments.LiveDataFragment_;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {
    @Getter FloatingActionButton _fab;
    @Getter private boolean _isActive = false;
    private DrawerLayout _menuLayout;
    private ActionBarDrawerToggle _menuToggle;
    private Disposable _stateDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setUpNavigationDrawer();

        _fab = findViewById(R.id.fab);
        BluetoothController.getInstance().getPublishSubjectState().subscribe(new StateObserver());

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mainContent, new DevicesFragment());
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        _isActive = true;
    }

    @Override
    protected void onStop() {
        if (_stateDisposable != null && !_stateDisposable.isDisposed()) {
            _stateDisposable.dispose();
        }

        _isActive = false;

        super.onStop();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (_menuToggle != null) {
            _menuToggle.syncState();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (_menuToggle.isDrawerIndicatorEnabled()) {
                    menuToggle(false);
                } else {
                    //FragmentController.getInstance().backFragment(this, false);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        menuToggle(true);

        if (item.isChecked()) {
            return true;
        }

        switch (item.getItemId()){
            case R.id.menuLogout:
                new PreferencesController(getApplicationContext()).deleteSessionData();

                if (isActive()) {
                    Intent i = new Intent(this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }

                return true;
            case R.id.menuSettings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                return true;
            case R.id.menuAccountSettings:
                return true;
        }

        return false;
    }

    private void setUpNavigationDrawer() {
        _menuLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        PreferencesController preferencesController = new PreferencesController(getApplicationContext());

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.textFullName);
        navUsername.setText(preferencesController.getUserId());

        _menuToggle = new ActionBarDrawerToggle(this, _menuLayout, R.string.menu_open, R.string.menu_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        _menuLayout.addDrawerListener(_menuToggle);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private boolean menuToggle(boolean close) {
        if (_menuLayout.isDrawerOpen(GravityCompat.START)) {
            _menuLayout.closeDrawer(GravityCompat.START);

            return true;
        } else if (!close) {
            _menuLayout.openDrawer(GravityCompat.START);
        }

        return false;
    }

    @NoArgsConstructor
    private class StateObserver implements Observer<CONNECTION_STATE> {
        @Override
        public void onSubscribe(Disposable d) {
            _stateDisposable = d;
        }

        @Override
        public void onNext(CONNECTION_STATE connection_state) {
            switch (connection_state) {
                case HAS_DISCONNECTED:
                    _fab.setImageDrawable(getDrawable(R.drawable.ic_refresh));
                    _fab.show();

                    break;
                case IS_CONNECTING:
                    _fab.setImageDrawable(getDrawable(R.drawable.cancel));
                    _fab.show();

                    break;
                case HAS_CONNECTED:
                    _fab.hide();

                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.mainContent, new LiveDataFragment_());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    break;
            }
        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onError(Throwable e) {
        }
    }
}
