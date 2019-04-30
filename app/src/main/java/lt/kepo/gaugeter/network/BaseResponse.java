package lt.kepo.gaugeter.network;

import android.content.Context;
import io.reactivex.disposables.Disposable;
import lt.kepo.gaugeter.tools.ToastNotifier;
import io.reactivex.SingleObserver;
import lombok.AllArgsConstructor;
import retrofit2.HttpException;

@AllArgsConstructor
public abstract class BaseResponse<T> implements SingleObserver<T> {
    private Context _context;

    @Override
    public void onSubscribe(Disposable d) { }

    @Override
    public void onError(Throwable e) {
        if (e instanceof HttpException)
            ToastNotifier.showHttpError(_context, ((HttpException)e).code());
    }
}
