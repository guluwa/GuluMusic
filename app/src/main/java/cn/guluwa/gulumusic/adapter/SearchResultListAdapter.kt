package cn.guluwa.gulumusic.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.data.bean.SearchHistoryBean
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean
import cn.guluwa.gulumusic.databinding.ListEmptyLayoutBinding
import cn.guluwa.gulumusic.databinding.LoadMoreLayoutBinding
import cn.guluwa.gulumusic.databinding.SearchHistoryListItemLayoutBinding
import cn.guluwa.gulumusic.databinding.SearchResultListItemLayoutBinding
import cn.guluwa.gulumusic.listener.OnClickListener

/**
 * Created by guluwa on 2018/2/5.
 */

class SearchResultListAdapter(private val listener: OnClickListener) :
        RecyclerView.Adapter<SearchResultListAdapter.ViewHolder>() {

    private var color: Int = 0//按钮颜色

    var data = arrayListOf<Any>()

    fun setSongs(data: List<SearchResultSongBean>?, mLoadMoreTip: String) {
        if (data != null && data.isNotEmpty()) {
            this.data.removeAll(this.data)
            this.data.addAll(data)
            this.data.add(mLoadMoreTip)
            notifyDataSetChanged()
        }
    }

    fun addSongs(data: List<SearchResultSongBean>?, mLoadMoreTip: String) {
        if (data != null && data.isNotEmpty()) {
            val position = this.data.size
            this.data.removeAt(this.data.size - 1)
            notifyItemRemoved(this.data.size)
            this.data.addAll(data)
            this.data.add(mLoadMoreTip)
            notifyItemRangeInserted(position, data.size)
        }
    }

    fun setLoadMoreTip(mLoadMoreTip: String) {
        if (data.size != 0) {
            data[data.size - 1] = mLoadMoreTip
            notifyItemChanged(data.size - 1)
        }
    }

    fun setSearchHistory(mListEmptyTip: String, list: List<SearchHistoryBean>?) {
        if (data.size == 0) {
            if (list != null) {
                data.addAll(list)
            }
            data.add(SearchHistoryBean(0L, mListEmptyTip))
        } else {
            (data[data.size - 1] as SearchHistoryBean).text = mListEmptyTip
        }
        notifyDataSetChanged()
    }

    fun setColor(color: Int) {
        this.color = color
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position] is String) {
            TYPE_FOOTER
        } else if (data[position] is SearchHistoryBean) {
            if ((data[position] as SearchHistoryBean).date == 0L) {
                TYPE_EMPTY
            } else {
                TYPE_SEARCH_HISTORY
            }
        } else {
            TYPE_NORMAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultListAdapter.ViewHolder {
        return when (viewType) {
            TYPE_NORMAL -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                        R.layout.search_result_list_item_layout, parent, false)
                ViewHolder(mDataBinding as SearchResultListItemLayoutBinding)
            }
            TYPE_FOOTER -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                        R.layout.load_more_layout, parent, false)
                ViewHolder(mDataBinding as LoadMoreLayoutBinding)
            }
            TYPE_SEARCH_HISTORY -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                        R.layout.search_history_list_item_layout, parent, false)
                ViewHolder((mDataBinding as SearchHistoryListItemLayoutBinding))
            }
            else -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                        R.layout.list_empty_layout, parent, false)
                ViewHolder(mDataBinding as ListEmptyLayoutBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: SearchResultListAdapter.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            holder.mViewBinder as SearchResultListItemLayoutBinding
            holder.mViewBinder.song = data[position] as SearchResultSongBean
            holder.mViewBinder.index = position + 1
            //more
            val vectorDrawableMore = VectorDrawableCompat.create(
                    holder.mViewBinder.root.resources,
                    R.drawable.ic_more_vertical,
                    holder.mViewBinder.root.context.theme)
            vectorDrawableMore!!.setTint(holder.mViewBinder.root.resources.getColor(color))
            holder.mViewBinder.ivMore.setImageDrawable(vectorDrawableMore)
            //download
            if ((data[position] as SearchResultSongBean).isDownLoad) {
                val vectorDrawableDownLoad = VectorDrawableCompat.create(
                        holder.mViewBinder.root.resources,
                        R.drawable.ic_song_has_down_load,
                        holder.mViewBinder.root.context.theme)
                vectorDrawableDownLoad!!.setTint(holder.mViewBinder.root.resources.getColor(color))
                holder.mViewBinder.ivSongStatus.setImageDrawable(vectorDrawableDownLoad)
                holder.mViewBinder.ivSongStatus.visibility = View.VISIBLE
            } else {
                holder.mViewBinder.ivSongStatus.visibility = View.GONE
            }
        } else if (getItemViewType(position) == TYPE_FOOTER) {
            (holder.mViewBinder as LoadMoreLayoutBinding).loadMoreTip = data[position] as String
        } else if (getItemViewType(position) == TYPE_SEARCH_HISTORY) {
            (holder.mViewBinder as SearchHistoryListItemLayoutBinding).history = (data[position] as SearchHistoryBean).text
        } else {
            (holder.mViewBinder as ListEmptyLayoutBinding).pageTip = (data[position] as SearchHistoryBean).text
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val mViewBinder: ViewDataBinding) :
            RecyclerView.ViewHolder(mViewBinder.root) {

        init {
            when (mViewBinder) {
                is SearchResultListItemLayoutBinding -> mViewBinder.setClickListener({ view ->
                    (data[adapterPosition] as SearchResultSongBean).index = adapterPosition
                    when (view.id) {
                        R.id.mSongContainer -> listener.click(1, data[adapterPosition])
                        R.id.ivMore -> listener.click(2, data[adapterPosition])
                    }
                })
                is LoadMoreLayoutBinding -> mViewBinder.setClickListener { listener.click(1, data[adapterPosition]) }
                is SearchHistoryListItemLayoutBinding -> mViewBinder.setClickListener { listener.click(1, data[adapterPosition]) }
            }
        }

    }

    companion object {

        private const val TYPE_FOOTER = 1//loadMoreView

        private const val TYPE_NORMAL = 0//普通数据

        private const val TYPE_EMPTY = 2//列表空、错误提示

        private const val TYPE_SEARCH_HISTORY = 3//搜索记录
    }
}
