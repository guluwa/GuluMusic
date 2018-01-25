package cn.guluwa.gulumusic.view;

import android.graphics.RectF;
import android.view.View;;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import cn.guluwa.gulumusic.R;
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
     * 是否正在播放 -1 暂停 0 加载 1播放
     */
    private int isPlaying;

    /**
     * 画弧线开始角度
     */
    private int startAngle = -90;

    /**
     * 弧线旋转角度
     */
    private int sweepAngle = 0;

    /**
     * 当前角度
     */
    private int curAngle = 0;

    /**
     * 标记当前弧度是增加还是减小
     */
    private boolean isGrow;

    /**
     * 加载圆弧外框
     */
    private RectF rectF;

    /**
     * 歌曲加载动画
     */
    private ValueAnimator mLoadAnimator;

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
        mBtmColor = getResources().getColor(R.color.play_view_gray);
        mTopColor = getResources().getColor(R.color.play_view_black);
        mLineWidth = AppUtils.dp2px(getContext(), 2);
        mRadius = (AppUtils.dp2px(getContext(), 64) - 2 * mLineWidth) / 2;
        mViewWidth = mViewHeight = AppUtils.dp2px(getContext(), 64);
        mCircleX = mCircleY = 0;
        mTriangleWidth = AppUtils.dp2px(getContext(), 20);
        isGrow = true;

        //加载圆弧外框
        rectF = new RectF(-mRadius, -mRadius, mRadius, mRadius);

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
        mPlayAnimator.setDuration(500);
        mPlayAnimator.addUpdateListener(valueAnimator -> {
            mPlayAnimatorValue = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        //进入暂停状态动画初始化
        mStopPlayAnimator = ValueAnimator.ofFloat(0f, 1f);
        mStopPlayAnimator.setDuration(500);
        mStopPlayAnimator.addUpdateListener(valueAnimator -> {
            mStopPlayAnimatorValue = (float) valueAnimator.getAnimatedValue();
            invalidate();
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
        switch (isPlaying) {
            case 1://暂停--》播放
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
                break;
            case 0://正在加载
                canvas.drawPath(mShunTrianglePath,mTopCirclePaint);
                if (isGrow) {//处于增加状态，增加角度
                    sweepAngle += 6;
                } else {//处于减少状态，减少角度，并通过减少起始角度，控制最终角度不变
                    startAngle += 6;
                    sweepAngle -= 6;
                }
                //旋转范围20~270
                if (sweepAngle >= 270) {
                    isGrow = false;
                }
                if (sweepAngle <= 20) {
                    isGrow = true;
                }
                canvas.rotate(curAngle += 4, 0, 0);  //旋转的弧长为4
                canvas.drawArc(rectF, startAngle, sweepAngle, false, mTopCirclePaint);
                invalidate();
                break;
            case -1://播放--》暂停
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
                    canvas.drawPath(mDstPath, mTopCirclePaint);
                    if (mStopPlayAnimatorValue >= 0.8) {
                        canvas.rotate(-90);
                        mPathMeasure.setPath(mNiTrianglePath, false);
                        mDstPath.reset();
                        mPathMeasure.getSegment(0, (float) (mPathMeasure.getLength() * (mStopPlayAnimatorValue - 0.8) * 5), mDstPath, true);
                        canvas.drawPath(mDstPath, mTopCirclePaint);
                    }
                }
                break;
        }
    }

    public void setPlaying(int playing) {
        isPlaying = playing;
        System.out.println(isPlaying);
        switch (isPlaying) {
            case 1://暂停--》播放
                mPlayAnimatorValue = 0f;
                mPlayAnimator.start();
                break;
            case -1://播放--》暂停
                mStopPlayAnimatorValue = 0f;
                mStopPlayAnimator.start();
                break;
        }
    }

    public int getIsPlaying() {
        return isPlaying;
    }
}