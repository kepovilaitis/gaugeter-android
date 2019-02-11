package com.example.kestutis.cargauges.fragments;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.activities.MainActivity;
import com.example.kestutis.cargauges.helpers.AnimationHelper;
import com.example.kestutis.cargauges.holders.RealTimeDataHolder;
import com.example.kestutis.cargauges.views.GaugeCardView_;
import com.example.kestutis.cargauges.constants.PreferenceKeys;
import com.example.kestutis.cargauges.controllers.BluetoothController;
import com.example.kestutis.cargauges.controllers.PreferenceController;
import com.example.kestutis.cargauges.interfaces.InputDataUpdateListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_gauges)
public class LiveDataFragment extends Fragment {

    @ViewById(R.id.oil_temp_gauge) GaugeCardView_ _oilTempGaugeCard;
    @ViewById(R.id.oil_pressure_gauge) GaugeCardView_ _oilPressureGaugeCard;
    @ViewById(R.id.water_temp_gauge) GaugeCardView_ _waterTempGaugeCard;
    @ViewById(R.id.charge_gauge) GaugeCardView_ _chargeGaugeCard;

    private BluetoothDevice _device;
    private BluetoothController _bluetoothController;
    private PreferenceController _preferences;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            _device = getArguments().getParcelable("device");
        }

        _bluetoothController = BluetoothController.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

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

        _chargeGaugeCard.setUnits(R.string.volts);
        _bluetoothController.setDataUpdateListener(_inputDataListener);
    }

    @AfterViews
    void setUpViews(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null){
            mainActivity.setTitle(getResources().getString(R.string.live_data));
        }

        _preferences = new PreferenceController(getContext());

        _oilPressureGaugeCard.setText(R.string.text_oil_pressure);
        _oilTempGaugeCard.setText(R.string.text_oil_temp);
        _waterTempGaugeCard.setText(R.string.text_water_temp);
        _chargeGaugeCard.setText(R.string.text_charge);

        ((MainActivity) getActivity()).getFab().setOnClickListener(_onClickListener);
    }

    @Override
    public void onStop() {


        super.onStop();
    }

    private InputDataUpdateListener _inputDataListener = new InputDataUpdateListener() {
        @Override
        public void update(RealTimeDataHolder data) {
            _oilPressureGaugeCard.setValue(data.getOilPressure());
            _oilTempGaugeCard.setValue(data.getOilPressure());
            _waterTempGaugeCard.setValue(data.getWaterTemperature());
            _chargeGaugeCard.setValue(data.getCharge());
        }
    };

    private OnClickListener _onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            AnimationHelper.rotateAround(view, 0);
            _bluetoothController.connectToDevice(_device);
        }
    };
}
