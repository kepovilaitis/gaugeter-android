package fragments;

import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentTransaction;
import com.example.kestutis.cargauges.R;

import holders.RealTimeDataHolder;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import constants.PreferenceKeys;
import controllers.BluetoothController;
import controllers.PreferenceController;
import interfaces.InputDataUpdateListener;
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

        BluetoothController.getInstance().setDataUpdateListener(_inputDataListener);

        _chargeGaugeCard.setUnits(R.string.volts);
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
    public void onStop() {
        super.onStop();

            BluetoothController.getInstance().closeSocket();
    }

    private InputDataUpdateListener _inputDataListener = new InputDataUpdateListener() {
        @Override
        public void update(RealTimeDataHolder data) {
            if(isVisible()) {
                _oilPressureGaugeCard.setValue(data.getOilPressure());
                _oilTempGaugeCard.setValue(data.getOilTemperature());
                _waterTempGaugeCard.setValue(data.getWaterTemperature());
                _chargeGaugeCard.setValue(data.getCharge());
            }
        }

        @Override
        public void connectionWasLost() {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        }
    };
}
