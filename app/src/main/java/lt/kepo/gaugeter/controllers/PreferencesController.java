package lt.kepo.gaugeter.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import lt.kepo.gaugeter.constants.Constants;
import lt.kepo.gaugeter.constants.PreferenceKeys;

import lombok.Getter;

public class PreferencesController extends PreferenceKeys {

    @Getter private Context _context;
    private SharedPreferences _preferences;

    public PreferencesController(Context context) {
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

    public void deleteSessionData() {
        Editor editor = _preferences.edit();
        editor.remove(Constants.USER_ID);
        editor.remove(Constants.USER_TOKEN);
        editor.remove(Constants.LOGGED_IN);

        editor.apply();
    }

    public String getUserId() {
        return _preferences.getString(Constants.USER_ID, "userId");
    }

    public String getUserToken() {
        return _preferences.getString(Constants.USER_TOKEN, "user_token");
    }

    public String getEditorValue(String key, String defaultValue) {
        return _preferences.getString(key, defaultValue);
    }
}
