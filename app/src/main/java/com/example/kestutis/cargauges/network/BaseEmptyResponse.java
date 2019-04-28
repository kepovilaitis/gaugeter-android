package com.example.kestutis.cargauges.network;

import android.content.Context;
import com.example.kestutis.cargauges.constants.ErrorCodes;
import com.example.kestutis.cargauges.tools.ToastNotifier;
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
