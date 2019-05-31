package com.mp3.launcher4.customs.views.complex;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;

/**
 * @author longzj
 */
public abstract class BaseComplexProxy<T extends RecyclerView.ViewHolder> {

    /**
     * 本代理对应的视图类型，先定义两种类型，均在{@link ComplexAdapter}中定义
     */
    private int viewType;
    /**
     * 主容器Complex暴露的接口
     */
    private ComplexImpl mComplexRecycler;
    private Context mContext;
    /**
     * 父视图是否滑动的判断标志位
     */
    private boolean isParentScrolling;
    /**
     * 本代理视图是否附着在主视图的判断标志位
     */
    private boolean isAttachToRecycle;
    /**
     * 本代理对应的视图Holder 对应视图类型{@code viewType}定义的Holder
     */
    private WeakReference<T> mHolder;
    /**
     * 是否忽略更新，此变量决定主视图滑动或者Activity执行OnResume后是否重新请求数据，请求的方法定义为{@link #requestData()}
     */
    private boolean isIgnoreUpdateData = false;
    /**
     * 是否已经请求，此变量控制请求的次数，在请求结束后应当重置此变量为false,重置方法为 {@link #resetRequestedFlag()}
     */
    private boolean isRequested = false;

    public BaseComplexProxy(Context context, int viewType) {
        this.viewType = viewType;
        mContext = context;
    }

    protected boolean isParentScrolling() {
        return isParentScrolling;
    }

    protected boolean isAttachToRecycle() {
        return isAttachToRecycle;
    }

    protected void onAttachedToRecycler() {
        isAttachToRecycle = true;
    }

    protected void onDetachToRecycler() {
        isAttachToRecycle = false;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public Context getContext() {
        return mContext;
    }

    public void load(T holder) {
        onBindViews(holder);
        if (mHolder == null) {
            mHolder = new WeakReference<>(holder);
        }
    }

    public T getHolder() {
        if (mHolder == null) {
            return null;
        }
        return mHolder.get();
    }

    /**
     * 初始化holder内的view
     *
     * @param holder holder
     * @return 是否需要去重新加载数据
     */
    public abstract boolean onBindViews(T holder);

    public ComplexImpl getComplexRecycler() {
        return mComplexRecycler;
    }

    /**
     * 设置主容器代理接口
     *
     * @param complexRecycler 主容器代理接口 complexRecyclerImpl
     */
    public void setComplexRecycler(ComplexImpl complexRecycler) {
        mComplexRecycler = complexRecycler;
    }

    /**
     * 是否同上一个item为同一个组，如果是，则在父RecyclerView中会将其归为同一个组，其体现在侧滑菜单中
     *
     * @return true - 是，false - 否
     */
    public abstract boolean isGroupAbove();

    public boolean ignoreUpdate() {
        return isIgnoreUpdateData;
    }

    /**
     * 设置是否忽略requestData请求
     *
     * @param ignoreUpdateData true - 是 ，false - 否
     */
    public void setIgnoreUpdateData(boolean ignoreUpdateData) {
        isIgnoreUpdateData = ignoreUpdateData;
    }

    /**
     * 滑动后获取焦点逻辑，因为向屏幕外的视图滑动时，视图还未添加至父布局中，</br>
     * 在添加后系统不会主动处理焦点，故需要子页面自处理
     *
     * @param isLastChange 是否是最后一次请求的机会
     * @return 请求成功返回true，否则返回false
     */
    public boolean refocus(boolean isLastChange) {
        return isLastChange;
    }

    /**
     * 请求获取数据并刷新本页面的RecyclerView
     */
    public void requestData() {
        isRequested = true;
    }

    /**
     * 取消请求获取数据
     */
    public void cancelRequest() {
        isRequested = false;
    }

    /**
     * 监听父recyclerView的滑动情况，并决定是否重新加载数据
     *
     * @param newState RecyclerView的滑动状态
     */
    public void onParentScrollStateChange(int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            isParentScrolling = false;
            Glide.with(getContext()).resumeRequests();
            if (isAttachToRecycle() && !ignoreUpdate() && !isRequested) {
                requestData();
            }
        } else {
            isParentScrolling = true;
            cancelRequest();
            Glide.with(getContext()).pauseRequests();
        }
    }

    /**
     * 同Activity或Fragment的生命周期OnResume绑定，需要父布局在其中手动绑定
     */
    public void onActivityResume() {
        if (!isIgnoreUpdateData && !isRequested) {
            requestData();
        }
    }

    /**
     * 同Activity或Fragment的生命周期OnResume绑定，需要父布局在其中手动绑定
     */
    public void onActivityPaused() {
        cancelRequest();
    }


    /**
     * 同Activity或Fragment的生命周期OnStop绑定，需要父布局在其中手动绑定
     */
    public void onActivityStopped() {

    }

    /**
     * 同Activity或Fragment的生命周期OnDestroy绑定，需要父布局在其中手动绑定
     */
    public void onActivityDestroy() {

    }

    /**
     * 回收当前页面可能造成内存泄漏的对象
     *
     * @deprecated 由于页面缓存和用户体验的原因，禁用掉该回收操作
     */
    public void onViewRecycled() {
        if (mHolder != null) {
            mHolder.clear();
            mHolder = null;
        }
    }

    /**
     * 本页面的滑动延时
     *
     * @return 延时时间
     */
    public long getScrollDelayTime() {
        return 0;
    }

    /**
     * 在同一个group滑动前的操作，其为了给在非子页面手动滑动时而需要操作时使用，</br>
     * 例如电视页面，在侧滑标题栏滑动是感知不到的，在此之前需要切换信源，并保持滑动流畅
     */
    public void beforeGroupScrolling() {
    }

    /**
     * 网络连接时回调
     */
    void onNetWorkConnected(boolean hasNetwork) {
        if (ignoreNetworkChange()) {
            return;
        }

        if (!ignoreUpdate() && hasNetwork) {
            requestData();
        }
        if (!hasNetwork) {
            cancelRequest();
        }
    }

    /**
     * 是否忽略网络变化 默认不忽略
     *
     * @return true-忽略，反之亦然
     */
    protected boolean ignoreNetworkChange() {
        return false;
    }

    /**
     * 重置请求flag-isRequested
     */
    public void resetRequestedFlag() {
        isRequested = false;
    }
}
