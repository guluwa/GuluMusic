package cn.guluwa.gulumusic.data.remote

import android.arch.lifecycle.LiveData

import java.io.File
import java.util.HashMap

import cn.guluwa.gulumusic.data.bean.FreshBean
import cn.guluwa.gulumusic.data.bean.LocalSongBean
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean
import cn.guluwa.gulumusic.data.bean.SongPathBean
import cn.guluwa.gulumusic.data.bean.SongWordBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.data.bean.ViewDataBean
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource
import cn.guluwa.gulumusic.data.remote.LiveDataObservableAdapter.fromObservableViewData
import cn.guluwa.gulumusic.data.remote.retrofit.RetrofitWorker
import cn.guluwa.gulumusic.data.total.SongDataSource
import cn.guluwa.gulumusic.listener.OnResultListener
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.manage.Contacts
import cn.guluwa.gulumusic.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

/**
 * Created by guluwa on 2018/1/12.
 */

class RemoteSongsDataSource : SongDataSource {

    /**
     * 查询热门歌曲
     *
     * @return
     */
    override fun queryNetCloudHotSong(): LiveData<ViewDataBean<List<TracksBean>>> {
        val map = HashMap<String, String>()
        map["types"] = "playlist"
        map["id"] = Contacts.NET_CLOUD_HOT_ID
        return fromObservableViewData(
                RetrofitWorker.retrofitWorker
                        .obtainNetCloudHot(Contacts.SONG_CALLBACK, map)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map<List<TracksBean>> { playListBean ->
                            for (i in 0 until playListBean.playlist!!.tracks!!.size) {
                                playListBean.playlist!!.tracks!![i].singer =
                                        if (playListBean.playlist!!.tracks!![i].ar!!.isEmpty())
                                            null
                                        else
                                            playListBean.playlist!!.tracks!![i].ar!![0]
                                playListBean.playlist!!.tracks!![i].tag =
                                        if (playListBean.playlist!!.tracks!![i].alia!!.isEmpty())
                                            ""
                                        else
                                            playListBean.playlist!!.tracks!![i].alia!![0]
                                playListBean.playlist!!.tracks!![i].source = "netease"
                                playListBean.playlist!!.tracks!![i].index = i
                            }
                            LocalSongsDataSource.getInstance().addSongs(playListBean.playlist!!.tracks!!)
                            playListBean.playlist!!.tracks
                        }
                        .observeOn(AndroidSchedulers.mainThread())
        )
    }

    /**
     * 歌曲搜索
     *
     * @param freshBean
     * @return
     */
    fun searchSongByKeyWord(freshBean: FreshBean): LiveData<ViewDataBean<List<SearchResultSongBean>>> {
        val map = HashMap<String, String>()
        map["types"] = "search"
        map["count"] = "20"
        map["source"] = AppManager.getInstance().searchPlatform
        map["pages"] = freshBean.page.toString()
        map["name"] = freshBean.key
        return fromObservableViewData(
                RetrofitWorker.retrofitWorker
                        .searchSongByKeyWord(Contacts.SONG_CALLBACK, map)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map { searchResultSongBeans ->
                            for (i in searchResultSongBeans.indices) {
                                val queryLocalSong = LocalSongsDataSource.getInstance().queryLocalSong(
                                        searchResultSongBeans[i].id, searchResultSongBeans[i].name)
                                println(queryLocalSong)
                                searchResultSongBeans[i].isDownLoad = queryLocalSong != null
                            }
                            searchResultSongBeans
                        }
                        .observeOn(AndroidSchedulers.mainThread())
        )
    }

    /**
     * 查询歌曲路径(首页)
     *
     * @param song
     * @return
     */
    override fun querySongPath(song: TracksBean, listener: OnResultListener<SongPathBean>) {
        val map = HashMap<String, String>()
        map["types"] = "url"
        map["id"] = if ("" == song.url_id) song.id else song.url_id
        map["source"] = song.source
        RetrofitWorker.retrofitWorker
                .obtainSongPath(Contacts.SONG_CALLBACK, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { songPathBean ->
                    songPathBean.song = song
                    songPathBean.id = song.id
                    LocalSongsDataSource.getInstance().addSongPath(songPathBean)
                    songPathBean
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ listener.success(it) }, { listener.failed(it.message!!) })
    }

    /**
     * 查询歌曲歌词(首页)
     *
     * @param song
     * @return
     */
    override fun querySongWord(song: TracksBean, listener: OnResultListener<SongWordBean>) {
        val map = HashMap<String, String>()
        map["types"] = "lyric"
        map["id"] = if ("" == song.lyric_id) song.id else song.lyric_id
        map["source"] = song.source
        RetrofitWorker.retrofitWorker
                .obtainSongWord(Contacts.SONG_CALLBACK, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { songWordBean ->
                    songWordBean.song = song
                    songWordBean.id = song.id
                    LocalSongsDataSource.getInstance().addSongWord(songWordBean)
                    songWordBean
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ listener.success(it) }, { listener.failed(it.message!!) })
    }

    /**
     * 查询歌曲封面图(搜索)
     *
     * @param song
     * @param listener
     */
    fun querySearchSongPic(song: TracksBean, listener: OnResultListener<SongPathBean>) {
        val map = HashMap<String, String>()
        map["types"] = "pic"
        map["id"] = if ("" == song.pic_id) song.id else song.pic_id
        map["source"] = song.source
        RetrofitWorker.retrofitWorker
                .obtainSongPath(Contacts.SONG_CALLBACK, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { songPathBean ->
                    if (LocalSongsDataSource.getInstance().queryLocalSong(song.id, song.name) == null) {
                        song.al!!.picUrl = songPathBean.url
                        LocalSongsDataSource.getInstance().addLocalSong(AppUtils.getLocalSongBean(song))
                    } else {
                        println("歌曲已存在")
                    }
                    songPathBean
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ listener.success(it) }, { listener.failed(it.message!!) })
    }

    /**
     * 歌曲下载
     *
     * @param songPathBean
     * @param songName
     * @param listener
     */
    fun downloadSongFile(songPathBean: SongPathBean, songName: String, listener: OnResultListener<File>) {
        RetrofitWorker.retrofitWorker
                .downloadSongFile(songPathBean.url)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map<ResponseBody> { responseBody ->
                    if (LocalSongsDataSource.getInstance().queryLocalSong(songPathBean.id, songPathBean.song!!.name) == null) {
                        LocalSongsDataSource.getInstance().addLocalSong(AppUtils.getLocalSongBean(songPathBean.song!!))
                    } else {
                        println("歌曲已存在")
                    }
                    responseBody
                }
                .observeOn(Schedulers.io())
                .map { responseBody -> AppUtils.writeSong2Disk(responseBody, songName) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ listener.success(it!!) }, {
                    println(it.message)
                    listener.failed("歌曲下载失败")
                })
    }

    object SingletonHolder {
        //单例（静态内部类）
        val instance = RemoteSongsDataSource()
    }

    companion object {

        fun getInstance()= SingletonHolder.instance
    }
}
