package cn.guluwa.gulumusic.data.local;

import android.arch.lifecycle.LiveData;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.local.dao.SongsDao;
import cn.guluwa.gulumusic.data.local.db.DBHelper;

/**
 * Created by guluwa on 2018/1/12.
 */

public class SongsServiceImpl implements SongsService {

    public static final SongsServiceImpl instance = new SongsServiceImpl();

    public static SongsServiceImpl getInstance() {
        return instance;
    }

    private SongsDao songsDao = DBHelper.getInstance().getGuluMusicDataBase().getSongsDao();

    public SongsServiceImpl() {
    }

    @Override
    public void addSongs(List<TracksBean> songs) {
        songsDao.addSongs(songs);
    }

    @Override
    public LiveData<List<TracksBean>> queryNetCloudHotSong() {
        return songsDao.queryNetCloudHotSong();
    }

    @Override
    public LiveData<SongPathBean> querySongPath(String id) {
        return songsDao.querySongPath(id);
    }

    @Override
    public LiveData<SongWordBean> querySongWord(String id) {
        return songsDao.querySongWord(id);
    }

    @Override
    public void addSongPath(SongPathBean songPathBean) {
        songsDao.addSongPath(songPathBean);
    }

    @Override
    public void addSongWord(SongWordBean songWordBean) {
        songsDao.addSongWord(songWordBean);
    }
}
