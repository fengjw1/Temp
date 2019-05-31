package com.mp3.launcher4.customs.views.complex;

/**
 * @author longzj
 */
public interface ComplexImpl {

    /**
     * 调用此RecycleView 向下滑
     */
    void scrollDown();

    /**
     * 调用此RecycleView 向上滑
     */
    void scrollUp();

    /**
     * 无特定滑动方向，根据当前焦点位置选择滑动
     */
    void scrollSelf();

    /**
     * 焦点从视图左边即将离开时调用
     */
    void leaveFromLeft();
}
