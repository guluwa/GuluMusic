package cn.guluwa.gulumusic.service

import android.media.MediaPlayer
import android.os.Binder
import android.util.Log

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.TimeUnit

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.data.bean.BaseSongBean
import cn.guluwa.gulumusic.data.bean.LocalSongBean
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean
import cn.guluwa.gulumusic.data.bean.SongPathBean
import cn.guluwa.gulumusic.data.bean.SongWordBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource
import cn.guluwa.gulumusic.data.total.SongsRepository
import cn.guluwa.gulumusic.listener.OnResultListener
import cn.guluwa.gulumusic.listener.OnSongStatusListener
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.utils.AppUtils
import cn.guluwa.gulumusic.utils.RandomPicker
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by guluwa on 2018/2/2.
 */
class MusicBinder(val service: MusicAutoService) : Binder() {

    /**
     * 歌曲路径
     */
    private var mSongPath: String? = null

    /**
     * 播放器
     */
    var mediaPlayer: MediaPlayer? = null

    /**
     * 歌曲列表
     */
    private var mSongList: MutableList<BaseSongBean>? = null

    /**
     * 当前播放歌曲
     */
    var currentSong: TracksBean? = null
        private set

    /**
     * 歌曲播放进度
     */
    private val listeners: MutableList<OnSongStatusListener>

