package cn.guluwa.gulumusic.ui;

import android.graphics.Color;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.adapter.PlayListAdapter;
import cn.guluwa.gulumusic.base.BaseActivity;
import cn.guluwa.gulumusic.dagger.component.DaggerMainComponent;
import cn.guluwa.gulumusic.dagger.module.MainModule;
import cn.guluwa.gulumusic.data.bean.PlayListBean;
import cn.guluwa.gulumusic.databinding.ActivityMainBinding;
import cn.guluwa.gulumusic.presenter.MainPresenter;
import cn.guluwa.gulumusic.utils.AppUtils;

public class MainActivity extends BaseActivity {

    @Inject
    MainPresenter presenter;
    private ActivityMainBinding mMainBinding;

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
        mMainBinding.mSwipeRefreshLayout.setOnRefreshListener(() -> presenter.obtainNetCloudHot());
    }

    private void initRecyclerView() {
        PlayListAdapter mAdapter = new PlayListAdapter();
        mMainBinding.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMainBinding.mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void initDagger() {
        DaggerMainComponent.builder().mainModule(new MainModule(this))
                .build().inject(this);
        mMainBinding.mSwipeRefreshLayout.setRefreshing(true);
        presenter.obtainNetCloudHot();
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
        searchView.setQueryHint("搜索歌手、歌名、专辑");
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

    public void setData(List<PlayListBean.PlaylistBean.TracksBean> data) {
        ((PlayListAdapter)mMainBinding.mRecyclerView.getAdapter()).setData(data);
    }

    public ActivityMainBinding getmMainBinding() {
        return mMainBinding;
    }
}
