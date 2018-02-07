package cn.guluwa.gulumusic.service;

import android.media.MediaPlayer;
import android.os.Binder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.data.bean.BaseSongBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import cn.guluwa.gulumusic.data.total.SongsRepository;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.listener.OnSongStatusListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.utils.AppUtils;
import cn.guluwa.gulumusic.utils.RandomPicker;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by guluwa on 2018/2/2.
 */
public class MusicBinder<T> extends Binder {

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
    private List<T> mSongList;

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

    /**
     * 歌曲进度轮询
     */
    private Disposable disposable;

    /**
     * 歌曲是否正在加载
     */
    private boolean isLoading;

    public MusicBinder(MusicAutoService musicAutoService) {
        isLoading = false;
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
        bindProgressQuery();
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
            isLoading = true;
            querySongPath();
            if (listeners.size() != 0) {
                listeners.get(listeners.size() - 1).loading();
            }
        } else {
            isLoading = false;
            if (listeners.size() != 0) {
                listeners.get(listeners.size() - 1).start();
            }
            stop();
            playNewSong(mCurrentTime);
        }
        if ("".equals(AppUtils.isExistFile(String.format("%s_%s.txt", mCurrentSong.getName(), mCurrentSong.getId()), 2))) {
            querySongWord();
        }
        if ("".equals(song.getAl().getPicUrl())) {
            querySongPic();
        }
    }

    /**
     * 查询歌曲路径(首页、搜索)
     */
    private void querySongPath() {
        songsRepository.querySongPath(mCurrentSong, new OnResultListener<SongPathBean>() {
            @Override
            public void success(SongPathBean result) {
                if ("".equals(result.getUrl())) {
                    if (listeners.size() != 0) {
                        listeners.get(listeners.size() - 1).error(musicAutoService.getString(R.string.song_cant_plsy_tip));
                        getNextSong(mCurrentSong);
                        listeners.get(listeners.size() - 1).end(mCurrentSong);
                    }
                } else {
                    songsRepository.downloadSongFile(result, String.format("%s_%s.mp3", result.getSong().getName(), result.getSong().getId()),
                            new OnResultListener<File>() {
                                @Override
                                public void success(File file) {
                                    System.out.println(file.getAbsolutePath());
                                    mSongPath = file.getAbsolutePath();
                                    if (result.getId().equals(mCurrentSong.getId())) {//下载完成的歌曲和当前播放歌曲是同一首
                                        isLoading = true;
                                        if (mediaPlayer.isPlaying()) {
                                            stop();
                                        }
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
                                        getNextSong(mCurrentSong);
                                        listeners.get(listeners.size() - 1).end(mCurrentSong);
                                    }
                                }
                            });
                }
            }

            @Override
            public void failed(String error) {
                if (listeners.size() != 0) {
                    listeners.get(listeners.size() - 1).error(error);
                    getNextSong(mCurrentSong);
                    listeners.get(listeners.size() - 1).end(mCurrentSong);
                }
            }
        });
    }

    /**
     * 查询歌曲歌词(首页、搜索)
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
     * 查询歌曲封面图（首页、搜索）
     */
    private void querySongPic() {
        songsRepository.querySongPic(mCurrentSong, new OnResultListener<SongPathBean>() {
            @Override
            public void success(SongPathBean result) {
                System.out.println(result.getUrl());
                mCurrentSong.getAl().setPicUrl(result.getUrl());
                mSongList.add((T) mCurrentSong);
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
                if (listeners.size() != 0) {
                    listeners.get(listeners.size() - 1).pause();
                }
            } else {
                mediaPlayer.start();
                if (listeners.size() != 0) {
                    listeners.get(listeners.size() - 1).start();
                }
            }
        }
    }

    /**
     * 结束播放（开始下一首）
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            if (listeners.size() != 0) {
                listeners.get(listeners.size() - 1).pause();
            }
        }
    }

    /**
     * 开始播放新一首歌(play)
     *
     * @param currentTime
     */
    public void playNewSong(int currentTime) {
        if (musicAutoService.getAudioFocusManager().requestAudioFocus()) {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(mSongPath);
                    mediaPlayer.prepare();
                    mediaPlayer.seekTo(currentTime);
                    mediaPlayer.start();
                    if (listeners.size() != 0) {
                        listeners.get(listeners.size() - 1).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    /**
     * 开始轮询
     */
    public void bindProgressQuery() {
        if (disposable == null) {
            disposable = Observable.interval(0, 150, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        if (mediaPlayer.isPlaying()) {
                            if (listeners.size() != 0) {
                                listeners.get(listeners.size() - 1).progress(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
                            }
                        }
                    });
        }
    }

    /**
     * 结束轮询
     */
    public void unbindProgressQuery() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    public void setSongList(List<T> mSongList) {
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

    public boolean isLoading() {
        return isLoading;
    }

    public TracksBean getCurrentSong() {
        return mCurrentSong;
    }
}
