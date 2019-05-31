package com.mp3.launcher4.holders;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.utils.AnimatorUtils;
import com.mp3.launcher4.utils.CommonUtils;

/**
 * @author longzj
 */
public class BaseAppHolder extends AbstractHomeHolder<AppDetailBean> {

    public static final int SHADOW_STATE_LIGHT = 0;
    public static final int SHADOW_STATE_NULL = 1;
    public static final int SHADOW_STATE_DARK = 2;

    private ImageView mCover;
    private TextView mTitle;
    private CardView mCardView;
    private Context mContext;
    private View mShadow;


    public BaseAppHolder(View itemView) {
        super(itemView);
        this.mContext = itemView.getContext();
    }

    @Override
    public void bindData(AppDetailBean bean) {

        String url = bean.getUrl();
        if (!TextUtils.isEmpty(url) && CommonUtils.isLocalApp(mContext, bean.getUrl())) {
            handleLocalApps(bean);
        } else {
            handleStoreApp(bean);
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
        itemView.setTranslationZ(1.0f);
        setShadow(SHADOW_STATE_NULL);
    }

    @Override
    protected void initView(View itemView) {
        mCover = (ImageView) itemView.findViewById(R.id.category_cover);
        mTitle = (TextView) itemView.findViewById(R.id.category_title);
        mCardView = (CardView) itemView.findViewById(R.id.card);
        mShadow = itemView.findViewById(R.id.app_shadow);
        mTitle.setAlpha(0);
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
        set.setInterpolator(new DecelerateInterpolator());
    }

    @Override
    public void unfocused() {
        super.unfocused();
        itemView.setTranslationZ(0.0f);
        setShadow(SHADOW_STATE_LIGHT);
    }

    private void handleLocalApps(AppDetailBean bean) {

        PackageManager manager = mContext.getApplicationContext().getPackageManager();
        try {
            String url = bean.getUrl();
            Drawable drawable;
            if(CommonUtils.isSystemApp(mContext,url)){
                drawable = manager.getApplicationBanner(url);
                mCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }else {
                drawable = getIconFromPackageName(url,mContext);
                mCover.setScaleType(ImageView.ScaleType.CENTER);
            }
            if (drawable != null) {
                mCover.setImageDrawable(drawable);
            }else{
                drawable = manager.getApplicationIcon(url);
                mCover.setImageDrawable(drawable);
                mCover.setScaleType(ImageView.ScaleType.CENTER);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mTitle.setText(bean.getTitle());

    }

    private synchronized static Drawable getIconFromPackageName(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            try {
                PackageInfo pi = pm.getPackageInfo(packageName, 0);
                Context otherAppCtx = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
                int defDisplayMetrics = context.getResources().getDisplayMetrics().densityDpi;
                if (defDisplayMetrics==DisplayMetrics.DENSITY_MEDIUM){
                    defDisplayMetrics=DisplayMetrics.DENSITY_HIGH;
                }else {
                    defDisplayMetrics = DisplayMetrics.DENSITY_XXXHIGH;
                }
                int[]  displayMetrics = {DisplayMetrics.DENSITY_XXXHIGH,DisplayMetrics.DENSITY_XXHIGH,DisplayMetrics.DENSITY_XHIGH, DisplayMetrics.DENSITY_HIGH, DisplayMetrics.DENSITY_TV};
                for (int displayMetric : displayMetrics) {
                    try {
                        Drawable d = otherAppCtx.getResources().getDrawableForDensity(pi.applicationInfo.icon, displayMetric);
                        if (d != null&& displayMetric==defDisplayMetrics) {
                            return d;
                        }
                    } catch (Resources.NotFoundException e) {
                        continue;
                    }
                }
            } catch (Exception e) {
                // Handle Error here
            }
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return appInfo.loadIcon(pm);
    }

    private void handleStoreApp(AppDetailBean bean) {
        String imageUrl = bean.getImage();
        String title = bean.getTitle();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(mContext)
                    .load(bean.getImage())
                    .centerCrop()
                    .into(mCover);
        }

        if (!TextUtils.isEmpty(title)) {
            mTitle.setText(title);
        }
    }

    ImageView getCover() {
        return mCover;
    }

    public TextView getTitle() {
        return mTitle;
    }

    CardView getCardView() {
        return mCardView;
    }

    public Context getContext() {
        return mContext;
    }

    public void setShadow(int state) {
        float alpha = 0.25f;
        if (state == SHADOW_STATE_DARK) {
            alpha = 0.75f;
        } else if (state == SHADOW_STATE_NULL) {
            alpha = 0;
        }
        mShadow.setAlpha(alpha);
    }
}
