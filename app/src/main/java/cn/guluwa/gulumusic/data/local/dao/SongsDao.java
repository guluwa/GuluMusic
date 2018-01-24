package cn.guluwa.gulumusic.data.local.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;

/**
 * Created by guluwa on 2018/1/12.
 */

@Dao
public interface SongsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSongs(List<TracksBean> songs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSongPath(SongPathBean songPathBean);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSongWord(SongWordBean songWordBean);

    @Query("select * from net_cloud_hot_songs order by `index`")
    LiveData<List<TracksBean>> queryNetCloudHotSong();

    @Query("select * from songs_path where id=:id")
    LiveData<SongPathBean> querySongPath(String id);

    @Query("select * from songs_words where id=:id")
    LiveData<SongWordBean> querySongWord(String id);
}
