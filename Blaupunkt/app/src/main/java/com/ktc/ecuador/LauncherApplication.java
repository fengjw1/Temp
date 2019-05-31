package com.ktc.ecuador;


import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemProperties;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * @author zhouxw
 */
public class LauncherApplication extends Application {
    private final static String MODEL_348 = "TV348_ISDB";
    public static String LANGUAGE;
    public static Context mContext;
    public static String baseUrl;
    private static LauncherApplication instance;

    public static LauncherApplication getInstance() {
        if (null == instance) {
            synchronized (LauncherApplication.class) {
                if (null == instance) {
                    instance = new LauncherApplication();
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initRealm();
        mContext = getApplicationContext();
        initGlobalLanguage();
        initBaseUrl();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LANGUAGE = null;
        baseUrl = null;
        mContext = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initGlobalLanguage();
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("shortcut.realm").schemaVersion(0).build();
        Realm.setDefaultConfiguration(configuration);
    }

    private void initGlobalLanguage() {
        Locale locale = getResources().getConfiguration().locale;
        LANGUAGE = locale.getLanguage() + "_" + locale.getCountry();
    }

    private void initBaseUrl() {
        String model = SystemProperties.get("ro.product.model", MODEL_348);
        if (MODEL_348.equals(model)) {
            baseUrl = "http://6745126767.fxm3183393180.com/";
        } else {
            baseUrl = "http://7017024353.fxm3183393180.com/";
        }
    }
}
