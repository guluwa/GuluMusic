package cn.guluwa.gulumusic.ui.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.data.bean.LocalSongBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.databinding.PlaySongListItemLayoutBinding
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.utils.AppUtils
import cn.guluwa.gulumusic.utils.listener.OnClickListener


/**
 * Created by guluwa on 2018/3/12.
 */
class PlaySongListAdapter(var index: Int, private val listener: OnClickListener) :
        BaseListAdapter<PlaySongListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaySongListAdapter.ViewHolder {
        val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context), R.layout.play_song_list_item_layout, parent, false)
        return ViewHolder(mDataBinding as PlaySongListItemLayoutBinding)
    }

    override fun getItemCount(): Int {
        return if (AppManager.getInstance().musicAutoService!!.binder.mSongList == null)
            0
        else
            AppManager.getInstance().musicAutoService!!.binder.mSongList!!.size
    }

    override fun convert(holder: ViewHolder) {
        val position = holder.adapterPosition
        holder.mViewBinding.song = AppManager.getInstance().musicAutoService!!.binder.mSongList!![position]
        holder.mViewBinding.index = position - index
        holder.mViewBinding.played = position - index <= 0
    }

    inner class ViewHolder(val mViewBinding: PlaySongListItemLayoutBinding) : RecyclerView.ViewHolder(mViewBinding.root) {

        init {
            mViewBinding.setClickListener({ view ->
                when (view.id) {
                    R.id.mSongContainer ->
                        if (AppManager.getInstance().musicAutoService!!.binder.mSongList!![adapterPosition] is TracksBean) {
                            this@PlaySongListAdapter.listener.click(
                                    1, AppManager.getInstance().musicAutoService!!.binder.mSongList!![adapterPosition])
                        } else {
                            this@PlaySongListAdapter.listener.click(
                                    1, AppUtils.getSongBean(AppManager.getInstance().musicAutoService!!.binder.mSongList!![adapterPosition] as LocalSongBean))
                        }
                    R.id.ivMore ->
                        if (AppManager.getInstance().musicAutoService!!.binder.mSongList!![adapterPosition] is TracksBean) {
                            (AppManager.getInstance().musicAutoService!!.binder.mSongList!![adapterPosition] as TracksBean).index = adapterPosition
                            this@PlaySongListAdapter.listener.click(
                                    2, AppManager.getInstance().musicAutoService!!.binder.mSongList!![adapterPosition])
                        } else {
                            (AppManager.getInstance().musicAutoService!!.binder.mSongList!![adapterPosition] as LocalSongBean).index=adapterPosition
                            this@PlaySongListAdapter.listener.click(
                                    2, AppUtils.getSongBean(AppManager.getInstance().musicAutoService!!.binder.mSongList!![adapterPosition] as LocalSongBean))
                        }
                }
            })
            val vectorDrawableCompat = VectorDrawableCompat.create(
                    mViewBinding.root.resources, R.drawable.ic_more_vertical, mViewBinding.root.context.theme)
            //你需要改变的颜色
            vectorDrawableCompat!!.setTint(mViewBinding.root.resources.getColor(R.color.play_view_black))
            mViewBinding.ivMore.setImageDrawable(vectorDrawableCompat)
        }
    }
}