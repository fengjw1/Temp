package com.ktc.ecuador.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

/**
 * @author longzj
 */
public class PredictRecyclerView extends RecyclerView {

    private static final int DIR_LEFT = 0;
    private static final int DIR_RIGHT = 1;
    /**
     * 当数据为空时展示的View
     */
    private View mEmptyView;
    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && mEmptyView != null) {
                if (adapter.getItemCount() == 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                    PredictRecyclerView.this.setVisibility(View.GONE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                    PredictRecyclerView.this.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    public PredictRecyclerView(Context context) {
        super(context);
    }

    public PredictRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PredictRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    && decideFocus(DIR_LEFT)) {
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    && decideFocus(DIR_RIGHT)) {
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

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }
        emptyObserver.onChanged();
    }
}
