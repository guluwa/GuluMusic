package cn.guluwa.gulumusic.ui.search;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.adapter.SearchResultListAdapter;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.data.bean.LocalSongBean;
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource;
import cn.guluwa.gulumusic.data.remote.retrofit.exception.BaseException;
import cn.guluwa.gulumusic.databinding.ActivitySearchBinding;
import cn.guluwa.gulumusic.listener.OnSongStatusListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.manage.Contacts;
import cn.guluwa.gulumusic.ui.main.MainActivity;
import cn.guluwa.gulumusic.ui.play.PlayActivity;
import cn.guluwa.gulumusic.utils.AppUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class SearchActivity extends BaseActivity {

    /**
     * ViewBinder
     */
    private ActivitySearchBinding mSearchBinding;

    /**
     * 搜索关键词
     */
    private String keyWord;

    /**
     * 标记搜索页数
     */
    private int page;

    /**
     * 是否加载更多
     */
    private boolean isLoadMoreIng;

    /**
     * 搜索平台主题颜色
     */
    private int color;

    /**
     * 是否下载了歌曲
     */
    private boolean isDownLoadSong;

    /**
     * 当前播放歌曲
     */
    private TracksBean mCurrentSong;

    /**
     * 格式化时间
     */
    private SimpleDateFormat time;

    @Override
    public int getViewLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void initViews() {
        initData();
        initClickListener();
        initToolBar();
        initSwipeRefreshLayout();
        initRecyclerView();
    }

    /**
     * 搜索平台切换动画
     */
    private void initAnimation() {
        switch (AppManager.getInstance().getSearchPlatform()) {
            case Contacts.TYPE_TENCENT:
                color = R.color.tencent_music_color;
                mSearchBinding.mToolBar.setTitle(R.string.type_qq);
                break;
            case Contacts.TYPE_XIAMI:
                color = R.color.xia_mi_music_color;
                mSearchBinding.mToolBar.setTitle(R.string.type_xia_mi);
                break;
            case Contacts.TYPE_KUGOU:
                color = R.color.ku_gou_music_color;
                mSearchBinding.mToolBar.setTitle(R.string.type_ku_gou);
                break;
            case Contacts.TYPE_BAIDU:
                color = R.color.bai_du_music_color;
                mSearchBinding.mToolBar.setTitle(R.string.type_bai_du);
                break;
            default:
                color = R.color.net_ease_music_color;
                mSearchBinding.mToolBar.setTitle(R.string.type_net_ease);
                break;
        }
        if (mSearchBinding.mRecyclerView.getAdapter() != null) {
            ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setColor(color);
        }
        getWindow().setStatusBarColor(AppUtils.deepenColor(getResources().getColor(color)));
        mSearchBinding.mToolBar.setBackgroundColor(getResources().getColor(color));
        mSearchBinding.mToolBar.post(() -> {
            int cy = (mSearchBinding.mToolBar.getTop() + mSearchBinding.mToolBar.getBottom()) / 2;
            int finalRadius = Math.max(mSearchBinding.mToolBar.getWidth(), mSearchBinding.mToolBar.getHeight());
            Animator animator = ViewAnimationUtils.createCircularReveal(
                    mSearchBinding.mToolBar, mSearchBinding.mToolBar.getRight(), cy, finalRadius / 3, finalRadius);
            animator.start();
        });
    }

    /**
     * toolbar初始化
     */
    private void initToolBar() {
        mSearchBinding.mToolBar.setTitle(R.string.app_name);//设置Toolbar标题
        setSupportActionBar(mSearchBinding.mToolBar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initAnimation();
    }

    /**
     * 数据初始化
     */
    private void initData() {
        keyWord = "";
        page = -1;
        mSearchBinding = (ActivitySearchBinding) mViewDataBinding;
        isDownLoadSong = false;
        time = new SimpleDateFormat("mm:ss");
        reFreshLayout((TracksBean) getIntent().getSerializableExtra("song"));
        mSearchBinding.mPlayBtn.setPlaying(getIntent().getIntExtra("status", -1));
        AppManager.getInstance().getMusicAutoService().binder.bindSongStatusListener(listener);
    }

    /**
     * 点击事件初始化
     */
    private void initClickListener() {
        mSearchBinding.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.mBottomPlayInfo:
                        Intent intent = new Intent(SearchActivity.this, PlayActivity.class);
                        if (AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().isPlaying()) {
                            mCurrentSong.setCurrentTime(AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().getCurrentPosition());
                        }
                        intent.putExtra("song", mCurrentSong);
                        intent.putExtra("status", mSearchBinding.mPlayBtn.getIsPlaying());
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(SearchActivity.this, new Pair<>(mSearchBinding.ivCurrentSongPic, "songImage"));
                        ActivityCompat.startActivityForResult(SearchActivity.this, intent, Contacts.REQUEST_CODE_PLAY, options.toBundle());
                        break;
                    case R.id.mPlayBtn:
                        if (AppManager.getInstance().getMusicAutoService() != null &&
                                AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer() != null) {
                            if (AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().isPlaying()) {
                                mSearchBinding.mPlayBtn.setPlaying(-1);
                            } else {
                                mSearchBinding.mPlayBtn.setPlaying(1);
                            }
                            playCurrentSong(mCurrentSong.getCurrentTime());
                        }
                        break;
                }
            }
        });
    }

    /**
     * 下拉刷新初始化
     */
    private void initSwipeRefreshLayout() {
        mSearchBinding.mSwipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.yellow),
                getResources().getColor(R.color.green));
        mSearchBinding.mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if ("".equals(keyWord)) {
                showSnackBar(getResources().getString(R.string.search_view_hint));
                return;
            }
            page = 1;
            mViewModel.refreshSearchSongs(keyWord, page, true);
        });
    }

    /**
     * 列表初始化
     */
    private void initRecyclerView() {
        SearchResultListAdapter mAdapter = new SearchResultListAdapter(song -> {
            if (song instanceof SearchResultSongBean) {
                if (AppManager.getInstance().getMusicAutoService().binder.getMediaPlayer().isPlaying()) {
                    if (((SearchResultSongBean) song).getId().equals(AppManager.getInstance().getMusicAutoService().binder.getCurrentSong().getId())) {
                        return;
                    }
                    AppManager.getInstance().getMusicAutoService().binder.stop();
                }
                playCurrentSong((SearchResultSongBean) song);
            } else {
                if (!isLoadMoreIng) {
                    isLoadMoreIng = true;
                    mViewModel.refreshSearchSongs(keyWord, page, true);
                    ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setLoadMoreTip(getString(R.string.load_moreing_tip));
                }
            }
        });
        mAdapter.setListEmptyTip(getString(R.string.search_list_tip));
        mAdapter.setColor(color);
        mSearchBinding.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSearchBinding.mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 播放歌曲
     *
     * @param song
     */
    private void playCurrentSong(SearchResultSongBean song) {
        reFreshLayout(AppUtils.getSongBean(song));
        if (song.isDownLoad()) {
            Observable.just("")
                    .observeOn(Schedulers.io())
                    .map(s -> LocalSongsDataSource.getInstance().queryLocalSong(song.getId(), song.getName()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(localSongBean -> {
                                mCurrentSong = AppUtils.getSongBean(localSongBean);
                                AppManager.getInstance().getMusicAutoService().binder.setPrepare(false);
                                playCurrentSong(0);
                            },
                            throwable -> {
                                AppManager.getInstance().getMusicAutoService().binder.setPrepare(false);
                                playCurrentSong(0);
                            });
        } else {
            isDownLoadSong = true;
            AppManager.getInstance().getMusicAutoService().binder.setPrepare(false);
            playCurrentSong(0);
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
        mSearchBinding.setSong(mCurrentSong);
        mSearchBinding.tvCurrentSongProgress.setText("00:00");
    }

    /**
     * viewModel初始化
     */
    @Override
    protected void initViewModel() {
        //歌曲搜索
        mViewModel.searchSongByKeyWord().observe(this, listViewDataBean -> {
            if (listViewDataBean == null) {
                isLoadMoreIng = false;
                ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setLoadMoreTip(getString(R.string.load_more_tip));
                mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                return;
            }
            switch (listViewDataBean.status) {
                case Loading:
                    if (page == 1)
                        mSearchBinding.mSwipeRefreshLayout.setRefreshing(true);
                    break;
                case Error:
                    isLoadMoreIng = false;
                    mViewModel.refreshSearchSongs(keyWord, page, false);
                    mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                    String msg;
                    if (listViewDataBean.throwable instanceof BaseException) {
                        msg = ((BaseException) listViewDataBean.throwable).getMsg();
                    } else {
                        msg = listViewDataBean.throwable.getMessage();
                    }
                    if (((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).getData().size() == 1 &&
                            ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).getData().get(0) instanceof String) {
                        ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setListEmptyTip(msg);
                    } else {
                        showSnackBar(msg);
                        ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setLoadMoreTip(getString(R.string.load_more_tip));
                    }
                    break;
                case Empty:
                    isLoadMoreIng = false;
                    mViewModel.refreshSearchSongs(keyWord, page, false);
                    mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                    if (((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).getData().size() == 1 &&
                            ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).getData().get(0) instanceof String) {
                        ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setListEmptyTip(getString(R.string.search_result_empty_tip));
                    } else {
                        showSnackBar(getString(R.string.search_result_no_more_tip));
                    }
                    break;
                case Content:
                    isLoadMoreIng = false;
                    mViewModel.refreshSearchSongs(keyWord, page, false);
                    mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                    setData(listViewDataBean.data);
                    break;
            }
        });
    }

    /**
     * 菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_activity_menu, menu);
        //找到searchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        searchView.setIconified(false);//一开始处于展开状态
        SearchView.SearchAutoComplete mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
        //设置输入框提示文字样式
        mSearchAutoComplete.setHintTextColor(getResources().getColor(R.color.gray));//设置提示文字颜色
        mSearchAutoComplete.setTextColor(getResources().getColor(android.R.color.white));//设置内容文字颜色
        searchView.setQueryHint(getString(R.string.search_view_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //提交按钮的点击事件
                keyWord = query;
                page = 1;
                mViewModel.refreshSearchSongs(keyWord, page, true);
                disAppearKeyBoard(searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //当输入框内容改变的时候回调
                return true;
            }
        });
        return true;
    }

    /**
     * 菜单点击事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_search_netease:
                if (!Contacts.TYPE_NETEASE.equals(AppManager.getInstance().getSearchPlatform())) {
                    AppManager.getInstance().setSearchPlatform(Contacts.TYPE_NETEASE);
                    initAnimation();
                }
                break;
            case R.id.action_search_tencent:
                if (!Contacts.TYPE_TENCENT.equals(AppManager.getInstance().getSearchPlatform())) {
                    AppManager.getInstance().setSearchPlatform(Contacts.TYPE_TENCENT);
                    initAnimation();
                }
                break;
            case R.id.action_search_xia_mi:
                if (!Contacts.TYPE_XIAMI.equals(AppManager.getInstance().getSearchPlatform())) {
                    AppManager.getInstance().setSearchPlatform(Contacts.TYPE_XIAMI);
                    initAnimation();
                }
                break;
            case R.id.action_search_ku_gou:
                if (!Contacts.TYPE_KUGOU.equals(AppManager.getInstance().getSearchPlatform())) {
                    AppManager.getInstance().setSearchPlatform(Contacts.TYPE_KUGOU);
                    initAnimation();
                }
                break;
            case R.id.action_search_bai_du:
                if (!Contacts.TYPE_BAIDU.equals(AppManager.getInstance().getSearchPlatform())) {
                    AppManager.getInstance().setSearchPlatform(Contacts.TYPE_BAIDU);
                    initAnimation();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 填充列表数据
     *
     * @param data
     */
    public void setData(List<SearchResultSongBean> data) {
        if (data != null && data.size() != 0) {
            if (page == 1) {
                ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setData(data, getString(R.string.load_more_tip));
            } else {
                ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).addData(data, getString(R.string.load_more_tip));
            }
            page++;
        } else {
            if (page == 1) {
                ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setListEmptyTip(getString(R.string.search_result_empty_tip));
            } else {
                showSnackBar(getString(R.string.search_result_no_more_tip));
                ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setLoadMoreTip(getString(R.string.load_more_tip));
            }
        }
    }

    /**
     * 歌曲播放进度
     */
    private OnSongStatusListener listener = new OnSongStatusListener() {

        @Override
        public void loading() {
            mSearchBinding.mPlayBtn.setPlaying(0);
        }

        @Override
        public void start() {
            mSearchBinding.mPlayBtn.setPlaying(1);
            if (!mCurrentSong.getId().equals(AppManager.getInstance().getMusicAutoService().binder.getCurrentSong().getId())) {
                reFreshLayout(AppManager.getInstance().getMusicAutoService().binder.getCurrentSong());
            }
        }

        @Override
        public void pause() {
            mSearchBinding.mPlayBtn.setPlaying(-1);
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
            if (mSearchBinding.mPlayBtn.getIsPlaying() != 0) {
                mSearchBinding.tvCurrentSongProgress.setText(time.format(progress));
            }
        }
    };

    @Override
    protected void onDestroy() {
        AppManager.getInstance().getMusicAutoService().binder.unBindSongStatusListener(listener);
        AppUtils.setString(Contacts.SEARCH_PLATFORM, AppManager.getInstance().getSearchPlatform());
        super.onDestroy();
    }

    //隐藏软键盘
    private void disAppearKeyBoard(SearchView searchView) {
        ((InputMethodManager) searchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onBackPressed() {
        if (isDownLoadSong) {
            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            intent.putExtra("isDownLoadSong", isDownLoadSong);
            setResult(Contacts.RESULT_SONG_CODE, intent);
        }
        super.onBackPressed();
    }
}
