package cn.guluwa.gulumusic.ui.play;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.data.bean.LrcBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.databinding.ActivityPlayBinding;
import cn.guluwa.gulumusic.listener.OnResultListener;
import cn.guluwa.gulumusic.listener.OnSongStatusListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.manage.Contacts;
import cn.guluwa.gulumusic.manage.MyApplication;
import cn.guluwa.gulumusic.ui.main.MainActivity;
import cn.guluwa.gulumusic.utils.AppUtils;
import cn.guluwa.gulumusic.utils.LrcParser;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlayActivity extends BaseActivity {

    /**
     * 当前播放歌曲
     */
    private TracksBean mCurrentSong;

    /**
     * ViewBinder
     */
    private ActivityPlayBinding mPlayBinding;

    /**
     * 歌词list
     */
    private List<LrcBean> mLrcList;

    /**
     * 歌曲当前位置
     */
    private int mLrcPosition;

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

    @Override
    protected void initViewModel() {

    }

    /**
     * 点击事件初始化
     */
    private void initClickListener() {
        mPlayBinding.setClickListener(view -> {
            switch (view.getId()) {
                case R.id.mPlayBtn:
                    if (AppManager.getInstance().getMusicAutoService() != null &&
                            AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer() != null) {
                        if (AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().isPlaying()) {
                            mPlayBinding.mPlayBtn.setPlaying(-1);
                        } else {
                            mPlayBinding.mPlayBtn.setPlaying(1);
                        }
                        playCurrentSong(mCurrentSong.getCurrentTime());
                    }
                    break;
                case R.id.ivDownBack:
                    onBackPressed();
                    break;
                case R.id.ivPlayMode:
                    int mode = AppUtils.getPlayMode(AppManager.getInstance().getPlayMode());
                    AppManager.getInstance().setPlayMode(mode);
                    showPlayModeImg(mode);
                    break;
                case R.id.ivPlayMenu:
                    showSnackBar("菜单");
                    break;
                case R.id.mLastSongBtn:
                    AppManager.getInstance().getMusicAutoService().binder.stop();
                    playCurrentSong(AppManager.getInstance().getMusicAutoService().binder.getLastSong(mCurrentSong));
                    break;
                case R.id.mNextSongBtn:
                    AppManager.getInstance().getMusicAutoService().binder.stop();
                    playCurrentSong(AppManager.getInstance().getMusicAutoService().binder.getNextSong(mCurrentSong));
                    break;
            }
        });
    }

    /**
     * 切换播放模式
     *
     * @param mode
     */
    private void showPlayModeImg(int mode) {
        if (mode == 0) {
            mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_single_circle);
        } else if (mode == 1) {
            mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_list_circle);
        } else {
            mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_list_random);
        }
    }

    /**
     * 数据初始化
     */
    private void initData() {
        mLrcPosition = -1;
        mCurrentSong = (TracksBean) getIntent().getSerializableExtra("song");
        mPlayBinding.mPlayBtn.setPlaying(getIntent().getIntExtra("status", -1));
        mPlayBinding.mProgressView.setSongPlayLength(mCurrentSong.getCurrentTime(), mCurrentSong.getDuration());
        mPlayBinding.setSong(mCurrentSong);
        showPlayModeImg(AppManager.getInstance().getPlayMode());
        initSongLrc();
        AppManager.getInstance().getMusicAutoService().binder.bindSongStatusListener(listener);
    }

    /**
     * 歌词处理
     */
    private void initSongLrc() {
        if (mLrcList == null) {
            try {
                mLrcList = LrcParser.parserLocal(String.format("%s_%s.txt", mCurrentSong.getName(), mCurrentSong.getId()));
                for (int i = 0; i < mLrcList.size(); i++) {
                    if (mLrcList.get(i).getTime() > mCurrentSong.getCurrentTime()) {
                        if (i != 0) {
                            mLrcPosition = i - 1;
                        } else {
                            mLrcPosition = 0;
                        }
                        if (mPlayBinding.mPlayBtn.getIsPlaying() != 0) {
                            mPlayBinding.tvSongWord.setText(mLrcList.get(mLrcPosition).getWord());
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                mLrcList = null;
                mPlayBinding.tvSongWord.setText("暂无歌词");
                e.printStackTrace();
            }
        }
    }

    /**
     * 图片初始化
     */
    private void initSongPic() {
        Glide.with(MyApplication.getContext()).asBitmap().apply(new RequestOptions().centerCrop())
                .load(mCurrentSong.getAl().getPicUrl())
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
    }

    /**
     * 状态栏初始化
     */
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

    /**
     * 播放歌曲
     *
     * @param song
     */
    private void playCurrentSong(TracksBean song) {
        //更新页面
        mCurrentSong = song;
        mPlayBinding.setSong(mCurrentSong);
        mPlayBinding.tvSongWord.setText("");
        mPlayBinding.mProgressView.setSongPlayLength(0, 0);
        initSongPic();
        //播放歌曲、利用服务后台播放
        AppManager.getInstance().getMusicAutoService().binder.setPrepare(false);
        playCurrentSong(0);
    }

    /**
     * 不换歌曲（第一次进入）
     *
     * @param mCurrentTime
     */
    private void playCurrentSong(int mCurrentTime) {
        if (AppManager.getInstance().getMusicAutoService().binder.isPrepare()) {
            AppManager.getInstance().getMusicAutoService().binder.playOrPauseSong(-1);
        } else {
            AppManager.getInstance().getMusicAutoService().binder.playCurrentSong(mCurrentSong, mCurrentTime);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("status", mPlayBinding.mPlayBtn.getIsPlaying());
        intent.putExtra("song", mCurrentSong);
        setResult(Contacts.RESULT_SONG_CODE, intent);
        super.onBackPressed();
    }

    /**
     * 歌曲播放进度
     */
    private OnSongStatusListener listener = new OnSongStatusListener() {

        @Override
        public void loading() {
            mPlayBinding.mPlayBtn.setPlaying(0);
        }

        @Override
        public void start() {
            mPlayBinding.mPlayBtn.setPlaying(1);
            initSongLrc();
        }

        @Override
        public void pause() {
            mPlayBinding.mPlayBtn.setPlaying(-1);
        }

        @Override
        public void end(TracksBean tracksBean) {
            playCurrentSong(tracksBean);
            mLrcPosition = -1;
            mLrcList = null;
        }

        @Override
        public void error(String msg) {
            showSnackBar(msg);
            mLrcPosition = -1;
            mLrcList = null;
        }

        @Override
        public void progress(int progress, int duration) {
            if (mLrcList != null) {
                if (mLrcPosition == -1) {//说明是第一次
                    for (int i = 0; i < mLrcList.size(); i++) {
                        if (mLrcList.get(i).getTime() > progress) {
                            if (i != 0) {
                                mLrcPosition = i - 1;
                            } else {
                                mLrcPosition = 0;
                            }
                            if (mPlayBinding.mPlayBtn.getIsPlaying() != 0) {
                                mPlayBinding.tvSongWord.setText(mLrcList.get(mLrcPosition).getWord());
                            }
                            break;
                        }
                    }
                } else {
                    if (mLrcList.size() > mLrcPosition + 1) {
                        if (mLrcList.get(mLrcPosition + 1).getTime() < progress) {
                            mLrcPosition++;
                            if (mLrcList.size() > mLrcPosition) {
                                if (mPlayBinding.mPlayBtn.getIsPlaying() != 0) {
                                    mPlayBinding.tvSongWord.setText(mLrcList.get(mLrcPosition).getWord());
                                    System.out.println(progress + ";" + mLrcList.get(mLrcPosition).getWord());
                                }
                            }
                        }
                    }
                }
            }
            if (mPlayBinding.mPlayBtn.getIsPlaying() != 0) {
                mPlayBinding.mProgressView.setSongPlayLength(progress, duration);
            }
        }
    };

    @Override
    protected void onDestroy() {
        AppManager.getInstance().getMusicAutoService().binder.unBindSongStatusListener(listener);
        super.onDestroy();
    }
}
