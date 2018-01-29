package cn.guluwa.gulumusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.utils.AppUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Created by guluwa on 2018/1/26.
 */

public class MusicAutoService extends Service {

    public static final String TAG = "MusicAutoService";
    public MediaPlayer mediaPlayer;
    public boolean isPlaying = false;

    public MusicAutoService() {
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void playOrPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void playNewSong(String path, int currentTime) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.setLooping(true);
                mediaPlayer.seekTo(currentTime);
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //  通过 Binder 来保持 Activity 和 Service 的通信
    public MyBinder binder = new MyBinder();

    public class MyBinder extends Binder {
        public MusicAutoService getService() {
            return MusicAutoService.this;
        }

        //获取歌曲长度
        public int getMusicDuration() {
            if (mediaPlayer != null) {
                return mediaPlayer.getDuration();
            }
            return 0;
        }

        //获取当前播放进度
        public int getMusicCurrentPosition() {
            if (mediaPlayer != null) {
                return mediaPlayer.getCurrentPosition();
            }
            return 0;
        }

        //调整进度
        public void seekTo(int position) {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "MusicAutoService in onCreate");
    }

    @Override
    public void onDestroy() {
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        AppManager.get().setmMusicAutoService(null);
        super.onDestroy();
        Log.w(TAG, "MusicAutoService in onDestroy");
    }

    public void quit() {
        stopSelf();
    }
}
