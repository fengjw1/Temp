package com.mp3.launcher4.holders;

import android.animation.AnimatorSet;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author longzj
 */
public abstract class AbstractHomeHolder<T> extends RecyclerView.ViewHolder {

    private AnimatorSet mFocusedSet;
    private AnimatorSet mUnfocusedSet;

    public AbstractHomeHolder(View itemView) {
        super(itemView);
        mFocusedSet = new AnimatorSet();
        mUnfocusedSet = new AnimatorSet();
        initView(itemView);
        createFocusedAnim(mFocusedSet);
        createUnfocusedAnim(mUnfocusedSet);
    }

    public void bindData(T data) {
    }

    public void onDetached() {

    }

    public void focused() {
        mFocusedSet.end();
        mUnfocusedSet.end();
        mFocusedSet.start();
    }

    protected abstract void initView(View itemView);

    public abstract void createFocusedAnim(AnimatorSet set);

    public abstract void createUnfocusedAnim(AnimatorSet set);


    public void unfocused() {
        mUnfocusedSet.end();
        mFocusedSet.end();
        mUnfocusedSet.start();
    }

    public AnimatorSet getFocusedSet() {
        return mFocusedSet;
    }

    public AnimatorSet getUnfocusedSet() {
        return mUnfocusedSet;
    }
}
