package com.example.kestutis.cargauges;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        BluetoothController.setInstance();
    }
}
