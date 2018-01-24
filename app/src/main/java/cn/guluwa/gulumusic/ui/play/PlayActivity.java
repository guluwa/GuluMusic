package cn.guluwa.gulumusic.ui.play;

import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.databinding.ActivityPlayBinding;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.manage.MyApplication;
import cn.guluwa.gulumusic.utils.AppUtils;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlayActivity extends BaseActivity {

    private TracksBean mSong;
    private ActivityPlayBinding mPlayBinding;
    private PlayViewModel mViewModel;

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
        initStatusBar();
        mPlayBinding.setClickListener(view -> {
            switch (view.getId()) {
                case R.id.mPlayBtn:
                    mPlayBinding.mPlayBtn.startAnimation();
                    break;
            }
        });
    }

    private void initStatus() {
        Glide.with(MyApplication.getContext()).asBitmap().apply(new RequestOptions().centerCrop())
                .load(mSong.getAl().getPicUrl())
                .apply(new RequestOptions().transform(new BlurTransformation(25)).override(100, 100))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Glide.with(PlayActivity.this).asBitmap()
                                .apply(new RequestOptions().transform(new BlurTransformation(25)).override(100, 100))
                                .load(R.mipmap.ic_launcher)
                                .into(mPlayBinding.ivBackGround);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null) {
                            mPlayBinding.ivBackGround.setImageBitmap(resource);
                            mPlayBinding.mPlayBtn.setPlaying(true);
                            mPlayBinding.mPlayBtn.startAnimation();
                            return true;
                        }
                        return false;
                    }
                })
                .into(mPlayBinding.ivBackGround);

        Glide.with(MyApplication.getContext())
                .asBitmap()
                .apply(new RequestOptions().centerCrop())
                .load(mSong.getAl().getPicUrl())
                .into(mPlayBinding.ivSongPic);
    }

    private void initStatusBar() {
        //5.0以上状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//禁止横屏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    protected void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(PlayViewModel.class);
        mViewModel.querySongPath().observe(this, songPathBeanViewDataBean -> {
            if (songPathBeanViewDataBean == null) {
                showSnackBar("歌曲播放失败");
            } else {
                switch (songPathBeanViewDataBean.status) {
                    case Content:
                        System.out.println("path :" + songPathBeanViewDataBean.data.getUrl());
                        mViewModel.downloadSongFile(songPathBeanViewDataBean.data.getUrl(),
                                String.format("%s_%s.mp3", mSong.getName(), mSong.getId()),
                                new OnResultListener<File>() {
                                    @Override
                                    public void success(File result) {
                                        System.out.println(result.getAbsolutePath());
                                    }

                                    @Override
                                    public void failed(String error) {
                                        showSnackBar(error);
                                    }
                                });
                        break;
                    case Empty:
                        showSnackBar("歌曲播放失败");
                        break;
                    case Error:
                        showSnackBar("歌曲播放失败");
                        break;
                    case Loading:
                        System.out.println("path loading");
                        break;
                }
            }
        });
        mViewModel.querySongWord().observe(this, songWordBeanViewDataBean -> {
            if (songWordBeanViewDataBean == null) {
                System.out.println("word null");
            } else {
                switch (songWordBeanViewDataBean.status) {
                    case Content:
                        AppUtils.writeWord2Disk(songWordBeanViewDataBean.data.getLyric(),
                                String.format("%s_%s.txt", mSong.getName(), mSong.getId()));
                        break;
                    case Empty:
                        System.out.println("word empty");
                        break;
                    case Error:
                        System.out.println("word error");
                        break;
                    case Loading:
                        System.out.println("word loading");
                        break;
                }
            }
        });
        mViewModel.refresh(mSong.getId());
    }
}
