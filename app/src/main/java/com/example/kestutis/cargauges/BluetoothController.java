package com.example.kestutis.cargauges;

import android.bluetooth.BluetoothAdapter;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BluetoothController {
    @Getter private static BluetoothController _instance;

    public static void setInstance() {
        _instance = new BluetoothController();
    }

    public boolean isBluetoothOn(){
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }
}
