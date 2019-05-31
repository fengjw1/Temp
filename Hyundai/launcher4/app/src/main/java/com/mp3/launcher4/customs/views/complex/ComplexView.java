package com.mp3.launcher4.customs.views.complex;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;

import com.mp3.launcher4.customs.views.TvRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author longzj
 */
public class ComplexView extends TvRecyclerView implements ComplexImpl {

    private ComplexAdapter mAdapter;
    private ComplexLayoutManager mLayoutManager;
    private List<BaseComplexProxy> mData;
    /**
     * 组容器对应的映射，当两个或者以上代理定义是同一组时，代理位置映射到同一组位置，见{@link BaseComplexProxy#isGroupAbove()}
     */
    private SparseIntArray mGroupNumMap;
    private int mCurrentGroupPos;
    private int mCurrentFocusPos;
    private boolean mScrollByUser;

    private OnGroupScrollListener mOnGroupScrollListener;
    private OnFocusLeaveFromLeftListener mOnFocusLeaveFromLeftListener;

    public ComplexView(Context context) {
        super(context);
        init(context, null);
    }

    public ComplexView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    public ComplexView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initRecycler();
    }

    private void initRecycler() {
        initParams();
        setClipToPadding(false);
        setClipChildren(false);
        setLayoutManager(mLayoutManager);
        setAdapter(mAdapter);
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        addOnScrollListener(new OnScrollListener());
        setItemViewCacheSize(4);
        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                int pos = getChildAdapterPosition(view);
                mData.get(pos).onAttachedToRecycler();
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                int pos = getChildAdapterPosition(view);
                mData.get(pos).onDetachToRecycler();
            }
        });
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                View view = getChildAt(0);
                if (view != null) {
                    view.requestFocus();
                }
            }
        });
    }

    void initParams() {
        mCurrentGroupPos = -1;
        mData = new ArrayList<>();
        mGroupNumMap = new SparseIntArray();
        mAdapter = new ComplexAdapter(getContext(), mData);
        mLayoutManager = new ComplexLayoutManager();
    }

    public void notifyDataSetChanged() {
        getAdapter().notifyDataSetChanged();
    }

    /**
     * 设置初始数据并让视图加载出来
     *
     * @param data 代理数据集
     */
    public void setData(List<BaseComplexProxy> data) {
        mData.clear();
        mData.addAll(data);
        mGroupNumMap.clear();
        int index = -1;
        int count = 0;
        for (BaseComplexProxy proxy : mData) {
            proxy.setComplexRecycler(this);
            if (proxy.isGroupAbove()) {
                mGroupNumMap.put(count, index);
            } else {
                mGroupNumMap.put(count, ++index);
            }
            count++;
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void scrollDown() {
        scrollByDirection(false);
    }

    @Override
    public void scrollUp() {
        scrollByDirection(true);
    }

    @Override
    public void scrollSelf() {
        View view = getFocusedChild();
        int pos = getChildAdapterPosition(view);
        scrollInternal(pos);
    }

    @Override
    public void leaveFromLeft() {
        if (mOnFocusLeaveFromLeftListener != null) {
            mOnFocusLeaveFromLeftListener.onLeftLeaved();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return getScrollState() != SCROLL_STATE_IDLE || super.dispatchKeyEvent(event);
    }

    /**
     * 上下滑动时，依据滑动方向滑动视图
     *
     * @param scrollUp 是否上滑
     */
    private void scrollByDirection(boolean scrollUp) {

        View view = getFocusedChild();
        if (view == null) {
            return;
        }

        int nextPos = getChildAdapterPosition(view);
        if (scrollUp) {
            nextPos -= 1;
        } else {
            nextPos += 1;
        }
        final int nextPosition = nextPos;
        scrollInternal(nextPosition);

    }

    /**
     * 设置组滑动，让小组的第一项滑动至顶部
     *
     * @param groupPosition 需要滑动到的组位置
     */
    public void scrollToGroupPosition(int groupPosition) {
        if (mCurrentGroupPos == groupPosition) {
            return;
        }
        final int pos = mGroupNumMap.indexOfValue(groupPosition);
        BaseComplexProxy proxy = mData.get(mCurrentFocusPos);
        proxy.beforeGroupScrolling();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollInternal(pos);
                mScrollByUser = false;
            }
        }, proxy.getScrollDelayTime());

    }

    /**
     * 滑动的具体实现，计算滑动距离并赋值各种当前组变量
     *
     * @param pos 当前获取焦点的位置
     */
    private void scrollInternal(int pos) {
        if (mData.isEmpty() || pos < 0 || pos >= mData.size()) {
            return;
        }
        int offset = mLayoutManager.getScrollOffset(pos);
        if (offset != 0) {
            smoothScrollBy(0, offset);
        }
        int groupPos = mGroupNumMap.get(pos);
        if (groupPos != mCurrentGroupPos && mOnGroupScrollListener != null) {
            mOnGroupScrollListener.onGroupChanged(groupPos, offset);
            mCurrentGroupPos = groupPos;
        }
        mCurrentFocusPos = pos;
        mScrollByUser = true;
    }

    public void setOnGroupScrollListener(OnGroupScrollListener onGroupScrollListener) {
        mOnGroupScrollListener = onGroupScrollListener;
    }

    public void setOnFocusLeaveFromLeftListener(OnFocusLeaveFromLeftListener onFocusLeaveFromLeftListener) {
        mOnFocusLeaveFromLeftListener = onFocusLeaveFromLeftListener;
    }

    /**
     * 默认让第一个子页面处理焦点
     */
    public void firstChildRequestFocus() {
        mData.get(mCurrentFocusPos).refocus(true);
    }

    /**
     * 绑定组件的生命周期OnResume,并通知子页面处理
     */
    public void onActivityResume() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        }
        int itemCount = getAdapter().getItemCount();
        int pos = getChildAdapterPosition(getChildAt(0));
        for (int index = pos; index < itemCount; index++) {
            BaseComplexProxy proxy = mData.get(index);
            if (proxy.isAttachToRecycle()) {
                proxy.onActivityResume();
            }
        }
    }

    public void onActivityPaused() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        }
        int itemCount = getAdapter().getItemCount();
        int pos = getChildAdapterPosition(getChildAt(0));
        for (int index = pos; index < itemCount; index++) {
            BaseComplexProxy proxy = mData.get(index);
            if (proxy.isAttachToRecycle()) {
                proxy.onActivityPaused();
            }
        }
    }

    /**
     * 绑定组件的生命周期OnStopped,并通知子页面处理
     */
    public void onActivityStopped() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        }
        int itemCount = getAdapter().getItemCount();
        int pos = getChildAdapterPosition(getChildAt(0));
        for (int index = pos; index < itemCount; index++) {
            BaseComplexProxy proxy = mData.get(index);
            if (proxy.isAttachToRecycle()) {
                proxy.onActivityStopped();
            }
        }
    }

    /**
     * 绑定组件的生命周期OnDestroy,并通知子页面处理
     */
    public void onActivityDestroy() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        }
        int itemCount = getAdapter().getItemCount();
        int pos = getChildAdapterPosition(getChildAt(0));
        for (int index = pos; index < itemCount; index++) {
            BaseComplexProxy proxy = mData.get(index);
            if (proxy.isAttachToRecycle()) {
                proxy.onActivityDestroy();
            }
        }
    }

    public void onNetworkConnected(boolean hasNetwork) {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        }
        int itemCount = getAdapter().getItemCount();
        int pos = getChildAdapterPosition(getChildAt(0));
        for (int index = pos; index < itemCount; index++) {
            BaseComplexProxy proxy = mData.get(index);
            if (proxy.isAttachToRecycle()) {
                proxy.onNetWorkConnected(hasNetwork);
            }
        }
    }

    public interface OnGroupScrollListener {

        /**
         * 组滑动时，小组下标改变并返回总的滑动距离供外部计算使用
         *
         * @param pos          小组位置
         * @param scrollOffset 需要滑动的总位置
         */
        void onGroupChanged(int pos, int scrollOffset);

        /**
         * 组纵向滑动的距离，非叠加态
         *
         * @param dy 滑动距离
         */
        void onGroupScrolled(float dy);

        /**
         * 组滑动状态变化
         *
         * @param state 状态
         */
        void onGroupScrollStateChanged(int state);
    }

    public interface OnFocusLeaveFromLeftListener {
        /**
         * 焦点即将从本视图左边离开，在离开前的拦截操作
         */
        void onLeftLeaved();
    }

    private class OnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            for (BaseComplexProxy proxy : mData) {
                proxy.onParentScrollStateChange(newState);
            }
            if (mOnGroupScrollListener != null) {
                mOnGroupScrollListener.onGroupScrollStateChanged(newState);
            }
            if (newState == SCROLL_STATE_IDLE) {
                if (mScrollByUser) {
                    mData.get(mCurrentFocusPos).refocus(true);
                    mScrollByUser = false;
                }
                //最后校正
                smoothScrollBy(0, mLayoutManager.getScrollOffset(mCurrentFocusPos));
            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mOnGroupScrollListener != null) {
                mOnGroupScrollListener.onGroupScrolled(dy);
            }
            checkFocusState();
        }

        /**
         * 检查滑动后的子视图的焦点状态，如果未获取到焦点，则让子视图处理获取
         */
        private void checkFocusState() {
            if (!mScrollByUser) {
                return;
            }
            View focusedChild = getFocusedChild();
            if (focusedChild != null) {
                int pos = getChildAdapterPosition(focusedChild);
                if (pos == mCurrentFocusPos) {
                    mScrollByUser = false;
                    return;
                }
            }
            if (mData.get(mCurrentFocusPos).refocus(false)) {
                mScrollByUser = false;
            }
        }
    }
}