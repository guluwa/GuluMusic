package cn.guluwa.gulumusic.ui.play;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.io.File;

import cn.guluwa.gulumusic.data.bean.FreshBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.total.SongsRepository;
import cn.guluwa.gulumusic.listener.OnResultListener;

/**
 * Created by guluwa on 2018/1/13.
 */

public class PlayViewModel extends ViewModel {

    private SongsRepository songsRepository = SongsRepository.getInstance();

    //歌曲链接刷新、歌曲歌词刷新
    private MutableLiveData<FreshBean> mPathFresh, mWordFresh;
    //单曲链接刷新
    private LiveData<ViewDataBean<SongPathBean>> mSongPath;
    //单曲歌词刷新
    private LiveData<ViewDataBean<SongWordBean>> mSongWord;

    /**
     * 查询歌曲链接
     *
     * @return
     */
    public LiveData<ViewDataBean<SongPathBean>> querySongPath() {
        if (mSongPath == null) {
            if (mPathFresh == null) {
                mPathFresh = new MutableLiveData<>();
            }
            mSongPath = Transformations.switchMap(mPathFresh, input -> {
                if (input.isFresh) {
                    return songsRepository.querySongPath(input.song);
                } else {
                    return null;
                }
            });
        }
        return mSongPath;
    }

    /**
     * 查询歌曲歌词
     *
     * @return
     */
    public LiveData<ViewDataBean<SongWordBean>> querySongWord() {
        if (mSongWord == null) {
            if (mWordFresh == null) {
                mWordFresh = new MutableLiveData<>();
            }
            mSongWord = Transformations.switchMap(mWordFresh, input -> {
                if (input.isFresh) {
                    return songsRepository.querySongWord(input.song);
                } else {
                    return null;
                }
            });
        }
        return mSongWord;
    }

    /**
     * 歌曲链接刷新
     *
     * @param song
     * @param fresh
     */
    void refreshPath(TracksBean song, boolean fresh) {
        mPathFresh.setValue(new FreshBean(song, fresh));
    }

    /**
     * 歌曲歌词刷新
     *
     * @param song
     * @param fresh
     */
    void refreshWord(TracksBean song, boolean fresh) {
        mWordFresh.setValue(new FreshBean(song, fresh));
    }

    /**
     * 歌曲下载
     *
     * @param songPathBean
     * @param songName
     * @param listener
     */
    void downloadSongFile(SongPathBean songPathBean, String songName, OnResultListener<File> listener) {
        songsRepository.downloadSongFile(songPathBean, songName, listener);
    }
}
