package cn.guluwa.gulumusic.data.local.db

import android.arch.persistence.room.Room
import android.content.Context
import cn.guluwa.gulumusic.manage.AppManager

/**
 * Created by guluwa on 2018/1/3.
 */

class DBHelper {

    lateinit var guluMusicDataBase: GuluMusicDataBase

    /**
     * 数据库初始化
     *
     * @param context
     */
    fun initDataBase(context: Context) {
        guluMusicDataBase = Room.databaseBuilder(context, GuluMusicDataBase::class.java, DATABASE_NAME).build()
    }

    object SingletonHolder {
        //单例（静态内部类）
        val instance = DBHelper()
    }

    companion object {

        fun getInstance() = SingletonHolder.instance

        private const val DATABASE_NAME = "gulu_music_database"
    }
}
