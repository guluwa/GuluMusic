package cn.guluwa.gulumusic.data.remote;

import android.arch.lifecycle.LiveData;

import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import io.reactivex.Observable;

/**
 * Created by guluwa on 2018/1/4.
 */

public class LiveDataObservableAdapter {

    public static <T> LiveData<ViewDataBean<T>> fromObservableViewData(final Observable<T> observable) {
        return new ObservableViewLiveData<>(observable);
    }
}
