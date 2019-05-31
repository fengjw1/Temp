package com.mp3.launcher4;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemProperties;

import com.mp3.launcher4.utils.AppsUtils;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * @author longzj
 */
public class LauncherApplication extends Application {

    private final static String MODEL_348 = "TV348_ISDB";
    private final static String MODEL_6586 = "TV6586_ISDB";
    public static String LANGUAGE;
    public static String baseUrl;
    public static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder();
        builder.name("Shortcut_DB");
        builder.deleteRealmIfMigrationNeeded();
        Realm.setDefaultConfiguration(builder.build());
        initGlobalLanguage();
        initBaseUrl();
        mAppContext = getApplicationContext();
        AppsUtils.getInstance(mAppContext).init();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LANGUAGE = null;
        baseUrl = null;
        mAppContext = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initGlobalLanguage();
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
