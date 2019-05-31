package com.ktc.ecuador.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

/**
 * @author longzj
 */
public class AnimatorUtils {

    public static final long BASE_TIME = 100;
    public static final float BASE_Z = 5.0f;
    public static final float BASE_RECTANGLE_SCALE = 1.2f;
    public static final float BASE_SQUARE_SCALE = 1.2f;
    public static final float BASE_BANNER_SCALE = 1.1f;
    private final static long APP_TIME = 350;
    private final static float SQUARE_SCALE_RATIO = 1.15f;
    private final static float COMMON_SCALE_RATIO = 1.1f;
    private final static float NORMAL_SCALE_RATIO = 1.0f;
    private final static int NORMAL_TRANS_Z = 0;
    private final static int EXPAND_TRANS_Z = 3;
    private final static int BASE_IN_TIME = 30;
    private final static int BASE_OUT_TIME = 10;

    public static Animator createElevationAnim(View view, float endValue) {
        return ObjectAnimator.ofFloat(view, "translationZ", endValue);
    }

    public static Animator createAlphaAnim(View view, float endValue) {
        return ObjectAnimator.ofFloat(view, "alpha", endValue);
    }

    public static Animator createScaleAnim(View view, float endValue) {
        PropertyValuesHolder xHolder = PropertyValuesHolder.ofFloat("scaleX", endValue);
        PropertyValuesHolder yHolder = PropertyValuesHolder.ofFloat("scaleY", endValue);
        return ObjectAnimator.ofPropertyValuesHolder(view, xHolder, yHolder);
    }

    public static Animator createAlphaAnim(View view, float start, float end) {
        return ObjectAnimator.ofFloat(view, "alpha", start, end);
    }

    public static AnimatorSet createCommonInScaleAnim(View view, float ratio, long duration) {
        Animator scaleAnim = createScaleAnimator(view, ratio, ratio);
        Animator elevationAnim = createTransZAnimator(view, EXPAND_TRANS_Z);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(duration);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(scaleAnim, elevationAnim);
        return set;
    }

    public static Animator createScaleAnimator(View view, float scaleX, float scaleY) {
        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", scaleX);
        PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", scaleY);
        return ObjectAnimator.ofPropertyValuesHolder(view, scaleXHolder, scaleYHolder);
    }

    public static Animator createTransZAnimator(View view, int translationZDp) {
        return ObjectAnimator.ofFloat(view, "TranslationZ",
                ImageUtils.dp2Px(view.getContext(), translationZDp));
    }

    public static AnimatorSet createCommonOutScaleAnim(View view, float ratio, long duration) {
        Animator scaleAnim = createScaleAnimator(view, NORMAL_SCALE_RATIO, NORMAL_SCALE_RATIO);
        Animator elevationAnim = createTransZAnimator(view, NORMAL_TRANS_Z);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(duration);
        set.setInterpolator(new AccelerateInterpolator());
        set.playTogether(scaleAnim, elevationAnim);
        return set;
    }


}
