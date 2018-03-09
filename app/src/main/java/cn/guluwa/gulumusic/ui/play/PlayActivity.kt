package cn.guluwa.gulumusic.ui.play

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
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
import cn.guluwa.gulumusic.utils.listener.OnActionListener
import cn.guluwa.gulumusic.utils.listener.OnSongStatusListener
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.manage.Contacts
import cn.guluwa.gulumusic.manage.MyApplication
import cn.guluwa.gulumusic.ui.main.MainActivity
import cn.guluwa.gulumusic.utils.AppUtils
import cn.guluwa.gulumusic.utils.LrcParser
import jp.wasabeef.glide.transformations.BlurTransformation

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
     * 页面是否来自恢复
     */
    private var mActivityFromRestore: Boolean = false

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
            mPlayBinding.mPlayBtn.isPlaying = 0
        }

        override fun start() {
            mPlayBinding.mPlayBtn.isPlaying = 1
            if (mCurrentSong!!.id != AppManager.getInstance().musicAutoService!!.binder.currentSong!!.id) {
                reFreshLayout(AppManager.getInstance().musicAutoService!!.binder.currentSong!!)
            }
            initSongLrc()
        }

        override fun pause() {
            mPlayBinding.mPlayBtn.isPlaying = -1
        }

        override fun end(tracksBean: TracksBean) {
            playCurrentSong(tracksBean)
            mLrcPosition = -1
            mLrcList = null
        }

        override fun error(msg: String) {
            showSnackBar(msg)
            mLrcPosition = -1
            mLrcList = null
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
                            if (mPlayBinding.mPlayBtn.isPlaying != 0) {
                                mPlayBinding.tvSongWord.text = mLrcList!![mLrcPosition].word
                            }
                            break
                        }
                    }
                } else {
                    if (mLrcList!!.size > mLrcPosition + 1) {
                        if (mLrcList!![mLrcPosition + 1].time < progress) {
                            mLrcPosition++
                            if (mLrcList!!.size > mLrcPosition) {
                                if (mPlayBinding.mPlayBtn.isPlaying != 0) {
                                    mPlayBinding.tvSongWord.text = mLrcList!![mLrcPosition].word
                                    println(progress.toString() + ";" + mLrcList!![mLrcPosition].word)
                                }
                            }
                        }
                    }
                }
            }
            if (mPlayBinding.mPlayBtn.isPlaying != 0) {
                mPlayBinding.mProgressView.setSongPlayLength(progress, duration)
            }
        }

        override fun pic(url: String) {
            mCurrentSong!!.al!!.picUrl = url
            mPlayBinding.song = mCurrentSong
        }

        override fun download(position: Int) {
        }
    }

    override fun initViews() {
        initDataBinding()
        initData()
        initSongPic()
        initStatusBar()
        initClickListener()
    }

    /**
     * DataBinding类型强转
     */
    private fun initDataBinding() {
        mPlayBinding = mViewDataBinding as ActivityPlayBinding
    }

    /**
     * 数据初始化
     */
    private fun initData() {
        mLrcPosition = -1
        mCurrentSong = intent.getSerializableExtra("song") as TracksBean
        mPlayBinding.mPlayBtn.isPlaying = intent.getIntExtra("status", -1)
        mPlayBinding.mProgressView.setSongPlayLength(mCurrentSong!!.currentTime, mCurrentSong!!.duration)
        mPlayBinding.song = mCurrentSong
        showPlayModeImg(AppManager.getInstance().playMode)
        initSongLrc()
    }

    /**
     * 歌词处理
     */
    private fun initSongLrc() {
        if (mLrcList == null) {
            try {
                println(mCurrentSong!!.name)
                mLrcList = LrcParser.parserLocal(String.format("%s_%s.txt", mCurrentSong!!.name, mCurrentSong!!.id))
                for (i in mLrcList!!.indices) {
                    if (mLrcList!![i].time > mCurrentSong!!.currentTime) {
                        mLrcPosition = if (i != 0) {
                            i - 1
                        } else {
                            0
                        }
                        if (mPlayBinding.mPlayBtn.isPlaying != 0) {
                            mPlayBinding.tvSongWord.text = mLrcList!![mLrcPosition].word
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                mLrcList = null
                mPlayBinding.tvSongWord.text = "暂无歌词"
                e.printStackTrace()
            }
        }
    }

    /**
     * 图片初始化
     */
    private fun initSongPic() {
        Glide.with(MyApplication.getContext()).asBitmap().apply(RequestOptions().centerCrop())
                .load(mCurrentSong!!.al!!.picUrl)
                .apply(RequestOptions().transform(BlurTransformation(25)).override(100, 100))
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                        Glide.with(this@PlayActivity).asBitmap()
                                .apply(RequestOptions().transform(BlurTransformation(25)).override(100, 100))
                                .load(R.mipmap.ic_launcher)
                                .into(mPlayBinding.ivBackGround)
                        return true
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        if (resource != null) {
                            mPlayBinding.ivBackGround.setImageBitmap(resource)
                            return true
                        }
                        return false
                    }
                })
                .into(mPlayBinding.ivBackGround)
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
                        mPlayBinding.mPlayBtn.isPlaying = -1
                    } else {
                        mPlayBinding.mPlayBtn.isPlaying = 1
                    }
                    playCurrentSong(mCurrentSong!!.currentTime)
                }
                R.id.ivDownBack -> onBackPressed()
                R.id.ivPlayMode -> {
                    val mode = AppUtils.getPlayMode(AppManager.getInstance().playMode)
                    AppManager.getInstance().playMode = mode
                    showPlayModeImg(mode)
                }
                R.id.ivPlayMenu -> showSnackBar("菜单")
                R.id.mLastSongBtn -> {
                    AppManager.getInstance().musicAutoService!!.binder.stop()
                    AppManager.getInstance().musicAutoService!!.binder.getLastSong(mCurrentSong!!)
                    playCurrentSong(AppManager.getInstance().musicAutoService!!.binder.currentSong as TracksBean)
                }
                R.id.mNextSongBtn -> {
                    AppManager.getInstance().musicAutoService!!.binder.stop()
                    AppManager.getInstance().musicAutoService!!.binder.getNextSong(mCurrentSong!!)
                    playCurrentSong(AppManager.getInstance().musicAutoService!!.binder.currentSong as TracksBean)
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
        if (mActivityFromRestore && mPlayStatus == 1) {
            showSnackBarWithAction("播放被系统暂停，是否恢复播放", "是", object : OnActionListener {
                override fun action() {
                    playCurrentSong(mCurrentSong!!.currentTime)
                }
            })
        }
    }

    /**
     * 切换播放模式
     *
     * @param mode
     */
    private fun showPlayModeImg(mode: Int) {
        when (mode) {
            0 -> mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_single_circle)
            1 -> mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_list_circle)
            else -> mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_list_random)
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
     * 更新页面
     *
     * @param song
     */
    private fun reFreshLayout(song: TracksBean) {
        mCurrentSong = song
        mPlayBinding.song = mCurrentSong
        mPlayBinding.tvSongWord.text = ""
        mPlayBinding.mProgressView.setSongPlayLength(0, 0)
        initSongPic()
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

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("status", mPlayBinding.mPlayBtn.isPlaying)
        intent.putExtra("song", mCurrentSong)
        setResult(Contacts.RESULT_SONG_CODE, intent)
        super.onBackPressed()
    }

    override fun onDestroy() {
        AppManager.getInstance().musicAutoService!!.binder.unBindSongStatusListener(listener)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState!!.putInt("status", mPlayBinding.mPlayBtn.isPlaying)
        mCurrentSong!!.currentTime = AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.currentPosition
        println(AppUtils.formatTime(AppManager.getInstance().musicAutoService!!.binder.mediaPlayer!!.currentPosition))
        outState.putSerializable("song", mCurrentSong)
    }

    override fun onRestoreInstanceState(outState: Bundle?) {
        super.onRestoreInstanceState(outState)

        mActivityFromRestore = true
        mLrcPosition = -1
        mPlayStatus = outState!!.getInt("status")
        mPlayBinding.mPlayBtn.isPlaying = -1
        mCurrentSong = outState.getSerializable("song") as TracksBean?
        mPlayBinding.mProgressView.setSongPlayLength(mCurrentSong!!.currentTime, mCurrentSong!!.duration)
        mPlayBinding.song = mCurrentSong
        showPlayModeImg(AppManager.getInstance().playMode)
        initSongPic()
        initSongLrc()
    }
}
