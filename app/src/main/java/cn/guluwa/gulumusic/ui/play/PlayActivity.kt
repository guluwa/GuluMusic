package cn.guluwa.gulumusic.ui.play

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.base.BaseActivity
import cn.guluwa.gulumusic.data.bean.LrcBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.databinding.ActivityPlayBinding
import cn.guluwa.gulumusic.utils.listener.OnSongStatusListener
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.manage.Contacts
import cn.guluwa.gulumusic.manage.MyApplication
import cn.guluwa.gulumusic.ui.list.PlaySongListFragment
import cn.guluwa.gulumusic.ui.main.MainActivity
import cn.guluwa.gulumusic.ui.search.SearchActivity
import cn.guluwa.gulumusic.utils.AppUtils
import cn.guluwa.gulumusic.utils.LrcParser
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_play.*

class PlayActivity : BaseActivity() {

    /**
     * ViewBinder
     */
    private lateinit var mPlayBinding: ActivityPlayBinding

    /**
     * 歌词list
     */
    private var mLrcList: List<LrcBean>? = null

    /**
     * 歌曲当前位置
     */
    private var mLrcPosition: Int = 0

    /**
     * 歌曲播放状态
     */
    private var mPlayStatus: Int = 0

    /**
     * layout文件id
     */
    override val viewLayoutId: Int get() = R.layout.activity_play

    /**
     * 歌曲播放进度
     */
    private val listener = object : OnSongStatusListener {

        override fun loading() {
            mPlayBtn.isPlaying = 0
        }

        override fun start() {
            mLrcPosition = -1
            mLrcList = null
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
            println("PlayActivity end")
        }

        override fun error(msg: String) {
            showSnackBar(msg)
        }

        override fun progress(progress: Int, duration: Int) {
            if (mLrcList != null) {
                if (mLrcPosition == -1) {//说明是第一次
                    for (i in mLrcList!!.indices) {
                        if (mLrcList!![i].time > progress) {
                            mLrcPosition = if (i != 0) {
                                i - 1
                            } else {
                                0
                            }
                            if (mPlayBtn.isPlaying != 0) {
                                tvSongWord.text = mLrcList!![mLrcPosition].word
                            }
                            break
                        }
                    }
                } else {
                    if (mLrcList!!.size > mLrcPosition + 1) {
                        if (mLrcList!![mLrcPosition + 1].time < progress) {
                            mLrcPosition++
                            if (mLrcList!!.size > mLrcPosition) {
                                if (mPlayBtn.isPlaying != 0) {
                                    tvSongWord.text = mLrcList!![mLrcPosition].word
                                    println(progress.toString() + ";" + mLrcList!![mLrcPosition].word)
                                }
                            }
                        }
                    }
                }
            }
            if (mPlayBtn.isPlaying != 0) {
                mProgressView.setSongPlayLength(progress, duration)
            }
        }

        override fun pic(url: String) {
            mPlayBinding.song = AppManager.getInstance().musicAutoService!!.binder.currentSong!!
        }

        override fun download(position: Int) {

        }
    }

    override fun initViews() {
        initDataBinding()
        initStatusBar()
        initClickListener()
    }

    /**
     * DataBinding类型强转
     */
    private fun initDataBinding() {
        mPlayBinding = mViewDataBinding as ActivityPlayBinding
        initData()
    }

