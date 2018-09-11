package fragments;

import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.*;
import android.widget.*;

import com.example.kestutis.cargauges.R;

import org.androidannotations.annotations.*;

import java.util.*;

import views.GaugeView;

@EFragment(R.layout.fragment_gauges)
public class GaugesFragment extends Fragment {

    @ViewById(R.id.oil_pressure_gauge_view) GaugeView _oilPressureGaugeView;
    @ViewById(R.id.oil_pressure_gauge_text) TextView _oilPressureGaugeText;
    @ViewById(R.id.oil_pressure_chart) LinearLayout _oilPressureGaugeChart;


    @ViewById(R.id.oil_temp_gauge_view) GaugeView _oilTempGaugeView;
    @ViewById(R.id.oil_temp_gauge_text) TextView _oilTempGaugeText;
    @ViewById(R.id.oil_temp_chart) LinearLayout _oilTempGaugeChart;

    @AfterViews
    void setUpViews(){
        mTimer.start();
    }

    @Click({R.id.oil_pressure_expand_btn, R.id.oil_temp_expand_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.oil_pressure_expand_btn:

                if (_oilPressureGaugeChart.getVisibility() != View.GONE) {
                    view.startAnimation(getRotateAnimation(45.0f, 0.0f));
                    _oilPressureGaugeChart.setVisibility(View.GONE);
                    break;
                } else {
                    view.startAnimation(getRotateAnimation(0.0f, 45.0f));
                    _oilPressureGaugeChart.setVisibility(View.VISIBLE);
                    break;
                }
            case R.id.oil_temp_expand_btn:

                if (_oilTempGaugeChart.getVisibility() != View.GONE) {
                    view.startAnimation(getRotateAnimation(45.0f, 0.0f));
                    _oilTempGaugeChart.setVisibility(View.GONE);
                    break;
                } else {
                    view.startAnimation(getRotateAnimation(0.0f, 45.0f));
                    _oilTempGaugeChart.setVisibility(View.VISIBLE);
                    break;
                }
        }
    }

    private RotateAnimation getRotateAnimation(float fromDegrees, float toDegrees){
        RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF,  0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(150);
        rotate.setFillAfter(true);

        return rotate;
    }


    private final CountDownTimer mTimer = new CountDownTimer(60000, 1500) {

        @Override
        public void onTick(final long millisUntilFinished) {
            _oilPressureGaugeView.setValue(new Random().nextInt(100)*2);
            _oilPressureGaugeText.setText(String.format(Locale.US, "%5.1f", _oilPressureGaugeView.getValue()));

            _oilTempGaugeView.setValue(new Random().nextInt(100)*2);
            _oilTempGaugeText.setText(String.format(Locale.US, "%5.1f", _oilTempGaugeView.getValue()));
        }

        @Override
        public void onFinish() {}
    };
}
