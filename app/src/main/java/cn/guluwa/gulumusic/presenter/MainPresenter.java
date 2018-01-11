package cn.guluwa.gulumusic.presenter;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import cn.guluwa.gulumusic.data.bean.PlayListBean;
import cn.guluwa.gulumusic.data.remote.retrofit.ApiService;
import cn.guluwa.gulumusic.data.remote.retrofit.RetrofitFactory;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.manage.Contacts;
import cn.guluwa.gulumusic.model.MainModel;
import cn.guluwa.gulumusic.ui.MainActivity;

/**
 * Created by guluwa on 2018/1/11.
 */

public class MainPresenter {

    private MainActivity mainView;
    @Inject
    MainModel mainModel;

    @Inject
    public MainPresenter(MainActivity mainView) {
        this.mainView = mainView;
    }

    public void obtainNetCloudHot() {
        Map<String, Object> map = new HashMap<>();
        map.put("types", Contacts.NET_CLOUD_HOT_TYPES);
        map.put("id", Contacts.NET_CLOUD_HOT_ID);
        mainModel.obtainNetCloudHot(Contacts.NET_CLOUD_HOT_CALLBACK, map, new OnResultListener<PlayListBean>() {
            @Override
            public void success(PlayListBean result) {
                mainView.getmMainBinding().mSwipeRefreshLayout.setRefreshing(false);
                mainView.setData(result.getPlaylist().getTracks());
            }

            @Override
            public void failed(String error) {
                mainView.getmMainBinding().mSwipeRefreshLayout.setRefreshing(false);
                mainView.showToast(error);
            }
        });
    }
}
