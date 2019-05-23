package lt.kepo.gaugeter.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.constants.Constants;
import lt.kepo.gaugeter.holders.DeviceHolder;
import lt.kepo.gaugeter.network.BaseResponse;
import lt.kepo.gaugeter.tools.ToastNotifier;

public class EditDeviceFragment extends BaseFragment {
    private EditText _deviceNameEdit;
    private DeviceHolder _device;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args != null){
            _device = (DeviceHolder) args.getSerializable(DeviceHolder.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_edit_device, container, false);

        setTitle(R.string.title_edit_device);

        main.<TextView>findViewById(R.id.textView).setText(_device.getBluetoothAddress());
        _deviceNameEdit = main.findViewById(R.id.editTextView);
        _deviceNameEdit.setText(_device.getName());

        _fab = getFab();
        _fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_complete, null));
        _fab.setOnClickListener(_fabClickListener);
        _fab.show();

        return main;
    }

    private boolean validate(){
        String deviceName = _deviceNameEdit.getText().toString();

        if (deviceName.length() < 5){
            ToastNotifier.showError(_context, R.string. message_password_too_short);

            return false;
        }

        startProgress();

        _device.setName(deviceName);

        return true;
    }

    private OnClickListener _fabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validate()){
                _httpClient.updateDevice(_device)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new UpdateDeviceResponse());
            }
        }
    };

    private class UpdateDeviceResponse extends BaseResponse<DeviceHolder> {
        UpdateDeviceResponse() {
            super(EditDeviceFragment.this);
        }

        @Override
        public void onSuccess(DeviceHolder device) {
            super.onSuccess(device);

            Fragment fragment = getTargetFragment();
            FragmentManager fragmentManager = getFragmentManager();

            if (fragment != null) {
                fragment.onActivityResult(
                        Constants.REQUEST_EDIT_DEVICE,
                        Activity.RESULT_OK,
                        new Intent().putExtra(DeviceHolder.class.getSimpleName(), device.getName()));
            }

            if (fragmentManager != null) {
                fragmentManager.popBackStackImmediate();
            }
        }
    }
}
