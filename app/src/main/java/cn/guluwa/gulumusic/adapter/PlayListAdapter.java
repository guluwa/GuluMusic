package cn.guluwa.gulumusic.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.data.bean.BaseSongBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.databinding.PlayListItemLayoutBinding;
import cn.guluwa.gulumusic.listener.OnClickListener;
import cn.guluwa.gulumusic.utils.AppUtils;

/**
 * Created by guluwa on 2018/1/11.
 */

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {

    private OnClickListener<TracksBean> listener;

    private List<? extends BaseSongBean> data = new ArrayList<>();

    public PlayListAdapter(OnClickListener<TracksBean> listener) {
        this.listener = listener;
    }

    public void setData(List<? extends BaseSongBean> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public List<? extends BaseSongBean> getData() {
        return data;
    }

    @Override
    public PlayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding mDataBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.play_list_item_layout, parent, false);
        return new ViewHolder((PlayListItemLayoutBinding) mDataBinding);
    }

    @Override
    public void onBindViewHolder(PlayListAdapter.ViewHolder holder, int position) {
        holder.getViewBinder().setSong(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        PlayListItemLayoutBinding mViewBinder;

        public PlayListItemLayoutBinding getViewBinder() {
            return mViewBinder;
        }

        public void setViewBinder(PlayListItemLayoutBinding mPlayListItemLayoutBinding) {
            this.mViewBinder = mPlayListItemLayoutBinding;
        }

        public ViewHolder(PlayListItemLayoutBinding mViewBinder) {
            super(mViewBinder.getRoot());
            setViewBinder(mViewBinder);
            mViewBinder.setClickListener(view -> {
                if (data.get(getAdapterPosition()) instanceof TracksBean) {
                    listener.click((TracksBean) data.get(getAdapterPosition()));
                } else {
                    listener.click(AppUtils.getSongBean((LocalSongBean) data.get(getAdapterPosition())));
                }
            });
        }
    }
}
