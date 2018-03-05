package cn.guluwa.gulumusic.data.local.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import cn.guluwa.gulumusic.data.bean.*

import cn.guluwa.gulumusic.data.local.dao.SongsDao

/**
 * Created by guluwa on 2018/1/12.
 */

@Database(entities = [(TracksBean::class), (SongWordBean::class),
    (SongPathBean::class), (LocalSongBean::class), (SearchHistoryBean::class)], version = 1, exportSchema = false)
abstract class GuluMusicDataBase : RoomDatabase() {

    abstract val songsDao: SongsDao
}
