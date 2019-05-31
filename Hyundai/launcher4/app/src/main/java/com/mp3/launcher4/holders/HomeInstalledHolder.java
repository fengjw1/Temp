package com.mp3.launcher4.holders;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.utils.AnimatorUtils;

/**
 * @author longzj
 */
public class HomeInstalledHolder extends BaseAppHolder {

    public static final int STATE_NONE = -1;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_MENU = 1;
    public static final int STATE_MOVED = 2;
    public static final int FOCUSED_MOVE = 0;
    public static final int FOCUSED_DELETE = 1;

    private static final float SCALE_RATIO = 1.15f;
    private static final float NORMAL_RATIO = 1.0f;
    public int mFocusState;
    private int mMenuState;
    private LinearLayout menuParent;
    private ImageView menuMove;
    private ImageView menuDelete;

    private ImageView leftArrow;
    private ImageView rightArrow;
    private RelativeLayout mAppContent;

    public HomeInstalledHolder(View itemView) {
        super(itemView);
        mMenuState = STATE_NORMAL;
        clearFocusState();
    }

    @Override
    protected void initView(View itemView) {
        super.initView(itemView);
        menuParent = (LinearLayout) itemView.findViewById(R.id.app_menu_parent);
        menuMove = (ImageView) itemView.findViewById(R.id.app_menu_move);
        menuDelete = (ImageView) itemView.findViewById(R.id.app_menu_delete);
        leftArrow = (ImageView) itemView.findViewById(R.id.app_left_arrow);
        rightArrow = (ImageView) itemView.findViewById(R.id.app_right_arrow);
        mAppContent = (RelativeLayout) itemView.findViewById(R.id.app_content);
        menuParent.setVisibility(View.GONE);
    }

    @Override
    public void createFocusedAnim(AnimatorSet set) {
        Animator transZAnim = AnimatorUtils.createElevationAnim(getCardView(), AnimatorUtils.BASE_Z);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(mAppContent, AnimatorUtils.BASE_RECTANGLE_SCALE);
        Animator titleAlphaAnim = AnimatorUtils.createAlphaAnim(getTitle(), 1.0f);
        set.playTogether(transZAnim, scaleAnim, titleAlphaAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    public void createUnfocusedAnim(AnimatorSet set) {
        Animator transZAnim = AnimatorUtils.createElevationAnim(getCardView(), 0);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(mAppContent, 1.0f);
        Animator titleAlphaAnim = AnimatorUtils.createAlphaAnim(getTitle(), 0.0f);
        set.playTogether(transZAnim, scaleAnim, titleAlphaAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new DecelerateInterpolator());
    }

    public int getMenuState() {
        return mMenuState;
    }

    public void switchState(int state) {
        final ImageView cover = getCover();
        final TextView title = getTitle();
        if (state == STATE_NORMAL) {
            clearFocusState();
            menuParent.setVisibility(View.GONE);
            title.setVisibility(View.VISIBLE);
            getFocusedSet().start();
//            leftArrow.setVisibility(View.GONE);
//            rightArrow.setVisibility(View.GONE);
            setShadow(SHADOW_STATE_NULL);
        } else if (state == STATE_MENU) {
            menuParent.setVisibility(View.VISIBLE);
            setFocusState(KeyEvent.KEYCODE_DPAD_LEFT);
            title.setVisibility(View.INVISIBLE);
            getUnfocusedSet().start();
            setShadow(SHADOW_STATE_DARK);
        } else if (state == STATE_MOVED) {
            clearFocusState();
            menuParent.setVisibility(View.GONE);
            getFocusedSet().start();
//            leftArrow.setVisibility(View.VISIBLE);
//            rightArrow.setVisibility(View.VISIBLE);
            setShadow(SHADOW_STATE_NULL);
        } else {
            return;
        }
        this.mMenuState = state;
    }

    public int getFocusState() {
        return mFocusState;
    }

    public void setFocusState(int keycode) {
        if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
            menuMove.setActivated(true);
            menuDelete.setActivated(false);
            menuMove.setScaleX(SCALE_RATIO);
            menuMove.setScaleY(SCALE_RATIO);
            menuDelete.setScaleX(NORMAL_RATIO);
            menuDelete.setScaleY(NORMAL_RATIO);
            mFocusState = FOCUSED_MOVE;
        } else if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            menuMove.setActivated(false);
            menuDelete.setActivated(true);
            menuMove.setScaleX(NORMAL_RATIO);
            menuMove.setScaleY(NORMAL_RATIO);
            menuDelete.setScaleX(SCALE_RATIO);
            menuDelete.setScaleY(SCALE_RATIO);
            mFocusState = FOCUSED_DELETE;
        }
    }

    private void clearFocusState() {
        menuMove.setActivated(false);
        menuDelete.setActivated(false);
        menuMove.setScaleX(NORMAL_RATIO);
        menuMove.setScaleY(NORMAL_RATIO);
        menuDelete.setScaleX(NORMAL_RATIO);
        menuDelete.setScaleY(NORMAL_RATIO);
        mFocusState = FOCUSED_MOVE;
    }

    public void clearState() {
        clearFocusState();
        switchState(STATE_NORMAL);
        setShadow(SHADOW_STATE_LIGHT);
    }

}
