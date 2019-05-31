package com.mp3.launcher4.customs.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author longzj
 */
public class TvRecyclerView extends RecyclerView {

    public TvRecyclerView(Context context) {
        super(context);
        initRecycler();
    }

    public TvRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initRecycler();
    }

    public TvRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initRecycler();
    }

    void initRecycler() {

    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getAction();
        int toolType = e.getToolType(e.getPointerCount() - 1);
        return ((action == MotionEvent.ACTION_MOVE)
                && toolType == MotionEvent.TOOL_TYPE_MOUSE)
                || super.onTouchEvent(e);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        //屏蔽鼠标滚轮事件
        return event.getAction() == MotionEvent.ACTION_SCROLL || super.onGenericMotionEvent(event);
    }
}
