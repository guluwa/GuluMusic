package cn.guluwa.gulumusic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.utils.AppUtils;

/**
 * Created by guluwa on 2018/1/19.
 */

public class NextSongButton extends View {

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

    public NextSongButton(Context context) {
        this(context, null);
    }

    public NextSongButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NextSongButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        color = getResources().getColor(R.color.play_view_black);
        width = AppUtils.INSTANCE.dp2px(getContext(), 2);
        mViewWidth = mViewHeight = AppUtils.INSTANCE.dp2px(getContext(), 20);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        mViewPath = new Path();
        mViewPath.moveTo(width + getPaddingLeft(), width + getPaddingTop());
        mViewPath.lineTo(width + getPaddingLeft(), mViewHeight - width + getPaddingTop());
        mViewPath.lineTo(mViewWidth - width + getPaddingLeft(), mViewHeight / 2 + getPaddingTop());
        mViewPath.lineTo(width + getPaddingLeft(), width + getPaddingTop());
        mViewPath.moveTo(mViewWidth - width + getPaddingLeft(), width + getPaddingTop());
        mViewPath.lineTo(mViewWidth - width + getPaddingLeft(), mViewHeight - width + getPaddingTop());
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(
                mViewWidth + getPaddingLeft() + getPaddingRight(),
                mViewHeight + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mViewPath, paint);
    }
}
