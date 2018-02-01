package cn.guluwa.gulumusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Trace;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import cn.guluwa.gulumusic.data.bean.BaseSongBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.listener.OnSongFinishListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.utils.AppUtils;

/**
 * Created by guluwa on 2018/1/26.
 */

public class MusicAutoService extends Service {

    public static final String TAG = "MusicAutoService";
    /**
     * 播放器
     */
    public MediaPlayer mediaPlayer;

    /**
     * 歌曲列表
     */
    private List<? extends BaseSongBean> mSongList;

    /**
     * 当前播放歌曲
     */
    private TracksBean mCurrentSong;

    /**
     * 播放结束通知接口
     */
    private OnSongFinishListener listener;

    public MusicAutoService() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            switch (AppManager.getInstance().getPlayMode()) {
                case 0://单曲循环

                    break;
                case 1://顺序播放
                    int index = mCurrentSong.getIndex();
                    if (mSongList.get(index + 1) instanceof TracksBean) {
                        mCurrentSong = (TracksBean) mSongList.get(index + 1);
                    } else {
                        mCurrentSong = AppUtils.getSongBean((LocalSongBean) mSongList.get(index + 1));
                    }
                    if (listener != null) {
                        listener.finish(mCurrentSong);
                    }
                    break;
                case 2://随机播放

                    break;
            }
        });
        mSongList = new ArrayList<>();
    }

    public void bindSongFinishListener(OnSongFinishListener listener) {
        this.listener = listener;
    }

    public void unBindSongFinishListener() {
        listener = null;
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

    public void playNewSong(String path, int currentTime, TracksBean mCurrentSong) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.seekTo(currentTime);
                mediaPlayer.start();
                setCurrentSong(mCurrentSong);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setIsLoopPlaying(boolean isLoopPlaying) {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(isLoopPlaying);
        }
    }

    //  通过 Binder 来保持 Activity 和 Service 的通信
    public MyBinder binder = new MyBinder();

    public class MyBinder extends Binder {
        public MusicAutoService getService() {
            return MusicAutoService.this;
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
        AppManager.getInstance().setMusicAutoService(null);
        super.onDestroy();
        Log.w(TAG, "MusicAutoService in onDestroy");
    }

    public void quit() {
        stopSelf();
    }

    public List<? extends BaseSongBean> getSongList() {
        return mSongList;
    }

    public void setSongList(List<? extends BaseSongBean> mSongList) {
        this.mSongList = mSongList;
    }

    public TracksBean getCurrentSong() {
        return mCurrentSong;
    }

    public void setCurrentSong(TracksBean mCurrentSong) {
        this.mCurrentSong = mCurrentSong;
    }
}
