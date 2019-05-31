package com.mp3.launcher4.proxys;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.customs.layoutmanagers.HorizontalLayoutManager;
import com.mp3.launcher4.customs.views.SpecialLimitRecyclerView;
import com.mp3.launcher4.customs.views.complex.BaseComplexProxy;
import com.mp3.launcher4.customs.views.complex.ComplexAdapter;
import com.mp3.launcher4.customs.views.complex.ComplexImpl;
import com.mp3.launcher4.customs.views.complex.holds.TitleRecyclerHolder;
import com.mp3.launcher4.holders.AbstractHomeHolder;
import com.mp3.launcher4.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author longzj
 */
public abstract class BaseTitleRecyclerProxy<T>
        extends BaseComplexProxy<TitleRecyclerHolder>
        implements BaseAdapter.OnItemSelectListener,
        SpecialLimitRecyclerView.OnFirstLeftListener,
        BaseAdapter.OnItemClickListener,
        BaseAdapter.OnItemDispatchKeyListener {

    /**
     * 预加载数据的个数
     */
    final static int PRELOAD_NUM = 3;
    /**
     * 标题字符串
     */
    private String mTitleStr;
    /**
     * 对应列表的数据集
     */
    private List<T> mData;
    /**
     * 记录上次焦点对应的位置
     */
    private int mLastFocusedAdapterPosition;
    private FontUtils mFontUtils;
    /**
     * 是否有延迟更新，如果有，在对应视图附着至主容器时调用{@link #onPendingUpdate()}刷新视图
     */
    private boolean isPendingUpdate;

    private BaseTitleRecyclerProxy(Context context, String titleStr) {
        super(context, ComplexAdapter.TYPE_RECYCLER_WITH_TITLE);
        mTitleStr = titleStr;
        mFontUtils = FontUtils.getInstance(context.getApplicationContext());
        mData = new ArrayList<>();
        final List<T> list = preloadData();
        if (list != null) {
            mData.addAll(list);
        }
    }

    BaseTitleRecyclerProxy(Context context, int titleId) {
        this(context, context.getString(titleId));
    }

    @Override
    public boolean onBindViews(TitleRecyclerHolder holder) {
        holder.setTitle(mTitleStr);
        mFontUtils.setRegularFont(holder.getTitleText());
        final SpecialLimitRecyclerView recyclerView = holder.getRecyclerView();
        if (getHolder() == null) {
            initRecyclerInternal(holder.getRecyclerView());
            return true;
        }
        return false;
    }

    @Override
    public boolean isGroupAbove() {
        return false;
    }

    @Override
    public boolean refocus(boolean isLastChange) {
        final TitleRecyclerHolder holder = getHolder();
        if (holder == null) {
            return false;
        }
        RecyclerView recyclerView = holder.getRecyclerView();
        if (recyclerView.hasFocus()) {
            return true;
        }
        int childCount = recyclerView.getChildCount();
        int firstPos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0));
        View lastFocusedView = recyclerView.getChildAt(mLastFocusedAdapterPosition - firstPos);
        if (lastFocusedView == null) {
            if (isLastChange && childCount > 0) {
                recyclerView.getChildAt(0).requestFocus();
                return true;
            }
            return false;
        }
        return lastFocusedView.requestFocus();
    }

    @Override
    public void onParentScrollStateChange(int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE
                && isAttachToRecycle()) {
            onPendingUpdate();
        }
        super.onParentScrollStateChange(newState);
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        notifyAllReload();
    }

    void notifyAllReload() {
        int pos = -1;
        if (getHolder() == null) {
            return;
        }
        RecyclerView recyclerView = getHolder().getRecyclerView();
        if (recyclerView != null) {
            pos = recyclerView.getChildAdapterPosition(recyclerView.getFocusedChild());
        }
        notifyItemRangeChanged(0, getData().size(),
                new String[]{"reload", String.valueOf(pos)});
    }

    /**
     * 初始化RecyclerView
     *
     * @param recyclerView RecyclerView
     */
    private void initRecyclerInternal(SpecialLimitRecyclerView recyclerView) {
        HorizontalLayoutManager horizontalLayoutManager = new HorizontalLayoutManager(getContext());
        recyclerView.setLayoutManager(horizontalLayoutManager);
        BaseAdapter adapter = initAdapter(mData);
        adapter.setOnItemDispatchKeyListener(this);
        adapter.setOnItemSelectListener(this);
        adapter.setOnItemClickListener(this);
        int itemDecoCount = recyclerView.getItemDecorationCount();
        for (int index = 0; index < itemDecoCount; index++) {
            recyclerView.removeItemDecorationAt(index);
        }
        recyclerView.addItemDecoration(getItemDecoration());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(getContext()).resumeRequests();
                } else {
                    Glide.with(getContext()).pauseRequests();
                }
            }
        });
        recyclerView.setOnFirstLeftListener(this);
    }

    /**
     * 记录焦点位置
     *
     * @param view 对应获得焦点的子视图
     */
    private void recordLastFocusedPosition(View view) {
        final TitleRecyclerHolder holder = getHolder();
        if (view == null || holder == null) {
            return;
        }
        int lastFocusedPosition = holder.getRecyclerView().getChildAdapterPosition(view);
        if (lastFocusedPosition < 0) {
            return;
        }
        mLastFocusedAdapterPosition = lastFocusedPosition;
    }

    public List<T> getData() {
        return mData;
    }

    /**
     * 在附着在主容器时直接更新视图，没有时重写{@link #isPendingUpdate}延迟刷新
     */
    protected void notifyDataSetChanged() {
        final TitleRecyclerHolder holder = getHolder();
        if (holder == null) {
            return;
        }
        setIgnoreUpdateData(true);
        if (!isAttachToRecycle()) {
            isPendingUpdate = true;
            return;
        }
        isPendingUpdate = false;
        holder.notifyDataSetChanged();
        resetRequestedFlag();
    }

    /**
     * 在附着在主容器时直接更新视图，没有时重写{@link #isPendingUpdate}延迟刷新
     */
    void notifyItemRangeChanged(int from, int size, Object payloads) {
        final TitleRecyclerHolder holder = getHolder();
        if (holder == null) {
            return;
        }
        if (!isAttachToRecycle()) {
            isPendingUpdate = true;
            return;
        }
        isPendingUpdate = false;
        holder.notifyItemRangeChanged(from, size, payloads);
    }

    /**
     * 在附着在主容器时直接更新视图，没有时重写{@link #isPendingUpdate}延迟刷新
     */
    void notifyItemChanged(int pos) {
        final TitleRecyclerHolder holder = getHolder();
        if (holder == null) {
            return;
        }
        if (!isAttachToRecycle()) {
            isPendingUpdate = true;
            return;
        }
        isPendingUpdate = false;
        holder.notifyItemChanged(pos);
    }

    /**
     * 在附着在主容器时直接更新视图，没有时重写{@link #isPendingUpdate}延迟刷新
     */
    void notifyItemRemoved(int pos) {
        final TitleRecyclerHolder holder = getHolder();
        if (holder == null) {
            return;
        }
        if (!isAttachToRecycle()) {
            isPendingUpdate = true;
            return;
        }
        isPendingUpdate = false;
        final RecyclerView recyclerView = holder.getRecyclerView();
        int firstChildPos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0));
        holder.notifyItemRemoved(pos);
    }

    /**
     * 在附着在主容器时直接更新视图，没有时重写{@link #isPendingUpdate}延迟刷新
     */
    void notifyItemInsert(int pos) {
        final TitleRecyclerHolder holder = getHolder();
        if (holder == null) {
            return;
        }
        if (!isAttachToRecycle()) {
            isPendingUpdate = true;
            return;
        }
        isPendingUpdate = false;
        final RecyclerView recyclerView = holder.getRecyclerView();
        int firstChildPos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0));
        scrollToPosition(holder.getRecyclerView(), pos, pos < firstChildPos);
        holder.notifyItemInsert(pos);
    }

    /**
     * 在附着在主容器时直接更新视图，没有时重写{@link #isPendingUpdate}延迟刷新
     */
    void notifyItemMove(int from, int to) {
        final TitleRecyclerHolder holder = getHolder();
        if (holder == null) {
            return;
        }
        if (!isAttachToRecycle()) {
            isPendingUpdate = true;
            return;
        }
        isPendingUpdate = false;
        scrollToPosition(holder.getRecyclerView(), to, to < from);
        holder.notifyItemMove(from, to);
    }

    /**
     * 滑动至指定位置
     *
     * @param recyclerView recyclerView
     * @param pos          adapterPosition
     * @param isLeft       是否向左
     */
    private void scrollToPosition(RecyclerView recyclerView, int pos, boolean isLeft) {

        HorizontalLayoutManager manager = (HorizontalLayoutManager) recyclerView.getLayoutManager();
        int offset = manager.getScrollOffsetForPosition(
                isLeft, pos);
        if (offset != 0) {
            mLastFocusedAdapterPosition = pos;
            recyclerView.smoothScrollBy(offset, 0);
        }


    }

    /**
     * 添加预加载数据
     *
     * @return 预加载数据集
     */
    protected abstract List<T> preloadData();

    /**
     * 初始化adapter
     *
     * @param data data
     * @return BaseAdapter
     */
    protected abstract BaseAdapter<T> initAdapter(List<T> data);

    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
            }
        };
    }

    @Override
    public void onItemSelected(View view, int position) {
        final TitleRecyclerHolder parentHolder = getHolder();
        if (parentHolder == null) {
            return;
        }
        getComplexRecycler().scrollSelf();
        RecyclerView recyclerView = parentHolder.getRecyclerView();
        boolean isLeft = mLastFocusedAdapterPosition >= position;
        mLastFocusedAdapterPosition = position;
        int offset = parentHolder.getLayoutManager().calculateHorizontalOffset(isLeft);
        if (offset != 0) {
            recyclerView.smoothScrollBy(offset, 0);
        }
        RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
        if (holder instanceof AbstractHomeHolder) {
            ((AbstractHomeHolder) holder).focused();
        }
    }

    @Override
    public void onItemUnselected(View view, int position) {
        final TitleRecyclerHolder parentHolder = getHolder();
        if (parentHolder == null) {
            return;
        }
        RecyclerView.ViewHolder holder = parentHolder.getRecyclerView().getChildViewHolder(view);
        if (holder instanceof AbstractHomeHolder) {
            ((AbstractHomeHolder) holder).unfocused();
        }
    }

    @Override
    public boolean onLeftFirstAttached() {
        getComplexRecycler().leaveFromLeft();
        return true;
    }

    @Override
    public void onItemClicked(View view, int position) {

    }

    @Override
    public boolean onDispatchKey(View view, KeyEvent event, int keycode) {
        ComplexImpl impl = getComplexRecycler();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            if (keycode == KeyEvent.KEYCODE_DPAD_UP) {
                impl.scrollUp();
                return true;
            } else if (keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
                impl.scrollDown();
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前视图是否在执行更删改动画
     *
     * @return true - 是
     */
    boolean isNotifyAnimRunning() {
        final TitleRecyclerHolder holder = getHolder();
        return holder != null && holder.isNotifyAnimRunning();
    }

    /**
     * 延迟更新视图
     */
    private void onPendingUpdate() {
        if (isPendingUpdate) {
            isPendingUpdate = false;
            notifyAllReload();
        }
    }
}
