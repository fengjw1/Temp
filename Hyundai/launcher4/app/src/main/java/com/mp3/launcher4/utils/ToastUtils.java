package com.mp3.launcher4.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @author longzj
 */
public class ToastUtils {

    private static ToastUtils instance;
    private Toast mToast;


    public synchronized static ToastUtils getInstance() {
        if (instance == null) {
            instance = new ToastUtils();
        }
        return instance;
    }

    public void showToast(Context context, String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void showToast(Context context, int msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
