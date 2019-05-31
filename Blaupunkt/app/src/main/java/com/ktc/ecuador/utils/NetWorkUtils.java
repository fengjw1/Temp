package com.ktc.ecuador.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class NetWorkUtils {
    /*
        判断网络是否可用
     */
    public static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * 跳转MP3公司应用
     */
    public static void gotoHtml5Page(Context activity, String url, int backCode) {
        Uri appUri = Uri.parse(url);
        Intent webIntent = activity.getPackageManager()
                .getLaunchIntentForPackage("com.access_company.nfbe.content_shell_apk");
        if (webIntent != null) {
            webIntent.setData(appUri);
            webIntent.putExtra("BACK_KEYCODE", backCode);
            activity.startActivity(webIntent);
        }
    }
}