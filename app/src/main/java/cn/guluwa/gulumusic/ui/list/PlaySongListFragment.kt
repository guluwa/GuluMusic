package cn.guluwa.gulumusic.ui.list

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.ui.adapter.PlaySongListAdapter
import cn.guluwa.gulumusic.utils.listener.OnClickListener
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by guluwa on 2018/3/12.
 */
class PlaySongListFragment : BottomSheetDialogFragment() {

    var mRecyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.play_song_list_fragment, container, false)
        mRecyclerView = view.findViewById(R.id.mRecyclerView) as RecyclerView
        mRecyclerView!!.adapter = PlaySongListAdapter(AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition,
                object : OnClickListener {
                    override fun click(arg1: Int, arg2: Any) {
                        when (arg1) {
                            1 -> {
                                if (arg2 is TracksBean) {
                                    if (AppManager.getInstance().musicAutoService!!.binder.currentSong != null) {
                                        if (arg2.id == AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id &&
                                                AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                                            return
                                        }
                                    }
                                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                                        AppManager.getInstance().musicAutoService!!.binder.stop()
                                    }
                                    playNewSong(arg2)
                                }
                            }
                            2 -> {

                            }
                        }
                    }
                })
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
        mRecyclerView!!.scrollToPosition(AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition - 2)
        return view
    }

    private fun playNewSong(song: TracksBean) {
        //播放歌曲、利用服务后台播放
        AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
        AppManager.getInstance().musicAutoService!!.binder.currentSong = song
        AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(null, false)
        (mRecyclerView!!.adapter as PlaySongListAdapter).fresh(AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition)
    }
}