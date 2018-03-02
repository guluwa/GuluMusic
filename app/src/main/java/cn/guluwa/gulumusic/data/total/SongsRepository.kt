package cn.guluwa.gulumusic.data.total

import android.arch.lifecycle.LiveData
import android.view.View

import java.io.File

import cn.guluwa.gulumusic.data.bean.FreshBean
import cn.guluwa.gulumusic.data.bean.LocalSongBean
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean
import cn.guluwa.gulumusic.data.bean.SongPathBean
import cn.guluwa.gulumusic.data.bean.SongWordBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.data.bean.ViewDataBean
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource
import cn.guluwa.gulumusic.data.local.db.DBHelper
import cn.guluwa.gulumusic.data.remote.RemoteSongsDataSource
import cn.guluwa.gulumusic.listener.OnResultListener
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.utils.AppUtils

/**
 * Created by guluwa on 2018/1/12.
 */

class SongsRepository {

    /**
     * 服务器数据
     */
    private val remoteSongsDataSource = RemoteSongsDataSource.getInstance()

    /**
     * 本地数据
     */
    private val localSongsDataSource = LocalSongsDataSource.getInstance()

    /**
     * 查询热门歌曲
     *
     * @param isFirstComing
     * @return
     */
    fun queryNetCloudHotSong(isFirstComing: Boolean): LiveData<ViewDataBean<List<TracksBean>>> {
        if (!isFirstComing) {
            return localSongsDataSource.queryNetCloudHotSong()
        }
        return if (AppUtils.isNetConnected) {
            remoteSongsDataSource.queryNetCloudHotSong()
        } else {
            localSongsDataSource.queryNetCloudHotSong()
        }
    }

    /**
     * 查询本地歌曲
     *
     * @return
     */
    fun queryLocalSong(): LiveData<ViewDataBean<List<LocalSongBean>>> {
        return localSongsDataSource.queryLocalSong()
    }

    /**
     * 歌曲搜索
     *
     * @return
     */
    fun searchSongByKeyWord(freshBean: FreshBean): LiveData<ViewDataBean<List<SearchResultSongBean>>> {
        return remoteSongsDataSource.searchSongByKeyWord(freshBean)
    }

    /**
     * 查询歌曲路径(首页)
     *
     * @param song
     * @return
     */
    fun querySongPath(song: TracksBean, listener: OnResultListener<SongPathBean>) {
        if (AppUtils.isNetConnected) {
            remoteSongsDataSource.querySongPath(song, listener)
        } else {
            localSongsDataSource.querySongPath(song, listener)
        }
    }

    /**
     * 查询歌曲歌词(首页)
     *
     * @param song
     * @return
     */
    fun querySongWord(song: TracksBean, listener: OnResultListener<SongWordBean>) {
        if (AppUtils.isNetConnected) {
            remoteSongsDataSource.querySongWord(song, listener)
        } else {
            localSongsDataSource.querySongWord(song, listener)
        }
    }

    /**
     * 查询歌曲封面图（搜索）
     */
    fun querySongPic(song: TracksBean, listener: OnResultListener<SongPathBean>) {
        remoteSongsDataSource.querySearchSongPic(song, listener)
    }

    /**
     * 歌曲下载
     *
     * @param songPathBean
     * @param songName
     * @param listener
     */
    fun downloadSongFile(songPathBean: SongPathBean, songName: String, listener: OnResultListener<File>) {
        remoteSongsDataSource.downloadSongFile(songPathBean, songName, listener)
    }

    object SingletonHolder {
        //单例（静态内部类）
        val instance = SongsRepository()
    }

    companion object {

        fun getInstance() = SingletonHolder.instance
    }
}
