package cn.guluwa.gulumusic.manage;

import android.content.Context;

import cn.guluwa.gulumusic.service.MusicAutoService;

/**
 * Created by guluwa on 2018/1/26.
 */

public class AppManager {

    /**
     * 歌曲播放服务
     */
    private MusicAutoService mMusicAutoService;

    /**
     * 播放模式
     */
    private int mPlayMode;

    /**
     * 播放列表（热门、本地）
     */
    private String mPlayStatus;

    /**
     * 搜索平台（网易云、QQ、虾米、酷狗、百度）
     */
    private String mSearchPlatform;

    private AppManager() {
    }

    private static class SingletonHolder {
        private static AppManager instance = new AppManager();
    }

    public static AppManager getInstance() {
        return SingletonHolder.instance;
    }

    public MusicAutoService getMusicAutoService() {
        return mMusicAutoService;
    }

    public void setMusicAutoService(MusicAutoService mMusicAutoService) {
        this.mMusicAutoService = mMusicAutoService;
    }

    public int getPlayMode() {
        return mPlayMode;
    }

    public void setPlayMode(int mPlayMode) {
        this.mPlayMode = mPlayMode;
    }

    public String getPlayStatus() {
        return mPlayStatus;
    }

    public void setPlayStatus(String mPlayStatus) {
        this.mPlayStatus = mPlayStatus;
    }

    public String getSearchPlatform() {
        return mSearchPlatform;
    }

    public void setSearchPlatform(String mSearchPlatform) {
        this.mSearchPlatform = mSearchPlatform;
    }
}
