package cn.guluwa.gulumusic.data.local;

import android.arch.lifecycle.LiveData;

import java.util.List;

import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SongPathBean;
import cn.guluwa.gulumusic.data.bean.SongWordBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.local.dao.SongsDao;
import cn.guluwa.gulumusic.data.local.db.DBHelper;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * Created by guluwa on 2018/1/12.
 */

public class SongsServiceImpl implements SongsService {

    public static final SongsServiceImpl instance = new SongsServiceImpl();

    public static SongsServiceImpl getInstance() {
        return instance;
    }

    /**
     * 本地数据库操作
     */
    private SongsDao songsDao = DBHelper.getInstance().getGuluMusicDataBase().getSongsDao();

    public SongsServiceImpl() {
    }

    /**
     * 查询热门歌曲
     *
     * @return
     */
    @Override
    public LiveData<List<TracksBean>> queryNetCloudHotSong() {
        return songsDao.queryNetCloudHotSong();
    }

    /**
     * 查询本地歌曲
     *
     * @return
     */
    @Override
    public LiveData<List<LocalSongBean>> queryLocalSong() {
        return songsDao.queryLocalSong();
    }


    /**
     * 查询本地歌曲（单曲）
     *
     * @param id
     * @param name
     * @return
     */
    @Override
    public LocalSongBean queryLocalSong(String id, String name) {
        return songsDao.queryLocalSong(id, name);
    }

    /**
     * 查询歌曲路径
     *
     * @param id
     * @return
     */
    @Override
    public Flowable<List<SongPathBean>> querySongPath(String id) {
        return songsDao.querySongPath(id);
    }

    /**
     * 查询歌曲歌词
     *
     * @param id
     * @return
     */
    @Override
    public Flowable<List<SongWordBean>> querySongWord(String id) {
        return songsDao.querySongWord(id);
    }

    /**
     * 添加歌曲到热门歌曲表
     *
     * @param songs
     */
    @Override
    public void addSongs(List<TracksBean> songs) {
        songsDao.addSongs(songs);
    }

    /**
     * 添加歌曲到本地歌曲表
     *
     * @param localSongBean
     */
    @Override
    public void addLocalSong(LocalSongBean localSongBean) {
        songsDao.addLocalSong(localSongBean);
    }

    /**
     * 从本地歌曲表删除歌曲
     *
     * @param localSongBean
     */
    @Override
    public void deleteLocalSong(LocalSongBean localSongBean) {
        songsDao.deleteLocalSong(localSongBean);
    }

    /**
     * 添加歌曲路径
     *
     * @param songPathBean
     */
    @Override
    public void addSongPath(SongPathBean songPathBean) {
        songsDao.addSongPath(songPathBean);
    }

    /**
     * 添加歌曲歌词
     *
     * @param songWordBean
     */
    @Override
    public void addSongWord(SongWordBean songWordBean) {
        songsDao.addSongWord(songWordBean);
    }
}
