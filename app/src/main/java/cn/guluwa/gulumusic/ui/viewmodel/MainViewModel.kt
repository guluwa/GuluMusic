package cn.guluwa.gulumusic.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel

import cn.guluwa.gulumusic.data.bean.FreshBean
import cn.guluwa.gulumusic.data.bean.LocalSongBean
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.data.bean.ViewDataBean
import cn.guluwa.gulumusic.data.total.SongsRepository

/**
 * Created by guluwa on 2018/1/12.
 */

class MainViewModel : ViewModel() {

    //热门歌曲刷新
    private var mHotSongListFresh: MutableLiveData<FreshBean>? = null
    //热门歌曲
    private var mHotSongs: LiveData<ViewDataBean<List<TracksBean>>>? = null

    //本地歌曲刷新
    private var mLocalSongListFresh: MutableLiveData<Boolean>? = null
    //本地歌曲
    private var mLocalSongs: LiveData<ViewDataBean<List<LocalSongBean>>>? = null

    //歌曲搜索
    private var mSearchSongListFresh: MutableLiveData<FreshBean>? = null
    //搜索歌曲
    private var mSearchSongs: LiveData<ViewDataBean<List<SearchResultSongBean>>>? = null

    /**
     * 查询热门歌曲
     *
     * @return
     */
    fun queryNetCloudHotSong(): LiveData<ViewDataBean<List<TracksBean>>>? {
        if (mHotSongs == null) {
            mHotSongListFresh = MutableLiveData()
            mHotSongs = Transformations.switchMap(mHotSongListFresh!!) { input ->
                if (input.isFresh) {
                    SongsRepository.getInstance().queryNetCloudHotSong(input.isFirstComing)
                } else {
                    null
                }
            }
        }
        return mHotSongs
    }

    /**
     * 刷新热门歌曲
     *
     * @param isFresh
     * @param isFirstComing
     */
    fun refreshHot(isFresh: Boolean, isFirstComing: Boolean) {
        mHotSongListFresh!!.value = FreshBean(isFresh, isFirstComing)
    }

    /**
     * 查询本地歌曲
     *
     * @return
     */
    fun queryLocalSong(): LiveData<ViewDataBean<List<LocalSongBean>>>? {
        if (mLocalSongs == null) {
            mLocalSongListFresh = MutableLiveData()
            mLocalSongs = Transformations.switchMap(mLocalSongListFresh!!) { input ->
                if (input!!) {
                    SongsRepository.getInstance().queryLocalSong()
                } else {
                    null
                }
            }
        }
        return mLocalSongs
    }

    /**
     * 刷新本地歌曲
     *
     * @param isFresh
     */
    fun refreshLocal(isFresh: Boolean) {
        mLocalSongListFresh!!.value = isFresh
    }

    /**
     * 歌曲搜索
     *
     * @return
     */
    fun searchSongByKeyWord(): LiveData<ViewDataBean<List<SearchResultSongBean>>>? {
        if (mSearchSongs == null) {
            mSearchSongListFresh = MutableLiveData()
            mSearchSongs = Transformations.switchMap(mSearchSongListFresh!!) { input ->
                if (input.isFresh) {
                    SongsRepository.getInstance().searchSongByKeyWord(input)
                } else {
                    null
                }
            }
        }
        return mSearchSongs
    }

    /**
     * 刷新歌曲搜索
     */
    fun refreshSearchSongs(key: String, page: Int, isFresh: Boolean) {
        mSearchSongListFresh!!.value = FreshBean(key, page, isFresh)
    }
}
