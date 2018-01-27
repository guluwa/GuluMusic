package cn.guluwa.gulumusic.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.io.File;
import java.util.List;

import cn.guluwa.gulumusic.data.bean.FreshBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.total.SongsRepository;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.ui.play.PlayViewModel;

/**
 * Created by guluwa on 2018/1/12.
 */

public class MainViewModel extends ViewModel {

    private SongsRepository songsRepository = SongsRepository.getInstance();
    private MutableLiveData<Boolean> mSongListFresh;
    private LiveData<ViewDataBean<List<TracksBean>>> songs;

    public LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong() {
        if (songs == null) {
            mSongListFresh = new MutableLiveData<>();
            songs = Transformations.switchMap(mSongListFresh, input -> {
                if (input) {
                    return songsRepository.queryNetCloudHotSong();
                } else
                    return null;
            });
        }
        return songs;
    }

    public void refresh(boolean isFresh) {
        mSongListFresh.setValue(isFresh);
    }

    private MutableLiveData<FreshBean> mPathFresh, mWordFresh;
    private LiveData<ViewDataBean<SongPathBean>> mSongPath;
    private LiveData<ViewDataBean<SongWordBean>> mSongWord;

    public LiveData<ViewDataBean<SongPathBean>> querySongPath() {
        if (mSongPath == null) {
            if (mPathFresh == null)
                mPathFresh = new MutableLiveData<>();
            mSongPath = Transformations.switchMap(mPathFresh, input -> {
                if (input.isFresh) {
                    return songsRepository.querySongPath(String.valueOf(input.id));
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
                    return songsRepository.querySongWord(String.valueOf(input.id));
                } else {
                    return null;
                }
            });
        }
        return mSongWord;
    }

    void refreshPath(int id, boolean fresh) {
        mPathFresh.setValue(new FreshBean(id, fresh));
    }

    void refreshWord(int id, boolean fresh) {
        mWordFresh.setValue(new FreshBean(id, fresh));
    }

    void downloadSongFile(String url, String songName, OnResultListener<File> listener) {
        songsRepository.downloadSongFile(url, songName, listener);
    }
}
