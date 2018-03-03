package cn.guluwa.gulumusic.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.adapter.SearchResultListAdapter.ViewHolder
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean
import cn.guluwa.gulumusic.databinding.ListEmptyLayoutBinding
import cn.guluwa.gulumusic.databinding.LoadMoreLayoutBinding
import cn.guluwa.gulumusic.databinding.SearchResultListItemLayoutBinding
import cn.guluwa.gulumusic.listener.OnClickListener

/**
 * Created by guluwa on 2018/2/5.
 */

class SearchResultListAdapter(private val listener: OnClickListener) :
        RecyclerView.Adapter<ViewHolder>() {

    private var color: Int = 0//按钮颜色

    var dataList: MutableList<Any>? = ArrayList()

    fun getData(): MutableList<Any>? {
        return dataList
    }

    fun setData(data: MutableList<Any>?, mLoadMoreTip: String) {
        if (data != null && data.isNotEmpty()) {
            this.dataList!!.clear()
            this.dataList!!.addAll(data)
            this.dataList!!.add(mLoadMoreTip as Any)
            notifyDataSetChanged()
        }
    }

    fun addData(data: MutableList<Any>?, mLoadMoreTip: String) {
        if (data != null && data.isNotEmpty()) {
            val position = this.dataList!!.size
            this.dataList!!.removeAt(this.dataList!!.size - 1)
            notifyItemRemoved(this.dataList!!.size)
            this.dataList!!.addAll(data)
            this.dataList!!.add(mLoadMoreTip as Any)
            notifyItemRangeInserted(position, data.size)
        }
    }

    fun setLoadMoreTip(mLoadMoreTip: String) {
        if (dataList != null && dataList!!.size != 0) {
            dataList!![dataList!!.size - 1] = mLoadMoreTip as Any
            notifyItemChanged(dataList!!.size - 1)
        }
    }

    fun setListEmptyTip(mListEmptyTip: String) {
        if (dataList == null || dataList!!.size == 0) {
            dataList = ArrayList()
            dataList!!.add(mListEmptyTip as Any)
        } else {
            dataList!![dataList!!.size - 1] = mListEmptyTip as Any
        }
        notifyDataSetChanged()
    }

    fun setColor(color: Int) {
        this.color = color
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList!![position] is String) {
            if (dataList!!.size == 1) {
                TYPE_EMPTY
            } else {
                TYPE_FOOTER
            }
        } else {
            TYPE_NORMAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_NORMAL -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.search_result_list_item_layout, parent, false)
                ViewHolder(mDataBinding as SearchResultListItemLayoutBinding)
            }
            TYPE_FOOTER -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.load_more_layout, parent, false)
                ViewHolder(mDataBinding as LoadMoreLayoutBinding)
            }
            else -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.list_empty_layout, parent, false)
                ViewHolder(mDataBinding as ListEmptyLayoutBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            holder.viewBinder.song = dataList!![position] as SearchResultSongBean
            holder.viewBinder.index = position + 1
            //more
            val vectorDrawableMore = VectorDrawableCompat.create(
                    holder.viewBinder.root.resources,
                    R.drawable.ic_more_vertical,
                    holder.viewBinder.root.context.theme)
            vectorDrawableMore!!.setTint(holder.viewBinder.root.resources.getColor(color))
            holder.viewBinder.ivMore.setImageDrawable(vectorDrawableMore)
            //download
            if ((dataList!![position] as SearchResultSongBean).isDownLoad) {
                val vectorDrawableDownLoad = VectorDrawableCompat.create(
                        holder.viewBinder.root.resources,
                        R.drawable.ic_song_has_down_load,
                        holder.viewBinder.root.context.theme)
                vectorDrawableDownLoad!!.setTint(holder.viewBinder.root.resources.getColor(color))
                holder.viewBinder.ivSongStatus.setImageDrawable(vectorDrawableDownLoad)
                holder.viewBinder.ivSongStatus.visibility = View.VISIBLE
            } else {
                holder.viewBinder.ivSongStatus.visibility = View.GONE
            }
        } else if (getItemViewType(position) == TYPE_FOOTER) {
            holder.loadMoreViewBinder.loadMoreTip = dataList!![position] as String
        } else {
            holder.listEmptyViewBinder.pageTip = dataList!![position] as String
        }
    }

    override fun getItemCount(): Int {
        return if (dataList == null) 0 else dataList!!.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {

        lateinit var viewBinder: SearchResultListItemLayoutBinding
        lateinit var loadMoreViewBinder: LoadMoreLayoutBinding
        lateinit var listEmptyViewBinder: ListEmptyLayoutBinding

        constructor(mViewBinder: SearchResultListItemLayoutBinding) : super(mViewBinder.root) {
            viewBinder = mViewBinder
            mViewBinder.setClickListener { listener.click(dataList!![adapterPosition]) }
        }

        constructor(mLoadMoreViewBinder: LoadMoreLayoutBinding) : super(mLoadMoreViewBinder.root) {
            loadMoreViewBinder = mLoadMoreViewBinder
            mLoadMoreViewBinder.setClickListener { listener.click("" as Any) }
        }

        constructor(mListEmptyViewBinder: ListEmptyLayoutBinding) : super(mListEmptyViewBinder.root) {
            listEmptyViewBinder = mListEmptyViewBinder
        }
    }

    companion object {

        private const val TYPE_FOOTER = 1//loadMoreView

        private const val TYPE_NORMAL = 0//普通数据

        private const val TYPE_EMPTY = 2//列表空、错误提示
    }
}
