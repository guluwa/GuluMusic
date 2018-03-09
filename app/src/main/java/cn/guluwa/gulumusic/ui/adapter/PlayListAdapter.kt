package cn.guluwa.gulumusic.ui.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.data.bean.BaseSongBean
import cn.guluwa.gulumusic.data.bean.LocalSongBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.databinding.PlayListItemLayoutBinding
import cn.guluwa.gulumusic.utils.listener.OnClickListener
import cn.guluwa.gulumusic.utils.listener.OnLongClickListener
import cn.guluwa.gulumusic.utils.AppUtils

/**
 * Created by guluwa on 2018/1/11.
 */

class PlayListAdapter(private val listener: OnClickListener, private val longListener: OnLongClickListener) :
        RecyclerView.Adapter<PlayListAdapter.ViewHolder>() {

    var data = arrayListOf<BaseSongBean>()
        set(data) {
            field = data
            notifyDataSetChanged()
        }

    fun removeSong(position: Int) {
        if (data.size > position) {
            data.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListAdapter.ViewHolder {
        val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context), R.layout.play_list_item_layout, parent, false)
        return ViewHolder(mDataBinding as PlayListItemLayoutBinding)
    }

    override fun onBindViewHolder(holder: PlayListAdapter.ViewHolder, position: Int) {
        holder.mViewBinder.song = this.data[position]
    }

    override fun getItemCount(): Int {
        return this.data.size
    }

    inner class ViewHolder(val mViewBinder: PlayListItemLayoutBinding) : RecyclerView.ViewHolder(mViewBinder.root) {

        init {
            mViewBinder.setClickListener({ view ->
                when (view.id) {
                    R.id.mCardView -> {
                        if (this@PlayListAdapter.data[adapterPosition] is TracksBean) {
                            listener.click(1, this@PlayListAdapter.data[adapterPosition])
                        } else {
                            listener.click(1, AppUtils.getSongBean(this@PlayListAdapter.data[adapterPosition] as LocalSongBean))
                        }
                    }
                    R.id.ivMore -> {
                        if (this@PlayListAdapter.data[adapterPosition] is TracksBean) {
                            if ((this@PlayListAdapter.data[adapterPosition] as TracksBean).local) {
                                val localSongBean = AppUtils.getLocalSongBean(this@PlayListAdapter.data[adapterPosition] as TracksBean)
                                localSongBean.position = adapterPosition
                                listener.click(2, (localSongBean))
                            } else {
                                listener.click(2, this@PlayListAdapter.data[adapterPosition])
                            }
                        } else {
                            (this@PlayListAdapter.data[adapterPosition] as LocalSongBean).position = adapterPosition
                            listener.click(2, this@PlayListAdapter.data[adapterPosition] as LocalSongBean)
                        }
                    }
                }
            })
            mViewBinder.setLongClickListener {
                if (this@PlayListAdapter.data[adapterPosition] is LocalSongBean) {
                    (this@PlayListAdapter.data[adapterPosition] as LocalSongBean).position = adapterPosition
                    longListener.click(this@PlayListAdapter.data[adapterPosition] as LocalSongBean)
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
