package cn.guluwa.gulumusic.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
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
import java.util.*

/**
 * Created by guluwa on 2018/2/5.
 */

class SearchResultListAdapter(private val listener: OnClickListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var color: Int = 0//按钮颜色

    var dataList = arrayListOf<Any>()

    fun setData(data: MutableList<Any>?, mLoadMoreTip: String) {
        if (data != null && data.isNotEmpty()) {
            dataList.clear()
            dataList.addAll(data)
            dataList.add(mLoadMoreTip)
            notifyDataSetChanged()
        }
    }

    fun addData(data: MutableList<Any>?, mLoadMoreTip: String) {
        if (data != null && data.isNotEmpty()) {
            val position = dataList.size
            dataList.removeAt(this.dataList.size - 1)
            notifyItemRemoved(dataList.size)
            dataList.addAll(data)
            dataList.add(mLoadMoreTip)
            notifyItemRangeInserted(position, data.size)
        }
    }

    fun setLoadMoreTip(mLoadMoreTip: String) {
        if (dataList.size != 0) {
            dataList[dataList.size - 1] = mLoadMoreTip as Any
            notifyItemChanged(dataList.size - 1)
        }
    }

    fun setSearchHistory(mListEmptyTip: String, list: List<SearchHistoryBean>?) {
        if (dataList.size == 0) {
            dataList = ArrayList()
            if (list != null) {
                dataList.addAll(list)
            }
            dataList.add(SearchHistoryBean(0L, mListEmptyTip))
        } else {
            (dataList[dataList.size - 1] as SearchHistoryBean).text = mListEmptyTip
        }
        notifyDataSetChanged()
    }

    fun setColor(color: Int) {
        this.color = color
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] is String) {
            TYPE_FOOTER
        } else if (dataList[position] is SearchHistoryBean) {
            if ((dataList[position] as SearchHistoryBean).date == 0L) {
                TYPE_EMPTY
            } else {
                TYPE_SEARCH_HISTORY
            }
        } else {
            TYPE_NORMAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_NORMAL -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                        R.layout.search_result_list_item_layout, parent, false)
                SearchResultListItemViewHolder(mDataBinding as SearchResultListItemLayoutBinding)
            }
            TYPE_FOOTER -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                        R.layout.load_more_layout, parent, false)
                LoadMoreViewHolder(mDataBinding as LoadMoreLayoutBinding)
            }
            TYPE_SEARCH_HISTORY -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                        R.layout.search_history_list_item_layout, parent, false)
                SearchHistoryViewHolder((mDataBinding as SearchHistoryListItemLayoutBinding))

            }
            else -> {
                val mDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                        R.layout.list_empty_layout, parent, false)
                ListEmptyViewHolder(mDataBinding as ListEmptyLayoutBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    println("down")

                }
                MotionEvent.ACTION_UP ,MotionEvent.ACTION_MOVE-> {
                    println("up,move")

                }
                MotionEvent.ACTION_CANCEL -> {
                    println("cancel")
                }
            }
            false
        }
        if (getItemViewType(position) == TYPE_NORMAL) {
            holder as SearchResultListItemViewHolder
            holder.mViewBinder.song = dataList[position] as SearchResultSongBean
            holder.mViewBinder.index = position + 1
            //more
            val vectorDrawableMore = VectorDrawableCompat.create(
                    holder.mViewBinder.root.resources,
                    R.drawable.ic_more_vertical,
                    holder.mViewBinder.root.context.theme)
            vectorDrawableMore!!.setTint(holder.mViewBinder.root.resources.getColor(color))
            holder.mViewBinder.ivMore.setImageDrawable(vectorDrawableMore)
            //download
            if ((dataList[position] as SearchResultSongBean).isDownLoad) {
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
            (holder as LoadMoreViewHolder).mViewBinder.loadMoreTip = dataList[position] as String
        } else if (getItemViewType(position) == TYPE_SEARCH_HISTORY) {
            (holder as SearchHistoryViewHolder).mViewBinder.history = (dataList[position] as SearchHistoryBean).text
        } else {
            (holder as ListEmptyViewHolder).mViewBinder.pageTip = (dataList[position] as SearchHistoryBean).text
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class SearchResultListItemViewHolder(val mViewBinder: SearchResultListItemLayoutBinding) :
            RecyclerView.ViewHolder(mViewBinder.root) {

        init {
            mViewBinder.setClickListener {
                listener.click(dataList[adapterPosition])
                println("click $adapterPosition")
            }
        }
    }

    inner class LoadMoreViewHolder(val mViewBinder: LoadMoreLayoutBinding) :
            RecyclerView.ViewHolder(mViewBinder.root) {

        init {
            mViewBinder.setClickListener {
                listener.click("")
                println("click $adapterPosition")
            }
        }
    }

    inner class ListEmptyViewHolder(val mViewBinder: ListEmptyLayoutBinding) :
            RecyclerView.ViewHolder(mViewBinder.root) {

        init {

        }
    }

    inner class SearchHistoryViewHolder(val mViewBinder: SearchHistoryListItemLayoutBinding) :
            RecyclerView.ViewHolder(mViewBinder.root) {

        init {
            mViewBinder.setClickListener {
                listener.click(dataList[adapterPosition])
                println("click $adapterPosition")
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
