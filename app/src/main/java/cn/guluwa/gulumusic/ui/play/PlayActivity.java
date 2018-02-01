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
import cn.guluwa.gulumusic.listener.OnSongFinishListener;
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
     * ViewModel
     */
    private PlayViewModel mViewModel;

    /**
     * 歌词list
     */
    private List<LrcBean> mLrcList;

    /**
     * 歌曲进度轮询
     */
    private Disposable disposable;

    /**
     * 歌曲当前位置
     */
    private int mLrcPosition;

    /**
     * 是否第一首歌
     */
    private boolean isFirst;

    /**
     * 歌曲下载链接
     */
    private String mSongPath;

    /**
     * 歌曲播放结束回调
     */
    private OnSongFinishListener listener;

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
                    if (AppManager.getInstance().getMusicAutoService() != null &&
                            AppManager.getInstance().getMusicAutoService().mediaPlayer != null) {
                        if (AppManager.getInstance().getMusicAutoService().mediaPlayer.isPlaying()) {
                            mPlayBinding.mPlayBtn.setPlaying(-1);
                        } else {
                            mPlayBinding.mPlayBtn.setPlaying(1);
                        }
                        if (isFirst) {
                            isFirst = false;
                            AppManager.getInstance().getMusicAutoService().playNewSong(mSongPath, mCurrentSong.getCurrentTime(), mCurrentSong);
                            bindProgressQuery();
                        } else {
                            AppManager.getInstance().getMusicAutoService().playOrPause();
                        }
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

                    break;
                case R.id.mLastSongBtn:
                    AppManager.getInstance().getMusicAutoService().stop();
                    mCurrentSong = AppManager.getInstance().getMusicAutoService().getLastSong(mCurrentSong);
                    mPlayBinding.tvSongWord.setText("");
                    mPlayBinding.mProgressView.setSongPlayLength(0, 0);
                    mPlayBinding.setSong(mCurrentSong);
                    initSongPic();
                    playCurrentSong(0);
                    break;
                case R.id.mNextSongBtn:
                    AppManager.getInstance().getMusicAutoService().stop();
                    mCurrentSong = AppManager.getInstance().getMusicAutoService().getNextSong(mCurrentSong);
                    mPlayBinding.tvSongWord.setText("");
                    mPlayBinding.mProgressView.setSongPlayLength(0, 0);
                    mPlayBinding.setSong(mCurrentSong);
                    initSongPic();
                    playCurrentSong(0);
                    break;
            }
        });
    }

    private void showPlayModeImg(int mode) {
        if (mode == 0) {
            mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_single_circle);
        } else if (mode == 1) {
            mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_list_circle);
        } else {
            mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_list_random);
        }
    }

    private void initData() {
        mLrcPosition = -1;
        mCurrentSong = (TracksBean) getIntent().getSerializableExtra("song");
        mSongPath = AppUtils.isExistFile(String.format("%s_%s.mp3", mCurrentSong.getName(), mCurrentSong.getId()), 1);
        mPlayBinding.mPlayBtn.setPlaying(getIntent().getIntExtra("status", -1));
        if (mPlayBinding.mPlayBtn.getIsPlaying() == -1) {
            isFirst = true;
        } else if (mPlayBinding.mPlayBtn.getIsPlaying() == 1) {
            bindProgressQuery();
        }
        mPlayBinding.mProgressView.setSongPlayLength(mCurrentSong.getCurrentTime(), mCurrentSong.getDuration());
        mPlayBinding.setSong(mCurrentSong);
        showPlayModeImg(AppManager.getInstance().getPlayMode());
        initSongLrc();

        listener = tracksBean -> {
            mCurrentSong = tracksBean;
            mPlayBinding.tvSongWord.setText("");
            mPlayBinding.mProgressView.setSongPlayLength(0, 0);
            mPlayBinding.setSong(mCurrentSong);
            initSongPic();
            playCurrentSong(0);
        };
        AppManager.getInstance().getMusicAutoService().bindSongFinishListener(listener);
    }

    private void playCurrentSong(int mCurrentTime) {
        if ("".equals(mSongPath = AppUtils.isExistFile(String.format("%s_%s.mp3", mCurrentSong.getName(), mCurrentSong.getId()), 1))) {
            mViewModel.refreshPath(mCurrentSong, true);
            mPlayBinding.mPlayBtn.setPlaying(0);
        } else {
            AppManager.getInstance().getMusicAutoService().stop();
            AppManager.getInstance().getMusicAutoService().playNewSong(mSongPath, mCurrentTime, mCurrentSong);
            mPlayBinding.mPlayBtn.setPlaying(1);
            bindProgressQuery();
        }
        if ("".equals(AppUtils.isExistFile(String.format("%s_%s.txt", mCurrentSong.getName(), mCurrentSong.getId()), 2))) {
            mViewModel.refreshWord(mCurrentSong, true);
        } else {
            initSongLrc();
        }
    }

    private void initSongLrc() {
        try {
            mLrcList = LrcParser.parserLocal(String.format("%s_%s.txt", mCurrentSong.getName(), mCurrentSong.getId()));
        } catch (Exception e) {
            mLrcList = null;
            mPlayBinding.tvSongWord.setText("暂无歌词");
            e.printStackTrace();
        }
    }

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
                mViewModel.refreshPath(mCurrentSong, false);
            } else {
                switch (songPathBeanViewDataBean.status) {
                    case Content:
                        mViewModel.refreshPath(mCurrentSong, false);
                        mViewModel.downloadSongFile(songPathBeanViewDataBean.data,
                                String.format("%s_%s.mp3",
                                        songPathBeanViewDataBean.data.getSong().getName(),
                                        songPathBeanViewDataBean.data.getSong().getId()),
                                new OnResultListener<File>() {
                                    @Override
                                    public void success(File result) {
                                        System.out.println(result.getAbsolutePath());
                                        if (songPathBeanViewDataBean.data.getId() == mCurrentSong.getId()) {//下载完成的歌曲和当前播放歌曲是同一首
                                            AppManager.getInstance().getMusicAutoService().stop();
                                            AppManager.getInstance().getMusicAutoService().playNewSong(result.getAbsolutePath(), 0, mCurrentSong);
                                            mPlayBinding.mPlayBtn.setPlaying(1);
                                            bindProgressQuery();
                                        }
                                    }

                                    @Override
                                    public void failed(String error) {
                                        showSnackBar(error);
                                    }
                                });
                        break;
                    case Empty:
                        mViewModel.refreshPath(mCurrentSong, false);
                        showSnackBar("歌曲播放失败");
                        break;
                    case Error:
                        mViewModel.refreshPath(mCurrentSong, false);
                        showSnackBar("歌曲播放失败");
                        break;
                    case Loading:
                        mPlayBinding.mPlayBtn.setPlaying(0);
                        break;
                }
            }
        });
        mViewModel.querySongWord().observe(this, songWordBeanViewDataBean -> {
            if (songWordBeanViewDataBean == null) {
                mViewModel.refreshWord(mCurrentSong, false);
            } else {
                switch (songWordBeanViewDataBean.status) {
                    case Content:
                        mViewModel.refreshWord(mCurrentSong, false);
                        AppUtils.writeWord2Disk(songWordBeanViewDataBean.data.getLyric(),
                                String.format("%s_%s.txt",
                                        songWordBeanViewDataBean.data.getSong().getName(),
                                        songWordBeanViewDataBean.data.getSong().getId()));
                        initSongLrc();
                        break;
                    case Empty:
                        mViewModel.refreshWord(mCurrentSong, false);
                        break;
                    case Error:
                        mViewModel.refreshWord(mCurrentSong, false);
                        break;
                    case Loading:
                        System.out.println("歌词正在加载~~~");
                        break;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("status", mPlayBinding.mPlayBtn.getIsPlaying());
        intent.putExtra("song", mCurrentSong);
        setResult(Contacts.RESULT_SONG_CODE, intent);
        super.onBackPressed();
    }

    public void bindProgressQuery() {
        if (disposable == null) {
            disposable = Observable.interval(0, 150, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        if (AppManager.getInstance().getMusicAutoService() != null) {
                            int musicCurrentPosition = AppManager.getInstance().getMusicAutoService().mediaPlayer.getCurrentPosition();
                            if (musicCurrentPosition < 1000 && mLrcPosition != -1) {
                                mLrcPosition = -1;
                            }
                            if (mLrcList != null) {
                                if (mLrcPosition == -1) {//说明是第一次
                                    for (int i = 0; i < mLrcList.size(); i++) {
                                        if (mLrcList.get(i).getTime() > musicCurrentPosition) {
                                            mLrcPosition = i - 1;
                                            mPlayBinding.tvSongWord.setText(mLrcList.get(mLrcPosition).getWord());
                                            break;
                                        }
                                    }
                                } else {
                                    if (mLrcList.size() > mLrcPosition + 1) {
                                        if (mLrcList.get(mLrcPosition + 1).getTime() < musicCurrentPosition) {
                                            mLrcPosition++;
                                            if (mLrcList.size() > mLrcPosition) {
                                                mPlayBinding.tvSongWord.setText(mLrcList.get(mLrcPosition).getWord());
                                            }
                                        }
                                    }
                                }
                            }
                            if (mPlayBinding.mPlayBtn.getIsPlaying() != 0) {
                                mPlayBinding.mProgressView.setSongPlayLength(musicCurrentPosition, AppManager.getInstance().getMusicAutoService().mediaPlayer.getDuration());
                            }
                        }
                    });
        }
    }

    public void unbindProgressQuery() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    @Override
    protected void onDestroy() {
        unbindProgressQuery();
        AppManager.getInstance().getMusicAutoService().unBindSongFinishListener(listener);
        super.onDestroy();
    }
}
