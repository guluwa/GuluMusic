package cn.guluwa.gulumusic.ui.play;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.io.File;

import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.total.SongsRepository;
import cn.guluwa.gulumusic.listener.OnResultListener;

/**
 * Created by guluwa on 2018/1/13.
 */

public class PlayViewModel extends ViewModel {

    private SongsRepository songsRepository = SongsRepository.getInstance();
    private MutableLiveData<String> mId;
    private LiveData<ViewDataBean<SongPathBean>> mSongPath;
    private LiveData<ViewDataBean<SongWordBean>> mSongWord;

    public LiveData<ViewDataBean<SongPathBean>> querySongPath() {
        if (mSongPath == null) {
            if (mId == null)
                mId = new MutableLiveData<>();
            mSongPath = Transformations.switchMap(mId, input -> songsRepository.querySongPath(input));
        }
        return mSongPath;
    }

    public LiveData<ViewDataBean<SongWordBean>> querySongWord() {
        if (mSongWord == null) {
            if (mId == null)
                mId = new MutableLiveData<>();
            mSongWord = Transformations.switchMap(mId, input -> songsRepository.querySongWord(input));
        }
        return mSongWord;
    }

    public void refresh(int id) {
        mId.setValue(String.valueOf(id));
    }

    public void downloadSongFile(String url, String songName, OnResultListener<File> listener) {
        songsRepository.downloadSongFile(url, songName, listener);
    }
}
