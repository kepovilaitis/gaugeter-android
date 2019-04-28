package com.example.kestutis.cargauges.network;

import android.content.Context;
import com.example.kestutis.cargauges.tools.ToastNotifier;
import io.reactivex.SingleObserver;
import lombok.AllArgsConstructor;
import retrofit2.HttpException;

@AllArgsConstructor
public abstract class BaseResponse<T> implements SingleObserver<T> {
    private Context _context;

    @Override
    public void onError(Throwable e) {
        ToastNotifier.showHttpError(_context, ((HttpException)e).code());
    }
}
