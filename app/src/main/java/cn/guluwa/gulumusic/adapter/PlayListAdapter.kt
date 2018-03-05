package cn.guluwa.gulumusic.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import java.util.ArrayList

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.data.bean.BaseSongBean
import cn.guluwa.gulumusic.data.bean.LocalSongBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.databinding.PlayListItemLayoutBinding
import cn.guluwa.gulumusic.listener.OnClickListener
import cn.guluwa.gulumusic.listener.OnLongClickListener
import cn.guluwa.gulumusic.utils.AppUtils

/**
 * Created by guluwa on 2018/1/11.
 */

class PlayListAdapter(private val listener: OnClickListener, private val longListener: OnLongClickListener) :
        RecyclerView.Adapter<PlayListAdapter.ViewHolder>() {

    var data: List<BaseSongBean>? = ArrayList()
        set(data) {
            field = data
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListAdapter.ViewHolder {
        val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context), R.layout.play_list_item_layout, parent, false)
        return ViewHolder(mDataBinding as PlayListItemLayoutBinding)
    }

    override fun onBindViewHolder(holder: PlayListAdapter.ViewHolder, position: Int) {
        holder.mViewBinder.song = this.data!![position]
    }

    override fun getItemCount(): Int {
        return if (this.data == null) 0 else this.data!!.size
    }

    inner class ViewHolder(val mViewBinder: PlayListItemLayoutBinding) : RecyclerView.ViewHolder(mViewBinder.root) {

        init {
            mViewBinder.setClickListener {
                if (this@PlayListAdapter.data!![adapterPosition] is TracksBean) {
                    listener.click(this@PlayListAdapter.data!![adapterPosition] as TracksBean)
                } else {
                    listener.click(AppUtils.getSongBean(this@PlayListAdapter.data!![adapterPosition] as LocalSongBean))
                }
            }
            mViewBinder.setLongClickListener {
                if (this@PlayListAdapter.data!![adapterPosition] is LocalSongBean) {
                    longListener.click(this@PlayListAdapter.data!![adapterPosition] as LocalSongBean)
                }
                true
            }
            val vectorDrawableCompat = VectorDrawableCompat.create(
                    mViewBinder.root.resources, R.drawable.ic_more_vertical, mViewBinder.root.context.theme)
            //你需要改变的颜色
            vectorDrawableCompat!!.setTint(mViewBinder.root.resources.getColor(R.color.play_view_gray))
            mViewBinder.ivMore.setImageDrawable(vectorDrawableCompat)
        }
    }
}
