package cn.guluwa.gulumusic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.sql.Time;
import java.text.SimpleDateFormat;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.utils.AppUtils;

/**
 * Created by guluwa on 2018/1/19.
 */

public class ProgressView extends View {

    /**
     * 上部进度条画笔
     */
    private Paint mTopProgressPaint;

    /**
     * 底部进度条画笔
     */
    private Paint mBtmProgressPaint;

    /**
     * 文字画笔
     */
    private Paint mTextPaint;

    /**
     * 上部进度条颜色
     */
    private int mProgressColor;

    /**
     * 底部进度条颜色
     */
    private int mIndicatorColor;

    /**
     * view宽度
     */
    private int mViewWidth;

    /**
     * view高度
     */
    private int mViewHeight;

    /**
     * 歌曲播放长度
     */
    private String mSongPlayLength;

    /**
     * 歌曲总长度
     */
    private String mSongTotalLength;

    /**
     * 字体大小
     */
    private int mTextSize;

    /**
     * 文字中间偏移
     */
    private float yOffset;

    /**
     * 文字长度
     */
    private int mTextWidth;

    /**
     * 进度圆形指示器小半径
     */
    private int mIndicatorCircleSmallRadius;

    /**
     * 进度圆形指示器大半径
     */
    private int mIndicatorCircleBigRadius;

    /**
     * 进度
     */
    private float mProgress;

    /**
     * 时间转化
     */
    private SimpleDateFormat time;

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        mProgressColor = getResources().getColor(R.color.play_view_black);
        mIndicatorColor = getResources().getColor(R.color.play_view_gray);
        mViewHeight = AppUtils.INSTANCE.dp2px(getContext(), 16);
        mTextSize = AppUtils.INSTANCE.sp2px(getContext(), 12);
        mSongPlayLength = "00:00";
        mSongTotalLength = "00:00";
        mProgress = 0;
        time = new SimpleDateFormat("mm:ss");

        mTopProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTopProgressPaint.setStrokeWidth(AppUtils.INSTANCE.dp2px(getContext(), 2));
        mTopProgressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTopProgressPaint.setColor(mProgressColor);
        mTopProgressPaint.setStrokeJoin(Paint.Join.ROUND);
        mTopProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mBtmProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBtmProgressPaint.setStrokeWidth(AppUtils.INSTANCE.dp2px(getContext(), 2));
        mBtmProgressPaint.setStyle(Paint.Style.STROKE);
        mBtmProgressPaint.setColor(mIndicatorColor);
        mBtmProgressPaint.setStrokeJoin(Paint.Join.ROUND);
        mBtmProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mProgressColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTypeface(Typeface.SERIF);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        yOffset = -(fontMetrics.ascent + fontMetrics.descent) / 2;
        mTextWidth = mTextSize * mSongTotalLength.length() / 2 + AppUtils.INSTANCE.dp2px(getContext(), 10);
        mIndicatorCircleSmallRadius = AppUtils.INSTANCE.dp2px(getContext(), 2);
        mIndicatorCircleBigRadius = AppUtils.INSTANCE.dp2px(getContext(), 4);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(
                mTextWidth,
                mViewHeight / 2,
                mViewWidth - mTextWidth,
                mViewHeight / 2,
                mBtmProgressPaint);
        canvas.drawLine(
                mTextWidth,
                mViewHeight / 2,
                mTextWidth + (mViewWidth - 2 * mTextWidth) * mProgress - mIndicatorCircleBigRadius * 2,
                mViewHeight / 2,
                mTopProgressPaint);
        canvas.drawCircle(mTextWidth + (mViewWidth - 2 * mTextWidth) * mProgress - mIndicatorCircleSmallRadius*2,
                mViewHeight / 2,
                mIndicatorCircleBigRadius,
                mTopProgressPaint);
        canvas.drawText(mSongPlayLength, 0, mViewHeight / 2 + yOffset, mTextPaint);
        canvas.drawText(mSongTotalLength, mViewWidth - mTextSize * mSongTotalLength.length() / 2, mViewHeight / 2 + yOffset, mTextPaint);
    }

    public void setProgress(float mProgress) {
        this.mProgress = mProgress;
    }

    public void setSongPlayLength(int playMillisecond, int totalMillisecond) {
        this.mSongPlayLength = time.format(playMillisecond);
        this.mSongTotalLength = time.format(totalMillisecond);
        setProgress((float) (playMillisecond*1.0/totalMillisecond));
        invalidate();
    }
}
