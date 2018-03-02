package cn.guluwa.gulumusic.data.bean

/**
 * Created by guluwa on 2018/2/3.
 */

class SearchResultSongBean {

    /**
     * id : 481853040
     * name : 你就不要想起我
     * artist : ["于毅"]
     * album : 2017跨界歌王 第八期
     * pic_id : 19200771556014598
     * url_id : 481853040
     * lyric_id : 481853040
     * source : netease
     */

    var id: String = ""
    var name: String = ""
    var album: String = ""
    var pic_id: String = ""
    var url_id: String = ""
    var lyric_id: String = ""
    var source: String = ""
    var artist: List<String>? = null

    var isDownLoad: Boolean = false
}
