package cn.guluwa.gulumusic.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

import java.sql.Time
import java.text.SimpleDateFormat

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.utils.AppUtils

/**
 * Created by guluwa on 2018/1/19.
 */

class ProgressView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    /**
     * 上部进度条画笔
     */
    private var mTopProgressPaint: Paint? = null

    /**
     * 底部进度条画笔
     */
    private var mBtmProgressPaint: Paint? = null

    /**
     * 文字画笔
     */
    private var mTextPaint: Paint? = null

    /**
     * 上部进度条颜色
     */
    private var mProgressColor: Int = 0

    /**
     * 底部进度条颜色
     */
    private var mIndicatorColor: Int = 0

    /**
     * view宽度
     */
    private var mViewWidth: Int = 0

    /**
     * view高度
     */
    private var mViewHeight: Int = 0

    /**
     * 歌曲播放长度
     */
    private var mSongPlayLength: String? = null

    /**
     * 歌曲总长度
     */
    private var mSongTotalLength: String? = null

    /**
     * 字体大小
     */
    private var mTextSize: Int = 0

    /**
     * 文字中间偏移
     */
    private var yOffset: Float = 0.toFloat()

    /**
     * 文字长度
     */
    private var mTextWidth: Int = 0

    /**
     * 进度圆形指示器小半径
     */
    private var mIndicatorCircleSmallRadius: Int = 0

    /**
     * 进度圆形指示器大半径
     */
    private var mIndicatorCircleBigRadius: Int = 0

    /**
     * 进度
     */
    private var mProgress: Float = 0.toFloat()

    init {
        initPaint()
    }

    private fun initPaint() {
        mProgressColor = resources.getColor(R.color.play_view_black)
        mIndicatorColor = resources.getColor(R.color.play_view_gray)
        mViewHeight = AppUtils.dp2px(context, 16f)
        mTextSize = AppUtils.sp2px(context, 12f)
        mSongPlayLength = "00:00"
        mSongTotalLength = "00:00"
        mProgress = 0f


        mTopProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTopProgressPaint!!.strokeWidth = AppUtils.dp2px(context, 2f).toFloat()
        mTopProgressPaint!!.style = Paint.Style.FILL_AND_STROKE
        mTopProgressPaint!!.color = mProgressColor
        mTopProgressPaint!!.strokeJoin = Paint.Join.ROUND
        mTopProgressPaint!!.strokeCap = Paint.Cap.ROUND

        mBtmProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBtmProgressPaint!!.strokeWidth = AppUtils.dp2px(context, 2f).toFloat()
        mBtmProgressPaint!!.style = Paint.Style.STROKE
        mBtmProgressPaint!!.color = mIndicatorColor
        mBtmProgressPaint!!.strokeJoin = Paint.Join.ROUND
        mBtmProgressPaint!!.strokeCap = Paint.Cap.ROUND

        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint!!.color = mProgressColor
        mTextPaint!!.textSize = mTextSize.toFloat()
        mTextPaint!!.typeface = Typeface.SERIF

        val fontMetrics = mTextPaint!!.fontMetrics
        yOffset = -(fontMetrics.ascent + fontMetrics.descent) / 2
        mTextWidth = mTextSize * mSongTotalLength!!.length / 2 + AppUtils.dp2px(context, 10f)
        mIndicatorCircleSmallRadius = AppUtils.dp2px(context, 2f)
        mIndicatorCircleBigRadius = AppUtils.dp2px(context, 4f)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        mViewWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(mViewWidth, mViewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawLine(
                mTextWidth.toFloat(),
                (mViewHeight / 2).toFloat(),
                (mViewWidth - mTextWidth).toFloat(),
                (mViewHeight / 2).toFloat(),
                mBtmProgressPaint!!)
        canvas.drawLine(
                mTextWidth.toFloat(),
                (mViewHeight / 2).toFloat(),
                mTextWidth + (mViewWidth - 2 * mTextWidth) * mProgress - mIndicatorCircleBigRadius * 2,
                (mViewHeight / 2).toFloat(),
                mTopProgressPaint!!)
        canvas.drawCircle(mTextWidth + (mViewWidth - 2 * mTextWidth) * mProgress - mIndicatorCircleSmallRadius * 2,
                (mViewHeight / 2).toFloat(),
                mIndicatorCircleBigRadius.toFloat(),
                mTopProgressPaint!!)
        canvas.drawText(mSongPlayLength!!, 0f, mViewHeight / 2 + yOffset, mTextPaint!!)
        canvas.drawText(mSongTotalLength!!, (mViewWidth - mTextSize * mSongTotalLength!!.length / 2).toFloat(), mViewHeight / 2 + yOffset, mTextPaint!!)
    }

    private fun setProgress(mProgress: Float) {
        this.mProgress = mProgress
    }

    fun setSongPlayLength(playMillisecond: Int, totalMillisecond: Int) {
        this.mSongPlayLength = AppUtils.formatTime(playMillisecond)
        this.mSongTotalLength = AppUtils.formatTime(totalMillisecond)
        setProgress((playMillisecond * 1.0 / totalMillisecond).toFloat())
        invalidate()
    }
}
