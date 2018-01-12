package cn.guluwa.gulumusic.data.local;

import android.arch.lifecycle.LiveData;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.TracksBean;

/**
 * Created by guluwa on 2018/1/12.
 */

public interface SongsService {

    void addSongs(List<TracksBean> songs);

    LiveData<List<TracksBean>> queryNetCloudHotSong();
}
