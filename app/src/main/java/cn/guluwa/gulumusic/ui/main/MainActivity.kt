package cn.guluwa.gulumusic.ui.main

import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.CheckBox
import android.widget.TextView

import com.google.gson.Gson

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.ui.adapter.PlayListAdapter
import cn.guluwa.gulumusic.base.BaseActivity
import cn.guluwa.gulumusic.data.bean.*
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource
import cn.guluwa.gulumusic.databinding.ActivityMainBinding
import cn.guluwa.gulumusic.ui.view.dialog.SongMoreOperationDialog
import cn.guluwa.gulumusic.utils.listener.OnClickListener
import cn.guluwa.gulumusic.utils.listener.OnLongClickListener
import cn.guluwa.gulumusic.utils.listener.OnSelectListener
import cn.guluwa.gulumusic.utils.listener.OnSongStatusListener
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.manage.Contacts
import cn.guluwa.gulumusic.ui.play.PlayActivity
import cn.guluwa.gulumusic.ui.search.SearchActivity
import cn.guluwa.gulumusic.ui.setting.SettingsActivity
import cn.guluwa.gulumusic.utils.AppUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity<T> : BaseActivity() {

    /**
     * ViewBinder
     */
    private lateinit var mMainBinding: ActivityMainBinding

    /**
     * 是否滑动
     */
    private var sIsScrolling: Boolean = false

    /**
     * 是否第一次进入
     */
    private var isFirstComing: Boolean = false

    /**
     * 当前播放的歌曲是否在列表中存在
     */
    private var isSongInList: Boolean = false

    /**
     * 上一次点击时间
     */
    private var mLastClickTime: Long = 0

    /**
     * layout文件id
     */
    override val viewLayoutId: Int get() = R.layout.activity_main

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
        }

        override fun pause() {
            mPlayBtn.isPlaying = -1
        }

        override fun resume() {
            mPlayBtn.isPlaying = 1
        }

        override fun end(tracksBean: TracksBean) {
            println("MainActivity end")
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
            mMainBinding.song = AppManager.getInstance().musicAutoService!!.binder.currentSong!!
        }

        override fun download(position: Int) {
            if (AppManager.getInstance().playStatus == "hot"
                    && position != -1 && (mRecyclerView.adapter as PlayListAdapter).data.size != 0
                    && (mRecyclerView.adapter as PlayListAdapter).data[position] is TracksBean)
                ((mRecyclerView.adapter as PlayListAdapter).data[position] as TracksBean).local = true
        }
    }

    override fun initViews() {
        initDataBinding()
        initData()
        initClickListener()
        initToolBar()
        initDrawerLayout()
        initSwipeRefreshLayout()
        initRecyclerView()
    }

    /**
     * DataBinding类型强转
     */
    private fun initDataBinding() {
        mMainBinding = mViewDataBinding as ActivityMainBinding
        mMainBinding.currentSongShow = true
    }

    /**
     * 数据初始化
     */
    private fun initData() {
        isFirstComing = true
        mPlayBtn.isPlaying = -1
        tvCurrentSongName.text = "请选择播放歌曲"
        tvCurrentSongProgress.text = ""
    }

    /**
     * 点击事件初始化
     */
    private fun initClickListener() {
        mMainBinding.setClickListener { view ->
            when (view.id) {
                R.id.mBottomPlayInfo -> {
                    val intent = Intent(this@MainActivity, PlayActivity::class.java)
                    intent.putExtra("from", "main")
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, Pair(ivCurrentSongPic, "songImage"))
                    ActivityCompat.startActivityForResult(this, intent, Contacts.REQUEST_CODE_PLAY, options.toBundle())
                }
                R.id.mPlayBtn -> if (AppManager.getInstance().musicAutoService != null && AppManager.getInstance().musicAutoService!!.binder.mediaPlayer != null) {
                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                        mPlayBtn.isPlaying = -1
                    } else {
                        mPlayBtn.isPlaying = 1
                    }
                    playSong()
                }
                R.id.flNetCloudSongs -> {
                    mDrawerLayout.closeDrawer(Gravity.START)
                    if ("hot" != AppManager.getInstance().playStatus) {
                        getSongListData("hot")
                    }
                }
                R.id.flLocalSongs -> {
                    mDrawerLayout.closeDrawer(Gravity.START)
                    if ("local" != AppManager.getInstance().playStatus) {
                        getSongListData("local")
                    }
                }
                R.id.flAppSetting -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    mDrawerLayout.closeDrawer(Gravity.START)
                }
                R.id.fabLocationMusic -> {
                    val index = AppUtils.locationCurrentSongShow(AppManager.getInstance().musicAutoService!!.binder.currentSong, (mRecyclerView.adapter as PlayListAdapter).data)
                    mRecyclerView.smoothScrollToPosition(index)
                }
                R.id.mToolBar -> {
                    if (System.currentTimeMillis() - mLastClickTime > 2000) {
                        showSnackBar("再按一次返回顶部")
                        mLastClickTime = System.currentTimeMillis()
                    } else {
                        mRecyclerView.smoothScrollToPosition(0)
                    }
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
        window.statusBarColor = AppUtils.deepenColor(Color.rgb(85, 160, 122))
    }

    /**
     * 侧边栏初始化
     */
    private fun initDrawerLayout() {
        //创建返回键，并实现打开关/闭监听
        val mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout, mToolBar,
                R.string.drawer_open, R.string.drawer_close) {}
        mDrawerToggle.syncState()
        mDrawerLayout.setDrawerListener(mDrawerToggle)
    }

    /**
     * 下拉刷新初始化
     */
    private fun initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeColors(
                resources.getColor(R.color.yellow),
                resources.getColor(R.color.green))
        mSwipeRefreshLayout.setOnRefreshListener { getSongListData(AppManager.getInstance().playStatus) }
    }

    /**
     * 列表初始化
     */
    private fun initRecyclerView() {
        val mAdapter = PlayListAdapter(object : OnClickListener {
            override fun click(arg1: Int, arg2: Any) {
                when (arg1) {
                    1 -> {
                        if (arg2 is TracksBean) {
                            if (AppManager.getInstance().musicAutoService!!.binder.currentSong != null) {
                                if (arg2.id == AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id &&
                                        mPlayBtn.isPlaying == 1) {
                                    return
                                }
                            }
                            if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                                AppManager.getInstance().musicAutoService!!.binder.stop()
                            }
                            playNewSong(arg2)
                        }
                    }
                    2 -> {
                        showSongMoreOperation(arg2 is LocalSongBean, arg2)
                    }
                }
            }
        }, object : OnLongClickListener {
            override fun click(song: LocalSongBean) {
                showDeleteDialog(song)
            }
        })
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    sIsScrolling = true
//                    Glide.with(this@MainActivity).pauseRequests()
                    if (disposable != null && !disposable!!.isDisposed) {
                        disposable!!.dispose()
                        disposable = null
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (sIsScrolling) {
//                        Glide.with(this@MainActivity).resumeRequests()
                        if (isSongInList) {
                            mMainBinding.currentSongShow = true
                            startCountDown()
                        }
                    }
                }
            }
        })
    }

    /**
     * 开始倒计时
     */
    private fun startCountDown() {
        disposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .take(3).subscribe({ if (it == 2L) mMainBinding.currentSongShow = false })
    }

    var disposable: Disposable? = null

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
                        showDeleteDialog(song as LocalSongBean)
                    } else {
                        AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(song as TracksBean, true)
                    }
                    1 -> {
                        val intent = Intent(this@MainActivity, SearchActivity::class.java)
                        intent.putExtra("keyWord", (song as BaseSongBean).singer!!.name)
                        startActivity(intent)
                    }
                }
            }
        }, arrayListOf(if (isLocal) "删除歌曲" else "缓存歌曲", "查看歌手"))
        dialog!!.show()
    }

    /**
     * 删除本地歌曲确认对话框
     */
    private fun showDeleteDialog(song: LocalSongBean) {
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
    private fun deleteLocalSong(isChecked: Boolean, song: LocalSongBean) {
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
                    if (AppManager.getInstance().playStatus == "local") {
                        (mRecyclerView.adapter as PlayListAdapter).removeSong(song.position)
                        if (AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id == song.id &&
                                AppManager.getInstance().musicAutoService!!.binder.currentSong!!.name == song.name) {
                            AppManager.getInstance().musicAutoService!!.binder.currentSong = if ((mRecyclerView.adapter as PlayListAdapter).data[song.position - 1] is TracksBean) {
                                (mRecyclerView.adapter as PlayListAdapter).data[song.position - 1] as TracksBean
                            } else {
                                AppUtils.getSongBean((mRecyclerView.adapter as PlayListAdapter).data[song.position - 1] as LocalSongBean)
                            }
                            AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(null, false)
                        }
                    } else
                        ((mRecyclerView.adapter as PlayListAdapter).data[song.position] as TracksBean).local = false
                }) { showSnackBar("删除失败") }
    }

    /**
     * Service 初始化、数据
     */
    override fun initService() {
        getSongListData(AppManager.getInstance().playStatus)
        AppManager.getInstance().musicAutoService!!.binder.bindSongStatusListener(listener)
        val mCurrentSong = AppManager.getInstance().musicAutoService!!.binder.currentSong
        if (mCurrentSong != null &&
                "" != AppUtils.isExistFile(String.format("%s_%s.mp3", mCurrentSong.name, mCurrentSong.id), 1)) {
            mMainBinding.song = mCurrentSong
            tvCurrentSongProgress.text = AppUtils.formatTime(mCurrentSong.currentTime)
        }
    }

    /**
     * viewModel初始化
     */
    override fun initViewModel() {
        //查询热门歌曲
        mViewModel.queryNetCloudHotSong()!!.observe(this, Observer { data ->
            if (data == null) {
                showSnackBar("数据出错啦")
                mSwipeRefreshLayout.isRefreshing = false
                return@Observer
            }
            when (data.status) {
                PageStatus.Loading -> mSwipeRefreshLayout.isRefreshing = true
                PageStatus.Error -> {
                    mViewModel.refreshHot(false, isFirstComing)
                    showSnackBar(data.throwable!!.message!!)
                    mSwipeRefreshLayout.isRefreshing = false
                }
                PageStatus.Empty -> {
                    mViewModel.refreshHot(false, isFirstComing)
                    showSnackBar("还没有热门歌曲哦")
                    mSwipeRefreshLayout.isRefreshing = false
                }
                PageStatus.Content -> {
                    AppManager.getInstance().playStatus = "hot"
                    showSnackBar("热门")
                    isFirstComing = false
                    mViewModel.refreshHot(false, false)
                    mSwipeRefreshLayout.isRefreshing = false
                    setData(data.data as ArrayList<BaseSongBean>)
                }
            }
        })
        //查询本地歌曲
        mViewModel.queryLocalSong()!!.observe(this, Observer { listViewDataBean ->
            if (listViewDataBean == null) {
                showSnackBar("数据出错啦")
                mSwipeRefreshLayout.isRefreshing = false
                return@Observer
            }
            when (listViewDataBean.status) {
                PageStatus.Loading -> mSwipeRefreshLayout.isRefreshing = true
                PageStatus.Error -> {
                    mViewModel.refreshLocal(false)
                    showSnackBar(listViewDataBean.throwable!!.message!!)
                    mSwipeRefreshLayout.isRefreshing = false
                }
                PageStatus.Empty -> {
                    mViewModel.refreshLocal(false)
                    showSnackBar("还没有本地歌曲哦")
                    mSwipeRefreshLayout.isRefreshing = false
                }
                PageStatus.Content -> {
                    AppManager.getInstance().playStatus = "local"
                    mViewModel.refreshLocal(false)
                    mSwipeRefreshLayout.isRefreshing = false
                    setData(listViewDataBean.data as ArrayList<BaseSongBean>)
                }
            }
        })
    }

    /**
     * 设置列表数据、传到service、当前播放歌曲为空，设置为列表第一首
     *
     * @param data
     */
    private fun setData(data: ArrayList<BaseSongBean>) {
        //填充列表数据
        (mRecyclerView.adapter as PlayListAdapter).data = data
        //列表滚动到顶部
        mRecyclerView.smoothScrollToPosition(0)
        //更新service数据
        AppManager.getInstance().musicAutoService!!.binder.setSongList(data)
        if (AppManager.getInstance().musicAutoService!!.binder.currentSong == null) {
            AppManager.getInstance().musicAutoService!!.binder.currentSong = if (data[0] is TracksBean) {
                data[0] as TracksBean
            } else {
                AppUtils.getSongBean(data[0] as LocalSongBean)
            }
            reFreshLayout()
            mMainBinding.mPlayBtn.isPlaying = -1
            //todo 合并
        }
        isSongInList = AppUtils.locationCurrentSongShow(AppManager.getInstance().musicAutoService!!.binder.currentSong, data) != -1
        mMainBinding.currentSongShow = isSongInList
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
     *
     * @param
     */
    private fun reFreshLayout() {
        mMainBinding.song = AppManager.getInstance().musicAutoService!!.binder.currentSong!!
        tvCurrentSongProgress.text = getString(R.string.song_start_time)
    }


    /**
     * 刷新列表数据
     */
    private fun getSongListData(type: String) {
        if ("hot" == type) {
            mViewModel.refreshHot(true, isFirstComing)
        } else {
            mViewModel.refreshLocal(true)
        }
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
        } else if (requestCode == Contacts.REQUEST_CODE_SEARCH && resultCode == Contacts.RESULT_SONG_CODE) {
            mPlayBtn.isPlaying = if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) 1 else -1
            if (AppManager.getInstance().isDownLoadSong && "local" == AppManager.getInstance().playStatus) {
                mViewModel.refreshLocal(true)
            }
            if (AppManager.getInstance().isChangeSong || AppManager.getInstance().isDownLoadSong) {
                reFreshLayout()
                AppManager.getInstance().isChangeSong = false
                AppManager.getInstance().isDownLoadSong = false
            }
        }
    }

    override fun onResume() {
        if (AppManager.getInstance().isDownLoadSong && "local" == AppManager.getInstance().playStatus) {
            mViewModel.refreshLocal(true)
        }
        if (AppManager.getInstance().isChangeSong || AppManager.getInstance().isDownLoadSong) {
            reFreshLayout()
            AppManager.getInstance().isChangeSong = false
            AppManager.getInstance().isDownLoadSong = false
        }
        AppManager.getInstance().isChangeSong = false
        AppManager.getInstance().isDownLoadSong = false
        super.onResume()
    }

    override fun onStop() {
        println("stop")
        super.onStop()
    }

    override fun onDestroy() {
        if (AppManager.getInstance().musicAutoService != null) {
            AppManager.getInstance().musicAutoService!!.binder.unBindSongStatusListener(listener)
            AppManager.getInstance().musicAutoService!!.quit()
            println("onDestroy")
        }
        if (disposable != null && disposable!!.isDisposed) {
            disposable!!.dispose()
            disposable = null
        }
        super.onDestroy()
    }

    //  获取并设置返回键的点击事件
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 菜单
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_search -> {
                val intent = Intent(this@MainActivity, SearchActivity::class.java)
                startActivityForResult(intent, Contacts.REQUEST_CODE_SEARCH)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
