package cn.guluwa.gulumusic.data.local;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.view.View;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.total.SongDataSource;
import cn.guluwa.gulumusic.listener.OnResultListener;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by guluwa on 2018/1/12.
 */

public class LocalSongsDataSource implements SongDataSource {

    public static final LocalSongsDataSource instance = new LocalSongsDataSource();

    public static LocalSongsDataSource getInstance() {
        return instance;
    }

    /**
     * 本地数据库服务
     */
    private SongsService songsService = SongsServiceImpl.getInstance();

    public LocalSongsDataSource() {
    }

    /**
     * 查询热门歌曲
     *
     * @return
     */
    @Override
    public LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong() {
        MediatorLiveData<ViewDataBean<List<TracksBean>>> data = new MediatorLiveData<>();
        data.setValue(ViewDataBean.loading());

        data.addSource(songsService.queryNetCloudHotSong(), tracksBeans -> {
            if (null == tracksBeans || tracksBeans.size() == 0) {
                data.setValue(ViewDataBean.empty());
            } else {
                data.setValue(ViewDataBean.content(tracksBeans));
            }
        });
        return data;
    }

    /**
     * 查询歌曲路径
     *
     * @param song
     * @return
     */
    @Override
    public void querySongPath(TracksBean song, OnResultListener<SongPathBean> listener) {
        songsService.querySongPath(String.valueOf(song.getId()))
                .subscribe(songPathBeans -> {
                    if (songPathBeans == null || songPathBeans.size() == 0) {
                        listener.failed("本地找不到哦，请连接网络后播放");
                    } else {
                        listener.success(songPathBeans.get(0));
                    }
                }, throwable -> listener.failed(throwable.getMessage()));
    }

    /**
     * 查询歌曲歌词
     *
     * @param song
     * @return
     */
    @Override
    public void querySongWord(TracksBean song, OnResultListener<SongWordBean> listener) {
        songsService.querySongWord(String.valueOf(song.getId()))
                .subscribe(songWordBeans -> {
                    if (songWordBeans == null || songWordBeans.size() == 0) {
                        listener.failed("本地找不到哦，请连接网络后播放");
                    } else {
                        listener.success(songWordBeans.get(0));
                    }
                }, throwable -> {
                    listener.failed(throwable.getMessage());
                });
    }

    /**
     * 查询本地歌曲
     *
     * @return
     */
    public LiveData<ViewDataBean<List<LocalSongBean>>> queryLocalSong() {
        MediatorLiveData<ViewDataBean<List<LocalSongBean>>> data = new MediatorLiveData<>();
        data.setValue(ViewDataBean.loading());

        data.addSource(songsService.queryLocalSong(), localSongBeans -> {
            if (localSongBeans == null || localSongBeans.size() == 0) {
                data.setValue(ViewDataBean.empty());
            } else {
                data.setValue(ViewDataBean.content(localSongBeans));
            }
        });
        return data;
    }

    /**
     * 查询本地歌曲（单曲）
     *
     * @param id
     * @param name
     * @return
     */
    public LocalSongBean queryLocalSong(String id, String name) {
        return songsService.queryLocalSong(id, name);
    }

    /**
     * 添加歌曲到热门歌曲表
     *
     * @param songs
     */
    public void addSongs(List<TracksBean> songs) {
        songsService.addSongs(songs);
    }

    /**
     * 添加歌曲路径
     *
     * @param songPathBean
     */
    public void addSongPath(SongPathBean songPathBean) {
        songsService.addSongPath(songPathBean);
    }

    /**
     * 添加歌曲歌词
     *
     * @param songWordBean
     */
    public void addSongWord(SongWordBean songWordBean) {
        songsService.addSongWord(songWordBean);
    }

    /**
     * 添加歌曲到本地歌曲表
     *
     * @param localSongBean
     */
    public void addLocalSong(LocalSongBean localSongBean) {
        songsService.addLocalSong(localSongBean);
    }
}