    /**
     * 随机数生成器
     */
    private var mRandomPicker: RandomPicker? = null

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
        listeners = ArrayList()
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnPreparedListener { isPrepare = true }
        mediaPlayer!!.setOnCompletionListener {
            when (AppManager.getInstance().playMode) {
                0//单曲循环
                -> {
                }
                1//顺序播放
                -> getNextSong(currentSong!!)
                2//随机播放
                -> getNextSong(currentSong!!)
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
     * @param mCurrentTime
     */
    fun playCurrentSong(song: TracksBean, mCurrentTime: Int, onlyDownload: Boolean) {
        if (!onlyDownload)
            currentSong = song
        //本地不存在，则去下载
        mSongPath = AppUtils.isExistFile(String.format("%s_%s.mp3", currentSong!!.name, currentSong!!.id), 1)
        if ("" == mSongPath) {
            isLoading = true
            querySongPath(song, onlyDownload)
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
                listeners[listeners.size - 1].download(song.index)
            }
            Observable.just("")
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map {
                        if (LocalSongsDataSource.getInstance().queryLocalSong(song.id, song.name) == null) {
                            LocalSongsDataSource.getInstance().addLocalSong(AppUtils.getLocalSongBean(song))
                        } else {
                            println("歌曲已存在")
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            if (!onlyDownload)
                playOrPauseSong(mCurrentTime)
        }
        if ("" == AppUtils.isExistFile(String.format("%s_%s.txt", currentSong!!.name, currentSong!!.id), 2)) {
            querySongWord(onlyDownload)
        }
        if ("" == song.al!!.picUrl) {
            querySongPic(onlyDownload)
        }
    }

    /**
     * 查询歌曲路径(首页、搜索)
     */
    private fun querySongPath(song: TracksBean, onlyDownload: Boolean) {
        SongsRepository.getInstance().querySongPath(currentSong!!, object : OnResultListener<SongPathBean> {
            override fun success(result: SongPathBean) {
                if ("" == result.url) {
                    if (listeners.isNotEmpty() && !onlyDownload) {
                        listeners[listeners.size - 1].error(service.getString(R.string.song_cant_plsy_tip))
                        getNextSong(currentSong!!)
                        listeners[listeners.size - 1].end(currentSong!!)
                    }
                } else {
                    SongsRepository.getInstance().downloadSongFile(result, String.format("%s_%s.mp3", result.song!!.name, result.song!!.id),
                            object : OnResultListener<File> {
                                override fun success(file: File) {
                                    if (listeners.isNotEmpty()) {
                                        listeners[listeners.size - 1].download(song.index)
                                    }
                                    println(file.absolutePath)
                                    mSongPath = file.absolutePath
                                    if (result.id == currentSong!!.id && !onlyDownload) {//下载完成的歌曲和当前播放歌曲是同一首
                                        isLoading = true
                                        if (mediaPlayer!!.isPlaying) {
                                            stop()
                                        }
                                        playOrPauseSong(0)
                                        if (listeners.isNotEmpty()) {
                                            listeners[listeners.size - 1].start()
                                        }
                                    }
                                }

                                override fun failed(error: String) {
                                    if (listeners.isNotEmpty() && !onlyDownload) {
                                        listeners[listeners.size - 1].error(error)
                                        getNextSong(currentSong!!)
                                        listeners[listeners.size - 1].end(currentSong!!)
                                    }
                                }
                            })
                }
            }

            override fun failed(error: String) {
                if (listeners.isNotEmpty() && !onlyDownload) {
                    listeners[listeners.size - 1].error(error)
                    getNextSong(currentSong!!)
                    listeners[listeners.size - 1].end(currentSong!!)
                }
            }
        })
    }

    /**
     * 查询歌曲歌词(首页、搜索)
     */
    private fun querySongWord(onlyDownload: Boolean) {
        SongsRepository.getInstance().querySongWord(currentSong!!, object : OnResultListener<SongWordBean> {
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
    private fun querySongPic(onlyDownload: Boolean) {
        SongsRepository.getInstance().querySongPic(currentSong!!, object : OnResultListener<SongPathBean> {
            override fun success(result: SongPathBean) {
                println(result.url)
                currentSong!!.al!!.picUrl = result.url
                mSongList!!.add(currentSong!!)
                if (listeners.isNotEmpty() && !onlyDownload) {
                    listeners[listeners.size - 1].pic(result.url)
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
     *
     * @param currentTime
     */
    fun playOrPauseSong(currentTime: Int) {
        if (!isPrepare) {
            if (service.audioFocusManager!!.requestAudioFocus()) {
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer!!.reset()
                        mediaPlayer!!.setDataSource(mSongPath)
                        mediaPlayer!!.prepare()
                        mediaPlayer!!.seekTo(currentTime)
                        mediaPlayer!!.start()
                        if (listeners.isNotEmpty()) {
                            listeners[listeners.size - 1].start()
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
                    }
                } else {
                    mediaPlayer!!.start()
                    if (listeners.isNotEmpty()) {
                        listeners[listeners.size - 1].start()
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
    fun getNextSong(song: TracksBean): TracksBean {
        var index = song.index
        if (AppManager.getInstance().playMode == 0 || AppManager.getInstance().playMode == 1) {
            if (index + 1 >= mSongList!!.size) {
                index = 0
            } else {
                index++
            }
            if (mSongList!![index] is TracksBean) {
                currentSong = mSongList!![index] as TracksBean
            } else {
                currentSong = AppUtils.getSongBean(mSongList!![index] as LocalSongBean)
            }
        } else {
            if (mRandomPicker == null) {
                mRandomPicker = RandomPicker(mSongList!!.size)
            }
            index = mRandomPicker!!.next()
            if (index >= mSongList!!.size) {
                index = 0
            }
            if (mSongList!![index] is TracksBean) {
                currentSong = mSongList!![index] as TracksBean
            } else {
                currentSong = AppUtils.getSongBean(mSongList!![index] as LocalSongBean)
            }
        }
        Log.d(MusicAutoService.TAG, currentSong!!.name)
        return currentSong as TracksBean
    }

    /**
     * 获取上一首歌
     *
     * @return
     */
    fun getLastSong(song: TracksBean): TracksBean {
        var index = song.index
        if (AppManager.getInstance().playMode == 0 || AppManager.getInstance().playMode == 1) {
            if (index - 1 < 0) {
                index = mSongList!!.size - 1
            } else {
                index--
            }
            currentSong = if (mSongList!![index] is TracksBean) {
                mSongList!![index] as TracksBean
            } else {
                AppUtils.getSongBean(mSongList!![index] as LocalSongBean)
            }
        } else {
            if (mRandomPicker == null) {
                mRandomPicker = RandomPicker(mSongList!!.size)
            }
            index = mRandomPicker!!.next()
            if (index >= mSongList!!.size) {
                index = 0
            }
            currentSong = if (mSongList!![index] is TracksBean) {
                mSongList!![index] as TracksBean
            } else {
                AppUtils.getSongBean(mSongList!![index] as LocalSongBean)
            }
        }
        Log.d(MusicAutoService.TAG, currentSong!!.name)
        return currentSong as TracksBean
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
        this.mSongList = mSongList
        mRandomPicker = null
        mRandomPicker = RandomPicker(mSongList.size)
    }
}
