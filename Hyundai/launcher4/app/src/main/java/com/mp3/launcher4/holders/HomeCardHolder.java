package com.mp3.launcher4.holders;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.utils.AnimatorUtils;
import com.mp3.launcher4.utils.FontUtils;

/**
 * @author longzj
 */
public class HomeCardHolder extends AbstractHomeHolder {

    private CardView mCardView;
    private TextView mAllView;

    private float mScaleRatio = -1;

    public HomeCardHolder(View itemView, float scaleRatio) {
        super(itemView);
        mScaleRatio = scaleRatio;
        createFocusedAnim(getFocusedSet());
    }

    @Override
    public void focused() {
        super.unfocused();
        final AnimatorSet set = getFocusedSet();
        set.start();
    }

    @Override
    protected void initView(View itemView) {
        mCardView = (CardView) itemView.findViewById(R.id.card);
        mAllView = (TextView) itemView.findViewById(R.id.footer_viewall);
        if (mAllView!=null){
            FontUtils fontUtils = FontUtils.getInstance(itemView.getContext());
            fontUtils.setRegularFont(mAllView);
        }
    }

    @Override
    public void createFocusedAnim(AnimatorSet set) {
        if (mScaleRatio == -1) {
            return;
        }
        Animator transZAnim = AnimatorUtils.createElevationAnim(mCardView, AnimatorUtils.BASE_Z);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(mCardView, mScaleRatio);
        set.playTogether(transZAnim, scaleAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    public void createUnfocusedAnim(AnimatorSet set) {
        Animator transZAnim = AnimatorUtils.createElevationAnim(mCardView, 0);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(mCardView, 1.0f);
        set.playTogether(transZAnim, scaleAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new DecelerateInterpolator());
    }

}
