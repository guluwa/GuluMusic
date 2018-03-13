package cn.guluwa.gulumusic.ui.list

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.Slide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.R.id.mRecyclerView
import cn.guluwa.gulumusic.data.bean.BaseSongBean
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.ui.adapter.PlaySongListAdapter
import cn.guluwa.gulumusic.ui.anim.SlideInRightAnimation
import cn.guluwa.gulumusic.ui.search.SearchActivity
import cn.guluwa.gulumusic.ui.view.dialog.SongMoreOperationDialog
import cn.guluwa.gulumusic.utils.AppUtils
import cn.guluwa.gulumusic.utils.listener.OnClickListener
import cn.guluwa.gulumusic.utils.listener.OnSelectListener

/**
 * Created by guluwa on 2018/3/12.
 */
class PlaySongListFragment : BottomSheetDialogFragment() {

    var mRecyclerView: RecyclerView? = null
    var tvTopTitle: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.play_song_list_fragment, container, false)
        mRecyclerView = view.findViewById(R.id.mRecyclerView) as RecyclerView
        tvTopTitle = view.findViewById(R.id.tvTopTitle)
        tvTopTitle!!.text = "播放队列(${AppManager.getInstance().musicAutoService!!.binder.mSongList!!.size}首歌)"
        val adapter = PlaySongListAdapter(AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition,
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
                                showSongMoreOperation(arg2)
                            }
                        }
                    }
                })
        adapter.setAnimation(SlideInRightAnimation())
        mRecyclerView!!.adapter = adapter
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
        mRecyclerView!!.scrollToPosition(AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition - 2)
        return view
    }

    var songMoreOperationDialog: Dialog? = null

    /**
     * 歌曲更多操作对话框
     */
    private fun showSongMoreOperation(song: Any) {
        songMoreOperationDialog = SongMoreOperationDialog(context, R.style.DialogStyle, object : OnSelectListener {
            override fun select(index: Int) {
                songMoreOperationDialog!!.dismiss()
                when (index) {
                    0 -> {
                        AppManager.getInstance().musicAutoService!!.binder.mSongList!!.removeAt((song as TracksBean).index)
                        if (song.index == AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition) {
                            AppManager.getInstance().musicAutoService!!.binder.removeCurrentSong()
                        } else if (song.index < AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition) {
                            AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition = AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition - 1
                            (mRecyclerView!!.adapter as PlaySongListAdapter).index = AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition
                        }
                        mRecyclerView!!.adapter.notifyDataSetChanged()
                        tvTopTitle!!.text = "播放队列(${AppManager.getInstance().musicAutoService!!.binder.mSongList!!.size}首歌)"
                    }
                    1 -> {
                        if ((song as TracksBean).singer!!.name != "") {
                            val intent = Intent(context, SearchActivity::class.java)
                            intent.putExtra("keyWord", (song as TracksBean).singer!!.name)
                            startActivity(intent)
                        }
                    }
                }
            }
        }, arrayListOf("从播放队列中移除", "查看歌手"))
        songMoreOperationDialog!!.show()
    }

    private fun playNewSong(song: TracksBean) {
        //播放歌曲、利用服务后台播放
        AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
        AppManager.getInstance().musicAutoService!!.binder.currentSong = song
        AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(null, false)
        (mRecyclerView!!.adapter as PlaySongListAdapter).index = AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition
        (mRecyclerView!!.adapter as PlaySongListAdapter).notifyItemRangeChanged(0, AppManager.getInstance().musicAutoService!!.binder.mSongList!!.size, "")
//        mRecyclerView!!.scrollToPosition(AppManager.getInstance().musicAutoService!!.binder.mCurrentPosition - 2)
    }
}