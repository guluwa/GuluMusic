package cn.guluwa.gulumusic.data.bean

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

import java.io.Serializable

/**
 * 歌曲路径类
 *
 *
 * Created by guluwa on 2018/1/19.
 */

@Entity(tableName = "songs_path")
class SongPathBean : Serializable {

    @PrimaryKey
    var id: String = ""
    var url: String = ""
    @Ignore
    var br: Int = 0
    @Ignore
    var song: TracksBean? = null

    companion object {

        /**
         * url : https://m7c.music.126.net/20180119171008/de4c0224042f824bbe7d421a1196202f/ymusic/4afa/0216/a89f/c9941d4ebd3f829a9a3b3a52a8d738ce.mp3
         * br : 128
         */

        private const val serialVersionUID = -6267004480939769470L
    }
}
