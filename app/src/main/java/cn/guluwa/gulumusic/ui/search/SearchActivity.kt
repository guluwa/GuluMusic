package cn.guluwa.gulumusic.ui.search

import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
import cn.guluwa.gulumusic.R.id.ivCurrentSongPic
import cn.guluwa.gulumusic.ui.adapter.SearchResultListAdapter
import cn.guluwa.gulumusic.base.BaseActivity
import cn.guluwa.gulumusic.data.bean.*
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource
import cn.guluwa.gulumusic.data.remote.retrofit.exception.BaseException
import cn.guluwa.gulumusic.databinding.ActivitySearchBinding
import cn.guluwa.gulumusic.ui.view.dialog.SongMoreOperationDialog
import cn.guluwa.gulumusic.utils.listener.*
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.manage.Contacts
import cn.guluwa.gulumusic.ui.main.MainActivity
import cn.guluwa.gulumusic.ui.play.PlayActivity
import cn.guluwa.gulumusic.utils.AppUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*


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
     * 歌曲播放状态
     */
    private var mPlayStatus: Int = 0

    /**
     * 搜索输入框
     */
    private var mSearchAutoComplete: SearchView.SearchAutoComplete? = null

    /**
     * 搜索View
     */
    private var searchView: SearchView? = null

    /**
     * layout文件id
     */
    override val viewLayoutId: Int get() = R.layout.activity_search

    /**
     * 歌曲播放进度
     */
    private val listener = object : OnSongStatusListener {

        override fun loading() {
            mPlayBtn.isPlaying = 0
        }

        override fun start() {
            mPlayBtn.isPlaying = 1
            reFreshLayout()
            AppManager.getInstance().isChangeSong = true
        }

        override fun pause() {
            mPlayBtn.isPlaying = -1
        }

        override fun resume() {
            mPlayBtn.isPlaying = 1
        }

        override fun end(tracksBean: TracksBean) {
            println("SearchActivity end")
        }

        override fun error(msg: String) {
            showSnackBar(msg)
        }

        override fun progress(progress: Int, duration: Int) {
            if (mPlayBtn.isPlaying != 0) {
                tvCurrentSongProgress.text = AppUtils.formatTime(progress)
            }
        }

        override fun pic(url: String) {
            mSearchBinding.song = AppManager.getInstance().musicAutoService!!.binder.currentSong!!
        }

        override fun download(position: Int) {
            if (position != -1 &&
                    (mRecyclerView.adapter as SearchResultListAdapter).data[position] is SearchResultSongBean) {
                ((mRecyclerView.adapter as SearchResultListAdapter).data[position] as SearchResultSongBean).isDownLoad = true
                mRecyclerView.adapter.notifyItemChanged(position)
            }
        }
    }

    override fun initViews() {
        initDataBinding()
        initData()
        initClickListener()
        initToolBar()
        initSwipeRefreshLayout()
        initRecyclerView()
    }

    /**
     * DataBinding类型强转
     */
    private fun initDataBinding() {
        mSearchBinding = mViewDataBinding as ActivitySearchBinding
        reFreshLayout()
        mPlayBtn.isPlaying = if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) 1 else -1
    }

    /**
     * 数据初始化
     */
    private fun initData() {
        keyWord = if (intent.getStringExtra("keyWord") != null) intent.getStringExtra("keyWord") else ""
        page = -1
    }

    /**
     * 点击事件初始化
     */
    private fun initClickListener() {
        mSearchBinding.clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.mBottomPlayInfo -> {
                    val intent = Intent(this@SearchActivity, PlayActivity::class.java)
                    intent.putExtra("from", "search")
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@SearchActivity, Pair(ivCurrentSongPic, "songImage"))
                    ActivityCompat.startActivityForResult(this@SearchActivity, intent, Contacts.REQUEST_CODE_PLAY, options.toBundle())
                }
                R.id.mPlayBtn -> if (AppManager.getInstance().musicAutoService != null && AppManager.getInstance().musicAutoService!!.binder.mediaPlayer != null) {
                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                        mPlayBtn.isPlaying = -1
                    } else {
                        mPlayBtn.isPlaying = 1
                    }
                    playSong()
                }
            }
        }
    }

    /**
     * toolbar初始化
     */
    private fun initToolBar() {
        mToolBar.setTitle(R.string.app_name)//设置Toolbar标题
        setSupportActionBar(mToolBar)
        supportActionBar!!.setHomeButtonEnabled(true) //设置返回键可用
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initAnimation()
    }

    /**
     * 下拉刷新初始化
     */
    private fun initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeColors(
                resources.getColor(R.color.yellow),
                resources.getColor(R.color.green))
        mSwipeRefreshLayout.setOnRefreshListener {
            if ("" == keyWord) {
                showSnackBar(resources.getString(R.string.search_view_hint))
                mSwipeRefreshLayout.isRefreshing = false
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
                        (mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_moreing_tip))
                    }
                    2 -> showSongMoreOperation((arg2 as SearchResultSongBean).isDownLoad, arg2)
                }
            }
        })

        mAdapter.setColor(color)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = mAdapter
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
     * 搜索平台切换动画
     */
    private fun initAnimation() {
        when (AppManager.getInstance().searchPlatform) {
            Contacts.TYPE_TENCENT -> {
                color = R.color.tencent_music_color
                mToolBar.setTitle(R.string.type_qq)
            }
            Contacts.TYPE_XIAMI -> {
                color = R.color.xia_mi_music_color
                mToolBar.setTitle(R.string.type_xia_mi)
            }
            Contacts.TYPE_KUGOU -> {
                color = R.color.ku_gou_music_color
                mToolBar.setTitle(R.string.type_ku_gou)
            }
            Contacts.TYPE_BAIDU -> {
                color = R.color.bai_du_music_color
                mToolBar.setTitle(R.string.type_bai_du)
            }
            else -> {
                color = R.color.net_ease_music_color
                mToolBar.setTitle(R.string.type_net_ease)
            }
        }
        if (mRecyclerView.adapter != null) {
            (mRecyclerView.adapter as SearchResultListAdapter).setColor(color)
        }
        window.statusBarColor = AppUtils.deepenColor(resources.getColor(color))
        mToolBar.setBackgroundColor(resources.getColor(color))
        mToolBar.post {
            val cy = (mToolBar.top + mToolBar.bottom) / 2
            val finalRadius = Math.max(mToolBar.width, mToolBar.height)
            val animator = ViewAnimationUtils.createCircularReveal(
                    mToolBar, mToolBar.right, cy, (finalRadius / 3).toFloat(), finalRadius.toFloat())
            animator.start()
        }
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
                        playNewSong(AppUtils.getSongBean(localSongBean))
                    }) {
                        playNewSong(AppUtils.getSongBean(song))
                    }
        } else {
            playNewSong(AppUtils.getSongBean(song))
        }
    }

    /**
     * 播放歌曲
     *
     * @param song
     */
    private fun playNewSong(song: TracksBean) {
        reFreshLayout()
        //播放歌曲、利用服务后台播放
        AppManager.getInstance().musicAutoService!!.binder.isPrepare = false
        AppManager.getInstance().musicAutoService!!.binder.currentSong = song
        playSong()
    }

    /**
     * 不换歌曲（第一次进入）
     */
    private fun playSong() {
        if (AppManager.getInstance().musicAutoService!!.binder.isPrepare) {
            AppManager.getInstance().musicAutoService!!.binder.playOrPauseSong()
        } else {
            AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(null, false)
        }
    }

    /**
     * 更新页面
     */
    private fun reFreshLayout() {
        mSearchBinding.song = AppManager.getInstance().musicAutoService!!.binder.currentSong!!
        tvCurrentSongProgress.text = getString(R.string.song_start_time)
    }

    /**
     * Service 初始化、数据
     */
    override fun initService() {
        AppManager.getInstance().musicAutoService!!.binder.bindSongStatusListener(listener)
    }

    /**
     * viewModel初始化
     */
    override fun initViewModel() {
        //歌曲搜索
        mViewModel.searchSongByKeyWord()!!.observe(this, Observer { listViewDataBean ->
            if (listViewDataBean == null) {
                isLoadMoreIng = false
                (mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_more_tip))
                mSwipeRefreshLayout.isRefreshing = false
                return@Observer
            }
            when (listViewDataBean.status) {
                PageStatus.Loading -> if (page == 1)
                    mSwipeRefreshLayout.isRefreshing = true
                PageStatus.Error -> {
                    isLoadMoreIng = false
                    mViewModel.refreshSearchSongs(keyWord, page, false)
                    mSwipeRefreshLayout.isRefreshing = false
                    val msg: String = if (listViewDataBean.throwable is BaseException) {
                        (listViewDataBean.throwable as BaseException).msg
                    } else {
                        listViewDataBean.throwable!!.message!!
                    }
                    if ((mRecyclerView.adapter as SearchResultListAdapter).data.size == 1 &&
                            (mRecyclerView.adapter as SearchResultListAdapter).data[0] is String) {
                        (mRecyclerView.adapter as SearchResultListAdapter).setSearchHistory(msg, null)
                    } else {
                        showSnackBar(msg)
                        (mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_more_tip))
                    }
                }
                PageStatus.Empty -> {
                    isLoadMoreIng = false
                    mViewModel.refreshSearchSongs(keyWord, page, false)
                    mSwipeRefreshLayout.isRefreshing = false
                    if ((mRecyclerView.adapter as SearchResultListAdapter).data.size == 1 &&
                            (mRecyclerView.adapter as SearchResultListAdapter).data[0] is String) {
                        (mRecyclerView.adapter as SearchResultListAdapter).setSearchHistory(getString(R.string.search_result_empty_tip), null)
                    } else {
                        showSnackBar(getString(R.string.search_result_no_more_tip))
                    }
                }
                PageStatus.Content -> {
                    isLoadMoreIng = false
                    mViewModel.refreshSearchSongs(keyWord, page, false)
                    mSwipeRefreshLayout.isRefreshing = false
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
        startQuery()
        return true
    }

    private fun startQuery() {
        searchView!!.isIconified = false
        mSearchAutoComplete!!.setText(keyWord)
        mSearchAutoComplete!!.setSelection(keyWord.length)
        searchView!!.setQuery(keyWord, true)
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
                (mRecyclerView.adapter as SearchResultListAdapter).setSongs(data, getString(R.string.load_more_tip))
            } else {
                (mRecyclerView.adapter as SearchResultListAdapter).addSongs(data, getString(R.string.load_more_tip))
            }
            page++
        } else {
            if (page == 1) {
                (mRecyclerView.adapter as SearchResultListAdapter).setSearchHistory(getString(R.string.search_result_empty_tip), null)
            } else {
                showSnackBar(getString(R.string.search_result_no_more_tip))
                (mRecyclerView.adapter as SearchResultListAdapter).setLoadMoreTip(getString(R.string.load_more_tip))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Contacts.REQUEST_CODE_PLAY && resultCode == Contacts.RESULT_SONG_CODE) {
            mPlayBtn.isPlaying = if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) 1 else -1
            tvCurrentSongProgress.text = AppUtils.formatTime(AppManager.getInstance().musicAutoService!!.binder.currentSong!!.currentTime)
            if (AppManager.getInstance().isChangeSong) {
                reFreshLayout()
                AppManager.getInstance().isChangeSong = false
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@SearchActivity, MainActivity::class.java)
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
                        AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(
                                AppUtils.getSongBean(song as SearchResultSongBean), true)
                    }
                    1 -> {
                        if ((song as SearchResultSongBean).artist!![0] != "") {
                            keyWord = song.artist!![0]
                            startQuery()
                        }
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
                    ((mRecyclerView.adapter as SearchResultListAdapter).data[song.index] as SearchResultSongBean).isDownLoad = false
                    mRecyclerView.adapter.notifyItemChanged(song.index)
                }) { showSnackBar("删除失败") }
    }
}
