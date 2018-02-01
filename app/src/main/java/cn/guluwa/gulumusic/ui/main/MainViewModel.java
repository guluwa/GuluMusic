package cn.guluwa.gulumusic.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.io.File;
import java.util.List;

import cn.guluwa.gulumusic.data.bean.FreshBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.total.SongsRepository;
import cn.guluwa.gulumusic.listener.OnResultListener;

/**
 * Created by guluwa on 2018/1/12.
 */

public class MainViewModel extends ViewModel {

    private SongsRepository songsRepository = SongsRepository.getInstance();

    //热门歌曲刷新
    private MutableLiveData<FreshBean> mHotSongListFresh;
    //热门歌曲
    private LiveData<ViewDataBean<List<TracksBean>>> mHotSongs;

    /**
     * 查询热门歌曲
     *
     * @return
     */
    public LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong() {
        if (mHotSongs == null) {
            mHotSongListFresh = new MutableLiveData<>();
            mHotSongs = Transformations.switchMap(mHotSongListFresh, input -> {
                if (input.isFresh) {
                    return songsRepository.queryNetCloudHotSong(input.isFirstComing);
                } else {
                    return null;
                }
            });
        }
        return mHotSongs;
    }

    /**
     * 刷新热门歌曲
     *
     * @param isFresh
     * @param isFirstComing
     */
    public void refreshHot(boolean isFresh, boolean isFirstComing) {
        mHotSongListFresh.setValue(new FreshBean(isFresh, isFirstComing));
    }

    //本地歌曲刷新
    private MutableLiveData<Boolean> mLocalSongListFresh;
    //本地歌曲
    private LiveData<ViewDataBean<List<LocalSongBean>>> mLocalSongs;

    /**
     * 查询本地歌曲
     *
     * @return
     */
    public LiveData<ViewDataBean<List<LocalSongBean>>> queryLocalSong() {
        if (mLocalSongs == null) {
            mLocalSongListFresh = new MutableLiveData<>();
            mLocalSongs = Transformations.switchMap(mLocalSongListFresh, input -> {
                if (input) {
                    return songsRepository.queryLocalSong();
                } else {
                    return null;
                }
            });
        }
        return mLocalSongs;
    }

    /**
     * 刷新本地歌曲
     *
     * @param isFresh
     */
    public void refreshLocal(boolean isFresh) {
        mLocalSongListFresh.setValue(isFresh);
    }

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
