package cn.guluwa.gulumusic.service;

import android.arch.lifecycle.LiveData;
import android.media.MediaPlayer;
import android.os.Binder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.guluwa.gulumusic.data.bean.BaseSongBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.total.SongsRepository;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.listener.OnSongStatusListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.ui.main.MainViewModel;
import cn.guluwa.gulumusic.utils.AppUtils;
import cn.guluwa.gulumusic.utils.RandomPicker;

/**
 * Created by guluwa on 2018/2/2.
 */
public class MyBinder extends Binder {

    private MusicAutoService musicAutoService;

    private SongsRepository songsRepository = SongsRepository.getInstance();

    /**
     * 歌曲路径
     */
    private String mSongPath;

    /**
     * 播放器
     */
    private MediaPlayer mediaPlayer;

    /**
     * 歌曲列表
     */
    private List<? extends BaseSongBean> mSongList;

    /**
     * 当前播放歌曲
     */
    private TracksBean mCurrentSong;

    /**
     * 歌曲播放进度
     */
    private List<OnSongStatusListener> listeners;

    /**
     * 随机数生成器
     */
    private RandomPicker mRandomPicker;

    public MyBinder(MusicAutoService musicAutoService) {
        this.musicAutoService = musicAutoService;
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
                listeners.get(listeners.size() - 1).end(mCurrentSong);
            }
        });
        mSongList = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public MusicAutoService getService() {
        return musicAutoService;
    }

    /**
     * 绑定播放结束监听
     *
     * @param listener
     */
    public void bindSongStatusListener(OnSongStatusListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除播放结束监听
     */
    public void unBindSongStatusListener(OnSongStatusListener listener) {
        listeners.remove(listener);
    }

    /**
     * 播放当前歌曲
     *
     * @param song
     * @param mCurrentTime
     */
    public void playCurrentSong(TracksBean song, int mCurrentTime) {
        mCurrentSong = song;
        //本地不存在，则去下载
        if ("".equals(mSongPath = AppUtils.isExistFile(String.format("%s_%s.mp3", mCurrentSong.getName(), mCurrentSong.getId()), 1))) {
            querySongPath();
            if (listeners.size() != 0) {
                listeners.get(listeners.size() - 1).loading();
            }
        } else {
            if (listeners.size() != 0) {
                listeners.get(listeners.size() - 1).start();
            }
            stop();
            playNewSong(mCurrentTime);
        }
        if ("".equals(AppUtils.isExistFile(String.format("%s_%s.txt", mCurrentSong.getName(), mCurrentSong.getId()), 2))) {
            querySongWord();
        }
    }

    /**
     * 查询歌曲路径
     */
    private void querySongPath() {
        songsRepository.querySongPath(mCurrentSong, new OnResultListener<SongPathBean>() {
            @Override
            public void success(SongPathBean result) {
                songsRepository.downloadSongFile(result, String.format("%s_%s.mp3", result.getSong().getName(), result.getSong().getId()),
                        new OnResultListener<File>() {
                            @Override
                            public void success(File file) {
                                System.out.println(file.getAbsolutePath());
                                mSongPath = file.getAbsolutePath();
                                if (result.getId() == mCurrentSong.getId()) {//下载完成的歌曲和当前播放歌曲是同一首
                                    stop();
                                    playNewSong(0);
                                    if (listeners.size() != 0) {
                                        listeners.get(listeners.size() - 1).start();
                                    }
                                }
                            }

                            @Override
                            public void failed(String error) {
                                if (listeners.size() != 0) {
                                    listeners.get(listeners.size() - 1).error(error);
                                }
                            }
                        });
            }

            @Override
            public void failed(String error) {
                if (listeners.size() != 0) {
                    listeners.get(listeners.size() - 1).error(error);
                }
            }
        });
    }

    /**
     * 查询歌曲歌词
     */
    private void querySongWord() {
        songsRepository.querySongWord(mCurrentSong, new OnResultListener<SongWordBean>() {
            @Override
            public void success(SongWordBean result) {
                AppUtils.writeWord2Disk(result.getLyric(), String.format("%s_%s.txt", result.getSong().getName(), result.getSong().getId()));
            }

            @Override
            public void failed(String error) {
                if (listeners.size() != 0) {
                    listeners.get(listeners.size() - 1).error(error);
                }
            }
        });
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
     * 开始播放新一首歌(play)
     *
     * @param currentTime
     */
    public void playNewSong(int currentTime) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(mSongPath);
                mediaPlayer.prepare();
                mediaPlayer.seekTo(currentTime);
                mediaPlayer.start();
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
    public TracksBean getNextSong(TracksBean song) {
        int index = song.getIndex();
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
        Log.d(MusicAutoService.TAG, mCurrentSong.getName());
        return mCurrentSong;
    }

    /**
     * 获取上一首歌
     *
     * @return
     */
    public TracksBean getLastSong(TracksBean song) {
        int index = song.getIndex();
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
        Log.d(MusicAutoService.TAG, mCurrentSong.getName());
        return mCurrentSong;
    }

    public void setSongList(List<? extends BaseSongBean> mSongList) {
        this.mSongList = mSongList;
        mRandomPicker = null;
        mRandomPicker = new RandomPicker(mSongList.size());
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }
}
