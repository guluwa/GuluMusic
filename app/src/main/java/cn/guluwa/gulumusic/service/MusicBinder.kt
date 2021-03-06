package cn.guluwa.gulumusic.service

import android.media.MediaPlayer
import android.os.Binder
import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.data.bean.*
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource
import cn.guluwa.gulumusic.data.total.SongsRepository
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.service.notification.MyNotificationManager
import cn.guluwa.gulumusic.utils.AppUtils
import cn.guluwa.gulumusic.utils.listener.OnResultListener
import cn.guluwa.gulumusic.utils.listener.OnSongStatusListener
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by guluwa on 2018/2/2.
 */
class MusicBinder(val service: MusicAutoService) : Binder() {

    /**
     * 歌曲路径
     */
    var mSongPath: String = ""

    /**
     * 播放器
     */
    var mediaPlayer: MediaPlayer? = null

    /**
     * 歌曲列表
     */
    var mSongList: MutableList<BaseSongBean>? = null

    /**
     * 当前播放歌曲位置
     */
    var mCurrentPosition: Int = -1

    /**
     * 当前播放歌曲
     */
    var currentSong: TracksBean? = null
        set(value) {
            field = value
            if (mSongList != null)
                mCurrentPosition = AppUtils.locationCurrentSongShow(field, mSongList)
        }

    /**
     * 歌曲播放进度
     */
    private val listeners: MutableList<OnSongStatusListener>

    /**
     * 歌曲进度轮询
     */
    private var disposable: Disposable? = null

    /**
     * 歌曲是否正在加载
     */
    var isLoading: Boolean = false
        private set

    /**
     * mediaPlayer是否准备完成
     */
    var isPrepare: Boolean = false

    init {
        isLoading = false
        currentSong = Gson().fromJson(AppUtils.getString("mCurrentSong", ""), TracksBean::class.java)
        listeners = ArrayList()
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnPreparedListener { isPrepare = true }
        mediaPlayer!!.setOnCompletionListener {
            when (AppManager.getInstance().playMode) {
                0//单曲循环
                -> playCurrentSong(null, false)
                1, 2//顺序播放,随机播放
                -> {
                    getNextSong()
                    isPrepare = false
                    playCurrentSong(null, false)
                }
            }
            if (listeners.isNotEmpty()) {
                listeners[listeners.size - 1].end(currentSong!!)
            }
        }
        mSongList = ArrayList()
        bindProgressQuery()
    }

    /**
     * 绑定播放结束监听
     *
     * @param listener
     */
    fun bindSongStatusListener(listener: OnSongStatusListener) {
        listeners.add(listener)
        println("listeners: " + listeners.size)
    }

    /**
     * 移除播放结束监听
     */
    fun unBindSongStatusListener(listener: OnSongStatusListener) {
        listeners.remove(listener)
    }

