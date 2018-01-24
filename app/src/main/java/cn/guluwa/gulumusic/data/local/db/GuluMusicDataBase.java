package cn.guluwa.gulumusic.data.local.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.local.dao.SongsDao;

/**
 * Created by guluwa on 2018/1/12.
 */

@Database(entities = {TracksBean.class, SongWordBean.class, SongPathBean.class}, version = 1, exportSchema = false)
public abstract class GuluMusicDataBase extends RoomDatabase {

    public abstract SongsDao getSongsDao();
}
