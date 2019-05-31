package com.mp3.launcher4.holders;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mp3.launcher4.LauncherApplication;
import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.utils.AnimatorUtils;
import com.mp3.launcher4.utils.FontUtils;
import com.mp3.launcher4.utils.ImageUtils;

import static android.security.KeyStore.getApplicationContext;
import static android.view.View.LAYER_TYPE_SOFTWARE;

/**
 * @author longzj
 */
public class CategoryOtherHolder extends AbstractHomeHolder<CategoryDetailBean> {

    private ImageView mCover;
    private TextView mTitle;
    private CardView mCardView;
    private Context mContext;

    public CategoryOtherHolder(View itemView) {
        super(itemView);

    }

    public ImageView getCover() {
        return mCover;
    }

    public TextView getTitle() {
        return mTitle;
    }

    @Override
    public void bindData(CategoryDetailBean bean) {
        if (bean == null) {
            return;
        }
        String title = bean.getTitle();
        String description = bean.getDescription();
        String coverUrl = bean.getImage();
        if (!TextUtils.isEmpty(description)) {
            mTitle.setText(description);
        }
        if (!TextUtils.isEmpty(title)) {
            mTitle.setText(title);
        }
        if (!TextUtils.isEmpty(coverUrl)) {
            Glide.with(mCover.getContext()).load(coverUrl)
                    //.bitmapTransform(new CropCornerBitmapTransform(mCover.getContext(), 4))
                    .placeholder(R.drawable.default_place_hoder)
                    .override(375, 210).into(mCover);
        }
    }

    @Override
    protected void initView(View itemView) {
        mCover = (ImageView) itemView.findViewById(R.id.other_cover);
        mTitle = (TextView) itemView.findViewById(R.id.other_title);
        mCardView = itemView.findViewById(R.id.card);
        mContext = itemView.getContext().getApplicationContext();
        FontUtils utils = FontUtils.getInstance(mContext);
        utils.setMuliRegularFont(mTitle);

    }
    @Override
    public void onDetached() {
        super.onDetached();
        mCover.setImageDrawable(null);
    }

    @Override
    public void createFocusedAnim(AnimatorSet set) {
        Animator otherAnim = AnimatorUtils.createScaleAnim(mCardView, 1.15f);
        Animator otherAlphaAnim = AnimatorUtils.createAlphaAnim(mTitle, 0.4f);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.playTogether(otherAnim, otherAlphaAnim);
        reloadOtherCategoryImage(true);

    }

    @Override
    public void createUnfocusedAnim(AnimatorSet set) {
        Animator otherAnim = AnimatorUtils.createScaleAnim(mCardView, 1.0f);
        Animator otherAlphaAnim = AnimatorUtils.createAlphaAnim(mTitle, 0.4f);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.playTogether(otherAnim, otherAlphaAnim);
        reloadOtherCategoryImage(false);

    }



    /**
     * 重设其他分类视图中图的大小
     * @param hasSelected 是否选中
     */
    private void reloadOtherCategoryImage(boolean hasSelected) {
        int heightDp = hasSelected ? 107 : 50;
        ViewGroup.LayoutParams params = mTitle.getLayoutParams();
        params.height = ImageUtils.dp2Px(mContext, heightDp);
        mTitle.setMaxLines(hasSelected ? 5 : 2);
        mTitle.setLayoutParams(params);
    }
}
