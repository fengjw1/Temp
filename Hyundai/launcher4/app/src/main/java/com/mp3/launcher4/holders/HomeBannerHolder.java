package com.mp3.launcher4.holders;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.utils.AnimatorUtils;

/**
 * @author longzj
 */
public class HomeBannerHolder extends AbstractHomeHolder<CategoryDetailBean> {

    private static final float SCALE_RATIO = 1.06f;
    private CardView mCardView;
    private ImageView mCover;

    public HomeBannerHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindData(CategoryDetailBean bean) {
        if (bean == null) {
            return;
        }

        String coverUrl = bean.getImage();
        if (!TextUtils.isEmpty(coverUrl)) {
            Glide.with(mCover.getContext())
                    .load(coverUrl)
                    .placeholder(R.color.cardview_dark_background)
                    .centerCrop()
                    .into(mCover);
        }
    }

    @Override
    protected void initView(View itemView) {
        mCardView = (CardView) itemView.findViewById(R.id.card);
        mCover = (ImageView) itemView.findViewById(R.id.banner_cover);
    }

    @Override
    public void createFocusedAnim(AnimatorSet set) {
        Animator transZAnim = AnimatorUtils.createElevationAnim(mCardView, AnimatorUtils.BASE_Z);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(mCardView, SCALE_RATIO);
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
    public void onDetached() {
        super.onDetached();
        mCover.setImageDrawable(null);
    }
}
