package cn.guluwa.gulumusic.data.local;

import android.arch.lifecycle.LiveData;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;

/**
 * Created by guluwa on 2018/1/12.
 */

public interface SongsService {

    void addSongs(List<TracksBean> songs);

    void addLocalSong(LocalSongBean localSongBean);

    LiveData<List<TracksBean>> queryNetCloudHotSong();

    LiveData<List<LocalSongBean>> queryLocalSong();

    LiveData<SongPathBean> querySongPath(String id);

    LiveData<SongWordBean> querySongWord(String id);

    void addSongPath(SongPathBean songPathBean);

    void addSongWord(SongWordBean songWordBean);
}
