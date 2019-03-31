package com.example.kestutis.cargauges.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.FragmentManager;
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
import com.example.kestutis.cargauges.controllers.BluetoothController;
import com.example.kestutis.cargauges.controllers.PreferenceController;
import com.example.kestutis.cargauges.fragments.DevicesFragment;

import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.fragments.LiveDataFragment_;
import com.example.kestutis.cargauges.interfaces.SocketConnectionListener;
import lombok.Getter;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {
    @Getter FloatingActionButton _fab;
    private DrawerLayout _menuLayout;
    private ActionBarDrawerToggle _menuToggle;
    private NavigationView _navigationView;
    @Getter private boolean _isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpNavigationDrawer();

        BluetoothController.getInstance().setSocketConnectionListener(_socketConnectionListener );
        _fab = findViewById(R.id.fab);

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
                new PreferenceController(getApplicationContext()).deleteSessionData();

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
        _navigationView = findViewById(R.id.navigationView);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        PreferenceController preferenceController = new PreferenceController(getApplicationContext());

        View headerView = _navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.textFullName);
        navUsername.setText(preferenceController.getUserId());

        _menuToggle = new ActionBarDrawerToggle(this, _menuLayout, R.string.menu_open, R.string.menu_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        _menuLayout.addDrawerListener(_menuToggle);
        _navigationView.setNavigationItemSelectedListener(this);

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

    @Getter
    private SocketConnectionListener _socketConnectionListener = new SocketConnectionListener() {
        @Override
        public void hasConnected(BluetoothDevice device) {
            _fab.hide();

            FragmentManager fragmentManager = getSupportFragmentManager();

            if (isDevicesFragment(fragmentManager)){
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putParcelable("device", device);

                LiveDataFragment_ fragment = new LiveDataFragment_();

                fragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.mainContent, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }

        @Override
        public void isConnecting() {
            if (isDevicesFragment(getSupportFragmentManager())) {
                _fab.setImageDrawable(getDrawable(R.drawable.cancel));
                _fab.show();
            }
        }

        @Override
        public void hasDisconnected() {
            _fab.setImageDrawable(getDrawable(R.drawable.ic_refresh));
            _fab.show();
        }

        private boolean isDevicesFragment(FragmentManager fragmentManager){
            return fragmentManager.findFragmentById(R.id.mainContent) instanceof DevicesFragment;
        }
    };
}
