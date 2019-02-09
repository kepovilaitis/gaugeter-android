package com.example.kestutis.cargauges.fragments.preferences;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.*;

import com.example.kestutis.cargauges.R;

import com.example.kestutis.cargauges.constants.PreferenceKeys;

public class SettingsFragment extends PreferenceFragment {
    private Preference _versionPref;
    private ListPreference _language;
    private ListPreference _measurementSystem;
    private MultiSelectListPreference _gauges;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.main_preferences);

        _language = (ListPreference) findPreference(PreferenceKeys.PREFERENCE_LANGUAGE);
        _measurementSystem = (ListPreference) findPreference(PreferenceKeys.PREFERENCE_MEASUREMENT_SYSTEM);
        _gauges = (MultiSelectListPreference) findPreference(PreferenceKeys.PREFERENCE_GAUGES);
        _versionPref = findPreference(PreferenceKeys.PREFERENCE_VERSION);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setupLanguage();
        setupMeasurementSystem();
        setupVersionPref();
    }

    private void setupLanguage() {
        if (_language.getValue().equals("en_GB")) {
            _language.setValue("en_US");
        }

        String string = _language.getEntry().toString();

        _language.setSummary(string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase());
    }

    private void setupMeasurementSystem(){
    }


    /**Sets up version preference in settings*/
    private void setupVersionPref() {
        try {
            _versionPref.setSummary(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException ignore) { }
    }

}
