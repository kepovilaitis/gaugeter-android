package lt.kepo.gaugeter.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.view.View.OnClickListener;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.constants.Constants;
import lt.kepo.gaugeter.constants.Enums.CONNECTION_STATUS;
import lt.kepo.gaugeter.holders.DeviceHolder;
import lt.kepo.gaugeter.constants.PreferenceKeys;
import lt.kepo.gaugeter.controllers.BluetoothController;
import lt.kepo.gaugeter.controllers.PreferencesController;
import lt.kepo.gaugeter.holders.JobHolder;
import lt.kepo.gaugeter.holders.TelemDataHolder;
import lt.kepo.gaugeter.network.BaseResponse;
import lt.kepo.gaugeter.tools.ToastNotifier;
import lt.kepo.gaugeter.views.GaugeCardView_;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TelemDataFragment extends BaseFragment {

    private GaugeCardView_ _oilTempGaugeCard;
    private GaugeCardView_ _oilPressureGaugeCard;
    private GaugeCardView_ _waterTempGaugeCard;
    private GaugeCardView_ _chargeGaugeCard;
    private BluetoothController _bluetoothController;
    private Disposable _telemDataDisposable;
    private Disposable _statusDisposable;
    private DeviceHolder _device;
    private JobHolder _job;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        _bluetoothController = BluetoothController.getInstance();
        _device = _bluetoothController.getDevice();
        _job = new JobHolder(_device);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_telem_data, container, false);

        setTitle(_device.getName());

        _oilTempGaugeCard = main.findViewById(R.id.gaugeOilTemp);
        _oilPressureGaugeCard = main.findViewById(R.id.gaugeOilPressure);
        _waterTempGaugeCard = main.findViewById(R.id.gaugeWaterTemp);
        _chargeGaugeCard = main.findViewById(R.id.gaugeCharge);

        _oilPressureGaugeCard.setMaxValue(7);
        _oilTempGaugeCard.setMaxValue(125);
        _waterTempGaugeCard.setMaxValue(125);
        _chargeGaugeCard.setMaxValue(14);

        _oilPressureGaugeCard.setText(R.string.text_oil_pressure);
        _oilTempGaugeCard.setText(R.string.text_oil_temp);
        _waterTempGaugeCard.setText(R.string.text_water_temp);
        _chargeGaugeCard.setText(R.string.text_charge);

        _oilPressureGaugeCard.setUnits(R.string.bar);
        _oilTempGaugeCard.setUnits(R.string.degrees_celsius);
        _waterTempGaugeCard.setUnits(R.string.degrees_celsius);

        switch (new PreferencesController(_context).getEditorValue(PreferenceKeys.PREFERENCE_MEASUREMENT_SYSTEM, "Metric")){
            case "Metric":
                _oilPressureGaugeCard.setUnits(R.string.bar);
                _oilTempGaugeCard.setUnits(R.string.degrees_celsius);
                _waterTempGaugeCard.setUnits(R.string.degrees_celsius);

                break;
            case "Imperial":
                _oilPressureGaugeCard.setUnits(R.string.psi);
                _oilTempGaugeCard.setUnits(R.string.degrees_fahrenheit);
                _waterTempGaugeCard.setUnits(R.string.degrees_fahrenheit);

                break;
        }

        _chargeGaugeCard.setUnits(R.string.volts);

        _fab = getFab();
        _fab.setOnClickListener(_fabClickListener);

        _bluetoothController.getTelemDataSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_telemDataObserver);

        _bluetoothController.getStateSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_statusObserver);

        return main;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.live_data_menu, menu);

            super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyView() {
        if (_telemDataDisposable != null && !_telemDataDisposable.isDisposed()) {
            _telemDataDisposable.dispose();
        }

        if (_statusDisposable != null && !_statusDisposable.isDisposed()) {
            _statusDisposable.dispose();
        }

        if (_job.getState() != JobHolder.FINISHED) {
            finishJob();
        }

        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_EDIT_DEVICE) {
            setTitle(intent.getStringExtra(DeviceHolder.class.getSimpleName()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuDeviceSettings:

                FragmentManager fragmentManager = getFragmentManager();

                if (fragmentManager != null) {
                    Bundle args = new Bundle();
                    args.putSerializable(DeviceHolder.class.getSimpleName(), _device);

                    EditDeviceFragment editDeviceFragment = new EditDeviceFragment();
                    editDeviceFragment.setArguments(args);
                    editDeviceFragment.setTargetFragment(this, Constants.REQUEST_EDIT_DEVICE);

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.mainContent, editDeviceFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.hide(this);
                    fragmentTransaction.commit();
                }

                return true;

            case R.id.menuComplete:

                new AlertDialog.Builder(_context)
                        .setTitle(R.string.title_finish_job)
                        .setPositiveButton(R.string.yes, _completeWorkDialogClickListener)
                        .setNegativeButton(R.string.no, null)
                        .setMessage(R.string.dialog_finish_job)
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
            _oilTempGaugeCard.setValue(data.getOilTemperature());
            _waterTempGaugeCard.setValue(data.getWaterTemperature());
            _chargeGaugeCard.setValue(data.getCharge());
        }
    }

    private void updateJob(final JobHolder job) {
        job.setDateUpdated(System.currentTimeMillis());

        _httpClient
                .upsertJob(job)
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

        final JobHolder job = new JobHolder(_job);

        updateJob(job);
        showCompletedJobFragment(job);
    }

    private void showCompletedJobFragment(final JobHolder job) {
        if (_job.getState() == JobHolder.FINISHED && (!isRemoving() && isVisible())) {

            FragmentManager fragmentManager = getFragmentManager();

            if (fragmentManager != null) {

                Bundle args = new Bundle();
                args.putSerializable(JobHolder.class.getSimpleName(), job);

                CompletedJobFragment completedJobFragment = new CompletedJobFragment();
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
                updateJob(new JobHolder(_job));
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
                    ToastNotifier.showError(_context, R.string.message_connection_closed);
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
        public void onSubscribe(Disposable disposable) {
            _job.resetTelemData();
        }

        @Override
        public void onSuccess(JobHolder job) {
            _job.setId(job.getId());

            super.onSuccess(job);
        }

        @Override
        public void onError(Throwable e) {
            FragmentManager fragmentManager = getFragmentManager();

            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainContent, new DevicesFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }

            super.onError(e);
        }
    }
}
