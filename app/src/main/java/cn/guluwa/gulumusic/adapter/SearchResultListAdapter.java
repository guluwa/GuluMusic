package cn.guluwa.gulumusic.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean;
import cn.guluwa.gulumusic.databinding.ListEmptyLayoutBinding;
import cn.guluwa.gulumusic.databinding.LoadMoreLayoutBinding;
import cn.guluwa.gulumusic.databinding.SearchResultListItemLayoutBinding;
import cn.guluwa.gulumusic.listener.OnClickListener;

/**
 * Created by guluwa on 2018/2/5.
 */

public class SearchResultListAdapter<T> extends RecyclerView.Adapter<SearchResultListAdapter.ViewHolder> {

    private static final int TYPE_FOOTER = 1;//loadMoreView

    private static final int TYPE_NORMAL = 0;//普通数据

    private static final int TYPE_EMPTY = 2;//列表空、错误提示

    private OnClickListener listener;//点击事件监听

    private int color;//按钮颜色

    private List<T> data = new ArrayList<>();

    public SearchResultListAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data, String mLoadMoreTip) {
        if (data != null && data.size() != 0) {
            this.data.clear();
            this.data.addAll(data);
            this.data.add((T) mLoadMoreTip);
            notifyDataSetChanged();
        }
    }

    public void addData(List<T> data, String mLoadMoreTip) {
        if (data != null && data.size() != 0) {
            int position = this.data.size();
            this.data.remove(this.data.size() - 1);
            notifyItemRemoved(this.data.size());
            this.data.addAll(data);
            this.data.add((T) mLoadMoreTip);
            notifyItemRangeInserted(position, data.size());
        }
    }

    public void setLoadMoreTip(String mLoadMoreTip) {
        if (data != null && data.size() != 0) {
            data.set(data.size() - 1, (T) mLoadMoreTip);
            notifyItemChanged(data.size() - 1);
        }
    }

    public void setListEmptyTip(String mListEmptyTip) {
        if (data == null || data.size() == 0) {
            data = new ArrayList<>();
            data.add((T) mListEmptyTip);
        } else {
            data.set(data.size() - 1, (T) mListEmptyTip);
        }
        notifyDataSetChanged();
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position) instanceof String) {
            if (data.size() == 1) {
                return TYPE_EMPTY;
            } else {
                return TYPE_FOOTER;
            }
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public SearchResultListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            ViewDataBinding mDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.search_result_list_item_layout, parent, false);
            return new SearchResultListAdapter.ViewHolder((SearchResultListItemLayoutBinding) mDataBinding);
        } else if (viewType == TYPE_FOOTER) {
            ViewDataBinding mDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.load_more_layout, parent, false);
            return new SearchResultListAdapter.ViewHolder((LoadMoreLayoutBinding) mDataBinding);
        } else {
            ViewDataBinding mDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_empty_layout, parent, false);
            return new SearchResultListAdapter.ViewHolder((ListEmptyLayoutBinding) mDataBinding);
        }
    }

    @Override
    public void onBindViewHolder(SearchResultListAdapter.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            holder.getViewBinder().setSong((SearchResultSongBean) data.get(position));
            holder.getViewBinder().setIndex(position + 1);
            //more
            VectorDrawableCompat vectorDrawableMore = VectorDrawableCompat.create(
                    holder.getViewBinder().getRoot().getResources(),
                    R.drawable.ic_more_vertical,
                    holder.getViewBinder().getRoot().getContext().getTheme());
            vectorDrawableMore.setTint(holder.getViewBinder().getRoot().getResources().getColor(color));
            holder.getViewBinder().ivMore.setImageDrawable(vectorDrawableMore);
            //download
            if (((SearchResultSongBean) data.get(position)).isDownLoad()) {
                VectorDrawableCompat vectorDrawableDownLoad = VectorDrawableCompat.create(
                        holder.getViewBinder().getRoot().getResources(),
                        R.drawable.ic_song_has_down_load,
                        holder.getViewBinder().getRoot().getContext().getTheme());
                vectorDrawableDownLoad.setTint(holder.getViewBinder().getRoot().getResources().getColor(color));
                holder.getViewBinder().ivSongStatus.setImageDrawable(vectorDrawableDownLoad);
                holder.getViewBinder().ivSongStatus.setVisibility(View.VISIBLE);
            } else {
                holder.getViewBinder().ivSongStatus.setVisibility(View.GONE);
            }
        } else if (getItemViewType(position) == TYPE_FOOTER) {
            holder.getLoadMoreViewBinder().setLoadMoreTip((String) data.get(position));
        } else {
            holder.getListEmptyViewBinder().setPageTip((String) data.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        SearchResultListItemLayoutBinding mDataViewBinder;
        LoadMoreLayoutBinding mLoadMoreViewBinder;
        ListEmptyLayoutBinding mListEmptyViewBinder;

        ViewHolder(SearchResultListItemLayoutBinding mViewBinder) {
            super(mViewBinder.getRoot());
            setViewBinder(mViewBinder);
            mViewBinder.setClickListener(view -> {
                listener.click(data.get(getAdapterPosition()));
            });
        }

        ViewHolder(LoadMoreLayoutBinding mLoadMoreViewBinder) {
            super(mLoadMoreViewBinder.getRoot());
            setLoadMoreViewBinder(mLoadMoreViewBinder);
            mLoadMoreViewBinder.setClickListener(view -> {
                listener.click("");
            });
        }

        ViewHolder(ListEmptyLayoutBinding mListEmptyViewBinder) {
            super(mListEmptyViewBinder.getRoot());
            setListEmptyViewBinder(mListEmptyViewBinder);
        }

        LoadMoreLayoutBinding getLoadMoreViewBinder() {
            return mLoadMoreViewBinder;
        }

        void setLoadMoreViewBinder(LoadMoreLayoutBinding mLoadMoreViewBinder) {
            this.mLoadMoreViewBinder = mLoadMoreViewBinder;
        }

        SearchResultListItemLayoutBinding getViewBinder() {
            return mDataViewBinder;
        }

        void setViewBinder(SearchResultListItemLayoutBinding mViewBinder) {
            this.mDataViewBinder = mViewBinder;
        }

        ListEmptyLayoutBinding getListEmptyViewBinder() {
            return mListEmptyViewBinder;
        }

        void setListEmptyViewBinder(ListEmptyLayoutBinding mListEmptyViewBinder) {
            this.mListEmptyViewBinder = mListEmptyViewBinder;
        }
    }
}
