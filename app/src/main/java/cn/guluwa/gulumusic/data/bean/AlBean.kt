package cn.guluwa.gulumusic.data.bean

import android.arch.persistence.room.ColumnInfo

import java.io.Serializable

/**
 * Created by guluwa on 2018/3/2.
 */
class AlBean : Serializable {

    @ColumnInfo(name = "al_id")
    var id: Int = 0
    @ColumnInfo(name = "al_name")
    var name: String? = null
    var picUrl = ""

    companion object {

        /**
         * id : 36957040
         * name : 说散就散
         * picUrl : https://p1.music.126.net/e50cdn6BVUCFFHpN9RIidA==/109951163081271235.jpg
         */

        private const val serialVersionUID = 6802054785233923907L
    }
}
