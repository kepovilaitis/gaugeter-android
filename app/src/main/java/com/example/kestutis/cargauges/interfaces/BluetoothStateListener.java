package com.example.kestutis.cargauges.interfaces;

import android.content.Intent;

public interface BluetoothStateListener {
    void setFAB(Intent intent);
    void setFoundDevices();
    void setDevice();
}
