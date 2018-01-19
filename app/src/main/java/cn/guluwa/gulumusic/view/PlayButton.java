package cn.guluwa.gulumusic.view;

import android.view.View;;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import cn.guluwa.gulumusic.utils.AppUtils;

/**
 * Created by guluwa on 2018/1/16.
 */

public class PlayButton extends View {

    /**
     * 底部背景画笔
     */
    private Paint mBtmCirclePaint;

    /**
     * 上部画笔
     */
    private Paint mTopCirclePaint;

    /**
     * 底部背景颜色
     */
    private int mBtmColor;

    /**
     * 上部颜色
     */
    private int mTopColor;

    /**
     * 线条宽度
     */
    private int mLineWidth;

    /**
     * 圆半径
     */
    private int mRadius;

    /**
     * 圆心X坐标
     */
    private int mCircleX;

    /**
     * 圆心Y坐标
     */
    private int mCircleY;

    /**
     * view宽度
     */
    private int mViewWidth;

    /**
     * view高度
     */
    private int mViewHeight;

    /**
     * 三角形边长
     */
    private int mTriangleWidth;

    /**
     * 顺时针三角形path
     */
    private Path mShunTrianglePath;

    /**
     * 逆时针三角形path
     */
    private Path mNiTrianglePath;

    /**
     * 三角形到圆的连线path
     */
    private Path mLinkLinePath;

    /**
     * 顺时针圆形path
     */
    private Path mShunCirclePath;

    /**
     * 逆时针圆形path
     */
    private Path mNiCirclePath;

    /**
     * 左边竖线
     */
    private Path mLeftLinePath;

    /**
     * 右边竖线
     */
    private Path mRightLinePath;

    /**
     * Path测量类
     */
    private PathMeasure mPathMeasure;

    /**
     * 截取的部分path
     */
    private Path mDstPath;

    /**
     * 是否正在播放
     */
    private boolean isPlaying;

    /**
     * 进入播放状态动画
     */
    private ValueAnimator mPlayAnimator;

    /**
     * 进入暂停状态动画
     */
    private ValueAnimator mStopPlayAnimator;

    /**
     * 进入播放状态动画进度值
     */
    private float mPlayAnimatorValue;

    /**
     * 进入暂停状态动画进度值
     */
    private float mStopPlayAnimatorValue;

    public PlayButton(Context context) {
        this(context, null);
    }

