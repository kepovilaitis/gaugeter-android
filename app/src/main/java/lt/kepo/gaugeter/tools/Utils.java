package lt.kepo.gaugeter.tools;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Utils {

    public static void hideKeyboard(Activity activity) {
        hideKeyboard(activity, activity.getCurrentFocus());
    }

    public static void hideKeyboard(Activity activity, View view) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static String getFormattedDate(long timestamp){
        return DateFormat.format("dd-MM-yyyy", timestamp).toString();
    }

    public static String getFormattedDateTime(long timestamp){
        return DateFormat.format("dd-MM-yyyy kk:mm", timestamp).toString();
    }
}
