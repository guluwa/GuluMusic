package cn.guluwa.gulumusic.utils.listener

import cn.guluwa.gulumusic.data.bean.TracksBean

/**
 * Created by guluwa on 2018/2/2.
 */

interface OnSongStatusListener {

    /**
     * 加载
     */
    fun loading()

    /**
     * 开始
     */
    fun start()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 继续
     */
    fun resume()

    /**
     * 结束
     *
     * @param tracksBean
     */
    fun end(tracksBean: TracksBean)

    /**
     * 错误
     *
     * @param msg
     */
    fun error(msg: String)

    /**
     * 进度
     *
     * @param progress
     */
    fun progress(progress: Int, duration: Int)

    /**
     * 歌曲封面图
     *
     * @param url
     */
    fun pic(url: String)

    /**
     * 歌曲缓存完成
     */
    fun download(position: Int)
}
