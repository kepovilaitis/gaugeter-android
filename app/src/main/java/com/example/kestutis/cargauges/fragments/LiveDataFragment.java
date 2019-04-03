package com.example.kestutis.cargauges.fragments;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.kestutis.cargauges.R;
import com.example.kestutis.cargauges.activities.MainActivity;
import com.example.kestutis.cargauges.holders.LiveDataHolder;
import com.example.kestutis.cargauges.constants.PreferenceKeys;
import com.example.kestutis.cargauges.controllers.BluetoothController;
import com.example.kestutis.cargauges.controllers.PreferencesController;

import com.example.kestutis.cargauges.views.GaugeCardView_;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_gauges)
public class LiveDataFragment extends Fragment {

    @ViewById(R.id.gaugeOilTemp) GaugeCardView_ _oilTempGaugeCard;
    @ViewById(R.id.gaugeOilPressure) GaugeCardView_ _oilPressureGaugeCard;
    @ViewById(R.id.gaugeWaterTemp) GaugeCardView_ _waterTempGaugeCard;
    @ViewById(R.id.gaugeCharge) GaugeCardView_ _chargeGaugeCard;

    private BluetoothController _bluetoothController;
    private PreferencesController _preferences;
    private Disposable _liveDataDisposable;

    @Override
    public void onStart() {
        super.onStart();

        _bluetoothController = BluetoothController.getInstance();

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

        BluetoothController.getInstance().getPublishSubjectLiveData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_liveDataObserver);
    }

    @AfterViews
    void setUpViews(){
        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null){
            mainActivity.setTitle(getString(R.string.live_data));
            mainActivity.getFab().setOnClickListener(_onClickListener);
        }

        _preferences = new PreferencesController(getContext());

        _oilPressureGaugeCard.setText(R.string.text_oil_pressure);
        _oilTempGaugeCard.setText(R.string.text_oil_temp);
        _waterTempGaugeCard.setText(R.string.text_water_temp);
        _chargeGaugeCard.setText(R.string.text_charge);
    }

    @Override
    public void onStop() {
        if (_liveDataDisposable != null && !_liveDataDisposable.isDisposed()) {
            _liveDataDisposable.dispose();
        }

        _bluetoothController.getLiveDataThread().cancel();

        super.onStop();
    }

    private void update(LiveDataHolder data) {
        _oilPressureGaugeCard.setValue(data.getOilPressure());
        _oilTempGaugeCard.setValue(data.getOilPressure());
        _waterTempGaugeCard.setValue(data.getWaterTemperature());
        _chargeGaugeCard.setValue(data.getCharge());
    }

    private OnClickListener _onClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            _bluetoothController.reconnectToDevice();
        }
    };

    private Observer<LiveDataHolder> _liveDataObserver = new Observer<LiveDataHolder>() {
        @Override
        public void onSubscribe(Disposable d) {
            _liveDataDisposable = d;
        }

        @Override
        public void onNext(LiveDataHolder liveDataHolder) {
            update(liveDataHolder);
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }
    };
}
