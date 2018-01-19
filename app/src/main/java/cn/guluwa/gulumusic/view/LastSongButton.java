package cn.guluwa.gulumusic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import cn.guluwa.gulumusic.utils.AppUtils;

/**
 * Created by guluwa on 2018/1/19.
 */

public class LastSongButton extends View {

    /**
     * 画笔
     */
    private Paint paint;

    /**
     * view颜色
     */
    private int color;

    /**
     * view线条width
     */
    private int width;

    /**
     * view宽度
     */
    private int mViewWidth;

    /**
     * view高度
     */
    private int mViewHeight;

    /**
     * view路径
     */
    private Path mViewPath;

    public LastSongButton(Context context) {
        this(context, null);
    }

    public LastSongButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LastSongButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        color = Color.WHITE;
        width = AppUtils.dp2px(getContext(), 1);
        mViewWidth = mViewHeight = AppUtils.dp2px(getContext(), 20);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        mViewPath = new Path();
        mViewPath.lineTo(0, mViewHeight);
        mViewPath.moveTo(0, mViewHeight / 2);
        mViewPath.lineTo(mViewWidth, 0);
        mViewPath.lineTo(mViewWidth, mViewHeight);
        mViewPath.lineTo(0, mViewHeight / 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mViewPath, paint);
    }

    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
        invalidate();
    }
}
