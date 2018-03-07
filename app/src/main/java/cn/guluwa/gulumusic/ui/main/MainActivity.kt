package cn.guluwa.gulumusic.ui.main

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog.show
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.TextView

import com.google.gson.Gson

import java.text.SimpleDateFormat

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.adapter.PlayListAdapter
import cn.guluwa.gulumusic.base.BaseActivity
import cn.guluwa.gulumusic.data.bean.*
import cn.guluwa.gulumusic.data.local.LocalSongsDataSource
import cn.guluwa.gulumusic.databinding.ActivityMainBinding
import cn.guluwa.gulumusic.dialog.SongMoreOperationDialog
import cn.guluwa.gulumusic.listener.OnClickListener
import cn.guluwa.gulumusic.listener.OnLongClickListener
import cn.guluwa.gulumusic.listener.OnSelectListener
import cn.guluwa.gulumusic.listener.OnSongStatusListener
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.manage.Contacts
import cn.guluwa.gulumusic.service.MusicAutoService
import cn.guluwa.gulumusic.service.MusicBinder
import cn.guluwa.gulumusic.ui.play.PlayActivity
import cn.guluwa.gulumusic.ui.search.SearchActivity
import cn.guluwa.gulumusic.ui.setting.SettingsActivity
import cn.guluwa.gulumusic.utils.AppUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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
     * 当前播放歌曲
     */
    private var mCurrentSong: TracksBean? = null

    /**
     * 格式化时间
     */
    private var time: SimpleDateFormat? = null

    /**
     * 是否第一次进入
     */
    private var isFirstComing: Boolean = false

    override val viewLayoutId: Int
        get() = R.layout.activity_main

    /**
     * 回调onServiceConnected 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
     */
    private var serviceConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val mMusicService = (service as MusicBinder).service
            AppManager.getInstance().musicAutoService = mMusicService
            println("MusicAutoService 初始化完成")
            getSongListData(AppManager.getInstance().playStatus)
            //销毁serviceConnection
            unbindService(this)
            serviceConnection = null
            AppManager.getInstance().musicAutoService!!.binder.bindSongStatusListener(listener)
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    /**
     * 歌曲播放进度
     */
    private val listener = object : OnSongStatusListener {
        override fun loading() {
            mMainBinding.mPlayBtn.isPlaying = 0
        }

        override fun start() {
            mMainBinding.mPlayBtn.isPlaying = 1
            if (mCurrentSong!!.id != AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id) {
                reFreshLayout(AppManager.getInstance().musicAutoService!!.binder.currentSong!!)
            }
        }

        override fun pause() {
            mMainBinding.mPlayBtn.isPlaying = -1
        }

        override fun end(tracksBean: TracksBean) {
            playCurrentSong(tracksBean)
        }

        override fun error(msg: String) {
            showSnackBar(msg)
        }

        override fun progress(progress: Int, duration: Int) {
            if (mMainBinding.mPlayBtn.isPlaying != 0) {
                mMainBinding.tvCurrentSongProgress.text = time!!.format(progress)
            }
        }

        override fun pic(url: String) {
            mCurrentSong!!.al!!.picUrl = url
            mMainBinding.song = mCurrentSong
        }

        override fun download(position: Int) {
            if ((mMainBinding.mRecyclerView.adapter as PlayListAdapter).data[position] is TracksBean)
                ((mMainBinding.mRecyclerView.adapter as PlayListAdapter).data[position] as TracksBean).local = true
        }
    }

    override fun initViews() {
        mMainBinding = mViewDataBinding as ActivityMainBinding
        mMainBinding.currentSongShow = true
        initData()
        initClickListener()
        initToolBar()
        initDrawerLayout()
        initSwipeRefreshLayout()
        initRecyclerView()
    }

    /**
     * toolbar初始化
     */
    private fun initToolBar() {
        mMainBinding.mToolBar.setTitle(R.string.app_name)//设置Toolbar标题
        setSupportActionBar(mMainBinding.mToolBar)
        supportActionBar!!.setHomeButtonEnabled(true) //设置返回键可用
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        window.statusBarColor = AppUtils.deepenColor(Color.rgb(85, 160, 122))
    }

    /**
     * 数据初始化
     */
    private fun initData() {
        isFirstComing = true
        mCurrentSong = Gson().fromJson(AppUtils.getString("mCurrentSong", ""), TracksBean::class.java)
        time = SimpleDateFormat("mm:ss")
        if (mCurrentSong != null) {
            val mSongPath = AppUtils.isExistFile(String.format("%s_%s.mp3", mCurrentSong!!.name, mCurrentSong!!.id), 1)
            if ("" != mSongPath) {
                mMainBinding.song = mCurrentSong
                mMainBinding.tvCurrentSongProgress.text = time!!.format(mCurrentSong!!.currentTime)
            }
        }
        mMainBinding.mPlayBtn.isPlaying = -1
    }

    /**
     * 点击事件初始化
     */
    private fun initClickListener() {
        mMainBinding.setClickListener { view ->
            when (view.id) {
                R.id.mBottomPlayInfo -> {
                    val intent = Intent(this@MainActivity, PlayActivity::class.java)
                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                        mCurrentSong!!.currentTime = AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.currentPosition
                    }
                    intent.putExtra("song", mCurrentSong)
                    intent.putExtra("status", mMainBinding.mPlayBtn.isPlaying)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, Pair(mMainBinding.ivCurrentSongPic, "songImage"))
                    ActivityCompat.startActivityForResult(this, intent, Contacts.REQUEST_CODE_PLAY, options.toBundle())
                }
                R.id.mPlayBtn -> if (AppManager.getInstance().musicAutoService != null && AppManager.getInstance().musicAutoService!!.binder.mediaPlayer != null) {
                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                        mMainBinding.mPlayBtn.isPlaying = -1
                    } else {
                        mMainBinding.mPlayBtn.isPlaying = 1
                    }
                    playCurrentSong(mCurrentSong!!.currentTime)
                }
                R.id.flNetCloudSongs -> {
                    mMainBinding.mDrawerLayout.closeDrawer(Gravity.START)
                    if ("hot" != AppManager.getInstance().playStatus) {
                        getSongListData("hot")
                    }
                }
                R.id.flLocalSongs -> {
                    mMainBinding.mDrawerLayout.closeDrawer(Gravity.START)
                    if ("local" != AppManager.getInstance().playStatus) {
                        getSongListData("local")
                    }
                }
                R.id.flAppSetting -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    mMainBinding.mDrawerLayout.closeDrawer(Gravity.START)
                }
                R.id.fabLocationMusic -> {
                    val index = AppUtils.locationCurrentSongShow(mCurrentSong, (mMainBinding.mRecyclerView.adapter as PlayListAdapter).data)
                    mMainBinding.mRecyclerView.smoothScrollToPosition(index)
                }
            }
        }
    }

    /**
     * 侧边栏初始化
     */
    private fun initDrawerLayout() {
        //创建返回键，并实现打开关/闭监听
        val mDrawerToggle = object : ActionBarDrawerToggle(this, mMainBinding.mDrawerLayout, mMainBinding.mToolBar,
                R.string.drawer_open, R.string.drawer_close) {}
        mDrawerToggle.syncState()
        mMainBinding.mDrawerLayout.setDrawerListener(mDrawerToggle)
    }

    /**
     * 下拉刷新初始化
     */
    private fun initSwipeRefreshLayout() {
        mMainBinding.mSwipeRefreshLayout.setColorSchemeColors(
                resources.getColor(R.color.yellow),
                resources.getColor(R.color.green))
        mMainBinding.mSwipeRefreshLayout.setOnRefreshListener { getSongListData(AppManager.getInstance().playStatus) }
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
                            if (mCurrentSong != null) {
                                if (arg2.id == mCurrentSong!!.id && mMainBinding.mPlayBtn.isPlaying == 1) {
                                    return
                                }
                            }
                            if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                                AppManager.getInstance().musicAutoService!!.binder.stop()
                            }
                            playCurrentSong(arg2)
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
        mMainBinding.mRecyclerView.layoutManager = LinearLayoutManager(this)
        mMainBinding.mRecyclerView.adapter = mAdapter
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
                        showDeleteDialog(song as LocalSongBean)
                    } else {
                        AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(song as TracksBean, 0, true)
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
                        (mMainBinding.mRecyclerView.adapter as PlayListAdapter).removeSong(song.position)
                        if (mCurrentSong!!.id == song.id && mCurrentSong!!.name == song.name) {
                            val mNextSong = if ((mMainBinding.mRecyclerView.adapter as PlayListAdapter).data[song.position - 1] is TracksBean) {
                                (mMainBinding.mRecyclerView.adapter as PlayListAdapter).data[song.position - 1] as TracksBean
                            } else {
                                AppUtils.getSongBean((mMainBinding.mRecyclerView.adapter as PlayListAdapter).data[song.position - 1] as LocalSongBean)
                            }
                            AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(mNextSong, 0, false)
                        }
                    } else
                        ((mMainBinding.mRecyclerView.adapter as PlayListAdapter).data[song.position] as TracksBean).local = false
                }) { showSnackBar("删除失败") }
    }

    /**
     * viewModel初始化
     */
    override fun initViewModel() {
        //查询热门歌曲
        mViewModel.queryNetCloudHotSong()!!.observe(this, Observer { data ->
            if (data == null) {
                showSnackBar("数据出错啦")
                mMainBinding.mSwipeRefreshLayout.isRefreshing = false
                return@Observer
            }
            when (data.status) {
                PageStatus.Loading -> mMainBinding.mSwipeRefreshLayout.isRefreshing = true
                PageStatus.Error -> {
                    mViewModel.refreshHot(false, isFirstComing)
                    showSnackBar(data.throwable!!.message!!)
                    mMainBinding.mSwipeRefreshLayout.isRefreshing = false
                }
                PageStatus.Empty -> {
                    mViewModel.refreshHot(false, isFirstComing)
                    showSnackBar("还没有热门歌曲哦")
                    mMainBinding.mSwipeRefreshLayout.isRefreshing = false
                }
                PageStatus.Content -> {
                    AppManager.getInstance().playStatus = "hot"
                    showSnackBar("热门")
                    isFirstComing = false
                    mViewModel.refreshHot(false, false)
                    mMainBinding.mSwipeRefreshLayout.isRefreshing = false
                    setData(data.data as ArrayList<BaseSongBean>)
                }
            }
        })
        //查询本地歌曲
        mViewModel.queryLocalSong()!!.observe(this, Observer { listViewDataBean ->
            if (listViewDataBean == null) {
                showSnackBar("数据出错啦")
                mMainBinding.mSwipeRefreshLayout.isRefreshing = false
                return@Observer
            }
            when (listViewDataBean.status) {
                PageStatus.Loading -> mMainBinding.mSwipeRefreshLayout.isRefreshing = true
                PageStatus.Error -> {
                    mViewModel.refreshLocal(false)
                    showSnackBar(listViewDataBean.throwable!!.message!!)
                    mMainBinding.mSwipeRefreshLayout.isRefreshing = false
                }
                PageStatus.Empty -> {
                    mViewModel.refreshLocal(false)
                    showSnackBar("还没有本地歌曲哦")
                    mMainBinding.mSwipeRefreshLayout.isRefreshing = false
                }
                PageStatus.Content -> {
                    AppManager.getInstance().playStatus = "local"
                    mViewModel.refreshLocal(false)
                    mMainBinding.mSwipeRefreshLayout.isRefreshing = false
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
        (mMainBinding.mRecyclerView.adapter as PlayListAdapter).data = data
        //列表滚动到顶部
        mMainBinding.mRecyclerView.smoothScrollToPosition(0)
        //更新service数据
        AppManager.getInstance().musicAutoService!!.binder.setSongList(data)
        if (mCurrentSong == null) {
            mCurrentSong = if (data[0] is TracksBean) {
                data[0] as TracksBean
            } else {
                AppUtils.getSongBean(data[0] as LocalSongBean)
            }
            mMainBinding.song = mCurrentSong
            mMainBinding.tvCurrentSongProgress.text = "00:00"
            mMainBinding.mPlayBtn.isPlaying = -1
        }
        mMainBinding.currentSongShow = AppUtils.locationCurrentSongShow(mCurrentSong, data) != -1
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
            AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(this.mCurrentSong!!, mCurrentTime, false)
        }
    }

    /**
     * 更新页面
     *
     * @param song
     */
    private fun reFreshLayout(song: TracksBean) {
        mCurrentSong = song
        mMainBinding.song = mCurrentSong
        mMainBinding.tvCurrentSongProgress.text = "00:00"
    }

    /**
     * 在Activity中调用 bindService 保持与 Service 的通信
     */
    override fun bindServiceConnection() {
        val intent = Intent(this@MainActivity, MusicAutoService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
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
            mCurrentSong = data?.getSerializableExtra("song") as TracksBean
            mMainBinding.song = mCurrentSong
            val status = data.getIntExtra("status", -1)
            if (mMainBinding.mPlayBtn.isPlaying != status) {
                mMainBinding.mPlayBtn.isPlaying = status
            }
        } else if (requestCode == Contacts.REQUEST_CODE_SEARCH && resultCode == Contacts.RESULT_SONG_CODE) {
            val isDownLoadSong = data!!.getBooleanExtra("isDownLoadSong", false)
            if (isDownLoadSong && "local" == AppManager.getInstance().playStatus) {
                mViewModel.refreshLocal(true)
            }
            mCurrentSong = data.getSerializableExtra("song") as TracksBean
            mMainBinding.song = mCurrentSong
            val status = data.getIntExtra("status", -1)
            if (mMainBinding.mPlayBtn.isPlaying != status) {
                mMainBinding.mPlayBtn.isPlaying = status
            }
        }
    }

    override fun onStop() {
        if (AppManager.getInstance().musicAutoService != null &&
                AppManager.getInstance().musicAutoService!!.binder.mediaPlayer != null && mCurrentSong != null) {
            if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                mCurrentSong!!.duration = AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.duration
                mCurrentSong!!.currentTime = AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.currentPosition
                AppUtils.setString("mCurrentSong", Gson().toJson(mCurrentSong))
            }
        }
        AppUtils.setInteger(Contacts.PLAY_MODE, AppManager.getInstance().playMode)
        AppUtils.setString(Contacts.PLAY_STATUS, AppManager.getInstance().playStatus)
        super.onStop()
    }

    override fun onDestroy() {
        if (AppManager.getInstance().musicAutoService != null) {
            AppManager.getInstance().musicAutoService!!.binder.unBindSongStatusListener(listener)
            AppManager.getInstance().musicAutoService!!.quit()
            println("onDestroy")
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_search -> {
                val intent = Intent(this@MainActivity, SearchActivity::class.java)
                if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                    mCurrentSong!!.currentTime = AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.currentPosition
                }
                intent.putExtra("song", mCurrentSong)
                intent.putExtra("status", mMainBinding.mPlayBtn.isPlaying)
                startActivityForResult(intent, Contacts.REQUEST_CODE_SEARCH)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
