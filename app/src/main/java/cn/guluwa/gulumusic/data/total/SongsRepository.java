package cn.guluwa.gulumusic.data.total;

import android.arch.lifecycle.LiveData;
import android.view.View;

import java.io.File;
import java.util.List;

import cn.guluwa.gulumusic.data.bean.FreshBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import cn.guluwa.gulumusic.data.remote.RemoteSongsDataSource;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.manage.AppManager;
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
     * 歌曲搜索
     *
     * @return
     */
    public LiveData<ViewDataBean<List<SearchResultSongBean>>> searchSongByKeyWord(FreshBean freshBean) {
        return remoteSongsDataSource.searchSongByKeyWord(freshBean);
    }

    /**
     * 查询歌曲路径(首页)
     *
     * @param song
     * @return
     */
    public void querySongPath(TracksBean song, OnResultListener<SongPathBean> listener) {
        if (AppUtils.isNetConnected()) {
            remoteSongsDataSource.querySongPath(song, listener);
        } else {
            localSongsDataSource.querySongPath(song, listener);
        }
    }

    /**
     * 查询歌曲歌词(首页)
     *
     * @param song
     * @return
     */
    public void querySongWord(TracksBean song, OnResultListener<SongWordBean> listener) {
        if (AppUtils.isNetConnected()) {
            remoteSongsDataSource.querySongWord(song, listener);
        } else {
            localSongsDataSource.querySongWord(song, listener);
        }
    }

    /**
     * 查询歌曲封面图（搜索）
     */
    public void querySongPic(TracksBean song, OnResultListener<SongPathBean> listener) {
        remoteSongsDataSource.querySearchSongPic(song, listener);
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
