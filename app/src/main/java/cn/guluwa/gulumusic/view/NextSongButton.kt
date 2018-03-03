package cn.guluwa.gulumusic.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.utils.AppUtils

/**
 * Created by guluwa on 2018/1/19.
 */

class NextSongButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    /**
     * 画笔
     */
    private var paint: Paint? = null

    /**
     * view颜色
     */
    private var color: Int = 0

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

    init {
        initPaint()
    }

    private fun initPaint() {
        color = resources.getColor(R.color.play_view_black)
        viewWidth = AppUtils.dp2px(context, 2f)
        mViewHeight = AppUtils.dp2px(context, 20f)
        mViewWidth = mViewHeight

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.style = Paint.Style.STROKE
        paint!!.color = color
        paint!!.strokeWidth = viewWidth.toFloat()
        paint!!.strokeCap = Paint.Cap.ROUND
        paint!!.strokeJoin = Paint.Join.ROUND

        mViewPath = Path()
        mViewPath!!.moveTo((viewWidth + paddingLeft).toFloat(), (viewWidth + paddingTop).toFloat())
        mViewPath!!.lineTo((viewWidth + paddingLeft).toFloat(), (mViewHeight - viewWidth + paddingTop).toFloat())
        mViewPath!!.lineTo((mViewWidth - viewWidth + paddingLeft).toFloat(), (mViewHeight / 2 + paddingTop).toFloat())
        mViewPath!!.lineTo((viewWidth + paddingLeft).toFloat(), (viewWidth + paddingTop).toFloat())
        mViewPath!!.moveTo((mViewWidth - viewWidth + paddingLeft).toFloat(), (viewWidth + paddingTop).toFloat())
        mViewPath!!.lineTo((mViewWidth - viewWidth + paddingLeft).toFloat(), (mViewHeight - viewWidth + paddingTop).toFloat())
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
                mViewWidth + paddingLeft + paddingRight,
                mViewHeight + paddingTop + paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(mViewPath!!, paint!!)
    }
}
