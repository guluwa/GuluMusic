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

    /**
     * 服务器数据
     */
    private RemoteSongsDataSource remoteSongsDataSource = RemoteSongsDataSource.getInstance();

    /**
     * 本地数据
     */
    private LocalSongsDataSource localSongsDataSource = LocalSongsDataSource.getInstance();

    public static SongsRepository getInstance() {
        return instance;
    }

    public SongsRepository() {
    }

    /**
     * 查询热门歌曲
     *
     * @param isFirstComing
     * @return
     */
    public LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong(boolean isFirstComing) {
        if (!isFirstComing) {
            return localSongsDataSource.queryNetCloudHotSong();
        }
        if (AppUtils.isNetConnected()) {
            return remoteSongsDataSource.queryNetCloudHotSong();
        } else {
            return localSongsDataSource.queryNetCloudHotSong();
        }
    }

    /**
     * 查询本地歌曲
     *
     * @return
     */
    public LiveData<ViewDataBean<List<LocalSongBean>>> queryLocalSong() {
        return localSongsDataSource.queryLocalSong();
    }

    /**
     * 查询歌曲路径
     *
     * @param song
     * @return
     */
    public LiveData<ViewDataBean<SongPathBean>> querySongPath(TracksBean song) {
        if (AppUtils.isNetConnected()) {
            return remoteSongsDataSource.querySongPath(song);
        } else {
            return localSongsDataSource.querySongPath(song);
        }
    }

    /**
     * 查询歌曲歌词
     *
     * @param song
     * @return
     */
    public LiveData<ViewDataBean<SongWordBean>> querySongWord(TracksBean song) {
        if (AppUtils.isNetConnected()) {
            return remoteSongsDataSource.querySongWord(song);
        } else {
            return localSongsDataSource.querySongWord(song);
        }
    }

    /**
     * 歌曲下载
     *
     * @param songPathBean
     * @param songName
     * @param listener
     */
    public void downloadSongFile(SongPathBean songPathBean, String songName, OnResultListener<File> listener) {
        remoteSongsDataSource.downloadSongFile(songPathBean, songName, listener);
    }
}
