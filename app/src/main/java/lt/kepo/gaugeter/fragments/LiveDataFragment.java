package lt.kepo.gaugeter.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.View.OnClickListener;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.activities.MainActivity;
import lt.kepo.gaugeter.constants.Enums.CONNECTION_STATUS;
import lt.kepo.gaugeter.holders.DeviceInfoHolder;
import lt.kepo.gaugeter.holders.LiveDataHolder;
import lt.kepo.gaugeter.constants.PreferenceKeys;
import lt.kepo.gaugeter.controllers.BluetoothController;
import lt.kepo.gaugeter.controllers.PreferencesController;

import lt.kepo.gaugeter.views.GaugeCardView_;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_gauges)
public class LiveDataFragment extends BaseFragment {

    @ViewById(R.id.gaugeOilTemp) GaugeCardView_ _oilTempGaugeCard;
    @ViewById(R.id.gaugeOilPressure) GaugeCardView_ _oilPressureGaugeCard;
    @ViewById(R.id.gaugeWaterTemp) GaugeCardView_ _waterTempGaugeCard;
    @ViewById(R.id.gaugeCharge) GaugeCardView_ _chargeGaugeCard;

    private BluetoothController _bluetoothController;
    private PreferencesController _preferences;
    private Disposable _liveDataDisposable;
    private Disposable _statusDisposable;
    private DeviceInfoHolder _device;
    private FloatingActionButton _fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _bluetoothController = BluetoothController.getInstance();

        _bluetoothController.getLiveDataSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_liveDataObserver);

        _bluetoothController.getStateSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_statusObserver);

        _device = _bluetoothController.getDevice();
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
    }

    @AfterViews
    void setUpViews(){

        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null && mainActivity.isActive() ){
            mainActivity.setTitle(_device.getName());

            _fab = mainActivity.getFab();
            _fab.setOnClickListener(_fabClickListener);
        }

        _preferences = new PreferencesController(getContext());

        _oilPressureGaugeCard.setText(R.string.text_oil_pressure);
        _oilTempGaugeCard.setText(R.string.text_oil_temp);
        _waterTempGaugeCard.setText(R.string.text_water_temp);
        _chargeGaugeCard.setText(R.string.text_charge);
    }

    @Override
    public void onDestroy() {
//        if (_liveDataDisposable != null && !_liveDataDisposable.isDisposed()) {
//            _liveDataDisposable.dispose();
//        }

        _liveDataObserver.onComplete();
        _statusObserver.onComplete();

//        if (_statusDisposable != null && !_statusDisposable.isDisposed()) {
//            _statusDisposable.dispose();
//        }

        _bluetoothController.getLiveDataThread().cancel();

        super.onDestroy();
    }

    private void update(LiveDataHolder data) {
        if (isAdded()) {
            _oilPressureGaugeCard.setValue(data.getOilPressure());
            _oilTempGaugeCard.setValue(data.getOilPressure());
            _waterTempGaugeCard.setValue(data.getWaterTemperature());
            _chargeGaugeCard.setValue(data.getCharge());
        }
    }

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

    private Observer<CONNECTION_STATUS> _statusObserver = new Observer<CONNECTION_STATUS>() {
        @Override
        public void onSubscribe(Disposable d) {
            _statusDisposable = d;
        }

        @Override
        public void onNext(CONNECTION_STATUS connection_status) {
            switch (connection_status) {
                case DISCONNECTED:
                    _fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_refresh, null));
                    _fab.show();
                    break;
                case CONNECTING:
                    startProgress();
                    _fab.hide();
                    break;
                case CONNECTED:
                    stopProgress();
                    break;
            }
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
        }
    };

    private OnClickListener _fabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startProgress();
            _fab.hide();
            _bluetoothController.reconnectToDevice();
        }
    };
}
