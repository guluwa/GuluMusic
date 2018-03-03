package cn.guluwa.gulumusic.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.utils.AppUtils

/**
 * Created by guluwa on 2018/1/22.
 */

class DownBackButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    /**
     * 画笔
     */
    private var paint: Paint? = null

    /**
     * view背景颜色
     */
    private var mBackColor: Int = 0

    /**
     * 线条颜色
     */
    private var mLineColor: Int = 0

    /**
     * view线条width
     */
    private var viewWidth: Int = 0

    /**
     * view宽度
     */
    private var mViewWidth: Int = 0

    /**
     * view高度
     */
    private var mViewHeight: Int = 0

    /**
     * view路径
     */
    private var mViewPath: Path? = null

    /**
     * 背景圆半径
     */
    private var mRadius: Int = 0

    init {
        initPaint()
    }

    private fun initPaint() {
        mBackColor = resources.getColor(R.color.black)
        mLineColor = Color.WHITE
        viewWidth = AppUtils.dp2px(context, 2f)
        mViewHeight = AppUtils.dp2px(context, 24f)
        mViewWidth = mViewHeight
        mRadius = AppUtils.dp2px(context, 12f)

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.strokeWidth = viewWidth.toFloat()
        paint!!.strokeCap = Paint.Cap.ROUND
        paint!!.strokeJoin = Paint.Join.ROUND

        mViewPath = Path()
        mViewPath!!.moveTo(AppUtils.dp2px(context, 8f).toFloat(), AppUtils.dp2px(context, 12f).toFloat())
        mViewPath!!.lineTo(AppUtils.dp2px(context, 12f).toFloat(), AppUtils.dp2px(context, 15f).toFloat())
        mViewPath!!.lineTo(AppUtils.dp2px(context, 16f).toFloat(), AppUtils.dp2px(context, 12f).toFloat())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mViewWidth, mViewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint!!.color = mBackColor
        paint!!.style = Paint.Style.FILL
        canvas.drawCircle((mViewWidth / 2).toFloat(), (mViewHeight / 2).toFloat(), mRadius.toFloat(), paint!!)
        paint!!.color = mLineColor
        paint!!.style = Paint.Style.STROKE
        canvas.drawPath(mViewPath!!, paint!!)
    }
}
