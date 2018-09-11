package fragments;

import android.content.SharedPreferences;
import android.content.pm.PackageManager.*;
import android.os.Bundle;
import android.preference.*;
import android.support.annotation.*;
import android.view.*;

import com.example.kestutis.cargauges.R;

import constants.Preferences;

public class SettingsFragment extends PreferenceFragment {
    private Preference _versionPref;
    private ListPreference _language;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.main_preferences);

        _language = (ListPreference) findPreference(Preferences.PREFERENCE_LANGUAGE);
//        alertEnabledPref = (CheckBoxPreference) findPreference(PrefKeys.PREFERENCE_ALERT_ENABLED);
//        alertSoundPref = (AlertSoundPreference) findPreference(PrefKeys.PREFERENCE_ALERT_SOUND_URI);
//        mapTypePref = (ListPreference) findPreference(PrefKeys.PREFERENCE_MAP_TYPE);
//        eventHistory = (ListPreference) findPreference(PrefKeys.PREFERENCE_EVENT_HISTORY);
        _versionPref = findPreference(Preferences.PREFERENCE_VERSION);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        setupAlertEnabledPref();
//        setupAlertSoundPref(prefs);
//        setupMapTypePref();
          setupLanguage();
//        setupEventHistoryPref();
          setupVersionPref();
    }

    /*@Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }*/

    private void setupLanguage() {
        if (_language.getValue().equals("en_GB")) {
            _language.setValue("en_US");
        }

        String string = _language.getEntry().toString();

        _language.setSummary(string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase());
    }


    /**Sets up version preference in settings*/
    private void setupVersionPref() {
        try {
            _versionPref.setSummary(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (NameNotFoundException ignore) { }
    }

}
