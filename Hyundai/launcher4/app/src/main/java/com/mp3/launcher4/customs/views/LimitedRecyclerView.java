package com.mp3.launcher4.customs.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author longzj
 */
public class LimitedRecyclerView extends TvRecyclerView {

    private Collection<Integer> mKeyLimitList = new HashSet<>();

    public LimitedRecyclerView(Context context) {
        super(context);
    }

    public LimitedRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LimitedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        return action == KeyEvent.ACTION_DOWN && limit(keyCode) || super.dispatchKeyEvent(event);
    }

    public void setLimitKeycode(int... keycodes) {
        mKeyLimitList.clear();
        for (int key : keycodes) {
            mKeyLimitList.add(key);
        }
    }

    protected boolean limit(int keycode) {
        if (!mKeyLimitList.contains(keycode)) {
            return false;
        }
        View focusedView = getFocusedChild();
        if (focusedView == null) {
            return false;
        }
        int searchDir;
        switch (keycode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                searchDir = FOCUS_LEFT;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                searchDir = FOCUS_RIGHT;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                searchDir = FOCUS_UP;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                searchDir = FOCUS_DOWN;
                break;
            default:
                return false;
        }
        View searchView = focusedView.focusSearch(searchDir);
        if (searchView == null) {
            return true;
        }
        int pos = indexOfChild(searchView);
        return pos == NO_POSITION;

    }

    public void addLimitedKey(int keycode) {
        mKeyLimitList.add(keycode);
    }

    public void removeLimitedKey(int keycode) {
        mKeyLimitList.remove(keycode);
    }
}
