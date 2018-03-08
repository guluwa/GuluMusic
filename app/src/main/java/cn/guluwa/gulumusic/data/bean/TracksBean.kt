package cn.guluwa.gulumusic.data.bean

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

import java.io.Serializable

/**
 * 单曲信息类
 *
 *
 * Created by guluwa on 2018/1/12.
 */
@Entity(tableName = "net_cloud_hot_songs")
class TracksBean : BaseSongBean(), Serializable {

    @PrimaryKey
    var id: String = ""
    var index: Int = -1

    @Ignore
    var pic_id = ""
    @Ignore
    var url_id = ""
    @Ignore
    var lyric_id = ""
    @Ignore
    var local = false

    companion object {

        /**
         * name : 说散就散
         * id : 523251118
         * ar : [{"id":10473,"name":"袁娅维"}]
         * alia : ["电影《前任3：再见前任》主题曲"]
         * al : {"id":36957040,"name":"说散就散","picUrl":"https://p1.music.126.net/e50cdn6BVUCFFHpN9RIidA==/109951163081271235.jpg"}
         */

        private const val serialVersionUID = -5588034621712529228L
    }
}
