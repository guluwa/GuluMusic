package cn.guluwa.gulumusic.data.remote

import android.arch.lifecycle.LiveData
import cn.guluwa.gulumusic.data.bean.ViewDataBean
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference

/**
 * Created by guluwa on 2018/1/4.
 */

class ObservableViewLiveData<T>(private val mObservable: Observable<T>) : LiveData<ViewDataBean<T>>() {

    private var mDisposableRef: WeakReference<Disposable>? = null
    private val mLock = Any()

    override fun onActive() {
        super.onActive()

        mObservable.subscribe(object : Observer<T> {
            override fun onSubscribe(d: Disposable) {
                synchronized(mLock) {
                    mDisposableRef = WeakReference(d)
                }
                postValue(ViewDataBean.loading())
            }

            override fun onNext(t: T) {
                if (t == null) {
                    postValue(ViewDataBean.empty())
                } else {
                    postValue(ViewDataBean.content(t))
                }
            }

            override fun onError(e: Throwable) {
                println(e.message)
                synchronized(mLock) {
                    mDisposableRef = null
                }
                postValue(ViewDataBean.error(e))
            }

            override fun onComplete() {
                synchronized(mLock) {
                    mDisposableRef = null
                }
            }
        })
    }

    override fun onInactive() {
        super.onInactive()

        synchronized(mLock) {
            val disposableWeakReference = mDisposableRef
            if (disposableWeakReference != null) {
                val disposable = disposableWeakReference.get()
                disposable?.dispose()
                mDisposableRef = null
            }
        }
    }
}
