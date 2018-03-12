package cn.guluwa.gulumusic.manage

import cn.guluwa.gulumusic.service.MusicAutoService

/**
 * Created by guluwa on 2018/1/26.
 */

class AppManager {

    /**
     * 歌曲播放服务
     */
    var musicAutoService: MusicAutoService? = null

    /**
     * 播放模式 （单曲循环、顺序播放、随机播放）
     */
    var playMode: Int = 0

    /**
     * 播放列表（热门、本地）
     */
    var playStatus: String = "hot"

    /**
     * 搜索平台（网易云、QQ、虾米、酷狗、百度）
     */
    var searchPlatform: String = "tencent"

    object SingletonHolder {
        //单例（静态内部类）
        val instance = AppManager()
    }

    companion object {
        fun getInstance() = SingletonHolder.instance
    }
}
