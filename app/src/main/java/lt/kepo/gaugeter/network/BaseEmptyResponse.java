package lt.kepo.gaugeter.network;

import lt.kepo.gaugeter.constants.ErrorCodes;
import lt.kepo.gaugeter.tools.ToastNotifier;

import android.content.Context;

import lombok.AllArgsConstructor;

import retrofit2.Call;
import retrofit2.Callback;

@AllArgsConstructor
public abstract class BaseEmptyResponse  implements Callback<Void> {
    private Context _context;

    @Override
    public void onFailure(Call<Void> call, Throwable t) {
        ToastNotifier.showHttpError(_context, ErrorCodes.HTTP_SERVER);
    }
}
