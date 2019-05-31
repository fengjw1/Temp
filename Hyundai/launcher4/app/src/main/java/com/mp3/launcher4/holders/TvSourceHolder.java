package com.mp3.launcher4.holders;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.TvSourceBean;
import com.mp3.launcher4.utils.AnimatorUtils;
import com.mp3.launcher4.utils.FontUtils;

/**
 * @author longzj
 */
public class TvSourceHolder extends AbstractHomeHolder<TvSourceBean> {

    private ImageView mIcon;
    private TextView mName;
    private CardView mCardView;
    private View mShadow;
    private Context mContext;

    public TvSourceHolder(View itemView) {
        super(itemView);
        this.mContext = itemView.getContext();

    }

    @Override
    public void bindData(TvSourceBean data) {
        mIcon.setImageResource(data.getSourceIcon());
        mName.setText(data.getTvNameId());
    }

    @Override
    protected void initView(View itemView) {
        mIcon = (ImageView) itemView.findViewById(R.id.source_icon);
        mName = (TextView) itemView.findViewById(R.id.source_name);
        mCardView = (CardView) itemView.findViewById(R.id.card);
        mShadow = itemView.findViewById(R.id.source_shadow);
        FontUtils fontUtils = FontUtils.getInstance(mContext);
        fontUtils.setRegularFont(mName);
    }

    @Override
    public void createFocusedAnim(AnimatorSet set) {
        Animator transZAnim = AnimatorUtils.createElevationAnim(mCardView, AnimatorUtils.BASE_Z);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(mCardView, AnimatorUtils.BASE_SQUARE_SCALE);
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

    @Override
    public void unfocused() {
        super.unfocused();
        mShadow.setAlpha(0.45f);
    }

    @Override
    public void focused() {
        super.focused();
        mShadow.setAlpha(0f);
    }

    @Override
    public void onDetached() {
        super.onDetached();
        mIcon.setImageDrawable(null);
    }
}
