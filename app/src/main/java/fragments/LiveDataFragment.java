package fragments;

import android.os.CountDownTimer;
import android.support.v4.app.Fragment;

import com.example.kestutis.cargauges.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Random;

import constants.PreferenceKeys;
import controllers.PreferenceController;
import views.GaugeCardView;

@EFragment(R.layout.fragment_gauges)
public class LiveDataFragment extends Fragment {

    @ViewById(R.id.oil_temp_gauge) GaugeCardView _oilTempGaugeCard;
    @ViewById(R.id.oil_pressure_gauge) GaugeCardView _oilPressureGaugeCard;
    @ViewById(R.id.water_temp_gauge) GaugeCardView _waterTempGaugeCard;
    @ViewById(R.id.charge_gauge) GaugeCardView _chargeGaugeCard;

    private PreferenceController _preferences;

    @AfterViews
    void setUpViews(){
        getActivity().setTitle(getResources().getString(R.string.live_data));

        _preferences = new PreferenceController(getContext());

        _oilPressureGaugeCard.setText(R.string.text_oil_pressure);
        _oilTempGaugeCard.setText(R.string.text_oil_temp);
        _waterTempGaugeCard.setText(R.string.text_water_temp);
        _chargeGaugeCard.setText(R.string.text_charge);

        _chargeGaugeCard.setUnits(R.string.volts);

        mTimer.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        switch (_preferences.getEditorValue(PreferenceKeys.PREFERENCE_MEASUREMENT_SYSTEM, "Metric")){
            case "Metric":
                _oilPressureGaugeCard.setUnits(R.string.bar);
                _oilTempGaugeCard.setUnits(R.string.degrees_celsius);
                _waterTempGaugeCard.setUnits(R.string.degrees_celsius);

                break;
            case "Imperial":
                _oilPressureGaugeCard.setUnits(R.string.psi);
                _oilTempGaugeCard.setUnits(R.string.degrees_fahrenheit);
                _waterTempGaugeCard.setUnits(R.string.degrees_fahrenheit    );

                break;
        }
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
