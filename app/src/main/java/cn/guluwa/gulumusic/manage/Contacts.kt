package cn.guluwa.gulumusic.manage

/**
 * Created by guluwa on 2018/1/9.
 */

object Contacts {

    const val REQUEST_CODE_PLAY = 520

    const val REQUEST_CODE_SEARCH = 521

    const val RESULT_SONG_CODE = 502

    /**
     * 播放方式
     * 0：单曲循环（默认）
     * 1：列表循环
     * 2：随机播放
     */
    const val PLAY_MODE = "play_mode"

    /**
     * 播放列表（热门、本地）
     */
    const val PLAY_STATUS = "play_status"

    /**
     * 屏幕是否圆角
     */
    const val IS_CIRCLE_CORNER = "is_circle_corner"

    /**
     * 流量环境是否可以下载歌曲
     */
    const val CAN_DOWNLOAD_WITHOUT_WIFI = "can_download_without_wifi"

    /**
     * 搜索平台（网易云、QQ、虾米、酷狗、百度）
     */
    const val SEARCH_PLATFORM = "search_platform"

    /**
     * 服务器baseUrl
     */
    const val BASEURL = "http://lab.mkblog.cn/music/"

    /**
     * 网易云
     */

    const val NET_CLOUD_HOT_ID = "3778678"

    const val SONG_CALLBACK = "jQuery1113003709735504310796_1516351478034"

    /**
     * 网易云
     */
    const val TYPE_NETEASE = "netease"

    /**
     * QQ
     */
    const val TYPE_TENCENT = "tencent"

    /**
     * 虾米
     */
    const val TYPE_XIAMI = "xiami"

    /**
     * 酷狗
     */
    const val TYPE_KUGOU = "kugou"

    /**
     * 百度
     */
    const val TYPE_BAIDU = "baidu"
}
