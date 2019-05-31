package com.ktc.ecuador.view;

import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Scroller;

import com.ktc.ecuador.utils.ImageUtils;
import com.ktc.ecuador.view.layoutManagers.HorizontalLayoutManager;

public class CenterRecyclerView extends RecyclerView implements ViewTreeObserver.OnGlobalFocusChangeListener {

    private static final int DIR_LEFT = 0;
    private static final int DIR_RIGHT = 1;
    private final static String MODEL_348 = "TV348_ISDB";
    /**
     * 当数据为空时展示的View
     */
    private View mEmptyView;
    private HorizontalLayoutManager mLayoutManager;

    private Scroller mScroller;
    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && mEmptyView != null) {
                if (adapter.getItemCount() == 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                    CenterRecyclerView.this.setVisibility(View.GONE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                    CenterRecyclerView.this.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    public CenterRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        getViewTreeObserver().addOnGlobalFocusChangeListener(this);
        mLayoutManager = new HorizontalLayoutManager(context);
        setLayoutManager(mLayoutManager);
    }

    public CenterRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public CenterRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keycode = event.getKeyCode();
        if (action != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event);
        }
        View view = getFocusedChild();
        if (view == null) {
            return super.dispatchKeyEvent(event);
        }
        boolean isHandled;
        if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
            isHandled = getChildAdapterPosition(view) == 0;
        } else if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            isHandled = getChildAdapterPosition(view) == mLayoutManager.getItemCount() - 1;
        } else {
            return super.dispatchKeyEvent(event);
        }

        if (isHandled) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }
        emptyObserver.onChanged();
    }

    @Override
    public void onGlobalFocusChanged(View view, View view1) {
        if (view1 instanceof CardView) {
            int position = getChildAdapterPosition(view1);
            if (position != NO_POSITION) {
                refreshFocusItemToCenter();
            }
        }

    }

    public void refreshFocusItemToCenter() {
        View tView = getFocusedChild();
        if (tView == null) {
            return;
        }
        int[] tPosition = new int[2];
        tView.getLocationInWindow(tPosition);
        String model = SystemProperties.get("ro.product.model", MODEL_348);
        int tDes = (int) ((this.getX() + (model.equals(MODEL_348)?ImageUtils.getScreenWidth():this.getWidth()) / 2) - tView.getWidth() * tView.getScaleX() / 2);
        if (tPosition[0] != tDes) {
            this.smoothScrollBy(tPosition[0] - tDes, 0);
        }
    }
}
