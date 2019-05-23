package lt.kepo.gaugeter.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.constants.Constants;
import lt.kepo.gaugeter.controllers.PreferencesController;
import lt.kepo.gaugeter.holders.UserInfoHolder;
import lt.kepo.gaugeter.network.BaseResponse;
import lt.kepo.gaugeter.tools.ToastNotifier;

public class EditAccountFragment extends BaseFragment {
    private UserInfoHolder _userInfo;
    private EditText _passwordEdit;
    private EditText _repeatPasswordEdit;
    private PreferencesController _preferencesController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_edit_acount, container, false);

        setTitle(R.string.title_edit_account);

        _preferencesController =  new PreferencesController(_context);
        _userInfo = new UserInfoHolder();
        _userInfo.setUserId(_preferencesController.getUserId());
        _userInfo.setMeasurementSystem(_preferencesController.getUserMeasurementSystem());

        main.<TextView>findViewById(R.id.textUserId).setText(_userInfo.getUserId());
        _passwordEdit = main.findViewById(R.id.editPassword);
        _repeatPasswordEdit = main.findViewById(R.id.editRepeatPassword);

        Spinner spinner = main.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(_context, R.array.measurement_systems_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        spinner.setOnItemSelectedListener(_spinnerSelectedListener);
        spinner.setAdapter(adapter);
        spinner.setSelection(_userInfo.getMeasurementSystem());

        _fab = getFab();
        _fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_complete, null));
        _fab.setOnClickListener(_fabClickListener);
        _fab.show();

        return main;
    }

    private boolean validate(){
        String password = _passwordEdit.getText().toString();

        if (password.length() < 5){
            ToastNotifier.showError(_context, R.string. message_password_too_short);

            return false;
        }

        if (!password.equals(_repeatPasswordEdit.getText().toString())){
            ToastNotifier.showError(_context, R.string.message_passwords_do_not_match);

            return false;
        }

        startProgress();

        _userInfo.setPassword(password);
        _userInfo.setDescription("");

        return true;
    }

    private OnItemSelectedListener _spinnerSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            _userInfo.setMeasurementSystem(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    };

    private OnClickListener _fabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validate()){
                _httpClient.updateUser(_userInfo)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new UpdateUserResponse());
            }
        }
    };

    private class UpdateUserResponse extends BaseResponse<UserInfoHolder> {
        UpdateUserResponse() {
            super(EditAccountFragment.this);
        }

        @Override
        public void onSuccess(UserInfoHolder user) {
            super.onSuccess(user);

            _preferencesController.setEditorValue(Constants.USER_MEASUREMENT_SYSTEM, user.getMeasurementSystem());

            FragmentManager fragmentManager = getFragmentManager();

            if (fragmentManager != null) {
                fragmentManager.popBackStackImmediate();
            }
        }
    }
}
