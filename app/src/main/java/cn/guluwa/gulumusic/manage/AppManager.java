package cn.guluwa.gulumusic.manage;

import android.content.Context;

import cn.guluwa.gulumusic.service.MusicAutoService;

/**
 * Created by guluwa on 2018/1/26.
 */

public class AppManager {

    private MusicAutoService mMusicAutoService;

    private AppManager() {
    }

    private static class SingletonHolder {
        private static AppManager instance = new AppManager();
    }

    public static AppManager get() {
        return SingletonHolder.instance;
    }

    public void init(Context context) {

    }

    public MusicAutoService getmMusicAutoService() {
        return mMusicAutoService;
    }

    public void setmMusicAutoService(MusicAutoService mMusicAutoService) {
        this.mMusicAutoService = mMusicAutoService;
    }
}
