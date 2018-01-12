package cn.guluwa.gulumusic.data.remote;

import android.arch.lifecycle.LiveData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import cn.guluwa.gulumusic.data.remote.retrofit.ApiService;
import cn.guluwa.gulumusic.data.remote.retrofit.RetrofitFactory;
import cn.guluwa.gulumusic.data.total.SongDataSource;
import cn.guluwa.gulumusic.manage.Contacts;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by guluwa on 2018/1/12.
 */

public class RemoteSongsDataSource implements SongDataSource {

    public static final RemoteSongsDataSource instance = new RemoteSongsDataSource();

    public static RemoteSongsDataSource getInstance() {
        return instance;
    }

    public RemoteSongsDataSource() {
    }

    @Override
    public LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong() {
        Map<String, Object> map = new HashMap<>();
        map.put("types", Contacts.NET_CLOUD_HOT_TYPES);
        map.put("id", Contacts.NET_CLOUD_HOT_ID);
        return LiveDataObservableAdapter.fromObservableViewData(
                RetrofitFactory.getRetrofit().createApi(ApiService.class)
                        .obtainNetCloudHot(Contacts.NET_CLOUD_HOT_CALLBACK, map)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map(playListBean -> {
                            for (int i = 0; i < playListBean.getPlaylist().getTracks().size(); i++) {
                                playListBean.getPlaylist().getTracks().get(i).setSinger(
                                        playListBean.getPlaylist().getTracks().get(i).getAr().size() == 0 ?
                                                null : playListBean.getPlaylist().getTracks().get(i).getAr().get(0));
                                playListBean.getPlaylist().getTracks().get(i).setTag(
                                        playListBean.getPlaylist().getTracks().get(i).getAlia().size() == 0 ?
                                                "" : playListBean.getPlaylist().getTracks().get(i).getAlia().get(0)
                                );
                                playListBean.getPlaylist().getTracks().get(i).setIndex(i);
                            }
                            LocalSongsDataSource.getInstance().addSongs(playListBean.getPlaylist().getTracks());
                            return playListBean.getPlaylist().getTracks();
                        })
                        .observeOn(AndroidSchedulers.mainThread())
        );
    }
}
