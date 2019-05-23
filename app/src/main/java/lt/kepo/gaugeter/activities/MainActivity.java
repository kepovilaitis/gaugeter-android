package lt.kepo.gaugeter.activities;

import android.Manifest.permission;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.constants.Constants;
import lt.kepo.gaugeter.controllers.BluetoothController;
import lt.kepo.gaugeter.controllers.PreferencesController;
import lt.kepo.gaugeter.fragments.EditAccountFragment;
import lt.kepo.gaugeter.fragments.DevicesFragment;
import lt.kepo.gaugeter.fragments.JobsByDateFragment;
import lt.kepo.gaugeter.network.HttpClient;

import lombok.Getter;

public class MainActivity extends BaseActivity implements OnNavigationItemSelectedListener {
    @Getter private boolean _isActive = false;

    @Getter FloatingActionButton _fab;

    private DrawerLayout _menuLayout;
    private ActionBarDrawerToggle _menuToggle;
    private BluetoothController _bluetoothController = BluetoothController.getInstance();
    private PreferencesController _preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        _fab = findViewById(R.id.fab);
        _progressBar = findViewById(R.id.progressBar);
        _preferences = new PreferencesController(getApplicationContext());

        setUpNavigationDrawer();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mainContent, new DevicesFragment());
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        _isActive = true;

        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ permission.ACCESS_COARSE_LOCATION}, Constants.PERMISSION_COARSE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ permission.BLUETOOTH_ADMIN}, Constants.PERMISSION_BLUETOOTH_ADMIN);
        }

        if (!_bluetoothController.isBluetoothOn()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }
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

        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (item.getItemId()){
            case R.id.menuDevices:

                if (fragmentManager != null) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.mainContent, new DevicesFragment());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }

                break;

            case R.id.menuJobsByDate:

                if (fragmentManager != null) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.mainContent, new JobsByDateFragment());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }

                break;
            case R.id.menuLogout:

                _preferences.deleteSessionData();
                HttpClient.getInstance().logout();

                if (isActive()) {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }

                return true;
            case R.id.menuAccountSettings:

                if (fragmentManager != null) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.mainContent, new EditAccountFragment());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }

                return true;
        }

        return false;
    }

    private void setUpNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView
                .getHeaderView(0)
                .<TextView>findViewById(R.id.textUserId)
                .setText(_preferences.getUserId());

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        _menuLayout = findViewById(R.id.drawerLayout);
        _menuToggle = new ActionBarDrawerToggle(this, _menuLayout, R.string.menu_open, R.string.menu_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        _menuLayout.addDrawerListener(_menuToggle);
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
}
