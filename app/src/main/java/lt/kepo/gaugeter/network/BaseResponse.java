package lt.kepo.gaugeter.network;

import io.reactivex.disposables.Disposable;
import lt.kepo.gaugeter.fragments.BaseFragment;
import lt.kepo.gaugeter.tools.ToastNotifier;
import io.reactivex.SingleObserver;
import lombok.AllArgsConstructor;
import retrofit2.HttpException;

@AllArgsConstructor
public abstract class BaseResponse<T> implements SingleObserver<T> {
    private final BaseFragment _fragment;

    @Override
    public void onSubscribe(Disposable d) {
        _fragment.startProgress();
    }

    @Override
    public void onSuccess(T object) {
        _fragment.stopProgress();
    }

    @Override
    public void onError(Throwable e) {
        _fragment.stopProgress();

        if (e instanceof HttpException)
            ToastNotifier.showHttpError(_fragment.getContext(), ((HttpException)e).code());
    }
}
