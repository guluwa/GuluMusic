package cn.guluwa.gulumusic.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean;
import cn.guluwa.gulumusic.databinding.SearchResultListItemLayoutBinding;
import cn.guluwa.gulumusic.listener.OnClickListener;

/**
 * Created by guluwa on 2018/2/5.
 */

public class SearchResultListAdapter extends RecyclerView.Adapter<SearchResultListAdapter.ViewHolder> {

    private OnClickListener listener;

    private List<SearchResultSongBean> data = new ArrayList<>();

    public SearchResultListAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public List<SearchResultSongBean> getData() {
        return data;
    }

    public void setData(List<SearchResultSongBean> data) {
        if (data != null && data.size() != 0) {
            this.data.clear();
            data.get(data.size() - 1).setLoadMoreTip("点我，继续加载~~~");
            this.data.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void addData(List<SearchResultSongBean> data) {
        int position = this.data.size() - 1;
        if (data != null && data.size() != 0) {
            data.get(data.size() - 1).setLoadMoreTip("点我，继续加载~~~");
            this.data.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void setLoadMoreTip(String mLoadMoreTip) {
        if (data != null && data.size() != 0) {
            data.get(data.size() - 1).setLoadMoreTip(mLoadMoreTip);
            notifyDataSetChanged();
        }
    }

    @Override
    public SearchResultListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding mDataBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.search_result_list_item_layout, parent, false);
        return new SearchResultListAdapter.ViewHolder((SearchResultListItemLayoutBinding) mDataBinding);
    }

    @Override
    public void onBindViewHolder(SearchResultListAdapter.ViewHolder holder, int position) {
        holder.getViewBinder().setSong(data.get(position));
        holder.getViewBinder().setIndex(position + 1);
        holder.getViewBinder().setVisibility(position == data.size() - 1);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        SearchResultListItemLayoutBinding mViewBinder;

        ViewHolder(SearchResultListItemLayoutBinding mViewBinder) {
            super(mViewBinder.getRoot());
            setViewBinder(mViewBinder);
            mViewBinder.setClickListener(view -> {
                switch (view.getId()) {
                    case R.id.mSongContainer:
                        listener.click(data.get(getAdapterPosition()));
                        break;
                    case R.id.mSongLoadMore:
                        listener.click("");
                        break;
                }
            });
        }

        SearchResultListItemLayoutBinding getViewBinder() {
            return mViewBinder;
        }

        void setViewBinder(SearchResultListItemLayoutBinding mViewBinder) {
            this.mViewBinder = mViewBinder;
        }
    }
}
