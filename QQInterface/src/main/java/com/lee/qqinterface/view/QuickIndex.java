package com.lee.qqinterface.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.lee.qqinterface.uitils.Utils;

public class QuickIndex extends View {
    private static final String[] LETTERS = new String[]{
            "A", "B", "C", "D", "E", "F",
            "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X",
            "Y", "Z"};
    private int height;//控件高度
    private int width;//控件宽度
    private float cellHeight;//每个字母的平分的高度
    private OnLetterUpdateListener listener;

    public QuickIndex(Context context) {
        this(context, null);
    }

    public QuickIndex(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickIndex(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface OnLetterUpdateListener {
        void onLetterUpdate(String letter);

        void onViewReleased();
    }

    /**
     * 设置字母更新监听
     *
     * @param listener
     */
    public void setListener(OnLetterUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//ANTI_ALIAS_FLAG:抗锯齿

        mPaint.setTextSize(Utils.dip2Dimension(15, getContext()));

        for (int i = 0; i < LETTERS.length; i++) {
            String str = LETTERS[i];
            float textWidth = mPaint.measureText(str);
            Rect bounds = new Rect();
            mPaint.getTextBounds(str, 0, str.length(), bounds);
            int textHeight = bounds.height();
            mPaint.setColor(i == touchIndex ? Color.BLACK : Color.WHITE);

            canvas.drawText(str, width / 2.0f - textWidth / 2.0f, i * cellHeight + cellHeight / 2.0f +
                    textHeight * 1.0f / 2, mPaint);
        }
    }

    private int touchIndex = -1;

    public void setTouchIndex(int index) {
        touchIndex = index;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int index = -1;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                index = (int) (event.getY() / cellHeight);
                if (index >= 0 && index < LETTERS.length) {
                    if (index != touchIndex) {
                        touchIndex = index;
                        if (listener != null) {
                            listener.onLetterUpdate(LETTERS[index]);
                        }

                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                index = (int) (event.getY() / cellHeight);
                if (index >= 0 && index < LETTERS.length) {
                    if (index != touchIndex) {
                        touchIndex = index;
                        if (listener != null) {
                            listener.onLetterUpdate(LETTERS[index]);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                index = -1;
                listener.onViewReleased();
                break;
        }
        invalidate();//重新绘制
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = getMeasuredHeight();
        width = getMeasuredWidth();
        cellHeight = height * 1.0f / LETTERS.length;
    }


}







