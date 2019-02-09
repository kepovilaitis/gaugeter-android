package com.example.kestutis.cargauges.interfaces;

import android.bluetooth.BluetoothDevice;

public interface SocketConnectionListener {
    void hasConnected(BluetoothDevice device);
    void isConnecting();
    void hasDisconnected();
}
