package com.ktc.ecuador.utils;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Pair;

/**
 * @author longzj
 */
public class CommonUtils {

    public final static int INPUT_SOURCE_NONE = 0;
    public final static int INPUT_SOURCE_FILE_MANAGER = -1;
    public final static int INPUT_SOURCE_USB = -2;
    public static final String SHORTCUT_CHANGE = "com.mp3.launcher.SHORTCUT_CHANGED";
    final static String[] PACKAGE_FILTER_LIST = {
            "com.android.contacts",
            "com.android.camera2",
            "com.android.dummyactivity",
            "ktc.factorymenu.ui",
            "com.mstar.tvsetting",
            "com.ktc.launcher",
            "mstar.factorymenu.ui",
            "com.android.gallery3d",
            "com.android.phone",
            "com.broadcom.bluetoothmonitor",
            "com.awox.quickcontrolpoint",
            "com.awox.renderer3",
            "com.awox.server",
            "com.android.deskclock",
            "com.android.calculator2",
            "com.android.music",
            "com.mstar.android.tv.app",
            "com.google.android.googlequicksearchbox",
            "com.ecuador.ktc",
            "com.access_company.nfbe.content_shell_apk",
            "com.foxxum.downloader",
            "com.android.tv.settings",
            "com.mp3.launcher4",
    };

    public static void startActivityForAction(Context context, String action, Pair... pairs) {
        Intent intent = new Intent(action);
        startActivityInternal(context, intent, pairs);
    }

    private static void startActivityInternal(Context context, Intent intent, Pair... pairs) {
        if (!checkIntentValid(context, intent)) {
            return;
        }
        if (context instanceof Activity) {
            if (pairs != null && pairs.length > 0) {
                context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) context,
                        pairs).toBundle());
            } else {
                context.startActivity(intent);
            }

        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private static boolean checkIntentValid(Context context, Intent intent) {
        boolean isValid;
        if (intent == null || context == null) {
            isValid = false;
        } else {
            PackageManager manager = context.getApplicationContext().getPackageManager();
            ResolveInfo info = manager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            isValid = info != null;
        }
        return isValid;
    }

    public static void startActivityForClass(Activity activity, Class clazz, Pair... pairs) {
        Intent intent = new Intent(activity, clazz);
        startActivityInternal(activity, intent, pairs);
    }

    public static void startActivityForIntent(Activity activity, Intent intent, Pair... pairs) {
        startActivityInternal(activity, intent, pairs);
    }

    public static boolean isLocalApp(Context context, String pkg) {
        PackageManager manager = context.getApplicationContext().getPackageManager();
        Intent intent = manager.getLaunchIntentForPackage(pkg);
        return intent != null;
    }

    public static boolean isSystemApp(Context context, String pkg) {
        PackageManager manager = context.getApplicationContext().getPackageManager();
        ApplicationInfo info = null;
        try {
            info = manager.getApplicationInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info != null && (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public static void startAppForComponent(Context context, String pkg, String cls) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(pkg, cls));
        startActivityInternal(context, intent);
    }

    public static void startMP3Browser(Context context, String url, int backCode) {
        if (url == null) {
            return;
        }
        Uri appUri = Uri.parse(url);
        Intent webIntent = context.getPackageManager()
                .getLaunchIntentForPackage("com.access_company.nfbe.content_shell_apk");
        if (webIntent != null) {
            webIntent.setData(appUri);
            webIntent.putExtra("BACK_KEYCODE", backCode);
            context.startActivity(webIntent);
        }
    }

    public static void startMP3Downloader(Context context) {
        startAppForPkg(context, "com.foxxum.downloader");
    }

    public static void startAppForPkg(Context context, String pkg) {
        PackageManager manager = context.getApplicationContext().getPackageManager();
        Intent intent = manager.getLaunchIntentForPackage(pkg);
        startActivityInternal(context, intent);
    }

    public static void startTvApp(Context context, boolean isPowerOn,
                                  boolean isChangeSource, int inputSrc) {
        ComponentName componentName = new ComponentName(
                "com.mstar.tv.tvplayer.ui", "com.mstar.tv.tvplayer.ui.RootActivity");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(componentName);
        intent.putExtra("isPowerOn", isPowerOn);
        if (isChangeSource) {
            intent.putExtra("task_tag", "input_source_changed");
            intent.putExtra("inputSrc", inputSrc);
        }
        intent.putExtra("mIsFromHomeTv", true);
        startActivityInternal(context, intent);
    }
}