    /**
     * 数据初始化
     */
    private fun initData() {
        mLrcPosition = -1
        mPlayBtn.isPlaying = if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) 1 else -1
        mProgressView.setSongPlayLength(AppManager.getInstance().musicAutoService!!.binder.currentSong!!.currentTime,
                AppManager.getInstance().musicAutoService!!.binder.currentSong!!.duration)
        mPlayBinding.song = AppManager.getInstance().musicAutoService!!.binder.currentSong!!
        showPlayModeImg(AppManager.getInstance().playMode)
        initSongLrc(mPlayBinding.song)
        initSongPic(mPlayBinding.song)
    }

    /**
     * 歌词处理
     */
    private fun initSongLrc(tracksBean: TracksBean?) {
        if (mLrcList == null) {
            try {
                mLrcList = LrcParser.parserLocal(String.format("%s_%s.txt", tracksBean!!.name, tracksBean.id))
                val time = if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying)
                    AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.currentPosition
                else
                    AppManager.getInstance().musicAutoService!!.binder.currentSong!!.currentTime
                for (i in mLrcList!!.indices) {
                    if (mLrcList!![i].time > time) {
                        mLrcPosition = if (i != 0) {
                            i - 1
                        } else {
                            0
                        }
                        if (mPlayBtn.isPlaying != 0) {
                            tvSongWord.text = mLrcList!![mLrcPosition].word
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                mLrcList = null
                tvSongWord.text = "暂无歌词"
                e.printStackTrace()
            }
        }
    }

    /**
     * 图片初始化
     */
    private fun initSongPic(tracksBean: TracksBean?) {
        Glide.with(MyApplication.getContext()).asBitmap().apply(RequestOptions().centerCrop())
                .load(tracksBean!!.al!!.picUrl)
                .apply(RequestOptions().transform(BlurTransformation(25)).override(100, 100))
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                        Glide.with(this@PlayActivity).asBitmap()
                                .apply(RequestOptions().transform(BlurTransformation(25)).override(100, 100))
                                .load(R.mipmap.ic_launcher)
                                .into(ivBackGround)
                        return true
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        if (resource != null) {
                            ivBackGround.setImageBitmap(resource)
                            return true
                        }
                        return false
                    }
                })
                .into(ivBackGround)
    }

    /**
     * 状态栏初始化
     */
    private fun initStatusBar() {
        //5.0以上状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//禁止横屏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    /**
     * 点击事件初始化
     */
    private fun initClickListener() {
        mPlayBinding.setClickListener { view ->
            when (view.id) {
                R.id.mPlayBtn -> if (AppManager.getInstance().musicAutoService != null &&
                        AppManager.getInstance().musicAutoService!!.binder.mediaPlayer != null) {
                    if (AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.isPlaying) {
                        mPlayBtn.isPlaying = -1
                    } else {
                        mPlayBtn.isPlaying = 1
                    }
                    playSong()
                }
                R.id.ivDownBack -> onBackPressed()
                R.id.ivPlayMode -> {
                    val mode = AppUtils.getPlayMode(AppManager.getInstance().playMode)
                    AppManager.getInstance().playMode = mode
                    showPlayModeImg(mode)
                }
                R.id.ivPlayMenu -> PlaySongListFragment().show(supportFragmentManager, "dialog")
                R.id.mLastSongBtn -> {
                    AppManager.getInstance().musicAutoService!!.binder.stop()
                    AppManager.getInstance().musicAutoService!!.binder.getLastSong()
                    AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(null, false)
                }
                R.id.mNextSongBtn -> {
                    AppManager.getInstance().musicAutoService!!.binder.stop()
                    AppManager.getInstance().musicAutoService!!.binder.getNextSong()
                    AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(null, false)
                }
            }
        }
    }

    override fun initViewModel() {

    }

    /**
     * Service 初始化、数据
     */
    override fun initService() {
        AppManager.getInstance().musicAutoService!!.binder.bindSongStatusListener(listener)
    }

    /**
     * 切换播放模式
     *
     * @param mode
     */
    private fun showPlayModeImg(mode: Int) {
        when (mode) {
            0 -> ivPlayMode.setImageResource(R.drawable.ic_single_circle)
            1 -> ivPlayMode.setImageResource(R.drawable.ic_list_circle)
            else -> ivPlayMode.setImageResource(R.drawable.ic_list_random)
        }
    }

    /**
     * 更新页面
     */
    private fun reFreshLayout() {
        mPlayBinding.song = AppManager.getInstance().musicAutoService!!.binder.currentSong!!
        tvSongWord.text = ""
        mProgressView.setSongPlayLength(0, 0)
        initSongPic(mPlayBinding.song)
        initSongLrc(mPlayBinding.song)
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
     *
     * @param
     */
    private fun playSong() {
        if (AppManager.getInstance().musicAutoService!!.binder.isPrepare) {
            AppManager.getInstance().musicAutoService!!.binder.playOrPauseSong()
        } else {
            AppManager.getInstance().musicAutoService!!.binder.playCurrentSong(null, false)
        }
    }

    override fun onBackPressed() {
        val i: Intent = if (intent.getStringExtra("from") == "main")
            Intent(this, MainActivity::class.java)
        else
            Intent(this, SearchActivity::class.java)
        setResult(Contacts.RESULT_SONG_CODE, i)
        super.onBackPressed()
    }

    override fun onDestroy() {
        AppManager.getInstance().musicAutoService!!.binder.unBindSongStatusListener(listener)
        super.onDestroy()
    }
}
