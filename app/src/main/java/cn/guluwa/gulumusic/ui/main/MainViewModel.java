package cn.guluwa.gulumusic.ui.main;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.total.SongsRepository;

/**
 * Created by guluwa on 2018/1/12.
 */

public class MainViewModel extends ViewModel {

    private SongsRepository songsRepository = SongsRepository.getInstance();
    private MutableLiveData<Boolean> mRefresh;
    private LiveData<ViewDataBean<List<TracksBean>>> songs;

    public LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong() {
        if (songs == null) {
            mRefresh = new MutableLiveData<>();
            songs = Transformations.switchMap(mRefresh, input -> {
                if (input)
                    return songsRepository.queryNetCloudHotSong();
                else
                    return null;
            });
        }
        return songs;
    }

    public void refresh(boolean isFresh) {
        mRefresh.setValue(isFresh);
    }
}
