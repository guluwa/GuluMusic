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
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.data.bean.LrcBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.databinding.ActivityPlayBinding;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.manage.Contacts;
import cn.guluwa.gulumusic.manage.MyApplication;
import cn.guluwa.gulumusic.service.MusicAutoService;
import cn.guluwa.gulumusic.ui.main.MainActivity;
import cn.guluwa.gulumusic.utils.AppUtils;
import cn.guluwa.gulumusic.utils.LrcParser;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlayActivity extends BaseActivity {

    private TracksBean mSong;
    private ActivityPlayBinding mPlayBinding;
    private PlayViewModel mViewModel;
    private boolean hasWord;
    private List<LrcBean> mLrcList;
    private Disposable disposable;
    private int mLrcPosition;
    private boolean isFirst;
    private String mSongPath;

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
                        if (isFirst) {
                            isFirst = false;
                            AppManager.get().getmMusicAutoService().playNewSong(mSongPath, mSong.getCurrentTime());
                            bindProgressQuery();
                        } else {
                            AppManager.get().getmMusicAutoService().playOrPause();
                        }
                    }
                    break;
                case R.id.ivDownBack:
                    onBackPressed();
                    break;
            }
        });
    }

    private void initData() {
        mLrcPosition = -1;
        mSong = (TracksBean) getIntent().getSerializableExtra("song");
        isFirst = true;
        mSongPath = AppUtils.isExistFile(String.format("%s_%s.mp3", mSong.getName(), mSong.getId()), 1);
        mPlayBinding.mPlayBtn.setPlaying(getIntent().getIntExtra("status", -1));
        mPlayBinding.mProgressView.setSongPlayLength(mSong.getCurrentTime(), mSong.getDuration());
        mPlayBinding.setSong(mSong);
        try {
            mLrcList = LrcParser.parserLocal(String.format("%s_%s.txt", mSong.getName(), mSong.getId()));
        } catch (Exception e) {
            mLrcList = null;
            mPlayBinding.tvSongWord.setText("暂无歌词");
            e.printStackTrace();
        }
        if (getIntent().getIntExtra("status", -1) == 1)
            bindProgressQuery();
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("status", mPlayBinding.mPlayBtn.getIsPlaying());
        setResult(Contacts.RESULT_SONG_CODE, intent);
        super.onBackPressed();
    }

    public void bindProgressQuery() {
        if (disposable == null) {
            disposable = Observable.interval(0, 150, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        if (AppManager.get().getmMusicAutoService() != null) {
                            int musicCurrentPosition = AppManager.get().getmMusicAutoService().binder.getMusicCurrentPosition();
                            if (musicCurrentPosition < 1000 && mLrcPosition != -1) {
                                mLrcPosition = -1;
                            }
                            if (mLrcList != null) {
                                if (mLrcPosition == -1) {//说明是第一次
                                    for (int i = 0; i < mLrcList.size(); i++) {
                                        if (mLrcList.get(i).getTime() > musicCurrentPosition) {
                                            mLrcPosition = i;
                                            mPlayBinding.tvSongWord.setText(mLrcList.get(mLrcPosition).getWord());
                                            System.out.println(musicCurrentPosition + ";" + mLrcList.get(mLrcPosition).getWord());
                                            break;
                                        }
                                    }
                                } else {
                                    if (mLrcList.size() > mLrcPosition + 1) {
                                        if (mLrcList.get(mLrcPosition + 1).getTime() < musicCurrentPosition) {
                                            mLrcPosition++;
                                            if (mLrcList.size() > mLrcPosition) {
                                                mPlayBinding.tvSongWord.setText(mLrcList.get(mLrcPosition).getWord());
                                                System.out.println(musicCurrentPosition + ";" + mLrcList.get(mLrcPosition).getWord());
                                            }
                                        }
                                    }
                                }
                            }
                            mPlayBinding.mProgressView.setSongPlayLength(musicCurrentPosition, AppManager.get().getmMusicAutoService().mediaPlayer.getDuration());
                        }
                    });
        }
    }

    public void unbindProgressQuery() {
        if (disposable != null && disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    protected void onDestroy() {
        unbindProgressQuery();
        super.onDestroy();
    }
}