    public PlayButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        initAnimation();
    }

    private void initPaint() {
        mBtmColor = Color.GRAY;
        mTopColor = Color.BLACK;
        mLineWidth = AppUtils.dp2px(getContext(), 1);
        mRadius = (AppUtils.dp2px(getContext(), 64) - 2 * mLineWidth) / 2;
        mViewWidth = mViewHeight = AppUtils.dp2px(getContext(), 64);
        mCircleX = mCircleY = 0;
        mTriangleWidth = AppUtils.dp2px(getContext(), 20);

        //顺时针三角形路径
        mShunTrianglePath = new Path();
        mShunTrianglePath.moveTo(((float) (mCircleX - mTriangleWidth / Math.sqrt(3) / 2)), ((float) (mCircleY + mTriangleWidth / 2)));
        mShunTrianglePath.lineTo(((float) (mCircleX + mTriangleWidth / Math.sqrt(3))), mCircleY);
        mShunTrianglePath.lineTo(((float) (mCircleX - mTriangleWidth / Math.sqrt(3) / 2)), ((float) (mCircleY - mTriangleWidth / 2)));
        mShunTrianglePath.lineTo(((float) (mCircleX - mTriangleWidth / Math.sqrt(3) / 2)), ((float) (mCircleY + mTriangleWidth / 2)));

        //逆时针三角形路径
        mNiTrianglePath = new Path();
        mNiTrianglePath.moveTo(((float) (mCircleX - mTriangleWidth / Math.sqrt(3) / 2)), ((float) (mCircleY + mTriangleWidth / 2)));
        mNiTrianglePath.lineTo(((float) (mCircleX - mTriangleWidth / Math.sqrt(3) / 2)), ((float) (mCircleY - mTriangleWidth / 2)));
        mNiTrianglePath.lineTo(((float) (mCircleX + mTriangleWidth / Math.sqrt(3))), mCircleY);
        mNiTrianglePath.lineTo(((float) (mCircleX - mTriangleWidth / Math.sqrt(3) / 2)), ((float) (mCircleY + mTriangleWidth / 2)));

        //三角形到圆的连线path
        mLinkLinePath = new Path();
        mLinkLinePath.moveTo(((float) (mCircleX - mTriangleWidth / Math.sqrt(3) / 2)), ((float) (mCircleY + mTriangleWidth / 2)));
        mLinkLinePath.lineTo(((float) (mCircleX - mTriangleWidth / Math.sqrt(3) / 2)), mCircleY + mRadius);

        //顺时针圆形路径
        mShunCirclePath = new Path();
        mShunCirclePath.moveTo(((float) (mCircleX - mTriangleWidth / Math.sqrt(3) / 2)), mCircleY + mRadius);
        mShunCirclePath.addCircle(mCircleX, mCircleY, mRadius, Path.Direction.CCW);

        //逆时针圆形路径
        mNiCirclePath = new Path();
        mNiCirclePath.moveTo(((float) (mCircleX - mTriangleWidth / Math.sqrt(3) / 2)), mCircleY + mRadius);
        mNiCirclePath.addCircle(mCircleX, mCircleY, mRadius, Path.Direction.CW);

        //左边竖线路径
        mLeftLinePath = new Path();
        mLeftLinePath.moveTo(((float) (mCircleX - AppUtils.dp2px(getContext(), 8))), ((float) (mCircleY - AppUtils.dp2px(getContext(), 12))));
        mLeftLinePath.lineTo(((float) (mCircleX - AppUtils.dp2px(getContext(), 8))), ((float) (mCircleY + AppUtils.dp2px(getContext(), 12))));

        //右边竖线路径
        mRightLinePath = new Path();
        mRightLinePath.moveTo(((float) (mCircleX + AppUtils.dp2px(getContext(), 8))), ((float) (mCircleY - AppUtils.dp2px(getContext(), 12))));
        mRightLinePath.lineTo(((float) (mCircleX + AppUtils.dp2px(getContext(), 8))), ((float) (mCircleY + AppUtils.dp2px(getContext(), 12))));

        mPathMeasure = new PathMeasure();
        mDstPath = new Path();

        //背景画笔
        mBtmCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBtmCirclePaint.setColor(mBtmColor);
        mBtmCirclePaint.setStrokeWidth(mLineWidth);
        mBtmCirclePaint.setStyle(Paint.Style.STROKE);
        mBtmCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        mBtmCirclePaint.setStrokeCap(Paint.Cap.ROUND);

        //上部画笔
        mTopCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTopCirclePaint.setColor(mTopColor);
        mTopCirclePaint.setStrokeWidth(mLineWidth);
        mTopCirclePaint.setStyle(Paint.Style.STROKE);
        mTopCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        mTopCirclePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void initAnimation() {
        //进入播放状态动画初始化
        mPlayAnimator = ValueAnimator.ofFloat(0f, 1f);
        mPlayAnimator.setDuration(1000);
        mPlayAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mPlayAnimatorValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        //进入暂停状态动画初始化
        mStopPlayAnimator = ValueAnimator.ofFloat(0f, 1f);
        mStopPlayAnimator.setDuration(1000);
        mStopPlayAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mStopPlayAnimatorValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mViewWidth / 2, mViewHeight / 2);
        canvas.drawCircle(mCircleX, mCircleY, mRadius, mBtmCirclePaint);
        if (isPlaying) {//播放--》暂停
            canvas.drawCircle(mCircleX, mCircleY, mRadius, mTopCirclePaint);
            if (mStopPlayAnimatorValue <= 0.2) {
                canvas.rotate(180);
                mPathMeasure.setPath(mLeftLinePath, false);
                mDstPath.reset();
                mPathMeasure.getSegment(mPathMeasure.getLength() * mStopPlayAnimatorValue * 5, mPathMeasure.getLength(), mDstPath, true);
                canvas.drawPath(mDstPath, mTopCirclePaint);

                mPathMeasure.setPath(mRightLinePath, false);
                mDstPath.reset();
                mPathMeasure.getSegment(mPathMeasure.getLength() * mStopPlayAnimatorValue * 5, mPathMeasure.getLength(), mDstPath, true);
                canvas.drawPath(mDstPath, mTopCirclePaint);
            } else {
                canvas.rotate(90);
                mPathMeasure.setPath(mNiCirclePath, false);
                mDstPath.reset();
                mPathMeasure.getSegment(0, (float) (mPathMeasure.getLength() * (mStopPlayAnimatorValue - 0.2) * 5 / 4), mDstPath, true);
                canvas.drawPath(mDstPath, mBtmCirclePaint);
                if (mStopPlayAnimatorValue >= 0.8) {
                    canvas.rotate(-90);
                    mPathMeasure.setPath(mNiTrianglePath, false);
                    mDstPath.reset();
                    mPathMeasure.getSegment(0, (float) (mPathMeasure.getLength() * (mStopPlayAnimatorValue - 0.8) * 5), mDstPath, true);
                    canvas.drawPath(mDstPath, mTopCirclePaint);
                }
            }
        } else {//暂停--》播放
            if (mPlayAnimatorValue <= 0.2) {
                mPathMeasure.setPath(mShunTrianglePath, false);
                mDstPath.reset();
                mPathMeasure.getSegment(mPathMeasure.getLength() * mPlayAnimatorValue * 5,
                        mPathMeasure.getLength(), mDstPath, true);
                canvas.drawPath(mDstPath, mTopCirclePaint);
            } else {
                canvas.rotate(90);
                mPathMeasure.setPath(mShunCirclePath, false);
                mDstPath.reset();
                mPathMeasure.getSegment(0, (float) (mPathMeasure.getLength() * (mPlayAnimatorValue - 0.2) * 5 / 4), mDstPath, true);
                canvas.drawPath(mDstPath, mTopCirclePaint);
                if (mPlayAnimatorValue >= 0.8) {
                    canvas.rotate(-90);
                    mPathMeasure.setPath(mLeftLinePath, false);
                    mDstPath.reset();
                    mPathMeasure.getSegment(0, (float) (mPathMeasure.getLength() * (mPlayAnimatorValue - 0.8) * 5), mDstPath, true);
                    canvas.drawPath(mDstPath, mTopCirclePaint);

                    mPathMeasure.setPath(mRightLinePath, false);
                    mDstPath.reset();
                    mPathMeasure.getSegment(0, (float) (mPathMeasure.getLength() * (mPlayAnimatorValue - 0.8) * 5), mDstPath, true);
                    canvas.drawPath(mDstPath, mTopCirclePaint);
                }
            }
        }
    }

    public void startAnimation() {
        isPlaying = !isPlaying;
        if (isPlaying) {//播放--》暂停
            mStopPlayAnimatorValue = 0f;
            mStopPlayAnimator.start();
        } else {//暂停--》播放
            mPlayAnimatorValue = 0f;
            mPlayAnimator.start();
        }
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
        invalidate();
    }

    public void setBtmColor(int mBtmColor) {
        this.mBtmColor = mBtmColor;
        mBtmCirclePaint.setColor(mBtmColor);
        invalidate();
    }

    public void setTopColor(int mTopColor) {
        this.mTopColor = mTopColor;
        mTopCirclePaint.setColor(mTopColor
        );
        invalidate();
    }
}