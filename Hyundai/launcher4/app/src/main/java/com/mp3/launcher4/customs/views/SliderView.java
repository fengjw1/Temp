package com.mp3.launcher4.customs.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.activities.SearchActivity;
import com.mp3.launcher4.adapters.SliderMenuAdapter;
import com.mp3.launcher4.beans.SliderMenuBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.customs.layoutmanagers.SliderMenuLayoutManager;
import com.mp3.launcher4.customs.views.complex.ComplexView;
import com.mp3.launcher4.utils.CommonUtils;
import com.mp3.launcher4.utils.ImageUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author longzj
 */
public class SliderView extends RelativeLayout
        implements BaseAdapter.OnItemClickListener,
        BaseAdapter.OnItemSelectListener,
        ViewTreeObserver.OnGlobalFocusChangeListener {

    /**
     * 上标题对应的边距值
     */
    private final static int HEADER_TOP_DP = 53;
    /**
     * 中间标题见的边距值
     */
    private final static int ITEM_BOTTOM_DP = 30;

    private View mContentShadowView;
    private View mMenuShadowView;
    private SliderIndicatorView mIndicatorView;
    private LimitedRecyclerView mRecyclerView;
    /**
     * 记录上次设置Active状态的子标题位置，初始化为第一标题
     */
    private int mLastActiveChildIndex = 1;
    /**
     * 是否初始化标志变量
     */
    private boolean isInit = true;
    /**
     * 计算的滑动条滑动的距离值
     */
    private float mLastIndicatorVOffset;
    /**
     * 标志是否已经计算滑动条滑动距离的变量
     */
    private boolean isMeasuredLastIndV;

    /**
     * 展开动画集
     */
    private AnimatorSet mExpandAnimSet = new AnimatorSet();
    /**
     * 收束动画集
     */
    private AnimatorSet mShrinkAnimSet = new AnimatorSet();
    /**
     * 侧标题中带背景色的视图平移的距离
     */
    private int mTranslationDistance;

    /**
     * 记录侧标题RecyclerView是否获得焦点的变量
     */
    private boolean isRecyclerFocused = true;

    /**
     * 需要联动的{@link ComplexView}
     */
    private ComplexView mComplexView;

    /**
     * 记录滑动条是否滑动的变量
     */
    private boolean isIndicatorScrolling = false;

    /**
     * 记录是否忽略焦点变化的变量
     */
    private boolean ignoreFocusChanged = false;


    public SliderView(Context context) {
        super(context);
    }

    public SliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SliderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentShadowView = getChildAt(0);
        mMenuShadowView = getChildAt(1);
        mIndicatorView = (SliderIndicatorView) getChildAt(2);
        mRecyclerView = (LimitedRecyclerView) getChildAt(3);
        getViewTreeObserver().addOnGlobalFocusChangeListener(this);
        initRecycler();
    }

    private void initRecycler() {
        mRecyclerView.setLayoutManager(new SliderMenuLayoutManager(getContext()));
        SliderMenuAdapter adapter = new SliderMenuAdapter(getContext(), initData());
        adapter.setOnItemClickListener(this);
        adapter.setOnItemSelectListener(this);
        mRecyclerView.addOnLayoutChangeListener(new RecyclerLayoutListener());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLimitKeycode(
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT);
    }

    /**
     * 初始化侧标题数据集
     *
     * @return 数据集
     */
    List<SliderMenuBean> initData() {
        List<SliderMenuBean> data = new ArrayList<>();
        data.add(new SliderMenuBean(R.drawable.ic_search_selector, R.string.slide_search));
        data.add(new SliderMenuBean(R.drawable.ic_tv_selector, R.string.slide_source));
        data.add(new SliderMenuBean(R.drawable.ic_trending_selector, R.string.slide_trending));
        data.add(new SliderMenuBean(R.drawable.ic_my_app_selector, R.string.slide_my_app));
        data.add(new SliderMenuBean(R.drawable.ic_app_store_selector, R.string.slide_app_store));
        data.add(new SliderMenuBean(R.drawable.ic_demand_selector, R.string.slide_on_demand));
        data.add(new SliderMenuBean(R.drawable.ic_settings_selector, R.string.slide_settings));
        return data;
    }

    public void bindComplexRecyclerView(ComplexView complexView) {
        mComplexView = complexView;
        mComplexView.setOnGroupScrollListener(new ComplexView.OnGroupScrollListener() {

            int mScrollOffset = 0;

            @Override
            public void onGroupChanged(int pos, int scrollOffset) {
                mScrollOffset = scrollOffset;
                mRecyclerView.getChildAt(mLastActiveChildIndex).setActivated(false);
                setLastActiveChildIndex(pos + 1);
            }

            @Override
            public void onGroupScrolled(float dy) {
                if (mScrollOffset == 0) {
                    return;
                }
                scrollIndicator(dy / mScrollOffset);
            }

            @Override
            public void onGroupScrollStateChanged(int state) {
                if (state == RecyclerView.SCROLL_STATE_IDLE) {
                    mScrollOffset = 0;
                    isIndicatorScrolling = false;
                    isMeasuredLastIndV = false;
                    mIndicatorView.setVerticalOffset(calculateIndicatorOffset());
                    if (!hasFocus()) {
                        mRecyclerView.getChildAt(mLastActiveChildIndex).setActivated(true);
                    } else {
                        mRecyclerView.getChildAt(mLastActiveChildIndex).requestFocus();
                    }
                } else {
                    isIndicatorScrolling = true;
                }
            }
        });
        mComplexView.setOnFocusLeaveFromLeftListener(new ComplexView.OnFocusLeaveFromLeftListener() {
            @Override
            public void onLeftLeaved() {
                mRecyclerView.getChildAt(mLastActiveChildIndex).requestFocus();
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keycode = event.getKeyCode();
        if (action == KeyEvent.ACTION_DOWN) {
            if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT || keycode == KeyEvent.KEYCODE_BACK) {
                mComplexView.firstChildRequestFocus();
                return true;
            }
//            boolean isInterceptScrolling = isIndicatorScrolling && (keycode == KeyEvent.KEYCODE_DPAD_UP
//                    || keycode == KeyEvent.KEYCODE_DPAD_DOWN);
//            if (isInterceptScrolling) {
//                return true;
//            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 计算滑动条偏移距离 详见{@link #calculateIndicatorOffset(View)}
     *
     * @return 滑动距离
     */
    private int calculateIndicatorOffset() {
        View view = mRecyclerView.getChildAt(mLastActiveChildIndex);
        return calculateIndicatorOffset(view);
    }

    /**
     * 计算滑动条偏移距离
     *
     * @param view 需要滑动到对应的标题视图
     * @return 滑动距离
     */
    private int calculateIndicatorOffset(View view) {
        if (view == null) {
            return 0;
        }
        int mid = (view.getBottom() + view.getTop()) / 2;
        mid -= mIndicatorView.getBarHeight() / 2;
        return mid;
    }

    @Override
    public void onItemClicked(View view, int position) {
        mLastActiveChildIndex = position;
        if (position == 0) {
            ignoreFocusChanged = true;
            CommonUtils.startActivityForClass((Activity) getContext(), SearchActivity.class);
        } else if (position == mRecyclerView.getAdapter().getItemCount() - 1) {
            ignoreFocusChanged = true;
            CommonUtils.startActivityForAction(getContext(), "android.settings.SETTINGS");
        } else {
            if (mComplexView != null) {
                mComplexView.firstChildRequestFocus();
                mComplexView.scrollToGroupPosition(position - 1);
            }
        }
    }

    public int getLastActiveChildIndex() {
        return mLastActiveChildIndex;
    }

    public void setLastActiveChildIndex(int lastActiveChildIndex) {
        mLastActiveChildIndex = lastActiveChildIndex;
    }

    /**
     * 滑动条滑动
     *
     * @param ratio 滑动比例
     */
    private void scrollIndicator(float ratio) {
        measureIndicatorScrollOffset();
        float offset = ratio * mLastIndicatorVOffset;
        mIndicatorView.setVerticalOffset(mIndicatorView.getVerticalOffset() + offset);
    }

    /**
     * 计算滑动条需要滑动的距离
     */
    private void measureIndicatorScrollOffset() {
        if (!isMeasuredLastIndV) {
            int nextOffset = calculateIndicatorOffset();
            mLastIndicatorVOffset = nextOffset - mIndicatorView.getVerticalOffset();
            isMeasuredLastIndV = true;
        }
    }

    /**
     * 初始化侧标题展开动画
     */
    private void initExpandAnimSet() {
        Collection<Animator> animators = new ArrayList<>();
        final List<TextView> textViewList = new ArrayList<>();
        int childCount = mRecyclerView.getChildCount();
        for (int index = 0; index < childCount; index++) {
            RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(index));
            if (holder instanceof SliderMenuAdapter.SliderHolder) {
                TextView textView = ((SliderMenuAdapter.SliderHolder) holder).getTitle();
                ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "alpha", 1.0f);
                animators.add(animator);
                textViewList.add(textView);
            }

        }
        ObjectAnimator slideBarAlphaAnim = ObjectAnimator.ofFloat(mIndicatorView, "BarAlpha", 1.0f);
        animators.add(slideBarAlphaAnim);

        ObjectAnimator slideBarTransAnim = ObjectAnimator.ofFloat(mIndicatorView, "TranslationX", 0);
        animators.add(slideBarTransAnim);

        ObjectAnimator menuShadowAlphaAnim = ObjectAnimator.ofFloat(mMenuShadowView, "alpha", 1.0f);
        animators.add(menuShadowAlphaAnim);

        ObjectAnimator menuShadowTransAnim = ObjectAnimator.ofFloat(mMenuShadowView, "TranslationX", 0);
        animators.add(menuShadowTransAnim);

        ObjectAnimator contentShadowAlphaAnim = ObjectAnimator.ofFloat(mContentShadowView, "alpha", 0.75f);
        animators.add(contentShadowAlphaAnim);

        mExpandAnimSet.playTogether(animators);
        mExpandAnimSet.setDuration(300);
        mExpandAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());
        mExpandAnimSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (TextView textView : textViewList) {
                    textView.setVisibility(VISIBLE);
                }
                final View view = mRecyclerView.getChildAt(mLastActiveChildIndex);
                if (view != null && !view.isSelected()) {
                    view.setActivated(false);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    /**
     * 初始化侧标题收束动画
     */
    private void initShrinkAnimSet() {
        Collection<Animator> animators = new ArrayList<>();
        final List<TextView> textViewList = new ArrayList<>();
        int childCount = mRecyclerView.getChildCount();
        for (int index = 0; index < childCount; index++) {
            RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(index));
            if (holder instanceof SliderMenuAdapter.SliderHolder) {
                TextView textView = ((SliderMenuAdapter.SliderHolder) holder).getTitle();
                if (index == 0) {
                    mTranslationDistance = mIndicatorView.getWidth()
                            - ImageUtils.dp2Px(getContext().getApplicationContext(), 36) * 2
                            - ((SliderMenuAdapter.SliderHolder) holder).getIcon().getWidth();
                }
                textViewList.add(textView);
                ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "alpha", 0.0f);
                animators.add(animator);
            }
        }


        ObjectAnimator slideBarAlphaAnim = ObjectAnimator.ofFloat(mIndicatorView, "BarAlpha", 0.7f);
        animators.add(slideBarAlphaAnim);

        ObjectAnimator slideBarTransAnim = ObjectAnimator.ofFloat(mIndicatorView, "TranslationX", -mTranslationDistance);
        animators.add(slideBarTransAnim);

        ObjectAnimator menuShadowAlphaAnim = ObjectAnimator.ofFloat(mMenuShadowView, "alpha", 0.0f);
        animators.add(menuShadowAlphaAnim);

        ObjectAnimator menuShadowTransAnim = ObjectAnimator.ofFloat(mMenuShadowView, "TranslationX", -mTranslationDistance);
        animators.add(menuShadowTransAnim);

        ObjectAnimator contentShadowAlphaAnim = ObjectAnimator.ofFloat(mContentShadowView, "alpha", 0.0f);
        animators.add(contentShadowAlphaAnim);

        mShrinkAnimSet.playTogether(animators);
        mShrinkAnimSet.setDuration(300);
        mShrinkAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());
        mShrinkAnimSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                for (TextView textView : textViewList) {
                    textView.setVisibility(GONE);
                }
                final View view = mRecyclerView.getChildAt(mLastActiveChildIndex);
                if (view != null && !view.isSelected()) {
                    view.setActivated(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        if (ignoreFocusChanged) {
            ignoreFocusChanged = false;
            return;
        }
        int pos = mRecyclerView.indexOfChild(newFocus);
        if (pos == -1 && isRecyclerFocused) {
            mShrinkAnimSet.start();
            isRecyclerFocused = false;
            View view = mRecyclerView.getChildAt(mLastActiveChildIndex);
            if (view != null) {
                view.setActivated(true);
            }
        }
        if (pos != -1 && !isRecyclerFocused) {
            mExpandAnimSet.start();
            isRecyclerFocused = true;
            View view = mRecyclerView.getChildAt(mLastActiveChildIndex);
            if (view != null) {
                view.setActivated(false);
                view.requestFocus();
            }
        }
    }

    @Override
    public void onItemSelected(View view, int position) {
        int lastPos = mRecyclerView.getAdapter().getItemCount() - 1;
        boolean isNeedAnim = position == 0
                || position == lastPos
                || (position == 1 && mLastActiveChildIndex == 0)
                || (position == lastPos - 1 && mLastActiveChildIndex == lastPos);
        if (isNeedAnim) {
            startIndicatorScrollAnim(view);
            mLastActiveChildIndex = position;
            return;
        }

        if (mComplexView != null && mLastActiveChildIndex != position) {
            isIndicatorScrolling = true;
            isMeasuredLastIndV = false;
            mComplexView.scrollToGroupPosition(position - 1);
        }
        mLastActiveChildIndex = position;
    }

    public void shrink() {
        final View view = mRecyclerView.getChildAt(mLastActiveChildIndex);
        if (view != null) {
            view.performClick();
        }
    }

    @Override
    public void onItemUnselected(View view, int position) {

    }

    private void startIndicatorScrollAnim(View view) {
        int offset = calculateIndicatorOffset(view);
        @SuppressLint("AnimatorKeep") ObjectAnimator animator = ObjectAnimator.ofFloat(mIndicatorView,
                "VerticalOffset", offset);
        animator.setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isIndicatorScrolling = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isIndicatorScrolling = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private class RecyclerLayoutListener implements OnLayoutChangeListener {
        @Override
        public void onLayoutChange(final View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
            if (adapter == null) {
                return;
            }
            if (!isInit || mRecyclerView.getChildCount() <= mLastActiveChildIndex) {
                return;
            }
            int itemDecCount = mRecyclerView.getItemDecorationCount();
            for (int index = 0; index < itemDecCount; index++) {
                mRecyclerView.removeItemDecorationAt(index);
            }
            mRecyclerView.addItemDecoration(new ItemDecoration());
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    View activeChild = mRecyclerView.getChildAt(mLastActiveChildIndex);
                    if (activeChild != null) {
                        activeChild.requestFocus();
                    }
                    View view = mRecyclerView.getChildAt(mLastActiveChildIndex);
                    mIndicatorView.setVerticalOffset(calculateIndicatorOffset(view));
                    initExpandAnimSet();
                    initShrinkAnimSet();
                }
            }, 50);

            isInit = false;
        }

    }

    private class ItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            final Context context = getContext().getApplicationContext();
            int itemCount = parent.getAdapter().getItemCount();
            int pos = parent.getChildAdapterPosition(view);
            int top;
            int bottom;

            int tmp = HEADER_TOP_DP * 2 + (itemCount - 3) * ITEM_BOTTOM_DP;
            int space = (getHeight() - ImageUtils.dp2Px(context, tmp) -
                    view.getMeasuredHeight() * itemCount) / 2;
            if (pos == 0) {
                top = ImageUtils.dp2Px(context, HEADER_TOP_DP);
                bottom = space;
            } else if (pos == itemCount - 1) {
                top = space - ImageUtils.dp2Px(context, ITEM_BOTTOM_DP);
                bottom = 0;
            } else {
                top = 0;
                bottom = ImageUtils.dp2Px(context, ITEM_BOTTOM_DP);
            }
            outRect.set(0, top, 0, bottom);
        }
    }
}