    /**
     * 播放当前歌曲
     *
     * @param song
     * @param onlyDownload
     */
    fun playCurrentSong(song: TracksBean?, onlyDownload: Boolean) {

        //本地不存在，则去下载
        if (!onlyDownload)
            mSongPath = AppUtils.isExistFile(String.format("%s_%s.mp3", currentSong!!.name, currentSong!!.id), 1)
        else
            mSongPath = AppUtils.isExistFile(String.format("%s_%s.mp3", song!!.name, song.id), 1)
        println(mSongPath)
        if ("" == mSongPath) {
            isLoading = true
            if (onlyDownload)
                querySongPath(song, onlyDownload)
            else
                querySongPath(currentSong, onlyDownload)
            if (listeners.isNotEmpty() && !onlyDownload) {
                listeners[listeners.size - 1].loading()
            }
        } else {
            isLoading = false
            if (listeners.isNotEmpty() && !onlyDownload) {
                listeners[listeners.size - 1].start()
            }
            if (mediaPlayer!!.isPlaying && !onlyDownload) {
                stop()
            }
            if (listeners.isNotEmpty()) {
                listeners[listeners.size - 1].download(currentSong!!.index)
            }
            Observable.just("")
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map {
                        if (LocalSongsDataSource.getInstance().queryLocalSong(currentSong!!.id, currentSong!!.name) == null) {
                            LocalSongsDataSource.getInstance().addLocalSong(AppUtils.getLocalSongBean(currentSong!!))
                        } else {
                            println("歌曲已存在")
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            if (!onlyDownload)
                playOrPauseSong()
        }
    }

    /**
     * 查询歌曲路径(首页、搜索)
     */
    private fun querySongPath(song: TracksBean?, onlyDownload: Boolean) {
        SongsRepository.getInstance().querySongPath(song!!, object : OnResultListener<SongPathBean> {
            override fun success(result: SongPathBean) {
                if ("" == result.url) {
                    if (listeners.isNotEmpty() && !onlyDownload) {
                        listeners[listeners.size - 1].error(service.getString(R.string.song_cant_plsy_tip))
                        getNextSong()
                        listeners[listeners.size - 1].end(currentSong!!)
                    }
                } else {
                    if ("" == AppUtils.isExistFile(String.format("%s_%s.txt", song.name, song.id), 2)) {
                        querySongWord(song, onlyDownload)
                    }
                    if ("" == song.al!!.picUrl) {
                        querySongPic(song, onlyDownload)
                    }
                    SongsRepository.getInstance().downloadSongFile(result, String.format("%s_%s.mp3", result.song!!.name, result.song!!.id),
                            object : OnResultListener<File> {
                                override fun success(file: File) {
                                    if (listeners.isNotEmpty()) {
                                        listeners[listeners.size - 1].download(song.index)
                                    }
                                    AppManager.getInstance().isDownLoadSong = true
                                    println(file.absolutePath)
                                    mSongPath = file.absolutePath
                                    if (result.id == currentSong!!.id && !onlyDownload) {//下载完成的歌曲和当前播放歌曲是同一首
                                        isLoading = true
                                        if (mediaPlayer!!.isPlaying) {
                                            stop()
                                        }
                                        playOrPauseSong()
                                        if (listeners.isNotEmpty()) {
                                            listeners[listeners.size - 1].start()
                                        }
                                    }
                                }

                                override fun failed(error: String) {
                                    if (listeners.isNotEmpty() && !onlyDownload) {
                                        listeners[listeners.size - 1].error(error)
                                        getNextSong()
                                        listeners[listeners.size - 1].end(currentSong!!)
                                    }
                                }
                            })
                }
            }

            override fun failed(error: String) {
                if (listeners.isNotEmpty() && !onlyDownload) {
                    listeners[listeners.size - 1].error(error)
                    getNextSong()
                    listeners[listeners.size - 1].end(currentSong!!)
                }
            }
        })
    }

    /**
     * 查询歌曲歌词(首页、搜索)
     */
    private fun querySongWord(song: TracksBean?, onlyDownload: Boolean) {
        SongsRepository.getInstance().querySongWord(song!!, object : OnResultListener<SongWordBean> {
            override fun success(result: SongWordBean) {
                AppUtils.writeWord2Disk(result.lyric!!, String.format("%s_%s.txt", result.song!!.name, result.song!!.id))
            }

            override fun failed(error: String) {
                if (listeners.isNotEmpty() && !onlyDownload) {
                    listeners[listeners.size - 1].error(error)
                }
            }
        })
    }

    /**
     * 查询歌曲封面图（首页、搜索）
     */
    private fun querySongPic(song: TracksBean?, onlyDownload: Boolean) {
        SongsRepository.getInstance().querySongPic(song!!, object : OnResultListener<SongPathBean> {
            override fun success(result: SongPathBean) {
                println(result.url)
                if (listeners.isNotEmpty() && !onlyDownload) {
                    listeners[listeners.size - 1].pic(result.url)
                    currentSong!!.al!!.picUrl = result.url
                    mSongList!!.add(currentSong!!)
                }
            }

            override fun failed(error: String) {
                if (listeners.isNotEmpty() && !onlyDownload) {
                    listeners[listeners.size - 1].error(error)
                }
            }
        })
    }

    /**
     * 结束播放（开始下一首）
     */
    fun stop() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            isPrepare = false
            if (listeners.isNotEmpty()) {
                listeners[listeners.size - 1].pause()
            }
        }
    }

