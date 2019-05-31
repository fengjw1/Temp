package com.mp3.launcher4.holders;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.utils.AnimatorUtils;
import com.mp3.launcher4.utils.FontUtils;

/**
 * @author longzj
 */
public class VideoHolder extends AbstractHomeHolder<CategoryDetailBean> {

    private TextView mTitle;
    private ImageView mCover;
    private CardView mCardView;
    private View mShadowView;


    public VideoHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindData(CategoryDetailBean bean) {

        if (bean == null) {
            return;
        }
        String title = bean.getTitle();
        String coverUrl = bean.getImage();

        if (!TextUtils.isEmpty(title)) {
            mTitle.setText(title);
        }

        if (!TextUtils.isEmpty(coverUrl)) {
            Glide.with(mCover.getContext())
                    .load(coverUrl)
                    .centerCrop()
                    .into(mCover);
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();
        mCover.setImageDrawable(null);
    }

    @Override
    public void focused() {
        super.focused();
        mShadowView.setAlpha(0f);
    }

    @Override
    protected void initView(View itemView) {
        FontUtils fontUtils = FontUtils.getInstance(itemView.getContext().getApplicationContext());
        mCover = (ImageView) itemView.findViewById(R.id.category_cover);
        mTitle = (TextView) itemView.findViewById(R.id.category_title);
        mCardView = (CardView) itemView.findViewById(R.id.card);
        mShadowView = itemView.findViewById(R.id.category_shadow);
        fontUtils.setRegularFont(mTitle);
        mTitle.setAlpha(0);
    }

    @Override
    public void unfocused() {
        super.unfocused();
        mShadowView.setAlpha(0.25f);
    }

    @Override
    public void createFocusedAnim(AnimatorSet set) {
        Animator transZAnim = AnimatorUtils.createElevationAnim(mCardView, AnimatorUtils.BASE_Z);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(mCardView, AnimatorUtils.BASE_RECTANGLE_SCALE);
        Animator titleAlphaAnim = AnimatorUtils.createAlphaAnim(mTitle, 1.0f);
        set.playTogether(transZAnim, scaleAnim, titleAlphaAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    public void createUnfocusedAnim(AnimatorSet set) {
        Animator transZAnim = AnimatorUtils.createElevationAnim(mCardView, 0);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(mCardView, 1.0f);
        Animator titleAlphaAnim = AnimatorUtils.createAlphaAnim(mTitle, 0.0f);
        set.playTogether(transZAnim, scaleAnim, titleAlphaAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new AccelerateInterpolator());
    }

    public TextView getTitle() {
        return mTitle;
    }

    public ImageView getCover() {
        return mCover;
    }
}
