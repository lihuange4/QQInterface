package com.lee.qqinterface.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lee.qqinterface.adapter.HaoHanAdapter;


public class DragLayout extends FrameLayout {
    private ViewDragHelper mDragHelper;
    private int height;
    private int width;
    private ViewGroup mLeftView;
    private ViewGroup mMainView;
    private int mRange;
    private Status mStatus = Status.close;
    private GestureDetectorCompat mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mYGestureListener;

    public enum Status {
        close, open, draging;
    }

    /*
    返回状态
     */
    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        this.mStatus = status;
        if (status == Status.close) {
            close();
        }
    }

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        mGestureDetector = new GestureDetectorCompat(context, mYGestureListener);
    }

    private DragListener mDragListener;

    public interface DragListener {
        void open();

        void close();

        void draging(float percent);
    }

    public void setOnDragStatusChangeListener(DragListener dragListener) {
        this.mDragListener = dragListener;
    }

    private void initView() {
        mYGestureListener = new GestureDetector.SimpleOnGestureListener() {
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return Math.abs(distanceX) >= Math.abs(distanceY);
            }
        };
        mDragHelper = ViewDragHelper.create(this, callback);
    }

    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mMainView) {
                left = fixLeft(left);
            }
            return left;
        }

        //修正X坐标值
        private int fixLeft(int left) {
            if (left < 0) {
                left = 0;
            } else if (left > mRange) {
                left = mRange;
            }
            return left;
        }

        //水平移动的范围
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        //状态改变
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

            int newLeft = left;
            if (changedView == mLeftView) {
                // 把当前变化量传递给mMainContent
                newLeft = mMainView.getLeft() + dx;
            }
            // 进行修正
            newLeft = fixLeft(newLeft);
            if (changedView == mLeftView) {
                // 当左面板移动之后, 再强制放回去.
                mLeftView.layout(0, 0, width, height);
                mMainView.layout(newLeft, 0, newLeft + width, height);
            }

            disPathDragEvent(newLeft);
            // 为了兼容低版本, 每次修改值之后, 进行重绘
            invalidate();
        }

        //松开手时调用
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (mMainView.getLeft() > mRange * 0.5 && xvel == 0) {
                open();
            } else if (xvel > 0) {
                open(); //打开侧滑面板
            } else {
                close(); //关闭侧滑面板
            }
        }
    };


    //更新状态
    private Status updateStatus(int newLeft) {
        if (mMainView.getLeft() == 0) {
            mStatus = Status.close;
        } else if (mMainView.getLeft() == mRange) {
            mStatus = Status.open;
        } else {
            mStatus = Status.draging;
        }
        return mStatus;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    //打开侧滑面板
    private void open() {
        int finalLeft = mRange;
        mDragHelper.smoothSlideViewTo(mMainView, finalLeft, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    //关闭侧滑面板
    protected void close() {
        int finalLeft = 0;
        mDragHelper.smoothSlideViewTo(mMainView, finalLeft, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    //伴随动画
    private void disPathDragEvent(int left) {
        float percent = left * 1.0f / mRange;//位移完成的百分比
        if (mDragListener != null) {
            Status oldStatus = mStatus;//原来的状态
            mStatus = updateStatus(left);//更新后的状态
            mDragListener.draging(percent);
            if (!(oldStatus == mStatus)) {
                if (mStatus == Status.close) {
                    mDragListener.close();
                } else if (mStatus == Status.open) {
                    mDragListener.open();
                }
            }
        }
        animViews(percent);//伴随动画

    }

    //伴随动画
    private void animViews(float percent) {
        //主面板缩放，0.8
        mMainView.setScaleX(evaluate(percent, 1, 0.8f));
        mMainView.setScaleY(evaluate(percent, 1, 0.8f));
        //侧滑面板位移
        mLeftView.setTranslationX(evaluate(percent, -width * 0.5f, 0));
        //侧滑面板缩放，0.5>1.0
        mLeftView.setScaleX(evaluate(percent, 0.5f, 1.0f));
        mLeftView.setScaleY(evaluate(percent, 0.5f, 1.0f));
        //侧滑面板透明度
        mLeftView.setAlpha(evaluate(percent, 0.5, 1.0f));
        //背景颜色变化,黑色>透明
        getBackground().setColorFilter((Integer) evaluateColor(percent, Color.BLACK, Color.TRANSPARENT), PorterDuff
                .Mode.SRC_OVER);
    }

    private float evaluate(float percent, Number start, Number end) {
        return start.floatValue() + percent * (end.floatValue() - start.floatValue());
    }

    //颜色变化
    private Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int) ((startA + (int) (fraction * (endA - startA))) << 24) |
                (int) ((startR + (int) (fraction * (endR - startR))) << 16) |
                (int) ((startG + (int) (fraction * (endG - startG))) << 8) |
                (int) ((startB + (int) (fraction * (endB - startB))));
    }

    public void setAdapterInterface(HaoHanAdapter adapter) {
        this.adapter = adapter;
    }

    private HaoHanAdapter adapter;
    float mDownX;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getStatus() == Status.close) {
            int actionMasked = MotionEventCompat.getActionMasked(ev);
            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = ev.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (adapter.getUnClosedCount() > 0) {
                        return false;
                    }

                    float delta = ev.getRawX() - mDownX;
                    if (delta < 0) {
                        return false;
                    }
                    break;
                default:
                    mDownX = 0;
                    break;
            }
        }

        return mDragHelper.shouldInterceptTouchEvent(ev) & mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) {
            throw new IllegalStateException("至少有两个之View");
        }
        if (!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException("子View必须是ViewGroup");
        }
        mLeftView = (ViewGroup) getChildAt(0);
        mMainView = (ViewGroup) getChildAt(1);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = getMeasuredHeight();//屏幕高度
        width = getMeasuredWidth();//屏幕宽度
        mRange = (int) (width * 0.6);
    }
}

