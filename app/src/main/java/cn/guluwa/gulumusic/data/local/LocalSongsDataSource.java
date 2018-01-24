package cn.guluwa.gulumusic.data.local;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.view.View;

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
        MediatorLiveData<ViewDataBean<List<TracksBean>>> data = new MediatorLiveData<>();
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

    @Override
    public LiveData<ViewDataBean<SongPathBean>> querySongPath(String id) {
        MediatorLiveData<ViewDataBean<SongPathBean>> data = new MediatorLiveData<>();
        data.setValue(ViewDataBean.loading());

        data.addSource(songsService.querySongPath(id), songPathBean -> {
            if (songPathBean == null) {
                data.setValue(ViewDataBean.empty());
            } else {
                data.setValue(ViewDataBean.content(songPathBean));
            }
        });
        return data;
    }

    @Override
    public LiveData<ViewDataBean<SongWordBean>> querySongWord(String id) {
        MediatorLiveData<ViewDataBean<SongWordBean>> data = new MediatorLiveData<>();
        data.setValue(ViewDataBean.loading());

        data.addSource(songsService.querySongWord(id), songWordBean -> {
            if (songWordBean == null) {
                data.setValue(ViewDataBean.empty());
            } else {
                data.setValue(ViewDataBean.content(songWordBean));
            }
        });
        return data;
    }

    public void addSongs(List<TracksBean> songs) {
        songsService.addSongs(songs);
    }

    public void addSong(SongPathBean songPathBean) {
        songsService.addSongPath(songPathBean);
    }

    public void addSong(SongWordBean songWordBean) {
        songsService.addSongWord(songWordBean);
    }
}
