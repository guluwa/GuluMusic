package cn.guluwa.gulumusic.data.total;

import android.arch.lifecycle.LiveData;
import android.view.View;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.PlayListBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.bean.ViewDataBean;

/**
 * Created by guluwa on 2018/1/12.
 */

public interface SongDataSource {

    LiveData<ViewDataBean<List<TracksBean>>> queryNetCloudHotSong();
}
