package com.example.kestutis.cargauges;

import android.app.Application;
import android.content.Context;

import com.example.kestutis.cargauges.controllers.BluetoothController;

public class CarGauges extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        BluetoothController.setInstance(base);

    }
}