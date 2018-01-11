package cn.guluwa.gulumusic.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.data.bean.PlayListBean;
import cn.guluwa.gulumusic.databinding.PlayListItemLayoutBinding;

/**
 * Created by guluwa on 2018/1/11.
 */

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {

    private List<PlayListBean.PlaylistBean.TracksBean> data = new ArrayList<>();

    public PlayListAdapter() {
    }

    public void setData(List<PlayListBean.PlaylistBean.TracksBean> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public PlayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding mDataBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.play_list_item_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(mDataBinding.getRoot());
        viewHolder.setmPlayListItemLayoutBinding((PlayListItemLayoutBinding) mDataBinding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PlayListAdapter.ViewHolder holder, int position) {
        holder.getmPlayListItemLayoutBinding().setSong(data.get(position));
        holder.getmPlayListItemLayoutBinding().setSinger(data.get(position).getAr().get(0));
//        holder.getmPlayListItemLayoutBinding().set
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        PlayListItemLayoutBinding mPlayListItemLayoutBinding;

        public PlayListItemLayoutBinding getmPlayListItemLayoutBinding() {
            return mPlayListItemLayoutBinding;
        }

        public void setmPlayListItemLayoutBinding(PlayListItemLayoutBinding mPlayListItemLayoutBinding) {
            this.mPlayListItemLayoutBinding = mPlayListItemLayoutBinding;
        }

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
