package cn.guluwa.gulumusic.ui.play;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.databinding.ActivityPlayBinding;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.manage.MyApplication;
import cn.guluwa.gulumusic.service.MusicAutoService;
import cn.guluwa.gulumusic.ui.main.MainActivity;
import cn.guluwa.gulumusic.utils.AppUtils;
import cn.guluwa.gulumusic.utils.LrcParser;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlayActivity extends BaseActivity {

    private TracksBean mSong;
    private ActivityPlayBinding mPlayBinding;
    private PlayViewModel mViewModel;
    private boolean hasWord;
    private HashMap<Long, String> mSongWordsMap;
    private SimpleDateFormat time;

    @Override
    public int getViewLayoutId() {
        return R.layout.activity_play;
    }

    @Override
    protected void initViews() {
        mPlayBinding = (ActivityPlayBinding) mViewDataBinding;
        initData();
        initSongPic();
        initStatusBar();
        initClickListener();
    }

    private void initClickListener() {
        mPlayBinding.setClickListener(view -> {
            switch (view.getId()) {
                case R.id.mPlayBtn:
                    if (AppManager.get().getmMusicAutoService() != null) {
                        if (AppManager.get().getmMusicAutoService().isPlaying) {
                            mPlayBinding.mPlayBtn.setPlaying(-1);
                        } else {
                            mPlayBinding.mPlayBtn.setPlaying(1);
                        }
                        AppManager.get().getmMusicAutoService().isPlaying = !AppManager.get().getmMusicAutoService().isPlaying;
                        AppManager.get().getmMusicAutoService().playOrPause();
                    }
                    break;
                case R.id.ivDownBack:
                    onBackPressed();
                    break;
            }
        });
    }

    private void initData() {
        mSong = (TracksBean) getIntent().getSerializableExtra("song");
        mPlayBinding.setSong(mSong);
        time = new SimpleDateFormat("mm:ss");
    }

    private void initSongPic() {
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
    }
}
