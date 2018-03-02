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
 * Created by guluwa on 2018/1/22.
 */

public class DownBackButton extends View {

    /**
     * 画笔
     */
    private Paint paint;

    /**
     * view背景颜色
     */
    private int mBackColor;

    /**
     * 线条颜色
     */
    private int mLineColor;

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

    /**
     * 背景圆半径
     */
    private int mRadius;

    public DownBackButton(Context context) {
        this(context, null);
    }

    public DownBackButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownBackButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        mBackColor = getResources().getColor(R.color.black);
        mLineColor = Color.WHITE;
        width = AppUtils.INSTANCE.dp2px(getContext(), 2);
        mViewWidth = mViewHeight = AppUtils.INSTANCE.dp2px(getContext(), 24);
        mRadius = AppUtils.INSTANCE.dp2px(getContext(), 12);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        mViewPath = new Path();
        mViewPath.moveTo(AppUtils.INSTANCE.dp2px(getContext(), 8), AppUtils.INSTANCE.dp2px(getContext(), 12));
        mViewPath.lineTo(AppUtils.INSTANCE.dp2px(getContext(), 12), AppUtils.INSTANCE.dp2px(getContext(), 15));
        mViewPath.lineTo(AppUtils.INSTANCE.dp2px(getContext(), 16), AppUtils.INSTANCE.dp2px(getContext(), 12));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(mBackColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mViewWidth / 2, mViewHeight / 2, mRadius, paint);
        paint.setColor(mLineColor);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(mViewPath, paint);
    }
}
