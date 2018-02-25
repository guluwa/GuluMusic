package cn.guluwa.gulumusic.ui.main;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.adapter.PlayListAdapter;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.data.bean.BaseSongBean;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import cn.guluwa.gulumusic.databinding.ActivityMainBinding;
import cn.guluwa.gulumusic.listener.OnSongStatusListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.manage.Contacts;
import cn.guluwa.gulumusic.service.MusicAutoService;
import cn.guluwa.gulumusic.service.MusicBinder;
import cn.guluwa.gulumusic.ui.play.PlayActivity;
import cn.guluwa.gulumusic.ui.search.SearchActivity;
import cn.guluwa.gulumusic.ui.setting.SettingsActivity;
import cn.guluwa.gulumusic.utils.AppUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    /**
     * ViewBinder
     */
    private ActivityMainBinding mMainBinding;

    /**
     * 是否滑动
     */
    private boolean sIsScrolling;

    /**
     * 当前播放歌曲
     */
    private TracksBean mCurrentSong;

    /**
     * 格式化时间
     */
    private SimpleDateFormat time;

    /**
     * 是否第一次进入
     */
    private boolean isFirstComing;

    @Override
    public int getViewLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        mMainBinding = (ActivityMainBinding) mViewDataBinding;
        initData();
        initClickListener();
        initToolBar();
        initDrawerLayout();
        initSwipeRefreshLayout();
        initRecyclerView();
    }

    /**
     * toolbar初始化
     */
    private void initToolBar() {
        mMainBinding.mToolBar.setTitle(R.string.app_name);//设置Toolbar标题
        setSupportActionBar(mMainBinding.mToolBar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setStatusBarColor(AppUtils.deepenColor(Color.rgb(85, 160, 122)));
    }

    /**
     * 数据初始化
     */
    private void initData() {
        isFirstComing = true;
        mCurrentSong = new Gson().fromJson(AppUtils.getString("mCurrentSong", ""), TracksBean.class);
        time = new SimpleDateFormat("mm:ss");
        if (mCurrentSong != null) {
            String mSongPath = AppUtils.isExistFile(String.format("%s_%s.mp3", mCurrentSong.getName(), mCurrentSong.getId()), 1);
            if (!"".equals(mSongPath)) {
                mMainBinding.setSong(mCurrentSong);
                mMainBinding.tvCurrentSongProgress.setText(time.format(mCurrentSong.getCurrentTime()));
            }
        }
        mMainBinding.mPlayBtn.setPlaying(-1);
    }

    /**
     * 点击事件初始化
     */
    private void initClickListener() {
        mMainBinding.setClickListener(view -> {
            switch (view.getId()) {
                case R.id.mBottomPlayInfo:
                    Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                    if (AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().isPlaying()) {
                        mCurrentSong.setCurrentTime(AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().getCurrentPosition());
                    }
                    intent.putExtra("song", mCurrentSong);
                    intent.putExtra("status", mMainBinding.mPlayBtn.getIsPlaying());
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, new Pair<>(mMainBinding.ivCurrentSongPic, "songImage"));
                    ActivityCompat.startActivityForResult(this, intent, Contacts.REQUEST_CODE_PLAY, options.toBundle());
                    break;
                case R.id.mPlayBtn:
                    if (AppManager.getInstance().getMusicAutoService() != null &&
                            AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer() != null) {
                        if (AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().isPlaying()) {
                            mMainBinding.mPlayBtn.setPlaying(-1);
                        } else {
                            mMainBinding.mPlayBtn.setPlaying(1);
                        }
                        playCurrentSong(mCurrentSong.getCurrentTime());
                    }
                    break;
                case R.id.flNetCloudSongs:
                    mMainBinding.mDrawerLayout.closeDrawer(Gravity.START);
                    if ("hot".equals(AppManager.getInstance().getPlayStatus())) {
                        return;
                    }
                    getSongListData("hot");
                    break;
                case R.id.flLocalSongs:
                    mMainBinding.mDrawerLayout.closeDrawer(Gravity.START);
                    if ("local".equals(AppManager.getInstance().getPlayStatus())) {
                        return;
                    }
                    getSongListData("local");
                    break;
                case R.id.flAppSetting:
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    mMainBinding.mDrawerLayout.closeDrawer(Gravity.START);
                    break;
            }
        });
    }

    /**
     * 侧边栏初始化
     */
    private void initDrawerLayout() {
        //创建返回键，并实现打开关/闭监听
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mMainBinding.mDrawerLayout, mMainBinding.mToolBar,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerToggle.syncState();
        mMainBinding.mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /**
     * 下拉刷新初始化
     */
    private void initSwipeRefreshLayout() {
        mMainBinding.mSwipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.yellow),
                getResources().getColor(R.color.green));
        mMainBinding.mSwipeRefreshLayout.setOnRefreshListener(() -> getSongListData(AppManager.getInstance().getPlayStatus()));
    }

    /**
     * 列表初始化
     */
    private void initRecyclerView() {
        PlayListAdapter mAdapter = new PlayListAdapter((song) -> {
            if (mCurrentSong != null) {
                if (song.getId().equals(mCurrentSong.getId()) && mMainBinding.mPlayBtn.getIsPlaying() == 1) {
                    return;
                }
            }
            if (AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().isPlaying()) {
                AppManager.getInstance().getMusicAutoService().binder.stop();
            }
            playCurrentSong(song);
        }, song -> {
            showDeleteDialog(song);
        });
        mMainBinding.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMainBinding.mRecyclerView.setAdapter(mAdapter);
        mMainBinding.mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    sIsScrolling = true;
                    Glide.with(MainActivity.this).pauseRequests();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (sIsScrolling) {
                        Glide.with(MainActivity.this).resumeRequests();
                    }
                    sIsScrolling = false;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    /**
     * 删除本地歌曲确认对话框
     */
    private void showDeleteDialog(LocalSongBean song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        View view = LayoutInflater.from(this).inflate(R.layout.song_delete_tip_dialog, null, false);
        TextView tvDialogMessage = view.findViewById(R.id.tvDialogMessage);
        tvDialogMessage.setText(String.format("亲，您确定要删除「%s」吗？",
                song.getName().length() <= 8 ? song.getName() : String.format("%s…", song.getName().substring(0, 7))));
        CheckBox cbDeleteSongFile = view.findViewById(R.id.cbDeleteSongFile);
        builder.setView(view);
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("删除", (dialog, which) -> {
            deleteLocalSong(cbDeleteSongFile.isChecked(), song);
            dialog.dismiss();
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
    }

    /**
     * 删除本地歌曲
     */
    private void deleteLocalSong(boolean isChecked, LocalSongBean song) {
        Observable.just(isChecked)
                .observeOn(Schedulers.io())
                .map(o -> {
                    if (!isChecked) {
                        AppUtils.deleteFile(String.format("%s_%s.mp3", song.getName(), song.getId()), 1);
                        AppUtils.deleteFile(String.format("%s_%s.txt", song.getName(), song.getId()), 2);
                    }
                    LocalSongsDataSource.getInstance().deleteLocalSong(song);
                    return isChecked;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    showSnackBar("删除成功");
                    mViewModel.refreshLocal(true);
                }, throwable -> showSnackBar("删除失败"));
    }

    /**
     * viewModel初始化
     */
    @Override
    protected void initViewModel() {
        //查询热门歌曲
        mViewModel.queryNetCloudHotSong().observe(this, data -> {
            if (data == null) {
                showSnackBar("数据出错啦");
                mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                return;
            }
            switch (data.status) {
                case Loading:
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(true);
                    break;
                case Error:
                    mViewModel.refreshHot(false, isFirstComing);
                    showSnackBar(data.throwable.getMessage());
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case Empty:
                    mViewModel.refreshHot(false, isFirstComing);
                    showSnackBar("还没有热门歌曲哦");
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case Content:
                    AppManager.getInstance().setPlayStatus("hot");
                    showSnackBar("热门");
                    isFirstComing = false;
                    mViewModel.refreshHot(false, false);
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                    setData(data.data);
                    break;
            }
        });
        //查询本地歌曲
        mViewModel.queryLocalSong().observe(this, listViewDataBean -> {
            if (listViewDataBean == null) {
                showSnackBar("数据出错啦");
                mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                return;
            }
            switch (listViewDataBean.status) {
                case Loading:
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(true);
                    break;
                case Error:
                    mViewModel.refreshLocal(false);
                    showSnackBar(listViewDataBean.throwable.getMessage());
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case Empty:
                    mViewModel.refreshLocal(false);
                    showSnackBar("还没有本地歌曲哦");
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case Content:
                    AppManager.getInstance().setPlayStatus("local");
                    mViewModel.refreshLocal(false);
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                    setData(listViewDataBean.data);
                    break;
            }
        });
    }

    /**
     * 设置列表数据、传到service、当前播放歌曲为空，设置为列表第一首
     *
     * @param data
     */
    public void setData(List<? extends BaseSongBean> data) {
        //填充列表数据
        ((PlayListAdapter) mMainBinding.mRecyclerView.getAdapter()).setData(data);
        //列表滚动到顶部
        mMainBinding.mRecyclerView.scrollTo(0, 0);
        //更新service数据
        AppManager.getInstance().getMusicAutoService().binder.setSongList(data);
        if (mCurrentSong == null) {
            if (data.get(0) instanceof TracksBean) {
                mCurrentSong = (TracksBean) data.get(0);
            } else {
                mCurrentSong = AppUtils.getSongBean((LocalSongBean) data.get(0));
            }
            mMainBinding.setSong(mCurrentSong);
            mMainBinding.tvCurrentSongProgress.setText("00:00");
            mMainBinding.mPlayBtn.setPlaying(-1);
        }
    }

    /**
     * 播放歌曲
     *
     * @param song
     */
    private void playCurrentSong(TracksBean song) {
        reFreshLayout(song);
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

    /**
     * 更新页面
     *
     * @param song
     */
    private void reFreshLayout(TracksBean song) {
        mCurrentSong = song;
        mMainBinding.setSong(mCurrentSong);
        mMainBinding.tvCurrentSongProgress.setText("00:00");
    }

    /**
     * 在Activity中调用 bindService 保持与 Service 的通信
     */
    @Override
    public void bindServiceConnection() {
        Intent intent = new Intent(MainActivity.this, MusicAutoService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 回调onServiceConnected 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicAutoService mMusicService = ((MusicBinder) (service)).getService();
            AppManager.getInstance().setMusicAutoService(mMusicService);
            System.out.println("MusicAutoService 初始化完成");
            getSongListData(AppManager.getInstance().getPlayStatus());
            //销毁serviceConnection
            if (serviceConnection != null) {
                unbindService(serviceConnection);
                serviceConnection = null;
            }
            AppManager.getInstance().getMusicAutoService().binder.bindSongStatusListener(listener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    /**
     * 刷新列表数据
     */
    private void getSongListData(String type) {
        if ("hot".equals(type)) {
            mViewModel.refreshHot(true, isFirstComing);
        } else {
            mViewModel.refreshLocal(true);
        }
    }

    /**
     * 歌曲播放进度
     */
    private OnSongStatusListener listener = new OnSongStatusListener() {
        @Override
        public void loading() {
            mMainBinding.mPlayBtn.setPlaying(0);
        }

        @Override
        public void start() {
            mMainBinding.mPlayBtn.setPlaying(1);
            if (!mCurrentSong.getId().equals(AppManager.getInstance().getMusicAutoService().binder.getCurrentSong().getId())) {
                reFreshLayout(AppManager.getInstance().getMusicAutoService().binder.getCurrentSong());
            }
        }

        @Override
        public void pause() {
            mMainBinding.mPlayBtn.setPlaying(-1);
        }

        @Override
        public void end(TracksBean tracksBean) {
            playCurrentSong(tracksBean);
        }

        @Override
        public void error(String msg) {
            showSnackBar(msg);
        }

        @Override
        public void progress(int progress, int duration) {
            if (mMainBinding.mPlayBtn.getIsPlaying() != 0) {
                mMainBinding.tvCurrentSongProgress.setText(time.format(progress));
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Contacts.REQUEST_CODE_PLAY && resultCode == Contacts.RESULT_SONG_CODE) {
            mCurrentSong = (TracksBean) data.getSerializableExtra("song");
            mMainBinding.setSong(mCurrentSong);
            int status;
            if (mMainBinding.mPlayBtn.getIsPlaying() != (status = data.getIntExtra("status", -1))) {
                mMainBinding.mPlayBtn.setPlaying(status);
            }
        } else if (requestCode == Contacts.REQUEST_CODE_SEARCH && resultCode == Contacts.RESULT_SONG_CODE) {
            boolean isDownLoadSong = data.getBooleanExtra("isDownLoadSong", false);
            if (isDownLoadSong && "local".equals(AppManager.getInstance().getPlayStatus())) {
                mViewModel.refreshLocal(true);
            }
        }
    }

    @Override
    protected void onStop() {
        if (AppManager.getInstance().getMusicAutoService() != null &&
                AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer() != null && mCurrentSong != null) {
            if (AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().isPlaying()) {
                mCurrentSong.setDuration(AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().getDuration());
                mCurrentSong.setCurrentTime(AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().getCurrentPosition());
                AppUtils.setString("mCurrentSong", new Gson().toJson(mCurrentSong));
            }
        }
        AppUtils.setInteger(Contacts.PLAY_MODE, AppManager.getInstance().getPlayMode());
        AppUtils.setString(Contacts.PLAY_STATUS, AppManager.getInstance().getPlayStatus());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (AppManager.getInstance().getMusicAutoService() != null) {
            AppManager.getInstance().getMusicAutoService().binder.unBindSongStatusListener(listener);
            AppManager.getInstance().getMusicAutoService().quit();
            System.out.println("onDestroy");
        }
        super.onDestroy();
    }

    //  获取并设置返回键的点击事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                if (AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().isPlaying()) {
                    mCurrentSong.setCurrentTime(AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().getCurrentPosition());
                }
                intent.putExtra("song", mCurrentSong);
                intent.putExtra("status", mMainBinding.mPlayBtn.getIsPlaying());
                startActivityForResult(intent, Contacts.REQUEST_CODE_SEARCH);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
