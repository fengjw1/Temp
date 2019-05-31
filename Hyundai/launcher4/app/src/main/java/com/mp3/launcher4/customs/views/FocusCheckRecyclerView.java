package com.mp3.launcher4.customs.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author longzj
 */
public class FocusCheckRecyclerView extends RecyclerView
        implements View.OnLayoutChangeListener {

    private boolean isFocusedByUser;

    public FocusCheckRecyclerView(Context context) {
        super(context);
        init(context, null);
    }

    public FocusCheckRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FocusCheckRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    void init(Context context, AttributeSet attrs) {
        initAttributes(context, attrs);
        isFocusedByUser = true;
        setFocusable(false);
        setFocusableInTouchMode(false);
        addOnLayoutChangeListener(this);
    }

    void initAttributes(Context context, AttributeSet attrs) {

    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (isFocusedByUser && getChildCount() > 0) {
            if (getChildAt(0).requestFocus()) {
                isFocusedByUser = false;
            }
        }
    }

    public void setFocusedByUser(boolean focusedByUser) {
        isFocusedByUser = focusedByUser;
    }

}
