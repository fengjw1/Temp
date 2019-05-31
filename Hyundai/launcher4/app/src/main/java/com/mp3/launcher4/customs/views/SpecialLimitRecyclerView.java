package com.mp3.launcher4.customs.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

/**
 * @author longzj
 */
public class SpecialLimitRecyclerView extends LimitedRecyclerView {

    private OnFirstLeftListener mOnFirstLeftListener;

    public SpecialLimitRecyclerView(Context context) {
        super(context);
        init();
    }

    public SpecialLimitRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpecialLimitRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init() {
        setLimitKeycode(KeyEvent.KEYCODE_DPAD_RIGHT);
        setFocusable(false);
        setFocusableInTouchMode(false);
    }

    @Override
    protected boolean limit(int keycode) {
        if (keycode == KeyEvent.KEYCODE_DPAD_LEFT && mOnFirstLeftListener != null) {
            View view = getFocusedChild();
            if (view == null) {
                return super.limit(keycode);
            }
            if (getChildAdapterPosition(view) == 0
                    && mOnFirstLeftListener != null
                    && mOnFirstLeftListener.onLeftFirstAttached()) {
                return true;
            }
        }
        return super.limit(keycode);
    }

    public void setOnFirstLeftListener(OnFirstLeftListener onFirstLeftListener) {
        mOnFirstLeftListener = onFirstLeftListener;
    }

    public interface OnFirstLeftListener {
        boolean onLeftFirstAttached();
    }
}
