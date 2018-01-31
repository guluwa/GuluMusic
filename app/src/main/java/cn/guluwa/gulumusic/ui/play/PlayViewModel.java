package cn.guluwa.gulumusic.ui.play;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.io.File;
import java.io.StringReader;

import cn.guluwa.gulumusic.data.bean.BaseSongBean;
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
    private MutableLiveData<FreshBean> mPathFresh, mWordFresh;
    private LiveData<ViewDataBean<SongPathBean>> mSongPath;
    private LiveData<ViewDataBean<SongWordBean>> mSongWord;

    public LiveData<ViewDataBean<SongPathBean>> querySongPath() {
        if (mSongPath == null) {
            if (mPathFresh == null)
                mPathFresh = new MutableLiveData<>();
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

    public LiveData<ViewDataBean<SongWordBean>> querySongWord() {
        if (mSongWord == null) {
            if (mWordFresh == null)
                mWordFresh = new MutableLiveData<>();
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

    void refreshPath(BaseSongBean song, boolean fresh) {
        mPathFresh.setValue(new FreshBean(song, fresh));
    }

    void refreshWord(BaseSongBean song, boolean fresh) {
        mWordFresh.setValue(new FreshBean(song, fresh));
    }

    void downloadSongFile(SongPathBean songPathBean, String songName, OnResultListener<File> listener) {
        songsRepository.downloadSongFile(songPathBean, songName, listener);
    }
}
