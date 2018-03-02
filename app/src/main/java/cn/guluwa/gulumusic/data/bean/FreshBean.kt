package cn.guluwa.gulumusic.data.bean

/**
 * 数据刷新类
 *
 *
 * Created by guluwa on 2018/1/27.
 */
class FreshBean {
    var isFresh: Boolean = false
    var isFirstComing: Boolean = false
    var song: TracksBean?=null
    var page: Int = 0
    var key: String=""

    constructor(isFresh: Boolean, isFirstComing: Boolean) {
        this.isFresh = isFresh
        this.isFirstComing = isFirstComing
    }

    constructor(song: TracksBean, isFresh: Boolean) {
        this.song = song
        this.isFresh = isFresh
    }

    constructor(key: String, page: Int, isFresh: Boolean) {
        this.page = page
        this.key = key
        this.isFresh = isFresh
    }
}
