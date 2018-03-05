package cn.guluwa.gulumusic.ui.play

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

import java.io.File
import java.util.concurrent.TimeUnit

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.base.BaseActivity
import cn.guluwa.gulumusic.data.bean.LrcBean
import cn.guluwa.gulumusic.data.bean.TracksBean
import cn.guluwa.gulumusic.databinding.ActivityPlayBinding
import cn.guluwa.gulumusic.listener.OnResultListener
import cn.guluwa.gulumusic.listener.OnSongStatusListener
import cn.guluwa.gulumusic.manage.AppManager
import cn.guluwa.gulumusic.manage.Contacts
import cn.guluwa.gulumusic.manage.MyApplication
import cn.guluwa.gulumusic.ui.main.MainActivity
import cn.guluwa.gulumusic.utils.AppUtils
import cn.guluwa.gulumusic.utils.LrcParser
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import jp.wasabeef.glide.transformations.BlurTransformation

class PlayActivity : BaseActivity() {

    /**
     * 当前播放歌曲
     */
    private var mCurrentSong: TracksBean? = null

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

    override val viewLayoutId: Int
        get() = R.layout.activity_play

    /**
     * 歌曲播放进度
     */
    private val listener = object : OnSongStatusListener {

        override fun loading() {
            mPlayBinding.mPlayBtn.isPlaying = 0
        }

        override fun start() {
            mPlayBinding.mPlayBtn.isPlaying = 1
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
                            if (i != 0) {
                                mLrcPosition = i - 1
                            } else {
                                mLrcPosition = 0
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
    }

    override fun initViews() {
        mPlayBinding = mViewDataBinding as ActivityPlayBinding
        initData()
        initSongPic()
        initStatusBar()
        initClickListener()
    }

    override fun initViewModel() {

    }

    /**
     * 点击事件初始化
     */
    private fun initClickListener() {
        mPlayBinding.setClickListener { view ->
            when (view.id) {
                R.id.mPlayBtn -> if (AppManager.getInstance().musicAutoService != null && AppManager.getInstance().musicAutoService!!.binder.mediaPlayer != null) {
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
                    playCurrentSong(AppManager.getInstance().musicAutoService!!.binder.getLastSong(mCurrentSong!!))
                }
                R.id.mNextSongBtn -> {
                    AppManager.getInstance().musicAutoService!!.binder.stop()
                    playCurrentSong(AppManager.getInstance().musicAutoService!!.binder.getNextSong(mCurrentSong!!))
                }
            }
        }
    }

    /**
     * 切换播放模式
     *
     * @param mode
     */
    private fun showPlayModeImg(mode: Int) {
        if (mode == 0) {
            mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_single_circle)
        } else if (mode == 1) {
            mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_list_circle)
        } else {
            mPlayBinding.ivPlayMode.setImageResource(R.drawable.ic_list_random)
        }
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
        AppManager.getInstance().musicAutoService!!.binder.bindSongStatusListener(listener)
    }

    /**
     * 歌词处理
     */
    private fun initSongLrc() {
        if (mLrcList == null) {
            try {
                mLrcList = LrcParser.parserLocal(String.format("%s_%s.txt", mCurrentSong!!.name, mCurrentSong!!.id))
                for (i in mLrcList!!.indices) {
                    if (mLrcList!![i].time > mCurrentSong!!.currentTime) {
                        if (i != 0) {
                            mLrcPosition = i - 1
                        } else {
                            mLrcPosition = 0
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
     * 播放歌曲
     *
     * @param song
     */
    private fun playCurrentSong(song: TracksBean) {
        //更新页面
        mCurrentSong = song
        mPlayBinding.song = mCurrentSong
        mPlayBinding.tvSongWord.text = ""
        mPlayBinding.mProgressView.setSongPlayLength(0, 0)
        initSongPic()
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
}