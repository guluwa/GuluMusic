package cn.guluwa.gulumusic.data.total;

import android.arch.lifecycle.LiveData;

import java.io.File;
import java.util.List;

import cn.guluwa.gulumusic.data.bean.LocalSongBean;
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

    public LiveData<ViewDataBean<List<LocalSongBean>>> queryLocalSong() {
        return localSongsDataSource.queryLocalSong();
    }

    public void addLocalSong(LocalSongBean localSongBean){
        localSongsDataSource.addLocalSong(localSongBean);
    }

    public LiveData<ViewDataBean<SongPathBean>> querySongPath(TracksBean song) {
        if (AppUtils.isNetConnected()) {
            return remoteSongsDataSource.querySongPath(song);
        } else {
            return localSongsDataSource.querySongPath(song);
        }
    }

    public LiveData<ViewDataBean<SongWordBean>> querySongWord(TracksBean song) {
        if (AppUtils.isNetConnected()) {
            return remoteSongsDataSource.querySongWord(song);
        } else {
            return localSongsDataSource.querySongWord(song);
        }
    }

    public void downloadSongFile(SongPathBean songPathBean, String songName, OnResultListener<File> listener) {
        remoteSongsDataSource.downloadSongFile(songPathBean, songName, listener);
    }
}
