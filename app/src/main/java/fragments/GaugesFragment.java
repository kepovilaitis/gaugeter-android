package fragments;

import android.os.CountDownTimer;
import android.support.v4.app.Fragment;

import com.example.kestutis.cargauges.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Random;

import views.GaugeCardView;

@EFragment(R.layout.fragment_gauges)
public class GaugesFragment extends Fragment {

    @ViewById(R.id.oil_temp_gauge) GaugeCardView _oilTempGaugeCard;
    @ViewById(R.id.oil_pressure_gauge) GaugeCardView _oilPressureGaugeCard;
    @ViewById(R.id.water_temp_gauge) GaugeCardView _waterTempGaugeCard;
    @ViewById(R.id.charge_gauge) GaugeCardView _chargeGaugeCard;

    @AfterViews
    void setUpViews(){
        _oilPressureGaugeCard.setText(R.string.text_oil_pressure);
        _oilPressureGaugeCard.setUnits(R.string.bar);

        _oilTempGaugeCard.setText(R.string.text_oil_temp);
        _oilTempGaugeCard.setUnits(R.string.degrees_celcius);

        _waterTempGaugeCard.setText(R.string.text_water_temp);
        _waterTempGaugeCard.setUnits(R.string.degrees_celcius);

        _chargeGaugeCard.setText(R.string.text_charge);
        _chargeGaugeCard.setUnits(R.string.volts);

        mTimer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    private final CountDownTimer mTimer = new CountDownTimer(60000, 1500) {

        @Override
        public void onTick(final long millisUntilFinished) {
            double value1 = new Random().nextInt(100)*2;
            double value2 = new Random().nextInt(100)*2;

            _oilPressureGaugeCard.setValue(value1);
            _oilTempGaugeCard.setValue(value2);
            _waterTempGaugeCard.setValue(value1);
            _chargeGaugeCard.setValue(value2);
        }

        @Override
        public void onFinish() {}
    };
}
