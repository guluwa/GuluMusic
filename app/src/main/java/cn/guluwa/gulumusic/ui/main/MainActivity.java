package cn.guluwa.gulumusic.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.adapter.PlayListAdapter;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.data.bean.TracksBean;
import cn.guluwa.gulumusic.databinding.ActivityMainBinding;
import cn.guluwa.gulumusic.ui.play.PlayActivity;
import cn.guluwa.gulumusic.utils.AppUtils;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding mMainBinding;
    private boolean sIsScrolling;
    private MainViewModel mViewModel;

    @Override
    public int getViewLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        mMainBinding = (ActivityMainBinding) mViewDataBinding;
        initToolBar();
        initDrawerLayout();
        initSwipeRefreshLayout();
        initRecyclerView();
    }

    private void initToolBar() {
        mMainBinding.mToolBar.setTitle(R.string.app_name);//设置Toolbar标题
        setSupportActionBar(mMainBinding.mToolBar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setStatusBarColor(AppUtils.deepenColor(Color.rgb(85, 160, 122)));
    }

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

    private void initSwipeRefreshLayout() {
        mMainBinding.mSwipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.yellow),
                getResources().getColor(R.color.green));
        mMainBinding.mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mViewModel.refresh(true);
        });
    }

    private void initRecyclerView() {
        PlayListAdapter mAdapter = new PlayListAdapter(song -> {
            Intent intent = new Intent(MainActivity.this, PlayActivity.class);
            intent.putExtra("song", song);
            startActivity(intent);
            overridePendingTransition(0, 0);
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

    @Override
    protected void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mViewModel.queryNetCloudHotSong().observe(this, listViewDataBean -> {
            if (listViewDataBean == null) {
                mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                return;
            }
            switch (listViewDataBean.status) {
                case Loading:
                    System.out.println("loading");
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(true);
                    break;
                case Error:
                    mViewModel.refresh(false);
                    System.out.println("error: " + listViewDataBean.throwable.getMessage());
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case Empty:
                    mViewModel.refresh(false);
                    System.out.println("empty");
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case Content:
                    mViewModel.refresh(false);
                    System.out.println("content");
                    mMainBinding.mSwipeRefreshLayout.setRefreshing(false);
                    setData(listViewDataBean.data);
                    break;
            }
        });
        mViewModel.refresh(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        //找到searchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        SearchView.SearchAutoComplete mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
        //设置输入框提示文字样式
        mSearchAutoComplete.setHintTextColor(getResources().getColor(R.color.gray));//设置提示文字颜色
        mSearchAutoComplete.setTextColor(getResources().getColor(android.R.color.white));//设置内容文字颜色
        searchView.setQueryHint(getString(R.string.search_view_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //提交按钮的点击事件
                Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //当输入框内容改变的时候回调
                Log.i("yjk", "内容: " + newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String tip = "";
        switch (id) {
            case R.id.action_search:
                tip = "搜索";
                break;
        }
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    public void setData(List<TracksBean> data) {
        ((PlayListAdapter) mMainBinding.mRecyclerView.getAdapter()).setData(data);
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }
}
