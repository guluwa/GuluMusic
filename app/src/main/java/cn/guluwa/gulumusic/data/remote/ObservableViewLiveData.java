package cn.guluwa.gulumusic.data.remote;

import android.arch.lifecycle.LiveData;

import java.lang.ref.WeakReference;

import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by guluwa on 2018/1/4.
 */

public class ObservableViewLiveData<T> extends LiveData<ViewDataBean<T>> {

    private WeakReference<Disposable> mDisposableRef;
    private final Observable<T> mObservable;
    private final Object mLock = new Object();

    public ObservableViewLiveData(Observable<T> mObservable) {
        this.mObservable = mObservable;
    }

    @Override
    protected void onActive() {
        super.onActive();

        mObservable.subscribe(new Observer<T>() {
            @Override
            public void onSubscribe(Disposable d) {
                synchronized (mLock) {
                    mDisposableRef = new WeakReference<>(d);
                }
                postValue(ViewDataBean.loading());
            }

            @Override
            public void onNext(T t) {
                if (t == null) {
                    postValue(ViewDataBean.empty());
                } else {
                    postValue(ViewDataBean.content(t));
                }
            }

            @Override
            public void onError(Throwable e) {
                synchronized (mLock) {
                    mDisposableRef = null;
                }
                postValue(ViewDataBean.error(e));
            }

            @Override
            public void onComplete() {
                synchronized (mLock) {
                    mDisposableRef = null;
                }
            }
        });
    }

    @Override
    protected void onInactive() {
        super.onInactive();

        synchronized (mLock) {
            WeakReference<Disposable> disposableWeakReference = mDisposableRef;
            if (disposableWeakReference != null) {
                Disposable disposable = disposableWeakReference.get();
                if (disposable != null) {
                    disposable.dispose();
                }
                mDisposableRef = null;
            }
        }
    }
}
