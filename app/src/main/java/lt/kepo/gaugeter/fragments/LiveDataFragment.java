package lt.kepo.gaugeter.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.activities.MainActivity;
import lt.kepo.gaugeter.constants.Enums.CONNECTION_STATUS;
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
public class LiveDataFragment extends Fragment {

    @ViewById(R.id.gaugeOilTemp) GaugeCardView_ _oilTempGaugeCard;
    @ViewById(R.id.gaugeOilPressure) GaugeCardView_ _oilPressureGaugeCard;
    @ViewById(R.id.gaugeWaterTemp) GaugeCardView_ _waterTempGaugeCard;
    @ViewById(R.id.gaugeCharge) GaugeCardView_ _chargeGaugeCard;

    private BluetoothController _bluetoothController;
    private PreferencesController _preferences;
    private Disposable _liveDataDisposable;
    private Disposable _statusDisposable;
    private MainActivity _mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothController.getInstance().getLiveDataSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_liveDataObserver);

        BluetoothController.getInstance().getStateSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_statusObserver);
    }

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
    }

    @AfterViews
    void setUpViews(){

        _mainActivity = (MainActivity) getActivity();

        if (_mainActivity != null){
            _mainActivity.setTitle(getString(R.string.live_data));
            _mainActivity.getFab().setOnClickListener(_onClickListener);
        }

        _preferences = new PreferencesController(getContext());

        _oilPressureGaugeCard.setText(R.string.text_oil_pressure);
        _oilTempGaugeCard.setText(R.string.text_oil_temp);
        _waterTempGaugeCard.setText(R.string.text_water_temp);
        _chargeGaugeCard.setText(R.string.text_charge);
    }

    @Override
    public void onDestroy() {
        if (_liveDataDisposable != null && !_liveDataDisposable.isDisposed()) {
            _liveDataDisposable.dispose();
        }

        if (_statusDisposable != null && !_statusDisposable.isDisposed()) {
            _statusDisposable.dispose();
        }

        _bluetoothController.getLiveDataThread().cancel();

        super.onDestroy();
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
            _mainActivity.getFab().hide();
            getActivity().getSupportFragmentManager().popBackStack();
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

    private Observer<CONNECTION_STATUS> _statusObserver = new Observer<CONNECTION_STATUS>() {
        @Override
        public void onSubscribe(Disposable d) {
            _statusDisposable = d;
        }

        @Override
        public void onNext(CONNECTION_STATUS connection_status) {
            FloatingActionButton fab = _mainActivity.getFab();

            switch (connection_status) {
                case DISCONNECTED:
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add, null));
                    fab.show();
                    break;
                case CONNECTING:
                    _mainActivity.startProgress();
                    fab.hide();
                    break;
                case CONNECTED:
                    _mainActivity.stopProgress();
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
}
