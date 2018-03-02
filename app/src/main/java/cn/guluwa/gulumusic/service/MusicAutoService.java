package cn.guluwa.gulumusic.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import cn.guluwa.gulumusic.manage.AppManager;

/**
 * Created by guluwa on 2018/1/26.
 */

public class MusicAutoService extends Service {

    public static final String TAG = "MusicAutoService";

    /**
     * 播放器焦点管理器
     */
    private AudioFocusManager mAudioFocusManager;


    public MusicAutoService() {
    }

    //  通过 Binder 来保持 Activity 和 Service 的通信
    public MusicBinder binder = new MusicBinder(this);

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioFocusManager = new AudioFocusManager(this);
        Log.w(TAG, "MusicAutoService in onCreate");
    }

    @Override
    public void onDestroy() {
        binder.unbindProgressQuery();
        mAudioFocusManager.abandonAudioFocus();
        binder.getMediaPlayer().reset();
        binder.getMediaPlayer().release();
        binder.setMediaPlayer(null);
        AppManager.Companion.getInstance().setMusicAutoService(null);
        super.onDestroy();
        Log.w(TAG, "MusicAutoService in onDestroy");
    }

    /**
     * 主动结束服务
     */
    public void quit() {
        stopSelf();
    }

    public AudioFocusManager getAudioFocusManager() {
        return mAudioFocusManager;
    }
}
