package application;

import android.app.Application;
import android.content.Context;

import controllers.BluetoothController;

public class CarGauges extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        BluetoothController.setInstance(base);
    }
}