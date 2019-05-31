package com.mp3.launcher4.customs.views.complex;

import android.graphics.Rect;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author longzj
 */
public class ComplexLayoutManager extends RecyclerView.LayoutManager {
    private final static int ADD_TOP = 0;
    private final static int ADD_BOTTOM = 1;
    private final String TAG = getClass().getSimpleName();
    private OrientationHelper mOrientationHelper;
    private List<Rect> mRectList;
    private List<Integer> mHeightList;
    private int mTotalOffset;
    private int mContentTop;
    private int mContentBottom;

    public ComplexLayoutManager() {
        mHeightList = new ArrayList<>();
        mRectList = new ArrayList<>();
        initHelper();
        mTotalOffset = 0;
    }

    private void initHelper() {
        if (mOrientationHelper == null) {
            mOrientationHelper = OrientationHelper.createVerticalHelper(this);
        }
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int count = getItemCount();
        if (count <= 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }

        if (state.isPreLayout()) {
            return;
        }

        initHelper();
        detachAndScrapAttachedViews(recycler);
        mRectList.clear();
        mHeightList.clear();
        int parentBottom = mOrientationHelper.getEndAfterPadding();
        int topOffset = mOrientationHelper.getStartAfterPadding() - mTotalOffset;
        int parentWidth = getWidth();
        int parentHeight = getHeight();
        mContentTop = mOrientationHelper.getStartAfterPadding();
        mContentBottom = mOrientationHelper.getEndAfterPadding();
        for (int index = 0; index < count; index++) {
            View child = recycler.getViewForPosition(index);
            addView(child);
            measureChildWithMargins(child, 0, 0);
            int height = getDecoratedMeasuredHeight(child);
            int width = getDecoratedMeasuredWidth(child);
            int left = getDecoratedLeft(child);
            int top = topOffset;
            int right = left + width;
            int bottom = top + height;
            if (top < mContentBottom) {
                mRectList.add(new Rect(left, top, right, bottom));
            }
            mHeightList.add(height);
            detachAndScrapView(child, recycler);
            topOffset = bottom;
        }
        fillChildren(recycler);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //dy大于0：向上滑
        dy = reviseScrollOffset(dy);
        recycleViews(recycler, dy);
        fillChild(recycler, dy, state);
        offsetChildrenVertical(-dy);
        mTotalOffset += dy;
        return dy;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    public void resetTotalOffset() {
        mTotalOffset = 0;
    }

    private void recycleViews(RecyclerView.Recycler recycler, int dy) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (dy > 0) {
                int childBottom = mOrientationHelper.getDecoratedEnd(child);
                if (childBottom - dy <= mContentTop) {
                    removeAndRecycleView(child, recycler);
                }
            } else {
                int childTop = mOrientationHelper.getDecoratedStart(child);
                if (childTop - dy >= mContentBottom) {
                    removeAndRecycleView(child, recycler);
                }
            }
        }
    }

    private void fillChild(RecyclerView.Recycler recycler, int dy, RecyclerView.State state) {
        if (dy > 0) {
            fillDownChild(recycler, dy, state);
        } else {
            fillUpChild(recycler, dy);
        }
    }

    private void fillDownChild(RecyclerView.Recycler recycler, int dy, RecyclerView.State state) {
        View lastView = getChildAt(getChildCount() - 1);
        int position = getPosition(lastView) + 1;
        int offset = getDecoratedBottom(lastView);
        if (offset - dy < mContentBottom) {
            if (position < state.getItemCount()) {
                layoutScrap(recycler, position, offset, ADD_BOTTOM);
            }
        }
    }

    private void fillUpChild(RecyclerView.Recycler recycler, int dy) {
        View firstView = getChildAt(0);
        int position = getPosition(firstView) - 1;
        int offset = getDecoratedTop(firstView);
        if (offset - dy > mContentTop) {
            if (position >= 0) {
                layoutScrap(recycler, position, offset, ADD_TOP);
            }
        }
    }

    private void layoutScrap(RecyclerView.Recycler recycler, int position, int offset, int direction) {

        int count = getItemCount();
        if (count == 0 || position < 0 || position >= count) {
            return;
        }
        View child = recycler.getViewForPosition(position);
        if (direction == ADD_BOTTOM) {
            addView(child);
        } else if (direction == ADD_TOP) {
            addView(child, 0);
        }

        int width = getDecoratedMeasuredWidth(child);
        int height = getDecoratedMeasuredHeight(child);
        int left = getPaddingLeft();
        int right = left + width;
        int bottom = offset + height;
        mHeightList.remove(position);
        mHeightList.add(position, height);
        if (direction == ADD_BOTTOM) {
            layoutDecorated(child, left, offset, right, bottom);
        } else if (direction == ADD_TOP) {
            layoutDecorated(child, left, offset - height, right, offset);
        }
    }

    private void fillChildren(RecyclerView.Recycler recycler) {

        for (int index = 0; index < mRectList.size(); index++) {
            Rect rect = mRectList.get(index);
            if (rect.top >= mContentBottom
                    || rect.bottom <= mContentTop) {
                continue;
            }
            View view = recycler.getViewForPosition(index);
            addView(view);
            layoutDecorated(view, rect.left, rect.top, rect.right, rect.bottom);
        }
    }

    private int reviseScrollOffset(int dy) {
        int itemCount = getItemCount();
        int count = getChildCount();
        int parentTop = mOrientationHelper.getStartAfterPadding();
        View lastView = getChildAt(count - 1);
        int lastPos = getPosition(lastView);
        if (lastPos == itemCount - 1 && dy > 0) {
            int top = mOrientationHelper.getDecoratedStart(lastView);
            if (top - dy < mOrientationHelper.getStartAfterPadding()) {
                return top - mOrientationHelper.getStartAfterPadding();
            }
        }

        View firstView = getChildAt(0);
        int firstPos = getPosition(firstView);
        if (firstPos == 0 && dy < 0) {
            int top = mOrientationHelper.getDecoratedStart(firstView);
            if (top - dy > mOrientationHelper.getStartAfterPadding()) {
                return top - mOrientationHelper.getStartAfterPadding();
            }
        }
        return dy;
    }

    public int getScrollOffset(final int position) {

        View firstView = getChildAt(0);
        View lastView = getChildAt(getChildCount() - 1);
        if (firstView == null || lastView == null) {
            return 0;
        }
        int firstItem = getPosition(firstView);
        int lastItem = getPosition(lastView);
        int offset = 0;
        if (position < firstItem) {
            for (int index = position; index < firstItem; index++) {
                offset += mHeightList.get(index);
            }
            offset -= getDecoratedTop(firstView);
            offset = -offset;
        } else if (position > lastItem) {
            for (int index = firstItem; index < position; index++) {
                offset += mHeightList.get(index);
            }
            offset += getDecoratedTop(firstView);
        } else {
            int movePosition = position - firstItem;
            offset = getDecoratedTop(getChildAt(movePosition));
        }
        return offset;
    }

}
