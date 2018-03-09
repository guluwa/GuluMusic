package cn.guluwa.gulumusic.ui.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.databinding.SongMoreOperationRecylItemLayoutBinding
import cn.guluwa.gulumusic.utils.listener.OnSelectListener

/**
 * Created by guluwa on 2018/3/7.
 */

class SongMoreOperationAdapter(val list: List<String>, val listener: OnSelectListener) : RecyclerView.Adapter<SongMoreOperationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongMoreOperationAdapter.ViewHolder {
        val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context), R.layout.song_more_operation_recyl_item_layout, parent, false)
        return ViewHolder(mDataBinding as SongMoreOperationRecylItemLayoutBinding)
    }

    override fun onBindViewHolder(holder: SongMoreOperationAdapter.ViewHolder, position: Int) {
        holder.mViewBinding.text = list[position]
        holder.mViewBinding.isLast = position == list.size - 1
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val mViewBinding: SongMoreOperationRecylItemLayoutBinding) : RecyclerView.ViewHolder(mViewBinding.root) {
        init {
            itemView.setOnClickListener { this@SongMoreOperationAdapter.listener.select(adapterPosition) }
        }
    }
}