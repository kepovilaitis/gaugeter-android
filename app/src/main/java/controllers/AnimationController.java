package controllers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.kestutis.cargauges.R;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AnimationController {
    @Getter private static AnimationController _instance;

    public static void setInstance() {
        _instance = new AnimationController();
    }

    public Animation getAnimation(Context context, int animation) {
        Animation rotate = AnimationUtils.loadAnimation(context, animation);
        rotate.setFillAfter(true);
        return rotate;
    }

    public void setFAB(Intent intent, FloatingActionButton fab, Context context) {
        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    Log.d("ACTION", "BLA State changed");

                    setFAB(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR), fab, context);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    fab.setImageResource(R.drawable.ic_radar);
                    fab.startAnimation(getAnimation(context, R.anim.rotate));
                    Log.d("ACTION", "BLA Discovery started");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    fab.setImageResource(R.drawable.ic_bluetooth_white_48dp);
                    fab.startAnimation(getAnimation(context, R.anim.stay_still));
                    Log.d("ACTION", " BLA Discovery finished");
                    break;
            }
        }
    }

    private void setFAB(int state, FloatingActionButton fab, Context context) {
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                Log.d("STATE","BLA Off");

                fab.setImageResource(R.drawable.ic_bluetooth_off_white_48dp);
                fab.startAnimation(getAnimation(context, R.anim.stay_still));
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Log.d("STATE","BLA Turning off");

                fab.setImageResource(R.drawable.ic_bluetooth_off_white_48dp);
                fab.startAnimation(getAnimation(context, R.anim.rotate));
                break;
            case BluetoothAdapter.STATE_ON:
                Log.d("STATE","BLA On");

                fab.setImageResource(R.drawable.ic_radar);
                fab.startAnimation(getAnimation(context, R.anim.stay_still));
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                Log.d("STATE","BLA Turning on");

                fab.setImageResource(R.drawable.ic_bluetooth_white_48dp);
                fab.startAnimation(getAnimation(context, R.anim.rotate));
                break;
            case BluetoothAdapter.STATE_CONNECTING:
                Log.d("STATE","BLA Connecting");

                fab.setImageResource(R.drawable.ic_bluetooth_connect_white_48dp);
                fab.startAnimation(getAnimation(context, R.anim.rotate));
                break;
            case BluetoothAdapter.STATE_CONNECTED:
                Log.d("STATE","BLA Connected");

                fab.setImageResource(R.drawable.ic_bluetooth_connect_white_48dp);
                fab.startAnimation(getAnimation(context, R.anim.stay_still));
                break;
            case BluetoothAdapter.STATE_DISCONNECTING:
                Log.d("STATE","BLA Disconnecting");

                fab.setImageResource(R.drawable.ic_bluetooth_off_white_48dp);
                fab.startAnimation(getAnimation(context, R.anim.rotate));
                break;
            case BluetoothAdapter.STATE_DISCONNECTED:
                Log.d("STATE","BLA Disconnected");

                fab.setImageResource(R.drawable.ic_bluetooth_off_white_48dp);
                fab.startAnimation(getAnimation(context, R.anim.stay_still));
                break;
        }
    }
}
