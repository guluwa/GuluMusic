package cn.guluwa.gulumusic.ui.view

import android.graphics.RectF
import android.view.View

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.utils.AppUtils

/**
 * Created by guluwa on 2018/1/16.
 */

class PlayButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    /**
     * 底部背景画笔
     */
    private var mBtmCirclePaint: Paint? = null

    /**
     * 上部画笔
     */
    private var mTopCirclePaint: Paint? = null

    /**
     * 底部背景颜色
     */
    private var mBtmColor: Int = 0

    /**
     * 上部颜色
     */
    private var mTopColor: Int = 0

    /**
     * 线条宽度
     */
    private val mLineWidth: Int

    /**
     * 圆半径
     */
    private var mRadius: Int = 0

    /**
     * 圆心X坐标
     */
    private var mCircleX: Int = 0

    /**
     * 圆心Y坐标
     */
    private var mCircleY: Int = 0

    /**
     * 线条X偏移
     */
    private val mLineXOffset: Int

    /**
     * 线条Y偏移
     */
    private val mLineYOffset: Int

    /**
     * view宽度
     */
    private var mViewWidth: Int = 0

    /**
     * view高度
     */
    private var mViewHeight: Int = 0

    /**
     * 三角形边长
     */
    private val mTriangleWidth: Int

    /**
     * 顺时针三角形path
     */
    private var mShunTrianglePath: Path? = null

    /**
     * 逆时针三角形path
     */
    private var mNiTrianglePath: Path? = null

    /**
     * 三角形到圆的连线path
     */
    private var mLinkLinePath: Path? = null

    /**
     * 顺时针圆形path
     */
    private var mShunCirclePath: Path? = null

    /**
     * 逆时针圆形path
     */
    private var mNiCirclePath: Path? = null

    /**
     * 左边竖线
     */
    private var mLeftLinePath: Path? = null

    /**
     * 右边竖线
     */
    private var mRightLinePath: Path? = null

    /**
     * Path测量类
     */
    private var mPathMeasure: PathMeasure? = null

    /**
     * 截取的部分path
     */
    private var mDstPath: Path? = null

    /**
     * 是否正在播放 -1 暂停 0 加载 1播放
     */
    //暂停--》播放
    //播放--》暂停
    var isPlaying: Int = 0
        set(playing) {
            field = playing
            when (this.isPlaying) {
                1 -> {
                    mPlayAnimatorValue = 0f
                    mPlayAnimator!!.start()
                }
                -1 -> {
                    mStopPlayAnimatorValue = 0f
                    mStopPlayAnimator!!.start()
                }
                0 -> invalidate()
            }
        }

    /**
     * 画弧线开始角度
     */
    private var startAngle = -90

    /**
     * 弧线旋转角度
     */
    private var sweepAngle = 0

    /**
     * 当前角度
     */
    private var curAngle = 0

    /**
     * 标记当前弧度是增加还是减小
     */
    private var isGrow: Boolean = false

    /**
     * 加载圆弧外框
     */
    private var rectF: RectF? = null

    /**
     * 歌曲加载动画
     */
    private val mLoadAnimator: ValueAnimator? = null

    /**
     * 进入播放状态动画
     */
    private var mPlayAnimator: ValueAnimator? = null

    /**
     * 进入暂停状态动画
     */
    private var mStopPlayAnimator: ValueAnimator? = null

    /**
     * 进入播放状态动画进度值
     */
    private var mPlayAnimatorValue: Float = 0.toFloat()

    /**
     * 进入暂停状态动画进度值
     */
    private var mStopPlayAnimatorValue: Float = 0.toFloat()

    init {

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.PlayButton, defStyleAttr, 0)
        mLineWidth = AppUtils.dp2px(getContext(), typedArray.getInt(R.styleable.PlayButton_lineWidth, 2).toFloat())
        mTriangleWidth = AppUtils.dp2px(getContext(), typedArray.getInt(R.styleable.PlayButton_triangleWidth, 20).toFloat())
        mLineXOffset = AppUtils.dp2px(getContext(), typedArray.getInt(R.styleable.PlayButton_lineXOffset, 8).toFloat())
        mLineYOffset = AppUtils.dp2px(getContext(), typedArray.getInt(R.styleable.PlayButton_lineYOffset, 12).toFloat())
        typedArray.recycle()

        initPaint()
        initAnimation()
    }

    private fun initPaint() {
        mBtmColor = resources.getColor(R.color.play_view_gray)
        mTopColor = resources.getColor(R.color.play_view_black)
        isGrow = true

        //背景画笔
        mBtmCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBtmCirclePaint!!.color = mBtmColor
        mBtmCirclePaint!!.strokeWidth = mLineWidth.toFloat()
        mBtmCirclePaint!!.style = Paint.Style.STROKE
        mBtmCirclePaint!!.strokeJoin = Paint.Join.ROUND
        mBtmCirclePaint!!.strokeCap = Paint.Cap.ROUND

        //上部画笔
        mTopCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTopCirclePaint!!.color = mTopColor
        mTopCirclePaint!!.strokeWidth = mLineWidth.toFloat()
        mTopCirclePaint!!.style = Paint.Style.STROKE
        mTopCirclePaint!!.strokeJoin = Paint.Join.ROUND
        mTopCirclePaint!!.strokeCap = Paint.Cap.ROUND
    }

    private fun initAnimation() {
        //进入播放状态动画初始化
        mPlayAnimator = ValueAnimator.ofFloat(0f, 1f)
        mPlayAnimator!!.duration = 500
        mPlayAnimator!!.addUpdateListener { valueAnimator ->
            mPlayAnimatorValue = valueAnimator.animatedValue as Float
            invalidate()
        }
        //进入暂停状态动画初始化
        mStopPlayAnimator = ValueAnimator.ofFloat(0f, 1f)
        mStopPlayAnimator!!.duration = 500
        mStopPlayAnimator!!.addUpdateListener { valueAnimator ->
            mStopPlayAnimatorValue = valueAnimator.animatedValue as Float
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        mViewHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        mRadius = (mViewWidth - 2 * mLineWidth) / 2
        mCircleY = 0
        mCircleX = mCircleY
        initPath()
        setMeasuredDimension(mViewWidth, mViewHeight)
    }

    private fun initPath() {
        //加载圆弧外框
        rectF = RectF((-mRadius).toFloat(), (-mRadius).toFloat(), mRadius.toFloat(), mRadius.toFloat())

        //顺时针三角形路径
        mShunTrianglePath = Path()
        mShunTrianglePath!!.moveTo((mCircleX - mTriangleWidth.toDouble() / Math.sqrt(3.0) / 2.0).toFloat(), (mCircleY + mTriangleWidth / 2).toFloat())
        mShunTrianglePath!!.lineTo((mCircleX + mTriangleWidth / Math.sqrt(3.0)).toFloat(), mCircleY.toFloat())
        mShunTrianglePath!!.lineTo((mCircleX - mTriangleWidth.toDouble() / Math.sqrt(3.0) / 2.0).toFloat(), (mCircleY - mTriangleWidth / 2).toFloat())
        mShunTrianglePath!!.lineTo((mCircleX - mTriangleWidth.toDouble() / Math.sqrt(3.0) / 2.0).toFloat(), (mCircleY + mTriangleWidth / 2).toFloat())

        //逆时针三角形路径
        mNiTrianglePath = Path()
        mNiTrianglePath!!.moveTo((mCircleX - mTriangleWidth.toDouble() / Math.sqrt(3.0) / 2.0).toFloat(), (mCircleY + mTriangleWidth / 2).toFloat())
        mNiTrianglePath!!.lineTo((mCircleX - mTriangleWidth.toDouble() / Math.sqrt(3.0) / 2.0).toFloat(), (mCircleY - mTriangleWidth / 2).toFloat())
        mNiTrianglePath!!.lineTo((mCircleX + mTriangleWidth / Math.sqrt(3.0)).toFloat(), mCircleY.toFloat())
        mNiTrianglePath!!.lineTo((mCircleX - mTriangleWidth.toDouble() / Math.sqrt(3.0) / 2.0).toFloat(), (mCircleY + mTriangleWidth / 2).toFloat())

        //三角形到圆的连线path
        mLinkLinePath = Path()
        mLinkLinePath!!.moveTo((mCircleX - mTriangleWidth.toDouble() / Math.sqrt(3.0) / 2.0).toFloat(), (mCircleY + mTriangleWidth / 2).toFloat())
        mLinkLinePath!!.lineTo((mCircleX - mTriangleWidth.toDouble() / Math.sqrt(3.0) / 2.0).toFloat(), (mCircleY + mRadius).toFloat())

        //顺时针圆形路径
        mShunCirclePath = Path()
        mShunCirclePath!!.moveTo((mCircleX - mTriangleWidth.toDouble() / Math.sqrt(3.0) / 2.0).toFloat(), (mCircleY + mRadius).toFloat())
        mShunCirclePath!!.addCircle(mCircleX.toFloat(), mCircleY.toFloat(), mRadius.toFloat(), Path.Direction.CCW)

        //逆时针圆形路径
        mNiCirclePath = Path()
        mNiCirclePath!!.moveTo((mCircleX - mTriangleWidth.toDouble() / Math.sqrt(3.0) / 2.0).toFloat(), (mCircleY + mRadius).toFloat())
        mNiCirclePath!!.addCircle(mCircleX.toFloat(), mCircleY.toFloat(), mRadius.toFloat(), Path.Direction.CW)

        //左边竖线路径
        mLeftLinePath = Path()
        mLeftLinePath!!.moveTo((mCircleX - mLineXOffset).toFloat(), (mCircleY - mLineYOffset).toFloat())
        mLeftLinePath!!.lineTo((mCircleX - mLineXOffset).toFloat(), (mCircleY + mLineYOffset).toFloat())

        //右边竖线路径
        mRightLinePath = Path()
        mRightLinePath!!.moveTo((mCircleX + mLineXOffset).toFloat(), (mCircleY - mLineYOffset).toFloat())
        mRightLinePath!!.lineTo((mCircleX + mLineXOffset).toFloat(), (mCircleY + mLineYOffset).toFloat())

        mPathMeasure = PathMeasure()
        mDstPath = Path()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate((mViewWidth / 2).toFloat(), (mViewHeight / 2).toFloat())
        canvas.drawCircle(mCircleX.toFloat(), mCircleY.toFloat(), mRadius.toFloat(), mBtmCirclePaint!!)
        when (this.isPlaying) {
            1//暂停--》播放
            -> if (mPlayAnimatorValue <= 0.2) {
                mPathMeasure!!.setPath(mShunTrianglePath, false)
                mDstPath!!.reset()
                mPathMeasure!!.getSegment(mPathMeasure!!.length * mPlayAnimatorValue * 5f,
                        mPathMeasure!!.length, mDstPath, true)
                canvas.drawPath(mDstPath!!, mTopCirclePaint!!)
            } else {
                canvas.rotate(90f)
                mPathMeasure!!.setPath(mShunCirclePath, false)
                mDstPath!!.reset()
                mPathMeasure!!.getSegment(0f, (mPathMeasure!!.length.toDouble() * (mPlayAnimatorValue - 0.2) * 5.0 / 4).toFloat(), mDstPath, true)
                canvas.drawPath(mDstPath!!, mTopCirclePaint!!)
                if (mPlayAnimatorValue >= 0.8) {
                    canvas.rotate(-90f)
                    mPathMeasure!!.setPath(mLeftLinePath, false)
                    mDstPath!!.reset()
                    mPathMeasure!!.getSegment(0f, (mPathMeasure!!.length.toDouble() * (mPlayAnimatorValue - 0.8) * 5.0).toFloat(), mDstPath, true)
                    canvas.drawPath(mDstPath!!, mTopCirclePaint!!)

                    mPathMeasure!!.setPath(mRightLinePath, false)
                    mDstPath!!.reset()
                    mPathMeasure!!.getSegment(0f, (mPathMeasure!!.length.toDouble() * (mPlayAnimatorValue - 0.8) * 5.0).toFloat(), mDstPath, true)
                    canvas.drawPath(mDstPath!!, mTopCirclePaint!!)
                }
            }
            0//正在加载
            -> {
                canvas.drawPath(mShunTrianglePath!!, mTopCirclePaint!!)
                if (isGrow) {//处于增加状态，增加角度
                    sweepAngle += 6
                } else {//处于减少状态，减少角度，并通过减少起始角度，控制最终角度不变
                    startAngle += 6
                    sweepAngle -= 6
                }
                //旋转范围20~270
                if (sweepAngle >= 270) {
                    isGrow = false
                }
                if (sweepAngle <= 20) {
                    isGrow = true
                }
                curAngle += 4
                canvas.rotate((curAngle).toFloat(), 0f, 0f)  //旋转的弧长为4
                canvas.drawArc(rectF!!, startAngle.toFloat(), sweepAngle.toFloat(), false, mTopCirclePaint!!)
                invalidate()
            }
            -1//播放--》暂停
            -> if (mStopPlayAnimatorValue <= 0.2) {
                canvas.rotate(180f)
                mPathMeasure!!.setPath(mLeftLinePath, false)
                mDstPath!!.reset()
                mPathMeasure!!.getSegment(mPathMeasure!!.length * mStopPlayAnimatorValue * 5f, mPathMeasure!!.length, mDstPath, true)
                canvas.drawPath(mDstPath!!, mTopCirclePaint!!)

                mPathMeasure!!.setPath(mRightLinePath, false)
                mDstPath!!.reset()
                mPathMeasure!!.getSegment(mPathMeasure!!.length * mStopPlayAnimatorValue * 5f, mPathMeasure!!.length, mDstPath, true)
                canvas.drawPath(mDstPath!!, mTopCirclePaint!!)
            } else {
                canvas.rotate(90f)
                mPathMeasure!!.setPath(mNiCirclePath, false)
                mDstPath!!.reset()
                mPathMeasure!!.getSegment(0f, (mPathMeasure!!.length.toDouble() * (mStopPlayAnimatorValue - 0.2) * 5.0 / 4).toFloat(), mDstPath, true)
                canvas.drawPath(mDstPath!!, mTopCirclePaint!!)
                if (mStopPlayAnimatorValue >= 0.8) {
                    canvas.rotate(-90f)
                    mPathMeasure!!.setPath(mNiTrianglePath, false)
                    mDstPath!!.reset()
                    mPathMeasure!!.getSegment(0f, (mPathMeasure!!.length.toDouble() * (mStopPlayAnimatorValue - 0.8) * 5.0).toFloat(), mDstPath, true)
                    canvas.drawPath(mDstPath!!, mTopCirclePaint!!)
                }
            }
        }
    }
}