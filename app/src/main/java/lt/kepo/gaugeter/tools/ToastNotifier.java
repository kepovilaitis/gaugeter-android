package lt.kepo.gaugeter.tools;

import android.content.Context;
import android.widget.Toast;
import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.constants.ErrorCodes;

public class ToastNotifier {

    public static void showHttpError(Context context, int statusCode) {
        Toast.makeText(context, getHttpErrorMessageText(statusCode), Toast.LENGTH_LONG).show();
    }

    public static void showError(Context context, int messageId) {
        Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
    }

    private static int getHttpErrorMessageText(int statusCode){
        switch (statusCode) {
            case ErrorCodes.HTTP_UNAUTHORIZED:
                return R.string.error_http_bad_credentials;
            default:
                return R.string.error_http_internal_server;
        }
    }
}
