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
        mViewModel.querySongPath().observe(this, songPathBeanViewDataBean -> {
            if (songPathBeanViewDataBean == null) {
                showSnackBar("歌曲播放失败");
                mViewModel.refreshPath(mSong.getId(), false);
            } else {
                switch (songPathBeanViewDataBean.status) {
                    case Content:
                        mPlayBinding.mPlayBtn.setPlaying(1);
                        mViewModel.refreshPath(mSong.getId(), false);
                        mViewModel.downloadSongFile(songPathBeanViewDataBean.data.getUrl(),
                                String.format("%s_%s.mp3", mSong.getName(), mSong.getId()),
                                new OnResultListener<File>() {
                                    @Override
                                    public void success(File result) {
                                        System.out.println(result.getAbsolutePath());
                                        AppManager.get().getmMusicAutoService().stop();
                                        AppManager.get().getmMusicAutoService().playNewSong(result.getAbsolutePath());
                                        mPlayBinding.mPlayBtn.setPlaying(1);
                                    }

                                    @Override
                                    public void failed(String error) {
                                        showSnackBar(error);
                                    }
                                });
                        break;
                    case Empty:
                        mViewModel.refreshPath(mSong.getId(), false);
                        showSnackBar("歌曲播放失败");
                        break;
                    case Error:
                        mViewModel.refreshPath(mSong.getId(), false);
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
                hasWord = false;
                initWordViewFromRemote("");
                mViewModel.refreshWord(mSong.getId(), false);
            } else {
                switch (songWordBeanViewDataBean.status) {
                    case Content:
                        hasWord = true;
                        mViewModel.refreshWord(mSong.getId(), false);
                        initWordViewFromRemote(songWordBeanViewDataBean.data.getLyric());
                        AppUtils.writeWord2Disk(songWordBeanViewDataBean.data.getLyric(),
                                String.format("%s_%s.txt", mSong.getName(), mSong.getId()));
                        break;
                    case Empty:
                        hasWord = false;
                        mViewModel.refreshWord(mSong.getId(), false);
                        initWordViewFromRemote("");
                        break;
                    case Error:
                        hasWord = false;
                        mViewModel.refreshWord(mSong.getId(), false);
                        initWordViewFromRemote("");
                        break;
                    case Loading:
                        mPlayBinding.tvSongWord.setText("正在加载~~~");
                        break;
                }
            }
        });
        String path;
        if ("".equals(path = AppUtils.isExistFile(String.format("%s_%s.mp3", mSong.getName(), mSong.getId()), 1))) {
            mViewModel.refreshPath(mSong.getId(), true);
        } else {
            System.out.println(path);
            AppManager.get().getmMusicAutoService().stop();
            AppManager.get().getmMusicAutoService().playNewSong(path);
            mPlayBinding.mPlayBtn.setPlaying(1);
        }
        if ("".equals(AppUtils.isExistFile(String.format("%s_%s.txt", mSong.getName(), mSong.getId()), 2))) {
            mViewModel.refreshWord(mSong.getId(), true);
        } else {
            hasWord = true;
            initWordViewFromLocal(String.format("%s_%s.txt", mSong.getName(), mSong.getId()));
        }
    }

    private void initWordViewFromLocal(String name) {
        try {
            mSongWordsMap = LrcParser.parserLocal(name);
            if (hasWord) {
                mPlayBinding.tvSongWord.setText(mSongWordsMap.get(0L));
            } else {
                mPlayBinding.tvSongWord.setText("暂无歌词");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initWordViewFromRemote(String word) {
        try {
            mSongWordsMap = LrcParser.parserRemote(word);
            if (hasWord) {
                mPlayBinding.tvSongWord.setText(mSongWordsMap.get(0L));
            } else {
                mPlayBinding.tvSongWord.setText("暂无歌词");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
