package cn.guluwa.gulumusic.ui.search

import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.TextView
import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.adapter.PlayListAdapter
import cn.guluwa.gulumusic.adapter.SearchResultListAdapter
import cn.guluwa.gulumusic.base.BaseActivity
import cn.guluwa.gulumusic.data.bean.*
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource
import cn.guluwa.gulumusic.data.remote.retrofit.exception.BaseException
import cn.guluwa.gulumusic.databinding.ActivitySearchBinding
import cn.guluwa.gulumusic.dialog.SongMoreOperationDialog
import cn.guluwa.gulumusic.listener.OnClickListener
import cn.guluwa.gulumusic.listener.OnResultListener
import cn.guluwa.gulumusic.listener.OnSelectListener
import cn.guluwa.gulumusic.listener.OnSongStatusListener
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.manage.Contacts
import cn.guluwa.gulumusic.ui.main.MainActivity
import cn.guluwa.gulumusic.ui.play.PlayActivity
import cn.guluwa.gulumusic.utils.AppUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.text.SimpleDateFormat


class SearchActivity : BaseActivity() {

    /**
     * ViewBinder
     */
    private lateinit var mSearchBinding: ActivitySearchBinding

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
            mSearchBinding.mPlayBtn.isPlaying = 0
        }

        override fun start() {
            mSearchBinding.mPlayBtn.isPlaying = 1
            if (mCurrentSong!!.id != AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id) {
                reFreshLayout(AppManager.getInstance().musicAutoService!!.binder.currentSong!!)
            }
        }

        override fun pause() {
            mSearchBinding.mPlayBtn.isPlaying = -1
        }

        override fun end(tracksBean: TracksBean) {
            playCurrentSong(tracksBean)
        }

        override fun error(msg: String) {
            showSnackBar(msg)
        }

        override fun progress(progress: Int, duration: Int) {
            if (mSearchBinding.mPlayBtn.isPlaying != 0) {
                mSearchBinding.tvCurrentSongProgress.text = time!!.format(progress)
            }
        }

        override fun pic(url: String) {
            mCurrentSong!!.al!!.picUrl = url
            mSearchBinding.song = mCurrentSong
        }

        override fun download(position: Int) {
            if ((mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).data[position] is SearchResultSongBean) {
                ((mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).data[position] as SearchResultSongBean).isDownLoad = true
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
                mSearchBinding.mToolBar.setTitle(R.string.type_qq)
            }
            Contacts.TYPE_XIAMI -> {
                color = R.color.xia_mi_music_color
                mSearchBinding.mToolBar.setTitle(R.string.type_xia_mi)
            }
            Contacts.TYPE_KUGOU -> {
                color = R.color.ku_gou_music_color
                mSearchBinding.mToolBar.setTitle(R.string.type_ku_gou)
            }
            Contacts.TYPE_BAIDU -> {
                color = R.color.bai_du_music_color
                mSearchBinding.mToolBar.setTitle(R.string.type_bai_du)
            }
            else -> {
                color = R.color.net_ease_music_color
                mSearchBinding.mToolBar.setTitle(R.string.type_net_ease)
            }
        }
        if (mSearchBinding.mRecyclerView.adapter != null) {
            (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).setColor(color)
        }
        window.statusBarColor = AppUtils.deepenColor(resources.getColor(color))
        mSearchBinding.mToolBar.setBackgroundColor(resources.getColor(color))
        mSearchBinding.mToolBar.post {
            val cy = (mSearchBinding.mToolBar.top + mSearchBinding.mToolBar.bottom) / 2
            val finalRadius = Math.max(mSearchBinding.mToolBar.width, mSearchBinding.mToolBar.height)
            val animator = ViewAnimationUtils.createCircularReveal(
                    mSearchBinding.mToolBar, mSearchBinding.mToolBar.right, cy, (finalRadius / 3).toFloat(), finalRadius.toFloat())
            animator.start()
        }
    }

    /**
     * toolbar初始化
     */
    private fun initToolBar() {
        mSearchBinding.mToolBar.setTitle(R.string.app_name)//设置Toolbar标题
        setSupportActionBar(mSearchBinding.mToolBar)
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
        mSearchBinding.mPlayBtn.isPlaying = intent.getIntExtra("status", -1)
        AppManager.getInstance().musicAutoService!!.binder.bindSongStatusListener(listener)
    }

    /**
     * 点击事件初始化
     */
    private fun initClickListener() {
        mSearchBinding.clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.mBottomPlayInfo -> {
                    val intent = Intent(this@SearchActivity, PlayActivity::class.java)
                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                        mCurrentSong!!.currentTime = AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.currentPosition
                    }
                    intent.putExtra("song", mCurrentSong)
                    intent.putExtra("status", mSearchBinding.mPlayBtn.isPlaying)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@SearchActivity, Pair(mSearchBinding.ivCurrentSongPic, "songImage"))
                    ActivityCompat.startActivityForResult(this@SearchActivity, intent, Contacts.REQUEST_CODE_PLAY, options.toBundle())
                }
                R.id.mPlayBtn -> if (AppManager.getInstance().musicAutoService != null && AppManager.getInstance().musicAutoService!!.binder.mediaPlayer != null) {
                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                        mSearchBinding.mPlayBtn.isPlaying = -1
                    } else {
                        mSearchBinding.mPlayBtn.isPlaying = 1
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
        mSearchBinding.mSwipeRefreshLayout.setColorSchemeColors(
                resources.getColor(R.color.yellow),
                resources.getColor(R.color.green))
        mSearchBinding.mSwipeRefreshLayout.setOnRefreshListener {
            if ("" == keyWord) {
                showSnackBar(resources.getString(R.string.search_view_hint))
                mSearchBinding.mSwipeRefreshLayout.isRefreshing = false
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
            override fun click(arg1: Int, arg2: Any) {
                when (arg1) {
                    1 -> if (arg2 is SearchResultSongBean) {
                        if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                            if (arg2.id == AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id) {
                                return
                            }
                            AppManager.getInstance().musicAutoService!!.binder.stop()
                        }
                        playCurrentSong(arg2)
                    } else if (arg2 is SearchHistoryBean) {
                        if (arg2.date != 0L) {
                            mSearchAutoComplete!!.setText(arg2.text)
                            mSearchAutoComplete!!.setSelection(arg2.text.length)
                            searchView!!.setQuery(arg2.text, true)
                        }
                    } else if (!isLoadMoreIng) {
                        isLoadMoreIng = true
                        mViewModel.refreshSearchSongs(keyWord, page, true)
                        (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_moreing_tip))
                    }
                    2 -> showSongMoreOperation((arg2 as SearchResultSongBean).isDownLoad, arg2)
                }
            }
        })

        mAdapter.setColor(color)
        mSearchBinding.mRecyclerView.layoutManager = LinearLayoutManager(this)
        mSearchBinding.mRecyclerView.adapter = mAdapter
        //搜索记录
        LocalSongsDataSource.getInstance().querySearchRecord(object : OnResultListener<List<SearchHistoryBean>> {
            override fun success(result: List<SearchHistoryBean>) {
                println(result)
                mAdapter.setSearchHistory(getString(R.string.search_list_tip), result)
            }

            override fun failed(error: String) {
                println(error)
                mAdapter.setSearchHistory(getString(R.string.search_list_tip), null)
            }
        })
    }

    /**
     * 播放歌曲
     *
     * @param song
     */
    private fun playCurrentSong(song: SearchResultSongBean) {
        if (song.isDownLoad) {
            Observable.just("")
                    .observeOn(Schedulers.io())
                    .map { LocalSongsDataSource.getInstance().queryLocalSong(song.id, song.name) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ localSongBean ->
                        reFreshLayout(AppUtils.getSongBean(localSongBean))
                        AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
                        playCurrentSong(0)
                    }) {
                        reFreshLayout(AppUtils.getSongBean(song))
                        AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
                        playCurrentSong(0)
                    }
        } else {
            reFreshLayout(AppUtils.getSongBean(song))
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
            AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(mCurrentSong!!, mCurrentTime, false)
        }
    }


    /**
     * 更新页面
     *
     * @param song
     */
    private fun reFreshLayout(song: TracksBean) {
        mCurrentSong = song
        mSearchBinding.song = mCurrentSong
        mSearchBinding.tvCurrentSongProgress.text = "00:00"
    }

    /**
     * viewModel初始化
     */
    override fun initViewModel() {
        //歌曲搜索
        mViewModel.searchSongByKeyWord()!!.observe(this, Observer { listViewDataBean ->
            if (listViewDataBean == null) {
                isLoadMoreIng = false
                (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_more_tip))
                mSearchBinding.mSwipeRefreshLayout.isRefreshing = false
                return@Observer
            }
            when (listViewDataBean.status) {
                PageStatus.Loading -> if (page == 1)
                    mSearchBinding.mSwipeRefreshLayout.isRefreshing = true
                PageStatus.Error -> {
                    isLoadMoreIng = false
                    mViewModel.refreshSearchSongs(keyWord, page, false)
                    mSearchBinding.mSwipeRefreshLayout.isRefreshing = false
                    val msg: String
                    if (listViewDataBean.throwable is BaseException) {
                        msg = (listViewDataBean.throwable as BaseException).msg
                    } else {
                        msg = listViewDataBean.throwable!!.message!!
                    }
                    if ((mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).data.size == 1 &&
                            (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).data[0] is String) {
                        (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).setSearchHistory(msg, null)
                    } else {
                        showSnackBar(msg)
                        (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_more_tip))
                    }
                }
                PageStatus.Empty -> {
                    isLoadMoreIng = false
                    mViewModel.refreshSearchSongs(keyWord, page, false)
                    mSearchBinding.mSwipeRefreshLayout.isRefreshing = false
                    if ((mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).data.size == 1 &&
                            (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).data[0] is String) {
                        (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).setSearchHistory(getString(R.string.search_result_empty_tip), null)
                    } else {
                        showSnackBar(getString(R.string.search_result_no_more_tip))
                    }
                }
                PageStatus.Content -> {
                    isLoadMoreIng = false
                    mViewModel.refreshSearchSongs(keyWord, page, false)
                    mSearchBinding.mSwipeRefreshLayout.isRefreshing = false
                    setData(listViewDataBean.data)
                }
            }
        })
    }

    private var searchItem: MenuItem? = null

    private var mSearchAutoComplete: SearchView.SearchAutoComplete? = null

    private var searchView: SearchView? = null

    /**
     * 菜单
     *
     * @param menu
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_activity_menu, menu)
        //找到searchView
        searchItem = menu.findItem(R.id.action_search)
        searchView = MenuItemCompat.getActionView(searchItem) as SearchView
//        searchView.setIconified(false);//一开始处于展开状态
        mSearchAutoComplete = searchView!!.findViewById(R.id.search_src_text)
        //设置输入框提示文字样式
        mSearchAutoComplete!!.setHintTextColor(resources.getColor(R.color.gray))//设置提示文字颜色
        mSearchAutoComplete!!.setTextColor(resources.getColor(android.R.color.white))//设置内容文字颜色
        searchView!!.queryHint = getString(R.string.search_view_hint)
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                //提交按钮的点击事件
                keyWord = query
                page = 1
                mViewModel.refreshSearchSongs(keyWord, page, true)
                searchView!!.clearFocus()
                disAppearKeyBoard(searchView!!)
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
                (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).setSongs(data, getString(R.string.load_more_tip))
            } else {
                (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).addSongs(data, getString(R.string.load_more_tip))
            }
            page++
        } else {
            if (page == 1) {
                (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).setSearchHistory(getString(R.string.search_result_empty_tip), null)
            } else {
                showSnackBar(getString(R.string.search_result_no_more_tip))
                (mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_more_tip))
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
        val intent = Intent(this@SearchActivity, MainActivity::class.java)
        intent.putExtra("isDownLoadSong", isDownLoadSong)
        intent.putExtra("song", mCurrentSong)
        setResult(Contacts.RESULT_SONG_CODE, intent)
        super.onBackPressed()
    }

    var dialog: Dialog? = null

    /**
     * 歌曲更多操作对话框
     */
    private fun showSongMoreOperation(isLocal: Boolean, song: Any) {
        dialog = SongMoreOperationDialog(this, R.style.DialogStyle, object : OnSelectListener {
            override fun select(index: Int) {
                dialog!!.dismiss()
                when (index) {
                    0 -> if (isLocal) {
                        showDeleteDialog(song as SearchResultSongBean)
                    } else {
                        AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(AppUtils.getSongBean(song as SearchResultSongBean), 0, true)
                    }
                    1 -> {
                        println((song as BaseSongBean).singer!!.name)
                    }
                }
            }
        }, arrayListOf(if (isLocal) "删除歌曲" else "缓存歌曲", "查看歌手"))
        dialog!!.show()
    }

    /**
     * 删除本地歌曲确认对话框
     */
    private fun showDeleteDialog(song: SearchResultSongBean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("提示")
        val view = LayoutInflater.from(this).inflate(R.layout.song_delete_tip_dialog, null, false)
        val tvDialogMessage = view.findViewById<TextView>(R.id.tvDialogMessage)
        tvDialogMessage.text = String.format("亲，您确定要删除「%s」吗？",
                if (song.name.length < 8) song.name else String.format("%s…", song.name.substring(0, 6)))
        val cbDeleteSongFile = view.findViewById<CheckBox>(R.id.cbDeleteSongFile)
        builder.setView(view)
        builder.setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
        builder.setPositiveButton("删除") { dialog, _ ->
            deleteLocalSong(cbDeleteSongFile.isChecked, song)
            dialog.dismiss()
        }
        builder.setCancelable(false)
        val dialog = builder.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
    }

    /**
     * 删除本地歌曲
     */
    private fun deleteLocalSong(isChecked: Boolean, song: SearchResultSongBean) {
        Observable.just(isChecked)
                .observeOn(Schedulers.io())
                .map {
                    if (!isChecked) {
                        AppUtils.deleteFile(String.format("%s_%s.mp3", song.name, song.id), 1)
                        AppUtils.deleteFile(String.format("%s_%s.txt", song.name, song.id), 2)
                    }
                    LocalSongsDataSource.getInstance().deleteLocalSong(LocalSongsDataSource.getInstance().queryLocalSong(song.id, song.name))
                    isChecked
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showSnackBar("删除成功")
                    ((mSearchBinding.mRecyclerView.adapter as SearchResultListAdapter).data[song.index] as SearchResultSongBean).isDownLoad = false
                }) { showSnackBar("删除失败") }
    }
}
