package cn.guluwa.gulumusic.ui.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.view.View;

import java.io.File;
import java.util.List;

import cn.guluwa.gulumusic.data.bean.FreshBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean;
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

    //歌曲搜索
    private MutableLiveData<FreshBean> mSearchSongListFresh;
    //搜索歌曲
    private LiveData<ViewDataBean<List<SearchResultSongBean>>> mSearchSongs;

    /**
     * 歌曲搜索
     *
     * @return
     */
    public LiveData<ViewDataBean<List<SearchResultSongBean>>> searchSongByKeyWord() {
        if (mSearchSongs == null) {
            mSearchSongListFresh = new MutableLiveData<>();
            mSearchSongs = Transformations.switchMap(mSearchSongListFresh, input -> {
                if (input.page != -1) {
                    return songsRepository.searchSongByKeyWord(input);
                } else {
                    return null;
                }
            });
        }
        return mSearchSongs;
    }

    /**
     * 刷新歌曲搜索
     */
    public void refreshSearchSongs(String key, int page) {
        mSearchSongListFresh.setValue(new FreshBean(key, page));
    }
}
