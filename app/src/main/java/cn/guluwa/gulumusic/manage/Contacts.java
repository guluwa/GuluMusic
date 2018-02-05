package cn.guluwa.gulumusic.manage;

/**
 * Created by guluwa on 2018/1/9.
 */

public class Contacts {

    public static final int REQUEST_CODE_PLAY = 520;

    public static final int REQUEST_CODE_SEARCH = 521;

    public static final int RESULT_SONG_CODE = 502;

    /**
     * 播放方式
     * 0：单曲循环（默认）
     * 1：列表循环
     * 2：随机播放
     */
    public static final String PLAY_MODE = "play_mode";

    /**
     * 播放列表（热门、本地）
     */
    public static final String PLAY_STATUS = "play_status";

    /**
     * 屏幕是否圆角
     */
    public static final String IS_CIRCLE_CORNER = "is_circle_corner";

    /**
     * 流量环境是否可以下载歌曲
     */
    public static final String CAN_DOWNLOAD_WITHOUT_WIFI = "can_download_without_wifi";

    /**
     * 服务器baseUrl
     */
    public static final String BASEURL = "http://lab.mkblog.cn/music_new/";

    /**
     * 网易云
     */

    public static final String NET_CLOUD_HOT_ID = "3778678";

    public static final String SONG_CALLBACK = "jQuery1113003709735504310796_1516351478034";

    /**
     * 网易云
     */
    public static final String TYPE_NETEASE = "netease";

    /**
     * QQ
     */
    public static final String TYPE_TENCENT = "tencent";

    /**
     * 虾米
     */
    public static final String TYPE_XIAMI = "xiami";

    /**
     * 酷狗
     */
    public static final String TYPE_KUGOU = "kugou";

    /**
     * 百度
     */
    public static final String TYPE_BAIDU = "baidu";
}
