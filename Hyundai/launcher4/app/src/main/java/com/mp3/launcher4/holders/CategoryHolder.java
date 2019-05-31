package com.mp3.launcher4.holders;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.CategoryBean;
import com.mp3.launcher4.utils.AnimatorUtils;

/**
 * @author longzj
 */
public class CategoryHolder extends AbstractHomeHolder<CategoryBean> {

    private ImageView mCover;
    private TextView mTitle;
    private CardView mCardView;
    private View mShadow;

    public CategoryHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindData(CategoryBean bean) {
        if (bean == null) {
            return;
        }
        String title = bean.getDisplay();
        String coverUrl = bean.getImage();

        if (!TextUtils.isEmpty(title)) {
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText(title);
        } else {
            mTitle.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(coverUrl)) {
            Glide.with(mCover.getContext())
                    .load(coverUrl)
                    .centerCrop()
                    .into(mCover);
        }
    }

    @Override
    protected void initView(View itemView) {
        mCover = (ImageView) itemView.findViewById(R.id.category_cover);
        mTitle = (TextView) itemView.findViewById(R.id.category_title);
        mCardView = (CardView) itemView.findViewById(R.id.card);
        mShadow = itemView.findViewById(R.id.category_shadow);
    }

    @Override
    public void createFocusedAnim(AnimatorSet set) {
        Animator transZAnim = AnimatorUtils.createElevationAnim(mCardView, AnimatorUtils.BASE_Z);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(mCardView, AnimatorUtils.BASE_RECTANGLE_SCALE);
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
        mCover.setImageDrawable(null);
    }
}
