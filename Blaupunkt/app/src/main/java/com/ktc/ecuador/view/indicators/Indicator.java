package com.ktc.ecuador.view.indicators;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Jack Wang
 * @date 2016/8/5
 */

public abstract class Indicator extends Drawable implements Animatable {

    private static final Rect ZERO_BOUNDS_RECT = new Rect();
    private Rect drawBounds = ZERO_BOUNDS_RECT;
    private HashMap<ValueAnimator, ValueAnimator.AnimatorUpdateListener> mUpdateListeners = new HashMap<>();
    private ArrayList<ValueAnimator> mAnimators;
    private int alpha = 255;
    private boolean mHasAnimators;

    private Paint mPaint = new Paint();

    Indicator() {
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    public int getColor() {
        return mPaint.getColor();
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        draw(canvas, mPaint);
    }

    public abstract void draw(Canvas canvas, Paint paint);

    @Override
    public void start() {
        ensureAnimators();

        if (mAnimators == null) {
            return;
        }

        if (isStarted()) {
            return;
        }
        startAnimators();
        invalidateSelf();
    }

    @Override
    public void stop() {
        stopAnimators();
    }

    @Override
    public boolean isRunning() {
        boolean isRunning = false;
        if (mAnimators == null) {
            return false;
        }
        for (ValueAnimator animator : mAnimators) {
            isRunning = isRunning || animator.isRunning();
        }
        return isRunning;
    }    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    private void stopAnimators() {
        if (mAnimators != null) {
            for (ValueAnimator animator : mAnimators) {
                if (animator != null && animator.isStarted()) {
                    animator.removeAllUpdateListeners();
                    animator.end();
                }
            }
        }
    }

    private void ensureAnimators() {
        if (!mHasAnimators) {
            mAnimators = onCreateAnimators();
            mHasAnimators = true;
        }
    }

    private boolean isStarted() {
        boolean isRunning = false;
        if (mAnimators == null) {
            return false;
        }
        for (ValueAnimator animator : mAnimators) {
            isRunning = isRunning || animator.isStarted();
        }
        return isRunning;
    }

    private void startAnimators() {
        for (int i = 0; i < mAnimators.size(); i++) {
            ValueAnimator animator = mAnimators.get(i);

            //when the animator restart , add the updateListener again because they
            // was removed by animator stop .
            ValueAnimator.AnimatorUpdateListener updateListener = mUpdateListeners.get(animator);
            if (updateListener != null) {
                animator.addUpdateListener(updateListener);
            }

            animator.start();
        }
    }

    public abstract ArrayList<ValueAnimator> onCreateAnimators();    @Override
    public int getAlpha() {
        return alpha;
    }

    void addUpdateListener(ValueAnimator animator, ValueAnimator.AnimatorUpdateListener updateListener) {
        mUpdateListeners.put(animator, updateListener);
    }

    void postInvalidate() {
        invalidateSelf();
    }

    public Rect getDrawBounds() {
        return drawBounds;
    }

    private void setDrawBounds(Rect drawBounds) {
        setDrawBounds(drawBounds.left, drawBounds.top, drawBounds.right, drawBounds.bottom);
    }

    public int getWidth() {
        return drawBounds.width();
    }    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    public int getHeight() {
        return drawBounds.height();
    }

    public int centerX() {
        return drawBounds.centerX();
    }

    public int centerY() {
        return drawBounds.centerY();
    }

    public float exactCenterX() {
        return drawBounds.exactCenterX();
    }    private void setDrawBounds(int left, int top, int right, int bottom) {
        this.drawBounds = new Rect(left, top, right, bottom);
    }

    public float exactCenterY() {
        return drawBounds.exactCenterY();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }










    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        setDrawBounds(bounds);
    }


}
