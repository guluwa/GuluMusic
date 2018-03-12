package cn.guluwa.gulumusic.data.bean

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Ignore

import java.io.Serializable

/**
 * 歌曲基础类
 *
 *
 * Created by guluwa on 2018/1/31.
 */

open class BaseSongBean : Serializable {

    var name: String = ""
    @Embedded
    var al: AlBean? = null
    @Embedded
    var singer: ArBean? = null
    var tag: String? = null
    var source: String = ""

    @Ignore
    var currentTime = 0
    @Ignore
    var duration: Int = 0
    @Ignore
    var ar: List<ArBean>? = null
    @Ignore
    var alia: List<String>? = null
    @Ignore
    var isPlayed: Boolean = false

    companion object {

        private const val serialVersionUID = -3436929448594928827L
    }
}
