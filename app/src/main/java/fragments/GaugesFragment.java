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
    private GaugeView _gaugeView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_gauges, container, false);

        _gaugeView = main.findViewById(R.id.oil_gauge);
        mTimer.start();

        return main;
    }

    private final CountDownTimer mTimer = new CountDownTimer(30000, 1000) {

        @Override
        public void onTick(final long millisUntilFinished) {
            _gaugeView.setTargetValue(new Random().nextInt(101));
        }

        @Override
        public void onFinish() {}
    };
}
