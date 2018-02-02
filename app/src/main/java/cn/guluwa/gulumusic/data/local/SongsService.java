package cn.guluwa.gulumusic.data.local;

import android.arch.lifecycle.LiveData;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * Created by guluwa on 2018/1/12.
 */

public interface SongsService {

    /**
     * 查询热门歌曲
     *
     * @return
     */
    LiveData<List<TracksBean>> queryNetCloudHotSong();

    /**
     * 查询本地歌曲
     *
     * @return
     */
    LiveData<List<LocalSongBean>> queryLocalSong();

    /**
     * 查询本地歌曲（单曲）
     *
     * @param id
     * @param name
     * @return
     */
    LocalSongBean queryLocalSong(int id, String name);

    /**
     * 查询歌曲路径
     *
     * @param id
     * @return
     */
    Flowable<List<SongPathBean>> querySongPath(String id);

    /**
     * 查询歌曲歌词
     *
     * @param id
     * @return
     */
    Flowable<List<SongWordBean>> querySongWord(String id);

    /**
     * 添加歌曲到热门歌曲表
     *
     * @param songs
     */
    void addSongs(List<TracksBean> songs);

    /**
     * 添加歌曲到本地歌曲表
     *
     * @param localSongBean
     */
    void addLocalSong(LocalSongBean localSongBean);

    /**
     * 添加歌曲路径
     *
     * @param songPathBean
     */
    void addSongPath(SongPathBean songPathBean);

    /**
     * 添加歌曲歌词
     *
     * @param songWordBean
     */
    void addSongWord(SongWordBean songWordBean);
}
