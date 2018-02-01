package cn.guluwa.gulumusic.data.local.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;

/**
 * Created by guluwa on 2018/1/12.
 */

@Dao
public interface SongsDao {

    /**
     * 查询热门歌曲
     *
     * @return
     */
    @Query("select * from net_cloud_hot_songs order by `index`")
    LiveData<List<TracksBean>> queryNetCloudHotSong();

    /**
     * 查询本地歌曲
     *
     * @return
     */
    @Query("select * from local_songs order by `index`")
    LiveData<List<LocalSongBean>> queryLocalSong();

    /**
     * 查询歌曲路径
     *
     * @param id
     * @return
     */
    @Query("select * from songs_path where id=:id")
    LiveData<SongPathBean> querySongPath(String id);

    /**
     * 查询歌曲歌词
     *
     * @param id
     * @return
     */
    @Query("select * from songs_words where id=:id")
    LiveData<SongWordBean> querySongWord(String id);

    /**
     * 查询本地歌曲（单曲）
     *
     * @param id
     * @param name
     * @return
     */
    @Query("select * from local_songs where id=:id and name=:name")
    LocalSongBean queryLocalSong(int id, String name);

    /**
     * 添加歌曲到热门歌曲表
     *
     * @param songs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSongs(List<TracksBean> songs);

    /**
     * 添加歌曲到本地歌曲表
     *
     * @param songBean
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addLocalSong(LocalSongBean songBean);

    /**
     * 添加歌曲路径
     *
     * @param songPathBean
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSongPath(SongPathBean songPathBean);

    /**
     * 添加歌曲歌词
     *
     * @param songWordBean
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSongWord(SongWordBean songWordBean);
}
