package cn.guluwa.gulumusic.data.local;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.total.SongDataSource;

/**
 * Created by guluwa on 2018/1/12.
 */

public class LocalSongsDataSource implements SongDataSource {

    public static final LocalSongsDataSource instance = new LocalSongsDataSource();

    public static LocalSongsDataSource getInstance() {
        return instance;
    }

    private SongsService songsService = SongsServiceImpl.getInstance();

    public LocalSongsDataSource() {
    }

    @Override
    public LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong() {
        final MediatorLiveData<ViewDataBean<List<TracksBean>>> data = new MediatorLiveData<>();
        data.setValue(ViewDataBean.loading());

        data.addSource(songsService.queryNetCloudHotSong(), tracksBeans -> {
            if (null == tracksBeans || tracksBeans.size() == 0) {
                data.setValue(ViewDataBean.empty());
            } else {
                data.setValue(ViewDataBean.content(tracksBeans));
            }
        });
        return data;
    }

    public void addSongs(List<TracksBean> songs) {
        songsService.addSongs(songs);
    }
}