    /**
     * 开始或暂停播放
     */
    fun playOrPauseSong() {
        if (!isPrepare) {
            if (service.audioFocusManager!!.requestAudioFocus()) {
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer!!.reset()
                        mediaPlayer!!.setDataSource(mSongPath)
                        mediaPlayer!!.prepare()
                        mediaPlayer!!.seekTo(currentSong!!.currentTime)
                        mediaPlayer!!.start()
                        if (listeners.isNotEmpty()) {
                            listeners[listeners.size - 1].start()
                            service.initNotification()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        } else {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                    if (listeners.isNotEmpty()) {
                        listeners[listeners.size - 1].pause()
                        MyNotificationManager.getInstance().setPauseStatus()
                    }
                } else {
                    mediaPlayer!!.start()
                    if (listeners.isNotEmpty()) {
                        listeners[listeners.size - 1].resume()
                        service.initNotification()
                    }
                }
            }
        }
    }

    /**
     * 获取下一首歌
     *
     * @return
     */
    fun getNextSong() {
        if (mSongList != null && mSongList!!.size != 0) {
            if (listeners.isNotEmpty()) {
                listeners[listeners.size - 1].end(currentSong!!)
            }
            if (mCurrentPosition + 1 >= mSongList!!.size) {
                mCurrentPosition = 0
            } else {
                mCurrentPosition++
            }
            currentSong = if (mSongList!![mCurrentPosition] is TracksBean) {
                mSongList!![mCurrentPosition] as TracksBean
            } else {
                AppUtils.getSongBean(mSongList!![mCurrentPosition] as LocalSongBean)
            }
        }
    }

    /**
     * 获取上一首歌
     *
     * @return
     */
    fun getLastSong() {
        if (mSongList != null && mSongList!!.size != 0) {
            if (listeners.isNotEmpty()) {
                listeners[listeners.size - 1].end(currentSong!!)
            }
            if (mCurrentPosition - 1 < 0) {
                mCurrentPosition = mSongList!!.size - 1
            } else {
                mCurrentPosition--
            }
            currentSong = if (mSongList!![mCurrentPosition] is TracksBean) {
                mSongList!![mCurrentPosition] as TracksBean
            } else {
                AppUtils.getSongBean(mSongList!![mCurrentPosition] as LocalSongBean)
            }
        }
    }

    /**
     * 从歌单中移除歌曲
     */
    fun removeCurrentSong() {
        if (mSongList != null && mSongList!!.size != 0) {
            if (mCurrentPosition >= mSongList!!.size) {
                mCurrentPosition = 0
            }
            currentSong = if (mSongList!![mCurrentPosition] is TracksBean) {
                mSongList!![mCurrentPosition] as TracksBean
            } else {
                AppUtils.getSongBean(mSongList!![mCurrentPosition] as LocalSongBean)
            }
            playCurrentSong(null, false)
        }
    }

    /**
     * 开始轮询
     */
    private fun bindProgressQuery() {
        if (disposable == null) {
            disposable = Observable.interval(0, 150, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (mediaPlayer!!.isPlaying) {
                            if (listeners.isNotEmpty()) {
                                listeners[listeners.size - 1].progress(mediaPlayer!!.currentPosition, mediaPlayer!!.duration)
                                currentSong!!.currentTime = mediaPlayer!!.currentPosition
                                currentSong!!.duration = mediaPlayer!!.duration
                            }
                        }
                    }
        }
    }

    /**
     * 结束轮询
     */
    fun unbindProgressQuery() {
        if (disposable != null && !disposable!!.isDisposed) {
            disposable!!.dispose()
            disposable = null
        }
    }

    fun setSongList(mSongList: MutableList<BaseSongBean>) {
        val list = arrayListOf<BaseSongBean>()
        list.addAll(mSongList)
        if (AppManager.getInstance().playMode == 2) {
            list.shuffle()
            this.mSongList!!.clear()
            this.mSongList!!.addAll(list)
            mCurrentPosition = AppUtils.locationCurrentSongShow(currentSong, this.mSongList)
        } else {
            this.mSongList!!.clear()
            this.mSongList!!.addAll(list)
        }
    }
}
