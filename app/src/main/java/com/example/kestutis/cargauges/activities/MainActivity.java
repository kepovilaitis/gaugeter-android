package com.example.kestutis.cargauges.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.kestutis.cargauges.controllers.BluetoothController;
import com.example.kestutis.cargauges.fragments.DevicesFragment;

import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.fragments.LiveDataFragment_;
import com.example.kestutis.cargauges.interfaces.SocketConnectionListener;
import lombok.Getter;

public class MainActivity extends AppCompatActivity {
    @Getter FloatingActionButton _fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothController.getInstance().setSocketConnectionListener(_socketConnectionListener );
        _fab = findViewById(R.id.fab);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mainContent, new DevicesFragment());
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSettings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.actionAccountSettings:
                return true;
        }

        return super.onOptionsItemSelected(item);
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
