package com.mp3.launcher4.customs.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.mp3.launcher4.R;

/**
 * @author longzj
 */
public class SliderIndicatorView extends View {

    private int mBarColor;
    private Paint mPaint;
    private int mBarWidth;
    private int mBarHeight;
    private float mVerticalOffset = 30;
    private float mHorizontalOffset = 0;
    private Rect mRect;
    private RectF mRectF;
    @Keep
    private float mBarAlpha;
    private int mCurrentPos;

    public SliderIndicatorView(Context context) {
        super(context);
        init(context, null);
    }

    public SliderIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SliderIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        initDraw(context);
    }

    public float getVerticalOffset() {
        return mVerticalOffset;
    }

    public void setVerticalOffset(float verticalOffset) {
        mVerticalOffset = verticalOffset;
        invalidate();
    }

    public void setHorizontalOffset(int horizontalOffset) {
        mHorizontalOffset = horizontalOffset;
        invalidate();
    }

    public int getBarHeight() {
        return mBarHeight;
    }

    private void initDraw(Context context) {
        mRect = new Rect();
        mRectF = new RectF();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        int color;
        if (mBarColor != 0) {
            color = context.getResources().getColor(mBarColor);
        } else {
            color = Color.BLACK;
        }
        mPaint.setColor(color);
        mPaint.setAlpha((int) (mBarAlpha * 255));
    }

    @Keep
    public float getBarAlpha() {
        return mBarAlpha;
    }

    @Keep
    public void setBarAlpha(float barAlpha) {
        mBarAlpha = barAlpha;
        mPaint.setAlpha((int) (mBarAlpha * 255));
        invalidate();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SliderIndicatorView);
        mBarColor = array.getResourceId(R.styleable.SliderIndicatorView_barColor, 0);
        mBarWidth = array.getDimensionPixelSize(R.styleable.SliderIndicatorView_barWidth, 4);
        mBarHeight = array.getDimensionPixelSize(R.styleable.SliderIndicatorView_barHeight, 40);
        mBarAlpha = array.getFloat(R.styleable.SliderIndicatorView_barAlpha, 1.0f);
        array.recycle();
        float barArc = mBarWidth / 2;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        drawIndicator(c);
    }

    private void drawIndicator(Canvas canvas) {
        int right = getRight();
        int top = getTop();
        int baseRight = (int) (right - mHorizontalOffset);
        int baseLeft = baseRight - mBarWidth;
        int baseTop = (int) (top + mVerticalOffset);
        int baseBottom = baseTop + mBarWidth * 2;
        mRectF.set(baseLeft, baseTop, right, baseBottom);
        canvas.drawArc(mRectF, 0, -180, true, mPaint);
        canvas.save();
        baseTop = baseBottom - mBarWidth;
        baseBottom += mBarHeight - mBarWidth * 2;
        mRect.set(baseLeft, baseTop, right, baseBottom);
        canvas.drawRect(mRect, mPaint);
        canvas.save();
        baseTop = baseBottom - mBarWidth;
        baseBottom = baseTop + mBarWidth * 2;
        mRectF.set(baseLeft, baseTop, right, baseBottom);
        canvas.drawArc(mRectF, 0, 180, true, mPaint);
        canvas.restore();
    }

}
