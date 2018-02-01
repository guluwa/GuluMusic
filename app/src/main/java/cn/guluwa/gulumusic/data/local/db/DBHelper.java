package cn.guluwa.gulumusic.data.local.db;

import android.arch.persistence.room.Room;
import android.content.Context;

/**
 * Created by guluwa on 2018/1/3.
 */

public class DBHelper {

    public static final DBHelper instance = new DBHelper();

    public static DBHelper getInstance() {
        return instance;
    }

    private static final String DATABASE_NAME = "gulu_music_database";

    private GuluMusicDataBase GuluMusicDataBase;

    /**
     * 数据库初始化
     *
     * @param context
     */
    public void initDataBase(Context context) {
        GuluMusicDataBase = Room.databaseBuilder(context, GuluMusicDataBase.class, DATABASE_NAME).build();
    }

    public GuluMusicDataBase getGuluMusicDataBase() {
        return GuluMusicDataBase;
    }
}
