package com.lee.qqinterface.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 滑动条目布局
 *
 * @author PoplarTang
 */
public class SwipeLayout extends FrameLayout implements SwipeLayoutInterface {

    private static final String TAG = "SwipeLayout";
    private View mFrontView;
    private View mBackView;
    private int mDragDistance;
    private ShowEdge mShowEdge = ShowEdge.Right;
    private Status mStatus = Status.Close;
    private ViewDragHelper mDragHelper;
    private SwipeListener mSwipeListener;
    private GestureDetectorCompat mGestureDetector;

    public enum Status {
        Close, Swiping, Status, Open
    }

    public static enum ShowEdge {
        Left, Right
    }

    public static interface SwipeListener {
        void onClose(SwipeLayout swipeLayout);

        void onOpen(SwipeLayout swipeLayout);

        void onStartClose(SwipeLayout swipeLayout);

        void onStartOpen(SwipeLayout swipeLayout);
    }


    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mDragHelper = ViewDragHelper.create(this, mCallback);
        //初始化手势识别器
        mGestureDetector = new GestureDetectorCompat(context, mOnGestureListener);

    }

    private SimpleOnGestureListener mOnGestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // 当横向移动距离大于等于纵向时，返回true
            return Math.abs(distanceX) >= Math.abs(distanceY);
        }
    };

    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View view, int id) {
            return view == getFrontView() || view == getBackView();
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragDistance;
        }

        // 限定移动范围
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            // left
            if (child == mFrontView) {
                if (left > 0) {
                    return 0;
                } else if (left < -mDragDistance) {
                    return -mDragDistance;
                }
            } else if (child == mBackView) {
                if (left > getMeasuredWidth()) {
                    return getMeasuredWidth();
                } else if (left < getMeasuredWidth() - mDragDistance) {
                    return getMeasuredWidth() - mDragDistance;
                }
            }
            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            // 传递事件
            if (changedView == mFrontView) {
                mBackView.offsetLeftAndRight(dx);
            } else if (changedView == mBackView) {
                mFrontView.offsetLeftAndRight(dx);
            }
            updateStatus();
            // 兼容老版本
            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (xvel == 0 && mFrontView.getLeft() < -mDragDistance / 2.0f) {
                open();
            } else if (xvel < 0) {
                open();
            } else {
                close();
            }
            invalidate();
        }

    };
    private float mDownX;

    @Override
    public Status getCurrentStatus() {
        int left = getFrontView().getLeft();
        if (left == 0) {
            return Status.Close;
        }
        if ((left == 0 - mDragDistance) || (left == mDragDistance)) {
            return Status.Open;
        }
        return Status.Swiping;
    }

    public void close() {
        close(true);
    }

    public void close(boolean isSmooth) {
        close(isSmooth, true);
    }

    public void close(boolean isSmooth, boolean isNotify) {
        if (isSmooth) {
            Rect rect = computeFrontLayout(false);
            if (mDragHelper.smoothSlideViewTo(getFrontView(), rect.left, rect.top)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
            ;
        } else {
            layoutContent(false);
            updateStatus(isNotify);
        }
    }

    public void open() {
        open(true, true);
    }

    public void open(boolean isSmooth) {
        open(isSmooth, true);
    }

    /**
     * 展开layout
     *
     * @param isSmooth 是否是平滑的动画。
     * @param isNotify 是否进行通知回调
     */
    public void open(boolean isSmooth, boolean isNotify) {
        if (isSmooth) {
            Rect rect = computeFrontLayout(true);
            if (mDragHelper.smoothSlideViewTo(getFrontView(), rect.left, rect.top)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
            ;
        } else {
            layoutContent(true);
            updateStatus(isNotify);
        }
    }

    private void updateStatus() {
        updateStatus(true);
    }

    /**
     * 更新当前状态
     *
     * @param isNotify
     */
    private void updateStatus(boolean isNotify) {

        // 记录上一次的状态
        Status preStatus = mStatus;
        // 更新当前状态
        mStatus = getCurrentStatus();
        if (preStatus != mStatus && mSwipeListener != null) {

            if (mStatus == Status.Open) {
                mSwipeListener.onOpen(this);
            } else if (mStatus == Status.Close) {
                mSwipeListener.onClose(this);
            } else if (mStatus == Status.Swiping) {
                if (preStatus == Status.Open) {
                    mSwipeListener.onStartClose(this);
                } else if (preStatus == Status.Close) {
                    mSwipeListener.onStartOpen(this);
                }
            }
        }



     /*   Status lastStatus = mStatus;
        Status status = getCurrentStatus();

        if (status != mStatus) {
            mStatus = status;

            if (!isNotify || mSwipeListener == null) {
                return;
            }

            if (mStatus == Status.Open) {
                mSwipeListener.onOpen(this);
            } else if (mStatus == Status.Close) {
                mSwipeListener.onClose(this);
            } else if (mStatus == Status.Swiping) {
                if (lastStatus == Status.Open) {
                    mSwipeListener.onStartClose(this);
                } else if (lastStatus == Status.Close) {
                    mSwipeListener.onStartOpen(this);
                }
            }
        } else {
            mStatus = status;
        }*/
    }

    @Override
    public void computeScroll() {

        // 在这里判断动画是否需要继续执行。会在View.draw(Canvas mCanvas)之前执行。
        if (mDragHelper.continueSettling(true)) {
            // 返回true，表示动画还没执行完，需要继续执行。
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 决定当前的SwipeLayout是否要把touch事件拦截下来，直接交由自己的onTouchEvent处理
        // 返回true则为拦截
        return mDragHelper.shouldInterceptTouchEvent(ev) & mGestureDetector.onTouchEvent(ev);
    }

    ;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 当处理touch事件时，不希望被父类onInterceptTouchEvent的代码所影响。
        // 比如处理向右滑动关闭已打开的条目时，如果进行以下逻辑，则不会在关闭的同时引发左边菜单的打开。

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:

                float deltaX = event.getRawX() - mDownX;
                if (deltaX > mDragHelper.getTouchSlop()) {
                    // 请求父级View不拦截touch事件
                    requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                mDownX = 0;
            default:
                break;
        }

        try {
            mDragHelper.processTouchEvent(event);
        } catch (IllegalArgumentException e) {
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mDragDistance = getBackView().getMeasuredWidth();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        layoutContent(false);

    }

    private void layoutContent(boolean isOpen) {
        Rect rect = computeFrontLayout(isOpen);
        getFrontView().layout(rect.left, rect.top, rect.right, rect.bottom);
        rect = computeBackLayoutViaFront(rect);
        getBackView().layout(rect.left, rect.top, rect.right, rect.bottom);
        bringChildToFront(getFrontView());
    }

    private Rect computeBackLayoutViaFront(Rect mFrontRect) {
        Rect rect = mFrontRect;

        int bl = rect.left, bt = rect.top, br = rect.right, bb = rect.bottom;
        if (mShowEdge == ShowEdge.Left) {
            bl = rect.left - mDragDistance;
        } else if (mShowEdge == ShowEdge.Right) {
            bl = rect.right;
        }
        br = bl + getBackView().getMeasuredWidth();

        return new Rect(bl, bt, br, bb);
    }

    private Rect computeFrontLayout(boolean isOpen) {
        int l = 0, t = 0;
        if (isOpen) {
            if (mShowEdge == ShowEdge.Left) {
                l = 0 + mDragDistance;
            } else if (mShowEdge == ShowEdge.Right) {
                l = 0 - mDragDistance;
            }
        }

        return new Rect(l, t, l + getMeasuredWidth(), t + getMeasuredHeight());
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() != 2) {
            throw new IllegalStateException("At least 2 views in SwipeLayout");
        }

        mFrontView = getChildAt(1);
        if (mFrontView instanceof FrontLayout) {
            ((FrontLayout) mFrontView).setSwipeLayout(this);
        } else {
            throw new IllegalArgumentException("Front view must be an instanceof FrontLayout");
        }

        mBackView = getChildAt(0);

    }

    public View getFrontView() {
        return mFrontView;
    }

    public View getBackView() {
        return mBackView;
    }

    public void setShowEdge(ShowEdge showEdit) {
        mShowEdge = showEdit;
        requestLayout();
    }

    public SwipeListener getSwipeListener() {
        return mSwipeListener;
    }

    public void setSwipeListener(SwipeListener mSwipeListener) {
        this.mSwipeListener = mSwipeListener;
    }
}













/*
package com.lee.qqinterface.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;


*/
/**
 * 侧拉删除控件
 *//*

public class SwipeLayout extends FrameLayout implements SwipeLayoutInterface {
    private Status status = Status.Close;
    private OnSwipeLayoutListener swipeLayoutListener;
    private GestureDetectorCompat mGestureDetector;
    private float deltaX;


    public void setSwipeLayoutListener(OnSwipeLayoutListener swipeLayoutListener) {
        this.swipeLayoutListener = swipeLayoutListener;
    }

    protected enum Status {
        Close, Open, Draging
    }

    public interface OnSwipeLayoutListener {

        void onClose(SwipeLayout mSwipeLayout);

        void onOpen(SwipeLayout mSwipeLayout);

        void onDraging(SwipeLayout mSwipeLayout);

        // 要去关闭
        void onStartClose(SwipeLayout mSwipeLayout);

        // 要去开启
        void onStartOpen(SwipeLayout mSwipeLayout);
    }

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mDragHelper = ViewDragHelper.create(this, 1.0f, mCallback);
        mGestureDetector = new GestureDetectorCompat(context, mOnGestureListener);
    }

    private GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // 当横向移动距离大于等于纵向时，返回true
            return Math.abs(distanceX) >= Math.abs(distanceY);
        }
    };

    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        // c. 重写监听
        @Override
        public boolean tryCaptureView(View view, int id) {
            return view == getFrontView() || view == getBackView();
        }

        // 限定移动范围
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mFrontView) {
                if (left > 0) {
                    return 0;
                } else if (left < -mRange) {
                    return -mRange;
                }
            } else if (child == mBackView) {
                if (left > mWidth) {
                    return mWidth;
                } else if (left < mWidth - mRange) {
                    return mWidth - mRange;
                }
            }
            return left;
        }

        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            // 传递事件
            if (changedView == mFrontView) {
                mBackView.offsetLeftAndRight(dx);
            } else if (changedView == mBackView) {
                mFrontView.offsetLeftAndRight(dx);
            }
            dispatchSwipeEvent();
            // 兼容老版本
            invalidate();
        }

        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (xvel == 0 && mFrontView.getLeft() < -mRange / 2.0f) {
                open();
            } else if (xvel < 0) {
                open();
            } else {
                close();
            }
        }
    };
    private ViewDragHelper mDragHelper;
    private View mBackView;
    private View mFrontView;
    private int mHeight;
    private int mWidth;
    private int mRange;


    protected void dispatchSwipeEvent() {

        if (swipeLayoutListener != null) {
            swipeLayoutListener.onDraging(this);
        }
        // 记录上一次的状态
        Status preStatus = status;
        // 更新当前状态
        status = updateStatus();
        if (preStatus != status && swipeLayoutListener != null) {

            if (status == Status.Open) {
                swipeLayoutListener.onOpen(this);
            } else if (status == Status.Close) {
                swipeLayoutListener.onClose(this);
            } else if (status == Status.Draging) {
                if (preStatus == Status.Open) {
                    swipeLayoutListener.onStartClose(this);
                } else if (preStatus == Status.Close) {
                    swipeLayoutListener.onStartOpen(this);
                }
            }
        }
    }


    private Status updateStatus() {

        int left = mFrontView.getLeft();
        if (left == 0) {
            return Status.Close;
        } else if (left == -mRange) {
            return Status.Open;
        }
        return Status.Draging;
    }

    public View getFrontView() {
        return mFrontView;
    }

    public View getBackView() {
        return mBackView;
    }

    @Override
    public Status getCurrentStatus() {
        int left = getFrontView().getLeft();
        if (left == 0) {
            return Status.Close;
        }
        if ((left == 0 - mRange) || (left == mRange)) {
            return Status.Open;
        }
        return Status.Draging;
    }

    public void close() {
        close(true);
    }

    public void close(boolean isSmooth) {
        int finalLeft = 0;
        if (isSmooth) {
            //开始动画
            if (mDragHelper.smoothSlideViewTo(mFrontView, finalLeft, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent(false);
        }
    }

    public void open() {
        // Utils.showToast(getContext(), "Open");
        open(true);
    }

    public void open(boolean isSmooth) {
        int finalLeft = -mRange;
        if (isSmooth) {
            //开始动画
            if (mDragHelper.smoothSlideViewTo(mFrontView, finalLeft, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent(true);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }




    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 决定当前的SwipeLayout是否要把touch事件拦截下来，直接交由自己的onTouchEvent处理
        // 返回true则为拦截
        return mDragHelper.shouldInterceptTouchEvent(ev)& mGestureDetector.onTouchEvent(ev);
    }

    float mDownX;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 当处理touch事件时，不希望被父类onInterceptTouchEvent的代码所影响。
        // 比如处理向右滑动关闭已打开的条目时，如果进行以下逻辑，则不会在关闭的同时引发左边菜单的打开。

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                System.out.println("onTouchEvent+ACTION_DOWN:::" + event.getRawX());
                mDownX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                System.out.println("onTouchEvent+ACTION_MOVE:::" + event.getRawX());

                deltaX = event.getRawX() - mDownX;
                if (deltaX > mDragHelper.getTouchSlop()) {
                    // 请求父级View不拦截touch事件
                    requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                mDownX = 0;

        }

        try {
            mDragHelper.processTouchEvent(event);
        } catch (IllegalArgumentException ignored) {
        }

        return true;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 摆放位置
        layoutContent(false);
    }

    private void layoutContent(boolean isOpen) {
        // 摆放前View
        Rect frontRect = computeFrontViewRect(isOpen);
        mFrontView.layout(frontRect.left, frontRect.top, frontRect.right, frontRect.bottom);
        // 摆放后View
        Rect backRect = computeBackViewViaFront(frontRect);
        mBackView.layout(backRect.left, backRect.top, backRect.right, backRect.bottom);

        // 调整顺序, 把mFrontView前置
        bringChildToFront(mFrontView);
    }

    private Rect computeBackViewViaFront(Rect frontRect) {
        int left = frontRect.right;
        return new Rect(left, 0, left + mRange, mHeight);
    }

    private Rect computeFrontViewRect(boolean isOpen) {
        int left = 0;
        if (isOpen) {
            left = -mRange;
        }
        return new Rect(left, 0, left + mWidth, mHeight);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 当xml被填充完毕时调用
        mBackView = getChildAt(0);
        mFrontView = getChildAt(1);
        if (mFrontView instanceof FrontLayout) {
            ((FrontLayout) mFrontView).setSwipeLayout(this);
        } else {
            throw new IllegalArgumentException("Front view must be an instanceof FrontLayout");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = mFrontView.getMeasuredHeight();
        mWidth = mFrontView.getMeasuredWidth();
        mRange = mBackView.getMeasuredWidth();
    }
}
*/
