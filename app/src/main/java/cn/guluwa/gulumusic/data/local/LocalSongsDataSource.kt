package cn.guluwa.gulumusic.data.local

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import cn.guluwa.gulumusic.data.bean.*

import cn.guluwa.gulumusic.data.total.SongDataSource
import cn.guluwa.gulumusic.utils.listener.OnResultListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by guluwa on 2018/1/12.
 */

class LocalSongsDataSource : SongDataSource {

    /**
     * 本地数据库服务
     */
    private val songsService = SongsServiceImpl.getInstance()

    /**
     * 查询热门歌曲
     *
     * @return
     */
    override fun queryNetCloudHotSong(): LiveData<ViewDataBean<List<TracksBean>>> {
        val data = MediatorLiveData<ViewDataBean<List<TracksBean>>>()
        data.value = ViewDataBean.loading()

        data.addSource<List<TracksBean>>(songsService.queryNetCloudHotSong()) { tracksBeans ->
            if (null == tracksBeans || tracksBeans.isEmpty()) {
                data.setValue(ViewDataBean.empty())
            } else {
                Observable.just("")
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map {
                            for (i in 0 until tracksBeans.size) {
                                if (LocalSongsDataSource.getInstance().queryLocalSong(tracksBeans[i].id, tracksBeans[i].name) != null) {
                                    tracksBeans[i].local = true
                                }
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ data.setValue(ViewDataBean.content(tracksBeans)) })
            }
        }
        return data
    }

    /**
     * 查询歌曲路径
     *
     * @param song
     * @return
     */
    override fun querySongPath(song: TracksBean, listener: OnResultListener<SongPathBean>) {
        songsService.querySongPath(song.id)
                .subscribe({ songPathBeans ->
                    if (songPathBeans == null || songPathBeans.isEmpty()) {
                        listener.failed("本地找不到哦，请连接网络后播放")
                    } else {
                        listener.success(songPathBeans[0])
                    }
                }, { listener.failed(it.message!!) })
    }

    /**
     * 查询歌曲歌词
     *
     * @param song
     * @return
     */
    override fun querySongWord(song: TracksBean, listener: OnResultListener<SongWordBean>) {
        songsService.querySongWord(song.id)
                .subscribe({ songWordBeans ->
                    if (songWordBeans == null || songWordBeans.isEmpty()) {
                        listener.failed("本地找不到哦，请连接网络后播放")
                    } else {
                        listener.success(songWordBeans[0])
                    }
                }, { listener.failed(it.message!!) })
    }

    /**
     * 查询本地歌曲
     *
     * @return
     */
    fun queryLocalSong(): LiveData<ViewDataBean<List<LocalSongBean>>> {
        val data = MediatorLiveData<ViewDataBean<List<LocalSongBean>>>()
        data.value = ViewDataBean.loading()

        data.addSource<List<LocalSongBean>>(songsService.queryLocalSong()) { localSongBeans ->
            if (localSongBeans == null || localSongBeans.isEmpty()) {
                data.setValue(ViewDataBean.empty())
            } else {
                data.setValue(ViewDataBean.content(localSongBeans))
            }
        }
        return data
    }

    /**
     * 查询本地歌曲（单曲）
     *
     * @param id
     * @param name
     * @return
     */
    fun queryLocalSong(id: String, name: String): LocalSongBean {
        return songsService.queryLocalSong(id, name)
    }

    /**
     * 添加歌曲到热门歌曲表
     *
     * @param songs
     */
    fun addSongs(songs: List<TracksBean>) {
        songsService.addSongs(songs)
    }

    /**
     * 添加歌曲路径
     *
     * @param songPathBean
     */
    fun addSongPath(songPathBean: SongPathBean) {
        songsService.addSongPath(songPathBean)
    }

    /**
     * 添加歌曲歌词
     *
     * @param songWordBean
     */
    fun addSongWord(songWordBean: SongWordBean) {
        songsService.addSongWord(songWordBean)
    }

    /**
     * 添加歌曲到本地歌曲表
     *
     * @param localSongBean
     */
    fun addLocalSong(localSongBean: LocalSongBean) {
        songsService.addLocalSong(localSongBean)
    }

    /**
     * 从本地歌曲表删除歌曲
     *
     * @param localSongBean
     */
    fun deleteLocalSong(localSongBean: LocalSongBean) {
        songsService.deleteLocalSong(localSongBean)
    }

    /**
     * 添加新搜索记录
     *
     * @param searchHistoryBean
     */
    fun addSearchHistory(searchHistoryBean: SearchHistoryBean) {
        songsService.addSearchHistory(searchHistoryBean)
    }

    /**
     * 查询搜索记录
     *
     * @return
     */
    fun querySearchRecord(listener: OnResultListener<List<SearchHistoryBean>>) {
        songsService.querySearchRecord()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ listener.success(it) }, { listener.failed(it.message!!) })
    }

    object SingletonHolder {
        //单例（静态内部类）
        val instance = LocalSongsDataSource()
    }

    companion object {

        fun getInstance() = SingletonHolder.instance
    }
}
