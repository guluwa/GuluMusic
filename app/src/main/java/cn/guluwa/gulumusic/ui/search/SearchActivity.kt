package cn.guluwa.gulumusic.ui.search

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.inputmethod.InputMethodManager

import java.text.SimpleDateFormat

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.adapter.SearchResultListAdapter
import cn.guluwa.gulumusic.base.BaseActivity
import cn.guluwa.gulumusic.data.bean.PageStatus
import cn.guluwa.gulumusic.data.bean.SearchResultSongBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource
import cn.guluwa.gulumusic.data.remote.retrofit.exception.BaseException
import cn.guluwa.gulumusic.databinding.ActivitySearchBinding
import cn.guluwa.gulumusic.listener.OnClickListener
import cn.guluwa.gulumusic.listener.OnSongStatusListener
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.manage.Contacts
import cn.guluwa.gulumusic.ui.main.MainActivity
import cn.guluwa.gulumusic.ui.play.PlayActivity
import cn.guluwa.gulumusic.utils.AppUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SearchActivity : BaseActivity() {

    /**
     * ViewBinder
     */
    private var mSearchBinding: ActivitySearchBinding? = null

    /**
     * 搜索关键词
     */
    private var keyWord: String = ""

    /**
     * 标记搜索页数
     */
    private var page: Int = 0

    /**
     * 是否加载更多
     */
    private var isLoadMoreIng: Boolean = false

    /**
     * 搜索平台主题颜色
     */
    private var color: Int = 0

    /**
     * 是否下载了歌曲
     */
    private var isDownLoadSong: Boolean = false

    /**
     * 当前播放歌曲
     */
    private var mCurrentSong: TracksBean? = null

    /**
     * 格式化时间
     */
    private var time: SimpleDateFormat? = null

    override val viewLayoutId: Int
        get() = R.layout.activity_search

    /**
     * 歌曲播放进度
     */
    private val listener = object : OnSongStatusListener {

        override fun loading() {
            mSearchBinding!!.mPlayBtn.isPlaying = 0
        }

        override fun start() {
            mSearchBinding!!.mPlayBtn.isPlaying = 1
            if (mCurrentSong!!.id != AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id) {
                reFreshLayout(AppManager.getInstance().musicAutoService!!.binder.currentSong!!)
            }
        }

        override fun pause() {
            mSearchBinding!!.mPlayBtn.isPlaying = -1
        }

        override fun end(tracksBean: TracksBean) {
            playCurrentSong(tracksBean)
        }

        override fun error(msg: String) {
            showSnackBar(msg)
        }

        override fun progress(progress: Int, duration: Int) {
            if (mSearchBinding!!.mPlayBtn.isPlaying != 0) {
                mSearchBinding!!.tvCurrentSongProgress.text = time!!.format(progress)
            }
        }
    }

    override fun initViews() {
        initData()
        initClickListener()
        initToolBar()
        initSwipeRefreshLayout()
        initRecyclerView()
    }

    /**
     * 搜索平台切换动画
     */
    private fun initAnimation() {
        when (AppManager.getInstance().searchPlatform) {
            Contacts.TYPE_TENCENT -> {
                color = R.color.tencent_music_color
                mSearchBinding!!.mToolBar.setTitle(R.string.type_qq)
            }
            Contacts.TYPE_XIAMI -> {
                color = R.color.xia_mi_music_color
                mSearchBinding!!.mToolBar.setTitle(R.string.type_xia_mi)
            }
            Contacts.TYPE_KUGOU -> {
                color = R.color.ku_gou_music_color
                mSearchBinding!!.mToolBar.setTitle(R.string.type_ku_gou)
            }
            Contacts.TYPE_BAIDU -> {
                color = R.color.bai_du_music_color
                mSearchBinding!!.mToolBar.setTitle(R.string.type_bai_du)
            }
            else -> {
                color = R.color.net_ease_music_color
                mSearchBinding!!.mToolBar.setTitle(R.string.type_net_ease)
            }
        }
        if (mSearchBinding!!.mRecyclerView.adapter != null) {
            (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).setColor(color)
        }
        window.statusBarColor = AppUtils.deepenColor(resources.getColor(color))
        mSearchBinding!!.mToolBar.setBackgroundColor(resources.getColor(color))
        mSearchBinding!!.mToolBar.post {
            val cy = (mSearchBinding!!.mToolBar.top + mSearchBinding!!.mToolBar.bottom) / 2
            val finalRadius = Math.max(mSearchBinding!!.mToolBar.width, mSearchBinding!!.mToolBar.height)
            val animator = ViewAnimationUtils.createCircularReveal(
                    mSearchBinding!!.mToolBar, mSearchBinding!!.mToolBar.right, cy, (finalRadius / 3).toFloat(), finalRadius.toFloat())
            animator.start()
        }
    }

    /**
     * toolbar初始化
     */
    private fun initToolBar() {
        mSearchBinding!!.mToolBar.setTitle(R.string.app_name)//设置Toolbar标题
        setSupportActionBar(mSearchBinding!!.mToolBar)
        supportActionBar!!.setHomeButtonEnabled(true) //设置返回键可用
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initAnimation()
    }

    /**
     * 数据初始化
     */
    private fun initData() {
        keyWord = ""
        page = -1
        mSearchBinding = mViewDataBinding as ActivitySearchBinding
        isDownLoadSong = false
        time = SimpleDateFormat("mm:ss")
        reFreshLayout(intent.getSerializableExtra("song") as TracksBean)
        mSearchBinding!!.mPlayBtn.isPlaying = intent.getIntExtra("status", -1)
        AppManager.getInstance().musicAutoService!!.binder.bindSongStatusListener(listener)
    }

    /**
     * 点击事件初始化
     */
    private fun initClickListener() {
        mSearchBinding!!.clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.mBottomPlayInfo -> {
                    val intent = Intent(this@SearchActivity, PlayActivity::class.java)
                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                        mCurrentSong!!.currentTime = AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.currentPosition
                    }
                    intent.putExtra("song", mCurrentSong)
                    intent.putExtra("status", mSearchBinding!!.mPlayBtn.isPlaying)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@SearchActivity, Pair(mSearchBinding!!.ivCurrentSongPic, "songImage"))
                    ActivityCompat.startActivityForResult(this@SearchActivity, intent, Contacts.REQUEST_CODE_PLAY, options.toBundle())
                }
                R.id.mPlayBtn -> if (AppManager.getInstance().musicAutoService != null && AppManager.getInstance().musicAutoService!!.binder.mediaPlayer != null) {
                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                        mSearchBinding!!.mPlayBtn.isPlaying = -1
                    } else {
                        mSearchBinding!!.mPlayBtn.isPlaying = 1
                    }
                    playCurrentSong(mCurrentSong!!.currentTime)
                }
            }
        }
    }

    /**
     * 下拉刷新初始化
     */
    private fun initSwipeRefreshLayout() {
        mSearchBinding!!.mSwipeRefreshLayout.setColorSchemeColors(
                resources.getColor(R.color.yellow),
                resources.getColor(R.color.green))
        mSearchBinding!!.mSwipeRefreshLayout.setOnRefreshListener {
            if ("" == keyWord) {
                showSnackBar(resources.getString(R.string.search_view_hint))
            } else {
                page = 1
                mViewModel.refreshSearchSongs(keyWord, page, true)
            }
        }
    }

    /**
     * 列表初始化
     */
    private fun initRecyclerView() {
        val mAdapter = SearchResultListAdapter(object : OnClickListener {
            override fun click(song: Any) {
                if (song is SearchResultSongBean) {
                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                        if (song.id == AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id) {
                            return
                        }
                        AppManager.getInstance().musicAutoService!!.binder.stop()
                    }
                    playCurrentSong(song)
                } else {
                    if (!isLoadMoreIng) {
                        isLoadMoreIng = true
                        mViewModel.refreshSearchSongs(keyWord, page, true)
                        (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_moreing_tip))
                    }
                }
            }
        })
        mAdapter.setListEmptyTip(getString(R.string.search_list_tip))
        mAdapter.setColor(color)
        mSearchBinding!!.mRecyclerView.layoutManager = LinearLayoutManager(this)
        mSearchBinding!!.mRecyclerView.adapter = mAdapter
    }

    /**
     * 播放歌曲
     *
     * @param song
     */
    private fun playCurrentSong(song: SearchResultSongBean) {
        reFreshLayout(AppUtils.getSongBean(song))
        if (song.isDownLoad) {
            Observable.just("")
                    .observeOn(Schedulers.io())
                    .map { s -> LocalSongsDataSource.getInstance().queryLocalSong(song.id, song.name) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ localSongBean ->
                        mCurrentSong = AppUtils.getSongBean(localSongBean)
                        AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
                        playCurrentSong(0)
                    }
                    ) { throwable ->
                        AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
                        playCurrentSong(0)
                    }
        } else {
            isDownLoadSong = true
            AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
            playCurrentSong(0)
        }
    }

    /**
     * 播放歌曲
     *
     * @param song
     */
    private fun playCurrentSong(song: TracksBean) {
        reFreshLayout(song)
        //播放歌曲、利用服务后台播放
        AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
        playCurrentSong(0)
    }

    /**
     * 不换歌曲（第一次进入）
     *
     * @param mCurrentTime
     */
    private fun playCurrentSong(mCurrentTime: Int) {
        if (AppManager.getInstance().musicAutoService!!.binder.isPrepare) {
            AppManager.getInstance().musicAutoService!!.binder.playOrPauseSong(-1)
        } else {
            AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(mCurrentSong!!, mCurrentTime)
        }
    }


    /**
     * 更新页面
     *
     * @param song
     */
    private fun reFreshLayout(song: TracksBean) {
        mCurrentSong = song
        mSearchBinding!!.song = mCurrentSong
        mSearchBinding!!.tvCurrentSongProgress.text = "00:00"
    }

    /**
     * viewModel初始化
     */
    override fun initViewModel() {
        //歌曲搜索
        mViewModel.searchSongByKeyWord()!!.observe(this, Observer { listViewDataBean ->
            if (listViewDataBean == null) {
                isLoadMoreIng = false
                (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_more_tip))
                mSearchBinding!!.mSwipeRefreshLayout.isRefreshing = false
                return@Observer
            }
            when (listViewDataBean.status) {
                PageStatus.Loading -> if (page == 1)
                    mSearchBinding!!.mSwipeRefreshLayout.isRefreshing = true
                PageStatus.Error -> {
                    isLoadMoreIng = false
                    mViewModel.refreshSearchSongs(keyWord, page, false)
                    mSearchBinding!!.mSwipeRefreshLayout.isRefreshing = false
                    val msg: String
                    if (listViewDataBean.throwable is BaseException) {
                        msg = (listViewDataBean.throwable as BaseException).msg
                    } else {
                        msg = listViewDataBean.throwable!!.message!!
                    }
                    if ((mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).dataList!!.size == 1 &&
                            (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).dataList!![0] is String) {
                        (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).setListEmptyTip(msg)
                    } else {
                        showSnackBar(msg)
                        (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_more_tip))
                    }
                }
                PageStatus.Empty -> {
                    isLoadMoreIng = false
                    mViewModel.refreshSearchSongs(keyWord, page, false)
                    mSearchBinding!!.mSwipeRefreshLayout.isRefreshing = false
                    if ((mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).dataList!!.size == 1 &&
                            (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).dataList!![0] is String) {
                        (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).setListEmptyTip(getString(R.string.search_result_empty_tip))
                    } else {
                        showSnackBar(getString(R.string.search_result_no_more_tip))
                    }
                }
                PageStatus.Content -> {
                    isLoadMoreIng = false
                    mViewModel.refreshSearchSongs(keyWord, page, false)
                    mSearchBinding!!.mSwipeRefreshLayout.isRefreshing = false
                    setData(listViewDataBean.data)
                }
            }
        })
    }

    /**
     * 菜单
     *
     * @param menu
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_activity_menu, menu)
        //找到searchView
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        //        searchView.setIconified(false);//一开始处于展开状态
        val mSearchAutoComplete = searchView.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)
        //设置输入框提示文字样式
        mSearchAutoComplete.setHintTextColor(resources.getColor(R.color.gray))//设置提示文字颜色
        mSearchAutoComplete.setTextColor(resources.getColor(android.R.color.white))//设置内容文字颜色
        searchView.queryHint = getString(R.string.search_view_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                //提交按钮的点击事件
                keyWord = query
                page = 1
                mViewModel.refreshSearchSongs(keyWord, page, true)
                disAppearKeyBoard(searchView)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                //当输入框内容改变的时候回调
                return true
            }
        })
        return true
    }

    /**
     * 菜单点击事件
     *
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_search_netease -> if (Contacts.TYPE_NETEASE != AppManager.getInstance().searchPlatform) {
                AppManager.getInstance().searchPlatform = Contacts.TYPE_NETEASE
                initAnimation()
            }
            R.id.action_search_tencent -> if (Contacts.TYPE_TENCENT != AppManager.getInstance().searchPlatform) {
                AppManager.getInstance().searchPlatform = Contacts.TYPE_TENCENT
                initAnimation()
            }
            R.id.action_search_xia_mi -> if (Contacts.TYPE_XIAMI != AppManager.getInstance().searchPlatform) {
                AppManager.getInstance().searchPlatform = Contacts.TYPE_XIAMI
                initAnimation()
            }
            R.id.action_search_ku_gou -> if (Contacts.TYPE_KUGOU != AppManager.getInstance().searchPlatform) {
                AppManager.getInstance().searchPlatform = Contacts.TYPE_KUGOU
                initAnimation()
            }
            R.id.action_search_bai_du -> if (Contacts.TYPE_BAIDU != AppManager.getInstance().searchPlatform) {
                AppManager.getInstance().searchPlatform = Contacts.TYPE_BAIDU
                initAnimation()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 填充列表数据
     *
     * @param data
     */
    private fun setData(data: List<SearchResultSongBean>?) {
        if (data != null && data.isNotEmpty()) {
            if (page == 1) {
                (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).setData(data as MutableList<Any>, getString(R.string.load_more_tip))
            } else {
                (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).addData(data as MutableList<Any>, getString(R.string.load_more_tip))
            }
            page++
        } else {
            if (page == 1) {
                (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).setListEmptyTip(getString(R.string.search_result_empty_tip))
            } else {
                showSnackBar(getString(R.string.search_result_no_more_tip))
                (mSearchBinding!!.mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_more_tip))
            }
        }
    }

    override fun onDestroy() {
        AppManager.getInstance().musicAutoService!!.binder.unBindSongStatusListener(listener)
        AppUtils.setString(Contacts.SEARCH_PLATFORM, AppManager.getInstance().searchPlatform)
        super.onDestroy()
    }

    //隐藏软键盘
    private fun disAppearKeyBoard(searchView: SearchView) {
        (searchView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    override fun onBackPressed() {
        if (isDownLoadSong) {
            val intent = Intent(this@SearchActivity, MainActivity::class.java)
            intent.putExtra("isDownLoadSong", isDownLoadSong)
            setResult(Contacts.RESULT_SONG_CODE, intent)
        }
        super.onBackPressed()
    }
}
