package cn.guluwa.gulumusic.ui.search;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.adapter.PlayListAdapter;
import cn.guluwa.gulumusic.adapter.SearchResultListAdapter;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.databinding.ActivityMainBinding;
import cn.guluwa.gulumusic.databinding.ActivitySearchBinding;
import cn.guluwa.gulumusic.listener.OnClickListener;
import cn.guluwa.gulumusic.manage.AppManager;
import cn.guluwa.gulumusic.manage.Contacts;
import cn.guluwa.gulumusic.ui.main.MainActivity;
import cn.guluwa.gulumusic.utils.AppUtils;

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

    @Override
    public int getViewLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void initViews() {
        initData();
        initToolBar();
        initSwipeRefreshLayout();
        initRecyclerView();
    }

    private void initAnimation() {
        int color;
        switch (AppManager.getInstance().getSearchPlatform()) {
            case Contacts.TYPE_TENCENT:
                color = R.color.tencent_music_color;
                mSearchBinding.mToolBar.setTitle("QQ");
                break;
            case Contacts.TYPE_XIAMI:
                color = R.color.xia_mi_music_color;
                mSearchBinding.mToolBar.setTitle("虾米");
                break;
            case Contacts.TYPE_KUGOU:
                color = R.color.ku_gou_music_color;
                mSearchBinding.mToolBar.setTitle("酷狗");
                break;
            case Contacts.TYPE_BAIDU:
                color = R.color.bai_du_music_color;
                mSearchBinding.mToolBar.setTitle("百度");
                break;
            default:
                color = R.color.net_ease_music_color;
                mSearchBinding.mToolBar.setTitle("网易云");
                break;
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

    private void initToolBar() {
        mSearchBinding.mToolBar.setTitle(R.string.app_name);//设置Toolbar标题
        setSupportActionBar(mSearchBinding.mToolBar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initAnimation();
    }

    private void initData() {
        keyWord = "";
        page = -1;
        mSearchBinding = (ActivitySearchBinding) mViewDataBinding;
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
                showSnackBar(((SearchResultSongBean) song).getName());
            } else {
                mViewModel.refreshSearchSongs(keyWord, page, true);
                ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setLoadMoreTip("正在刷新呀~~~");
            }
        });
        mSearchBinding.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSearchBinding.mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void initViewModel() {
        //歌曲搜索
        mViewModel.searchSongByKeyWord().observe(this, listViewDataBean -> {
            if (listViewDataBean == null) {
                ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setLoadMoreTip("点我，继续加载~~~");
                mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                return;
            }
            switch (listViewDataBean.status) {
                case Loading:
                    if (page == 1)
                        mSearchBinding.mSwipeRefreshLayout.setRefreshing(true);
                    break;
                case Error:
                    mViewModel.refreshSearchSongs(keyWord, page, false);
                    showSnackBar(listViewDataBean.throwable.getMessage());
                    mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                    ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setLoadMoreTip("点我，继续加载~~~");
                    break;
                case Empty:
                    mViewModel.refreshSearchSongs(keyWord, page, false);
                    showSnackBar("没有更多了哦~");
                    mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case Content:
                    mViewModel.refreshSearchSongs(keyWord, page, false);
                    mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                    setData(listViewDataBean.data);
                    break;
            }
        });
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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

    public void setData(List<SearchResultSongBean> data) {
        //填充列表数据
        if (data != null && data.size() != 0) {
            if (page == 1) {
                ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).setData(data);
            } else {
                ((SearchResultListAdapter) mSearchBinding.mRecyclerView.getAdapter()).addData(data);
            }
            page++;
        } else {
            showSnackBar("没有更多了哦~");
        }
    }

    @Override
    protected void onDestroy() {
        AppUtils.setString(Contacts.SEARCH_PLATFORM, AppManager.getInstance().getSearchPlatform());
        super.onDestroy();
    }

    //隐藏软键盘
    private void disAppearKeyBoard(SearchView searchView) {
        ((InputMethodManager) searchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
