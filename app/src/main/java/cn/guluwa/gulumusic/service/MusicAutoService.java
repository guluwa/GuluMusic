package cn.guluwa.gulumusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.guluwa.gulumusic.data.bean.BaseSongBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.ui.main.MainViewModel;
import cn.guluwa.gulumusic.utils.RandomPicker;

/**
 * Created by guluwa on 2018/1/26.
 */

public class MusicAutoService extends Service {

    public static final String TAG = "MusicAutoService";

    public MusicAutoService() {}

    //  通过 Binder 来保持 Activity 和 Service 的通信
    public MyBinder binder = new MyBinder(this);

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
        Log.w(TAG, "MusicAutoService in onCreate");
    }

    @Override
    public void onDestroy() {
        binder.getMediaPlayer().reset();
        binder.getMediaPlayer().release();
        binder.setMediaPlayer(null);
        AppManager.getInstance().setMusicAutoService(null);
        super.onDestroy();
        Log.w(TAG, "MusicAutoService in onDestroy");
    }

    /**
     * 主动结束服务
     */
    public void quit() {
        stopSelf();
    }
}
