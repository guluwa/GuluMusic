package cn.guluwa.gulumusic.data.local

import android.arch.lifecycle.LiveData

import cn.guluwa.gulumusic.data.bean.LocalSongBean
import cn.guluwa.gulumusic.data.bean.SongPathBean
import cn.guluwa.gulumusic.data.bean.SongWordBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import io.reactivex.Flowable
import io.reactivex.Maybe

/**
 * Created by guluwa on 2018/1/12.
 */

interface SongsService {

    /**
     * 查询热门歌曲
     *
     * @return
     */
    fun queryNetCloudHotSong(): LiveData<List<TracksBean>>

    /**
     * 查询本地歌曲
     *
     * @return
     */
    fun queryLocalSong(): LiveData<List<LocalSongBean>>

    /**
     * 查询本地歌曲（单曲）
     *
     * @param id
     * @param name
     * @return
     */
    fun queryLocalSong(id: String, name: String): LocalSongBean

    /**
     * 查询歌曲路径
     *
     * @param id
     * @return
     */
    fun querySongPath(id: String): Flowable<List<SongPathBean>>

    /**
     * 查询歌曲歌词
     *
     * @param id
     * @return
     */
    fun querySongWord(id: String): Flowable<List<SongWordBean>>

    /**
     * 添加歌曲到热门歌曲表
     *
     * @param songs
     */
    fun addSongs(songs: List<TracksBean>)

    /**
     * 添加歌曲到本地歌曲表
     *
     * @param localSongBean
     */
    fun addLocalSong(localSongBean: LocalSongBean)

    /**
     * 从本地歌曲表删除歌曲
     *
     * @param localSongBean
     */
    fun deleteLocalSong(localSongBean: LocalSongBean)

    /**
     * 添加歌曲路径
     *
     * @param songPathBean
     */
    fun addSongPath(songPathBean: SongPathBean)

    /**
     * 添加歌曲歌词
     *
     * @param songWordBean
     */
    fun addSongWord(songWordBean: SongWordBean)
}
