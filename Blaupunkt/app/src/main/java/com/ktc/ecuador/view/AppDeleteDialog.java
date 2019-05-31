package com.ktc.ecuador.view;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ktc.ecuador.R;
import com.ktc.ecuador.data.Constants;
import com.ktc.ecuador.data.protocal.MyAppBean;
import com.ktc.ecuador.utils.CommonUtils;
import com.ktc.ecuador.utils.FontUtils;
import com.ktc.ecuador.utils.ImageUtils;


/**
 * @author longzj
 */
public class AppDeleteDialog extends Dialog implements View.OnClickListener,
        View.OnFocusChangeListener {

    private final static int STATE_ONE_SURE = 0;
    private final static int STATE_NORMAL = 1;
    private MyAppBean mAppDetailBean;
    private FontUtils mFontUtils;
    private int mState;

    private OnDeleteListener mOnDeleteListener;

    public AppDeleteDialog(@NonNull Context context, MyAppBean bean) {
        super(context, R.style.deleteDialogStyle);
        this.mAppDetailBean = bean;
        Window window = getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.alpha = 0.95f;
            window.setAttributes(layoutParams);
        }
        setContentView(R.layout.dialog_delete_app);
        mFontUtils = FontUtils.getInstance(getContext().getApplicationContext());
        mState = STATE_NORMAL;
    }


    @Override
    public void show() {
        super.show();
        resizeWidow();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView msgText = (TextView) findViewById(R.id.dialog_message);
        TextView buttonOk = (TextView) findViewById(R.id.dialog_sure);
        TextView buttonCancel = (TextView) findViewById(R.id.dialog_cancel);
        buttonOk.setText(R.string.dialog_yes);
        String msg = getContext().getString(R.string.dialog_delete_msg) + " “" + mAppDetailBean.getAppName() + "”";
        if (mAppDetailBean != null && CommonUtils.isLocalApp(getContext(), mAppDetailBean.getIntentUrl())) {
            if (CommonUtils.isSystemApp(getContext(), mAppDetailBean.getAppPackageName())) {
                msg = getContext().getString(R.string.dialog_delete_system_msg);
                mState = STATE_ONE_SURE;
                buttonCancel.setVisibility(View.GONE);
                buttonOk.setText(R.string.dialog_ok);
            }
        }

        msgText.setText(msg);
        buttonOk.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
        buttonOk.setOnFocusChangeListener(this);
        buttonCancel.setOnFocusChangeListener(this);
        buttonCancel.requestFocus();
        mFontUtils.setRegularFont(msgText);
    }

    private void resizeWidow() {
        Window window = this.getWindow();
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ImageUtils.getWindowWidth();
        params.height = ImageUtils.getWindowHeight();
        window.setAttributes(params);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dialog_sure && mState == STATE_NORMAL) {
            deleteApp();
        }
        dismiss();
    }

    private void deleteApp() {
        if (mAppDetailBean == null) {
            return;
        }
        final Context context = getContext();

        if (CommonUtils.isLocalApp(getContext(), mAppDetailBean.getIntentUrl())) {
            Intent intent = new Intent(context, context.getClass());
            PendingIntent sender = PendingIntent.getActivity(context, 0, intent, 0);
            PackageInstaller installer = getContext().getPackageManager().getPackageInstaller();
            installer.uninstall(mAppDetailBean.getAppPackageName(), sender.getIntentSender());
        } else {
            Intent intent = new Intent(Constants.UNINSTALL_SHORT_CUT);
            intent.putExtra(Constants.SHORT_CUT_APP_ID, mAppDetailBean.getId());
            context.sendBroadcast(intent);
        }
        if (mOnDeleteListener != null) {
            mOnDeleteListener.onDelete();
        }
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        mOnDeleteListener = onDeleteListener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        float scaleRatio = 1.0f;
        if (hasFocus) {
            scaleRatio = 1.25f;
        }
        v.setScaleX(scaleRatio);
        v.setScaleY(scaleRatio);
        if (v instanceof TextView) {
            if (hasFocus) {
                mFontUtils.setMediumFont((TextView) v);
            } else {
                mFontUtils.setRegularFont((TextView) v);
            }
        }
    }

    public interface OnDeleteListener {
        /**
         * 确认删除时回调
         */
        void onDelete();
    }
}
