package cn.guluwa.gulumusic.ui.play;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.databinding.ActivityPlayBinding;
import cn.guluwa.gulumusic.utils.AppUtils;

public class PlayActivity extends BaseActivity {

    private TracksBean mSong;
    private ActivityPlayBinding mPlayBinding;

    @Override
    public int getViewLayoutId() {
        return R.layout.activity_play;
    }

    @Override
    protected void initViews() {
        mPlayBinding = (ActivityPlayBinding) mViewDataBinding;
        mSong = (TracksBean) getIntent().getSerializableExtra("song");
        mPlayBinding.setSong(mSong);
        initStatus();
        mPlayBinding.setClickListener(view -> {
            switch (view.getId()) {
                case R.id.mPlayBtn:
                    mPlayBinding.mPlayBtn.startAnimation();
                    break;
            }
        });
    }

    private void initStatus() {
        Glide.with(this).asBitmap().apply(new RequestOptions().circleCrop())
                .load(mSong.getAl().getPicUrl())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Glide.with(PlayActivity.this).asBitmap()
                                .apply(new RequestOptions().circleCrop())
                                .load(R.mipmap.ic_launcher).into(mPlayBinding.ivSongPic);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null) {
                            mPlayBinding.ivSongPic.setImageBitmap(resource);
                            AppUtils.getBackGroundAndTextColor(mPlayBinding.ivSongPic, colors -> {
                                if (colors.length != 0) {
                                    getWindow().setStatusBarColor(AppUtils.deepenColor(colors[0]));
                                    mPlayBinding.mContainer.setBackgroundColor(colors[0]);
                                    mPlayBinding.tvSongName.setTextColor(colors[2]);
                                    mPlayBinding.tvSongSinger.setTextColor(colors[1]);
                                    mPlayBinding.ivDownBack.setColorFilter(colors[1]);
                                    mPlayBinding.ivPlayMore.setColorFilter(colors[1]);
                                    mPlayBinding.mProgressView.setmProgressColor(AppUtils.deepenLittleColor(colors[0]));
                                    mPlayBinding.mProgressView.setmIndicatorColor(AppUtils.deepenColor(colors[0]));
                                    mPlayBinding.mPlayBtn.setTopColor(AppUtils.deepenLittleColor(colors[0]));
                                    mPlayBinding.mPlayBtn.setBtmColor(AppUtils.deepenColor(colors[0]));
                                    mPlayBinding.mPlayBtn.setPlaying(true);
                                    mPlayBinding.mPlayBtn.startAnimation();
                                    mPlayBinding.mLastSongBtn.setColor(AppUtils.deepenLittleColor(colors[0]));
                                    mPlayBinding.mNextSongBtn.setColor(AppUtils.deepenLittleColor(colors[0]));
                                }
                            });
                            return true;
                        }
                        return false;
                    }
                })
                .into(mPlayBinding.ivSongPic);
    }

    @Override
    protected void initViewModel() {

    }
}
