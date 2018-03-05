package cn.guluwa.gulumusic.data.local.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import cn.guluwa.gulumusic.data.bean.*

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Created by guluwa on 2018/1/12.
 */

@Dao
interface SongsDao {

    /**
     * 查询热门歌曲
     *
     * @return
     */
    @Query("select * from net_cloud_hot_songs order by `index`")
    fun queryNetCloudHotSong(): LiveData<List<TracksBean>>

    /**
     * 查询本地歌曲
     *
     * @return
     */
    @Query("select * from local_songs order by `index`")
    fun queryLocalSong(): LiveData<List<LocalSongBean>>

    /**
     * 查询歌曲路径
     *
     * @param id
     * @return
     */
    @Query("select * from songs_path where id=:id")
    fun querySongPath(id: String): Single<List<SongPathBean>>

    /**
     * 查询歌曲歌词
     *
     * @param id
     * @return
     */
    @Query("select * from songs_words where id=:id")
    fun querySongWord(id: String): Single<List<SongWordBean>>

    /**
     * 查询本地歌曲（单曲）
     *
     * @param id
     * @param name
     * @return
     */
    @Query("select * from local_songs where id=:id and name=:name")
    fun queryLocalSong(id: String, name: String): LocalSongBean

    /**
     * 添加歌曲到热门歌曲表
     *
     * @param songs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSongs(songs: List<TracksBean>)

    /**
     * 添加歌曲到本地歌曲表
     *
     * @param songBean
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocalSong(songBean: LocalSongBean)

    /**
     * 从本地歌曲表删除歌曲
     *
     * @param songBean
     */
    @Delete
    fun deleteLocalSong(songBean: LocalSongBean)

    /**
     * 添加歌曲路径
     *
     * @param songPathBean
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSongPath(songPathBean: SongPathBean)

    /**
     * 添加歌曲歌词
     *
     * @param songWordBean
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSongWord(songWordBean: SongWordBean)

    /**
     * 添加新搜索记录
     *
     * @param searchHistoryBean
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSearchHistory(searchHistoryBean: SearchHistoryBean)

    /**
     * 查询搜索记录
     *
     * @return
     */
    @Query("select * from search_history order by `date` desc limit 5")
    fun querySearchRecord(): Single<List<SearchHistoryBean>>
}
