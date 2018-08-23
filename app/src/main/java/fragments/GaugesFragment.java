package fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kestutis.cargauges.R;

import java.util.Random;

import views.GaugeView;

public class GaugesFragment extends Fragment {
    private GaugeView _oilGaugeView;
    private GaugeView _chargeGaugeView;
    private GaugeView _waterTempGaugeView;
    private GaugeView _oilTempGaugeView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_gauges, container, false);

        _oilGaugeView = main.findViewById(R.id.oil_gauge);
        _chargeGaugeView = main.findViewById(R.id.charge_gauge);
        _waterTempGaugeView = main.findViewById(R.id.water_temp_gauge);
        _oilTempGaugeView = main.findViewById(R.id.oil_temp_gauge);
        mTimer.start();

        return main;
    }

    private final CountDownTimer mTimer = new CountDownTimer(60000, 1500) {
        @Override
        public void onTick(final long millisUntilFinished) {
            _oilGaugeView.setValue(new Random().nextInt(100)*2);
            _chargeGaugeView.setValue(new Random().nextInt(100)*2);
            _waterTempGaugeView.setValue(new Random().nextInt(100)*2);
            _oilTempGaugeView.setValue(new Random().nextInt(100)*2);
        }

        @Override
        public void onFinish() {}
    };
}
