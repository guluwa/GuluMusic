package cn.guluwa.gulumusic.data.bean

import android.arch.persistence.room.ColumnInfo

import java.io.Serializable

/**
 * Created by guluwa on 2018/3/2.
 */
class ArBean : Serializable {

    @ColumnInfo(name = "singer_id")
    var id: Int = 0
    @ColumnInfo(name = "singer_name")
    var name: String? = null

    companion object {

        /**
         * id : 10473
         * name : 袁娅维
         */

        private const val serialVersionUID = 1990373790504589909L
    }
}
