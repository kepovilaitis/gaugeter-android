package lt.kepo.gaugeter.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.activities.MainActivity;
import lt.kepo.gaugeter.constants.Constants;
import lt.kepo.gaugeter.constants.Enums.CONNECTION_STATUS;
import lt.kepo.gaugeter.holders.DeviceHolder;
import lt.kepo.gaugeter.constants.PreferenceKeys;
import lt.kepo.gaugeter.controllers.BluetoothController;
import lt.kepo.gaugeter.controllers.PreferencesController;
import lt.kepo.gaugeter.holders.JobHolder;
import lt.kepo.gaugeter.holders.TelemDataHolder;
import lt.kepo.gaugeter.network.BaseResponse;
import lt.kepo.gaugeter.network.HttpClient;
import lt.kepo.gaugeter.tools.ToastNotifier;
import lt.kepo.gaugeter.views.GaugeCardView_;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_telem_data)
public class TelemDataFragment extends BaseFragment {

    @ViewById(R.id.gaugeOilTemp) GaugeCardView_ _oilTempGaugeCard;
    @ViewById(R.id.gaugeOilPressure) GaugeCardView_ _oilPressureGaugeCard;
    @ViewById(R.id.gaugeWaterTemp) GaugeCardView_ _waterTempGaugeCard;
    @ViewById(R.id.gaugeCharge) GaugeCardView_ _chargeGaugeCard;

    private BluetoothController _bluetoothController;
    private HttpClient _httpClient;
    private Disposable _telemDataDisposable;
    private Disposable _statusDisposable;
    private DeviceHolder _device;
    private FloatingActionButton _fab;
    private Context _context;
    private JobHolder _job;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        _bluetoothController = BluetoothController.getInstance();
        _httpClient = HttpClient.getInstance();
        _device = _bluetoothController.getDevice();
        _context = getContext();
        _job = new JobHolder(_device);

        _bluetoothController.getTelemDataSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_telemDataObserver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.live_data_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @AfterViews
    void setUpViews(){
        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null && mainActivity.isActive() ){
            mainActivity.setTitle(_device.getName());

            _fab = mainActivity.getFab();
            _fab.setOnClickListener(_fabClickListener);
        }

        PreferencesController preferences = new PreferencesController(_context);

        _oilPressureGaugeCard.setText(R.string.text_oil_pressure);
        _oilTempGaugeCard.setText(R.string.text_oil_temp);
        _waterTempGaugeCard.setText(R.string.text_water_temp);
        _chargeGaugeCard.setText(R.string.text_charge);

        switch (preferences.getEditorValue(PreferenceKeys.PREFERENCE_MEASUREMENT_SYSTEM, "Metric")){
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

    @Override
    public void onStart() {
        super.onStart();

        if (!_bluetoothController.getLiveDataThread().isAlive()){
            _fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_refresh, null));
            _fab.show();
        }

        _bluetoothController.getStateSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_statusObserver);
    }

    @Override
    public void onStop() {
        if (_statusDisposable != null && !_statusDisposable.isDisposed()) {
            _statusDisposable.dispose();
        }

        _fab.hide();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (_telemDataDisposable != null && !_telemDataDisposable.isDisposed()) {
            _telemDataDisposable.dispose();
        }

        if (_job.getState() != JobHolder.FINISHED) {
            finishJob();
        }

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuDeviceSettings:
                // Action goes here
                return true;
            case R.id.menuComplete:

                new AlertDialog.Builder(_context)
                        .setTitle(R.string.title_remove_device)
                        .setPositiveButton(R.string.yes, _completeWorkDialogClickListener)
                        .setNegativeButton(R.string.no, null)
                        .setMessage(R.string.dialog_remove_device)
                        .create()
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateLiveData(TelemDataHolder data) {
        if (isAdded()) {
            _oilPressureGaugeCard.setValue(data.getOilPressure());
            _oilTempGaugeCard.setValue(data.getOilPressure());
            _waterTempGaugeCard.setValue(data.getWaterTemperature());
            _chargeGaugeCard.setValue(data.getCharge());
        }
    }

    private void updateJob() {
        _job.setDateUpdated(System.currentTimeMillis());

        _httpClient
                .upsertJob(_job)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UpsertJobResponse());
    }

    private void finishJob() {
        _job.setState(JobHolder.FINISHED);

        if (_statusDisposable != null && !_statusDisposable.isDisposed()) {
            _statusDisposable.dispose();
        }

        _bluetoothController.getLiveDataThread().cancel();

        updateJob();
    }

    private void showCompletedJobFragment() {
        if (_job.getState() == JobHolder.FINISHED) {

            FragmentManager fragmentManager = getFragmentManager();

            if (fragmentManager != null) {

                Bundle args = new Bundle();
                args.putSerializable(JobHolder.class.getSimpleName(), _job);

                CompletedJobFragment_ completedJobFragment = new CompletedJobFragment_();
                completedJobFragment.setArguments(args);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainContent, completedJobFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }

    private Observer<TelemDataHolder> _telemDataObserver = new Observer<TelemDataHolder>() {
        @Override
        public void onSubscribe(Disposable d) {
            _telemDataDisposable = d;
        }

        @Override
        public void onNext(TelemDataHolder telemDataHolder) {
            updateLiveData(telemDataHolder);

            _job.addTelemData(telemDataHolder);

            if (_job.getTelemData().size() == Constants.MIN_JOB_TELEM_COUNT) {
                updateJob();
            }
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
                    stopProgress();
                    ToastNotifier.showBluetoothError(_context, R.string.message_connection_closed);
                    _fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_refresh, null));
                    _fab.show();
                    break;
                case CONNECTING:
                    startProgress();
                    _fab.hide();
                    break;
                case CONNECTED:
                    _bluetoothController.getTelemDataSubject()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(_telemDataObserver);

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

    private DialogInterface.OnClickListener _completeWorkDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            finishJob();
        }
    };

    private class UpsertJobResponse extends BaseResponse<JobHolder> {
        UpsertJobResponse() {
            super(TelemDataFragment.this);
        }

        @Override
        public void onSuccess(JobHolder job) {
            _job.setId(job.getId());

            showCompletedJobFragment();

            _job.resetTelemData();

            super.onSuccess(job);
        }
    }
}
