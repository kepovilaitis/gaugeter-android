package com.example.kestutis.cargauges.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.kestutis.cargauges.constants.PreferenceKeys;
import lombok.Getter;

public class PreferenceController extends PreferenceKeys {

    @Getter
    private Context _context;

    SharedPreferences _preferences;

    public PreferenceController(Context context) {
        _context = context;

        _preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setEditorValue(String key, String value) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getEditorValue(String key, String defaultValue) {
        return _preferences.getString(key, defaultValue);
    }
}
