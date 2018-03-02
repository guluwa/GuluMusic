package cn.guluwa.gulumusic.data.remote

import android.arch.lifecycle.LiveData

import cn.guluwa.gulumusic.data.bean.ViewDataBean
import io.reactivex.Observable

/**
 * Created by guluwa on 2018/1/4.
 */

object LiveDataObservableAdapter {

    fun <T> fromObservableViewData(observable: Observable<T>): LiveData<ViewDataBean<T>> {
        return ObservableViewLiveData(observable)
    }
}
