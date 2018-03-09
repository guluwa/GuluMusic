package cn.guluwa.gulumusic.data.total

import android.arch.lifecycle.LiveData

import cn.guluwa.gulumusic.data.bean.SongPathBean
import cn.guluwa.gulumusic.data.bean.SongWordBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.data.bean.ViewDataBean
import cn.guluwa.gulumusic.utils.listener.OnResultListener

/**
 * Created by guluwa on 2018/1/12.
 */

interface SongDataSource {

    /**
     * 查询热门歌曲
     *
     * @return
     */
    fun queryNetCloudHotSong(): LiveData<ViewDataBean<List<TracksBean>>>

    /**
     * 查询歌曲路径(首页)
     *
     * @param song
     * @param listener
     * @return
     */
    fun querySongPath(song: TracksBean, listener: OnResultListener<SongPathBean>)

    /**
     * 查询歌曲歌词(首页)
     *
     * @param song
     * @param listener
     * @return
     */
    fun querySongWord(song: TracksBean, listener: OnResultListener<SongWordBean>)
}
