package com.ktc.ecuador.data;

import com.ktc.ecuador.LauncherApplication;

public class Constants {
    public static final String APPS_SERVER_ADDRESS = LauncherApplication.baseUrl;

    public static final String Setting_Package_Name = "com.android.tv.settings";
    public static final String MTvPlayer_Package_Name = "com.mstar.tv.tvplayer.ui";
    public static final String Store_Package_Name = "com.foxxum.downloader";

    public static final String NETWORK_ERROR = "network_error";
    public static final String NETWORK_OUT_TIME = "network_out_time";

    public static final int COMMAND_UP = 0x1451;
    //for shortcut
    public static final String INSTALL_SHORTCUT = "com.ecuador.ktc.install.shortcut";
    public static final String UNINSTALL_SHORT_CUT = "com.ecuador.ktc.uninstall.shortcut";
    public static final String SHORT_CUT_APP_ID = "shortcut.app.id";
    public static final String SHORT_CUT_ICON = "shortcut.icon";
    public static final String SHORT_CUT_INTENT_URL = "shortcut.intent.url";
    public static final String SHORT_CUT_NAME = "shortcut.app.name";
    public static final String SHORT_CUT_BACK_KEYCODE = "shortcut.app.back_keycode";


    public static final String SORT_APP_STORE_FILE_NAME = "sortApp.json";
}
