package cn.guluwa.gulumusic.ui.search;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewAnimationUtils;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.adapter.PlayListAdapter;
import cn.guluwa.gulumusic.adapter.SearchResultListAdapter;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.databinding.ActivityMainBinding;
import cn.guluwa.gulumusic.databinding.ActivitySearchBinding;
import cn.guluwa.gulumusic.listener.OnClickListener;
import cn.guluwa.gulumusic.manage.AppManager;
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
        initAnimation();
        initSwipeRefreshLayout();
        initRecyclerView();
    }

    private void initAnimation() {
        mSearchBinding.mToolBar.post(() -> {
            int cy = (mSearchBinding.mToolBar.getTop() + mSearchBinding.mToolBar.getBottom()) / 2;
            int finalRadius = Math.max(mSearchBinding.mToolBar.getWidth(), mSearchBinding.mToolBar.getHeight());
            Animator animator = ViewAnimationUtils.createCircularReveal(
                    mSearchBinding.mToolBar, mSearchBinding.mToolBar.getRight(), cy, finalRadius / 3, finalRadius);
            animator.setDuration(500);
            animator.start();
        });
    }

    private void initToolBar() {
        mSearchBinding.mToolBar.setTitle(R.string.app_name);//设置Toolbar标题
        setSupportActionBar(mSearchBinding.mToolBar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setStatusBarColor(AppUtils.deepenColor(Color.rgb(239, 200, 73)));
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
            mViewModel.refreshSearchSongs(keyWord, page);
        });
    }

    /**
     * 列表初始化
     */
    private void initRecyclerView() {
        SearchResultListAdapter mAdapter = new SearchResultListAdapter(song -> {

        });
        mSearchBinding.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSearchBinding.mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void initViewModel() {
        //歌曲搜索
        mViewModel.searchSongByKeyWord().observe(this, listViewDataBean -> {
            if (listViewDataBean == null) {
                mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                return;
            }
            switch (listViewDataBean.status) {
                case Loading:
                    mSearchBinding.mSwipeRefreshLayout.setRefreshing(true);
                    break;
                case Error:
                    mViewModel.refreshSearchSongs(keyWord, page = -1);
                    showSnackBar(listViewDataBean.throwable.getMessage());
                    mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case Empty:
                    mViewModel.refreshSearchSongs(keyWord, page = -1);
                    showSnackBar("没有更多了哦~");
                    mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case Content:
                    AppManager.getInstance().setPlayStatus("local");
                    mViewModel.refreshLocal(false);
                    mSearchBinding.mSwipeRefreshLayout.setRefreshing(false);
//                    setData(listViewDataBean.data);
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
        searchView.setIconified(false);//一开始处于展开状态
        SearchView.SearchAutoComplete mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
        //设置输入框提示文字样式
        mSearchAutoComplete.setHintTextColor(getResources().getColor(R.color.gray));//设置提示文字颜色
        mSearchAutoComplete.setTextColor(getResources().getColor(android.R.color.white));//设置内容文字颜色
        searchView.setQueryHint(getString(R.string.search_view_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //提交按钮的点击事件
                showSnackBar(query);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
