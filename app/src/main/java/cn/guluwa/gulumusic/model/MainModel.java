package cn.guluwa.gulumusic.model;

import com.google.gson.Gson;

import java.util.Map;

import javax.inject.Inject;

import cn.guluwa.gulumusic.data.bean.PlayListBean;
import cn.guluwa.gulumusic.data.remote.retrofit.ApiService;
import cn.guluwa.gulumusic.data.remote.retrofit.RetrofitFactory;
import cn.guluwa.gulumusic.listener.OnResultListener;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by guluwa on 2018/1/11.
 */

public class MainModel {

    @Inject
    public MainModel() {
    }

    public void obtainNetCloudHot(String callback, Map<String, Object> map, OnResultListener<PlayListBean> listener) {
        RetrofitFactory.getRetrofit().createApi(ApiService.class)
                .obtainNetCloudHot(callback, map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::success, throwable -> {
                    listener.failed(throwable.getMessage());
                });
    }
}
