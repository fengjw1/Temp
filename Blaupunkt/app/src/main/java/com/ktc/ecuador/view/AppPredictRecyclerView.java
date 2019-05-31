package com.ktc.ecuador.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.ktc.ecuador.adapter.MyAppsAdapter;

/**
 * @author longzj
 */
public class AppPredictRecyclerView extends RecyclerView {

    private static final int DIR_LEFT = 0;
    private static final int DIR_RIGHT = 1;

    public AppPredictRecyclerView(Context context) {
        super(context);
    }

    public AppPredictRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AppPredictRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 事件分发处理流程：
     * 首先在RecycleView中处理左右按键，针对乱获取焦点进行处理。如果当前处于MENU或者MOVE响应中则不进行屏蔽，直接分发给子view处理
     */

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            MyAppsAdapter adapter = (MyAppsAdapter) getAdapter();
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    && decideFocus(DIR_LEFT)) {
                boolean canInterceptEvent = (adapter != null && adapter.menuMode) || (adapter != null && adapter.isMoving);
                if (!canInterceptEvent)
                    return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    && decideFocus(DIR_RIGHT)) {
                boolean canInterceptEvent = (adapter != null && adapter.menuMode) || (adapter != null && adapter.isMoving);
                if (!canInterceptEvent)
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    boolean decideFocus(int direction) {
        View focusedView = getFocusedChild();
        if (focusedView == null) {
            return false;
        }
        View searchView = null;
        if (direction == DIR_LEFT) {
            searchView = focusedView.focusSearch(FOCUS_LEFT);
        } else if (direction == DIR_RIGHT) {
            searchView = focusedView.focusSearch(FOCUS_RIGHT);
        }
        if (searchView == null) {
            return true;
        }
        int pos = indexOfChild(searchView);
        return pos == NO_POSITION;
    }

    /**
     * 设置默认选中.
     */
    public void setDefaultSelect(int pos) {
        ViewHolder vh = (ViewHolder) findViewHolderForAdapterPosition(pos);
        requestFocusFromTouch();
        if (vh != null) {
            vh.itemView.requestFocus();
        }
    }
}
