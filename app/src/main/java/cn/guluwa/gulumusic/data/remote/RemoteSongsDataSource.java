package cn.guluwa.gulumusic.data.remote;

import android.arch.lifecycle.LiveData;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.guluwa.gulumusic.data.bean.BaseSongBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import cn.guluwa.gulumusic.data.remote.retrofit.ApiService;
import cn.guluwa.gulumusic.data.remote.retrofit.RetrofitFactory;
import cn.guluwa.gulumusic.data.total.SongDataSource;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.manage.Contacts;
import cn.guluwa.gulumusic.utils.AppUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

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
        map.put("types", "playlist");
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

    @Override
    public LiveData<ViewDataBean<SongPathBean>> querySongPath(TracksBean song) {
        Map<String, Object> map = new HashMap<>();
        map.put("types", "url");
        map.put("id", song.getId());
        map.put("source", "netease");
        return LiveDataObservableAdapter.fromObservableViewData(
                RetrofitFactory.getRetrofit().createApi(ApiService.class)
                        .obtainNetCloudHotSongPath(Contacts.NET_CLOUD_SONG_CALLBACK, map)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map(songPathBean -> {
                            songPathBean.setSong(song);
                            songPathBean.setId(Integer.valueOf(song.getId()));
                            LocalSongsDataSource.getInstance().addSong(songPathBean);
                            return songPathBean;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
        );
    }

    @Override
    public LiveData<ViewDataBean<SongWordBean>> querySongWord(BaseSongBean song) {
        Map<String, Object> map = new HashMap<>();
        map.put("types", "lyric");
        map.put("id", song.getId());
        map.put("source", "netease");
        return LiveDataObservableAdapter.fromObservableViewData(
                RetrofitFactory.getRetrofit().createApi(ApiService.class)
                        .obtainNetCloudHotSongWord(Contacts.NET_CLOUD_SONG_CALLBACK, map)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map(songWordBean -> {
                            songWordBean.setSong(song);
                            songWordBean.setId(Integer.valueOf(song.getId()));
                            LocalSongsDataSource.getInstance().addSong(songWordBean);
                            return songWordBean;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
        );
    }

    public void downloadSongFile(SongPathBean songPathBean, String songName, OnResultListener<File> listener) {
        RetrofitFactory.getRetrofit().createApi(ApiService.class)
                .downloadSongFile(songPathBean.getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(responseBody -> {
                    LocalSongBean localSongBean = new LocalSongBean();
                    localSongBean.setId(songPathBean.getSong().getId());
                    localSongBean.setName(songPathBean.getSong().getName());
                    localSongBean.setAl(songPathBean.getSong().getAl());
                    localSongBean.setSinger(songPathBean.getSong().getSinger());
                    localSongBean.setTag(songPathBean.getSong().getTag());
                    LocalSongsDataSource.getInstance().addLocalSong(localSongBean);
                    return responseBody;
                })
                .observeOn(Schedulers.io())
                .map(responseBody -> AppUtils.writeSong2Disk(responseBody, songName))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::success, throwable -> {
                    System.out.println(throwable.getMessage());
                    listener.failed("歌曲下载失败");
                });
    }
}
