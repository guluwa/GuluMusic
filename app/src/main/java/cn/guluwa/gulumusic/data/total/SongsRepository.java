package cn.guluwa.gulumusic.data.total;

import android.arch.lifecycle.LiveData;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import cn.guluwa.gulumusic.data.remote.RemoteSongsDataSource;
import cn.guluwa.gulumusic.utils.AppUtils;

/**
 * Created by guluwa on 2018/1/12.
 */

public class SongsRepository {

    private static final SongsRepository instance=new SongsRepository();
    private RemoteSongsDataSource remoteProjectsDataSource=RemoteSongsDataSource.getInstance();
    private LocalSongsDataSource localProjectsDataSource=LocalSongsDataSource.getInstance();

    public static SongsRepository getInstance() {
        return instance;
    }

    public SongsRepository() {
    }

    public LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong() {
        if (AppUtils.isNetConnected()) {
            return remoteProjectsDataSource.queryNetCloudHotSong();
        } else {
            return localProjectsDataSource.queryNetCloudHotSong();
        }
    }
}
