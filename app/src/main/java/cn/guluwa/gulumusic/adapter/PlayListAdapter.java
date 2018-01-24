package cn.guluwa.gulumusic.adapter;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.databinding.PlayListItemLayoutBinding;
import cn.guluwa.gulumusic.listener.OnClickListener;

/**
 * Created by guluwa on 2018/1/11.
 */

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {

    private OnClickListener listener;

    private List<TracksBean> data = new ArrayList<>();

    public PlayListAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<TracksBean> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public PlayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding mDataBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.play_list_item_layout, parent, false);
        return new ViewHolder((PlayListItemLayoutBinding) mDataBinding);
    }

    @Override
    public void onBindViewHolder(PlayListAdapter.ViewHolder holder, int position) {
        holder.getmPlayListItemLayoutBinding().setSong(data.get(position));
        holder.getmPlayListItemLayoutBinding().setIndex(position + 1);
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

        public ViewHolder(PlayListItemLayoutBinding mPlayListItemLayoutBinding) {
            super(mPlayListItemLayoutBinding.getRoot());
            this.mPlayListItemLayoutBinding = mPlayListItemLayoutBinding;
            mPlayListItemLayoutBinding.setClickListener(view -> {
                listener.click(data.get(getAdapterPosition()),mPlayListItemLayoutBinding.ivSongPic);
            });
        }
    }
}
