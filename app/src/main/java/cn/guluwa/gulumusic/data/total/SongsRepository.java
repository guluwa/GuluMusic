package cn.guluwa.gulumusic.data.total;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.view.View;

import java.io.File;
import java.util.List;

import cn.guluwa.gulumusic.data.bean.PageStatus;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import cn.guluwa.gulumusic.data.remote.RemoteSongsDataSource;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.utils.AppUtils;

/**
 * Created by guluwa on 2018/1/12.
 */

public class SongsRepository {

    private static final SongsRepository instance = new SongsRepository();
    private RemoteSongsDataSource remoteSongsDataSource = RemoteSongsDataSource.getInstance();
    private LocalSongsDataSource localSongsDataSource = LocalSongsDataSource.getInstance();

    public static SongsRepository getInstance() {
        return instance;
    }

    public SongsRepository() {
    }

    public LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong() {
        if (AppUtils.isNetConnected()) {
            return remoteSongsDataSource.queryNetCloudHotSong();
        } else {
            return localSongsDataSource.queryNetCloudHotSong();
        }
    }

    public LiveData<ViewDataBean<SongPathBean>> querySongPath(String id, String name) {
        if (AppUtils.isNetConnected()) {
            return remoteSongsDataSource.querySongPath(id, name);
        } else {
            return localSongsDataSource.querySongPath(id, name);
        }
    }

    public LiveData<ViewDataBean<SongWordBean>> querySongWord(String id, String name) {
        if (AppUtils.isNetConnected()) {
            return remoteSongsDataSource.querySongWord(id, name);
        } else {
            return localSongsDataSource.querySongWord(id, name);
        }
    }

    public void downloadSongFile(String url, String songName, OnResultListener<File> listener) {
        remoteSongsDataSource.downloadSongFile(url, songName, listener);
    }
}
