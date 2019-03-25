package com.example.kestutis.cargauges.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.example.kestutis.cargauges.constants.Constants;
import com.example.kestutis.cargauges.constants.PreferenceKeys;

import lombok.Getter;

public class PreferenceController extends PreferenceKeys {

    @Getter private Context _context;
    private SharedPreferences _preferences;

    public PreferenceController(Context context) {
        _context = context;

        _preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setEditorValue(String key, int value) {
        Editor editor = _preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void setEditorValue(String key, long value) {
        Editor editor = _preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void setEditorValue(String key, String value) {
        Editor editor = _preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void setEditorValue(String key, boolean value) {
        Editor editor = _preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getIsLoggedIn() {
        return _preferences.getBoolean(Constants.LOGGED_IN, false);
    }

    public String getUserId() {
        return _preferences.getString(Constants.USER_ID, "user id");
    }

    public String getEditorValue(String key, String defaultValue) {
        return _preferences.getString(key, defaultValue);
    }
}
