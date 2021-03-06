package com.mp3.launcher4.customs.layoutmanagers;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author longzj
 */
public class FocusLinearLayoutManager extends LinearLayoutManager {

    public FocusLinearLayoutManager(Context context) {
        super(context);
    }

    public FocusLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public FocusLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public View onInterceptFocusSearch(View focused, int direction) {


        int count = getItemCount();
        int fromPos = getPosition(focused);
        int lastVisibleItemPos = findLastVisibleItemPosition();
        switch (direction) {
            case View.FOCUS_RIGHT:
                fromPos++;
                break;
            case View.FOCUS_LEFT:
                fromPos--;
                break;
            default:
                break;
        }

        if (fromPos < 0 || fromPos >= count) {
            return focused;
        } else {
            if (fromPos > lastVisibleItemPos) {
                scrollToPosition(fromPos);
            }
        }
        return super.onInterceptFocusSearch(focused, direction);
    }
}
