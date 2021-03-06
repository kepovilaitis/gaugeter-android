package lt.kepo.gaugeter;

import android.app.Application;
import android.content.Context;

import lt.kepo.gaugeter.controllers.BluetoothController;
import lt.kepo.gaugeter.network.HttpClient;

public class Gaugeter extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        BluetoothController.setInstance();
        HttpClient.setInstance();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}