package cn.guluwa.gulumusic.data.remote;

import android.arch.lifecycle.LiveData;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.guluwa.gulumusic.data.bean.FreshBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import cn.guluwa.gulumusic.data.remote.retrofit.ApiService;
import cn.guluwa.gulumusic.data.remote.retrofit.RetrofitFactory;
import cn.guluwa.gulumusic.data.total.SongDataSource;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.manage.Contacts;
import cn.guluwa.gulumusic.utils.AppUtils;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
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

    /**
     * 查询热门歌曲
     *
     * @return
     */
    @Override
    public LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong() {
        Map<String, Object> map = new HashMap<>();
        map.put("types", "playlist");
        map.put("id", Contacts.NET_CLOUD_HOT_ID);
        return LiveDataObservableAdapter.fromObservableViewData(
                RetrofitFactory.getRetrofit().createApi(ApiService.class)
                        .obtainNetCloudHot(Contacts.SONG_CALLBACK, map)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map(playListBean -> {
                            for (int i = 0; i < playListBean.getPlaylist().getTracks().size(); i++) {
                                playListBean.getPlaylist().getTracks().get(i).setSinger(
                                        playListBean.getPlaylist().getTracks().get(i).getAr().size() == 0 ?
                                                null : playListBean.getPlaylist().getTracks().get(i).getAr().get(0));
                                playListBean.getPlaylist().getTracks().get(i).setTag(
                                        playListBean.getPlaylist().getTracks().get(i).getAlia().size() == 0 ?
                                                "" : playListBean.getPlaylist().getTracks().get(i).getAlia().get(0));
                                playListBean.getPlaylist().getTracks().get(i).setSource("netease");
                                playListBean.getPlaylist().getTracks().get(i).setIndex(i);
                            }
                            LocalSongsDataSource.getInstance().addSongs(playListBean.getPlaylist().getTracks());
                            return playListBean.getPlaylist().getTracks();
                        })
                        .observeOn(AndroidSchedulers.mainThread())
        );
    }

    /**
     * 歌曲搜索
     *
     * @param freshBean
     * @return
     */
    public LiveData<ViewDataBean<List<SearchResultSongBean>>> searchSongByKeyWord(FreshBean freshBean) {
        Map<String, Object> map = new HashMap<>();
        map.put("types", "search");
        map.put("count", 20);
        map.put("source", AppManager.getInstance().getSearchPlatform());
        map.put("pages", freshBean.page);
        map.put("name", freshBean.key);
        return LiveDataObservableAdapter.fromObservableViewData(
                RetrofitFactory.getRetrofit().createApi(ApiService.class)
                        .searchSongByKeyWord(Contacts.SONG_CALLBACK, map)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
        );
    }

    /**
     * 查询歌曲路径(首页)
     *
     * @param song
     * @return
     */
    @Override
    public void querySongPath(TracksBean song, OnResultListener<SongPathBean> listener) {
        Map<String, Object> map = new HashMap<>();
        map.put("types", "url");
        map.put("id", "".equals(song.getUrl_id()) ? song.getId() : song.getUrl_id());
        map.put("source", song.getSource());
        RetrofitFactory.getRetrofit().createApi(ApiService.class)
                .obtainSongPath(Contacts.SONG_CALLBACK, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(songPathBean -> {
                    songPathBean.setSong(song);
                    songPathBean.setId(song.getId());
                    LocalSongsDataSource.getInstance().addSongPath(songPathBean);
                    return songPathBean;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::success, throwable -> {
                    listener.failed(throwable.getMessage());
                });
    }

    /**
     * 查询歌曲歌词(首页)
     *
     * @param song
     * @return
     */
    @Override
    public void querySongWord(TracksBean song, OnResultListener<SongWordBean> listener) {
        Map<String, Object> map = new HashMap<>();
        map.put("types", "lyric");
        map.put("id", "".equals(song.getLyric_id()) ? song.getId() : song.getLyric_id());
        map.put("source", song.getSource());
        RetrofitFactory.getRetrofit().createApi(ApiService.class)
                .obtainSongWord(Contacts.SONG_CALLBACK, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(songWordBean -> {
                    songWordBean.setSong(song);
                    songWordBean.setId(song.getId());
                    LocalSongsDataSource.getInstance().addSongWord(songWordBean);
                    return songWordBean;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::success, throwable -> listener.failed(throwable.getMessage()));
    }

    /**
     * 查询歌曲封面图(搜索)
     *
     * @param song
     * @param listener
     */
    public void querySearchSongPic(TracksBean song, OnResultListener<SongPathBean> listener) {
        Map<String, Object> map = new HashMap<>();
        map.put("types", "pic");
        map.put("id", "".equals(song.getPic_id()) ? song.getId() : song.getPic_id());
        map.put("source", song.getSource());
        RetrofitFactory.getRetrofit().createApi(ApiService.class)
                .obtainSongPath(Contacts.SONG_CALLBACK, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(songPathBean -> {
                    if (LocalSongsDataSource.getInstance().queryLocalSong(song.getId(), song.getName()) == null ) {
                        song.getAl().setPicUrl(songPathBean.getUrl());
                        LocalSongsDataSource.getInstance().addLocalSong(AppUtils.getLocalSongBean(song));
                    } else {
                        System.out.println("歌曲已存在");
                    }
                    return songPathBean;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::success, throwable -> listener.failed(throwable.getMessage()));
    }

    /**
     * 歌曲下载
     *
     * @param songPathBean
     * @param songName
     * @param listener
     */
    public void downloadSongFile(SongPathBean songPathBean, String songName, OnResultListener<File> listener) {
        RetrofitFactory.getRetrofit().createApi(ApiService.class)
                .downloadSongFile(songPathBean.getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(responseBody -> {
                    if (LocalSongsDataSource.getInstance().queryLocalSong(songPathBean.getId(), songPathBean.getSong().getName()) == null) {
                        LocalSongsDataSource.getInstance().addLocalSong(AppUtils.getLocalSongBean(songPathBean.getSong()));
                    } else {
                        System.out.println("歌曲已存在");
                    }
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
