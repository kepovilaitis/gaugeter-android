package com.example.kestutis.cargauges.tools;

import android.support.design.widget.Snackbar;
import android.view.View;

public class SnackbarNotifier {

    public static void showMessage(View view, int message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

}
