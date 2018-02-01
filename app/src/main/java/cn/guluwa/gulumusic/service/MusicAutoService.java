package cn.guluwa.gulumusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.guluwa.gulumusic.data.bean.BaseSongBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.listener.OnSongFinishListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.utils.AppUtils;
import cn.guluwa.gulumusic.utils.RandomPicker;

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
    private List<OnSongFinishListener> listeners;

    private RandomPicker mRandomPicker;

    public MusicAutoService() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            switch (AppManager.getInstance().getPlayMode()) {
                case 0://单曲循环
                    break;
                case 1://顺序播放
                    getNextSong(mCurrentSong);
                    break;
                case 2://随机播放
                    getNextSong(mCurrentSong);
                    break;
            }
            if (listeners.size() != 0) {
                listeners.get(listeners.size() - 1).finish(mCurrentSong);
            }
        });
        mSongList = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    /**
     * 绑定播放结束监听
     *
     * @param listener
     */
    public void bindSongFinishListener(OnSongFinishListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除播放结束监听
     */
    public void unBindSongFinishListener(OnSongFinishListener listener) {
        listeners.remove(listener);
    }

    /**
     * 暂停（继续）播放（同一首歌）
     */
    public void playOrPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
    }

    /**
     * 结束播放（开始下一首）
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * 开始播放新一首歌
     *
     * @param path
     * @param currentTime
     * @param mCurrentSong
     */
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

    /**
     * 获取下一首歌
     *
     * @return
     */
    public TracksBean getNextSong(TracksBean mCurrentSong) {
        this.mCurrentSong=mCurrentSong;
        int index = mCurrentSong.getIndex();
        if (AppManager.getInstance().getPlayMode() == 0 ||
                AppManager.getInstance().getPlayMode() == 1) {
            if (index + 1 >= mSongList.size()) {
                index = 0;
            } else {
                index++;
            }
            if (mSongList.get(index) instanceof TracksBean) {
                mCurrentSong = (TracksBean) mSongList.get(index);
            } else {
                mCurrentSong = AppUtils.getSongBean((LocalSongBean) mSongList.get(index));
            }
        } else {
            if (mRandomPicker == null) {
                mRandomPicker = new RandomPicker(mSongList.size());
            }
            index = mRandomPicker.next();
            if (index >= mSongList.size()) {
                index = 0;
            }
            if (mSongList.get(index) instanceof TracksBean) {
                mCurrentSong = (TracksBean) mSongList.get(index);
            } else {
                mCurrentSong = AppUtils.getSongBean((LocalSongBean) mSongList.get(index));
            }
        }
        return mCurrentSong;
    }

    /**
     * 获取上一首歌
     *
     * @return
     */
    public TracksBean getLastSong(TracksBean mCurrentSong) {
        this.mCurrentSong=mCurrentSong;
        int index = mCurrentSong.getIndex();
        if (AppManager.getInstance().getPlayMode() == 0 ||
                AppManager.getInstance().getPlayMode() == 1) {
            if (index - 1 < 0) {
                index = mSongList.size() - 1;
            } else {
                index--;
            }
            if (mSongList.get(index) instanceof TracksBean) {
                mCurrentSong = (TracksBean) mSongList.get(index);
            } else {
                mCurrentSong = AppUtils.getSongBean((LocalSongBean) mSongList.get(index));
            }
        } else {
            if (mRandomPicker == null) {
                mRandomPicker = new RandomPicker(mSongList.size());
            }
            index = mRandomPicker.next();
            if (index >= mSongList.size()) {
                index = 0;
            }
            if (mSongList.get(index) instanceof TracksBean) {
                mCurrentSong = (TracksBean) mSongList.get(index);
            } else {
                mCurrentSong = AppUtils.getSongBean((LocalSongBean) mSongList.get(index));
            }
        }
        return mCurrentSong;
    }

    //  通过 Binder 来保持 Activity 和 Service 的通信
    public MyBinder binder = new MyBinder();

    public class MyBinder extends Binder {
        public MusicAutoService getService() {
            return MusicAutoService.this;
        }
    }

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
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
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

    public List<? extends BaseSongBean> getSongList() {
        return mSongList;
    }

    public void setSongList(List<? extends BaseSongBean> mSongList) {
        this.mSongList = mSongList;
        mRandomPicker = null;
        mRandomPicker = new RandomPicker(mSongList.size());
    }

    public TracksBean getCurrentSong() {
        return mCurrentSong;
    }

    public void setCurrentSong(TracksBean mCurrentSong) {
        this.mCurrentSong = mCurrentSong;
    }
}
