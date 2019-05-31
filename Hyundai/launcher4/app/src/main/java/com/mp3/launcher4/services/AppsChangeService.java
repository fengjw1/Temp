package com.mp3.launcher4.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.utils.AppsUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;

/**
 * @author longzj
 */
public class AppsChangeService extends Service {

    private static final String INSTALL_SHORTCUT = "com.ecuador.ktc.install.shortcut";
    public static final String UNINSTALL_SHORT_CUT = "com.ecuador.ktc.uninstall.shortcut";
    private static final String SHORT_CUT_NAME = "shortcut.app.name";
    public static final String SHORT_CUT_APP_ID = "shortcut.app.id";
    private static final String SHORT_CUT_ICON = "shortcut.icon";
    private static final String SHORT_CUT_INTENT_URL = "shortcut.intent.url";
    private static final String SHORT_CUT_BACK_KEYCODE = "shortcut.app.back_keycode";
    private ExecutorService mThreadPools;

    private AppChangedChangeReceiver mAppChangedChangeReceiver;
    private ShoutCutChangedChangeReceiver mShoutCutChangedChangeReceiver;

    private AppsUtils mAppsUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppsUtils = AppsUtils.getInstance(getApplicationContext());
        mThreadPools = Executors.newSingleThreadExecutor();
        registerShortcutReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAppChangedChangeReceiver != null) {
            unregisterReceiver(mAppChangedChangeReceiver);
        }
        if (mShoutCutChangedChangeReceiver!=null){
            unregisterReceiver(mShoutCutChangedChangeReceiver);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void registerShortcutReceiver() {
        mAppChangedChangeReceiver = new AppChangedChangeReceiver();
        mShoutCutChangedChangeReceiver = new ShoutCutChangedChangeReceiver();
        IntentFilter filter = new IntentFilter();
        IntentFilter appFilter = new IntentFilter();
        filter.addAction(INSTALL_SHORTCUT);
        filter.addAction(UNINSTALL_SHORT_CUT);
        appFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appFilter.addDataScheme("package");
        registerReceiver(mAppChangedChangeReceiver, appFilter);
        registerReceiver(mShoutCutChangedChangeReceiver,filter);
    }

    private void installAction(Intent intent) {
        String name = intent.getStringExtra(SHORT_CUT_NAME);
        long id = intent.getLongExtra(SHORT_CUT_APP_ID, -1);
        String iconUrl = intent.getStringExtra(SHORT_CUT_ICON);
        String url = intent.getStringExtra(SHORT_CUT_INTENT_URL);
        int backCode = intent.getIntExtra(SHORT_CUT_BACK_KEYCODE, -1);
        AppDetailBean bean = new AppDetailBean(id, iconUrl, name, url, backCode);
        mThreadPools.submit(new AddedTask(bean));
    }

    private class AppChangedChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case Intent.ACTION_PACKAGE_ADDED:
                    String name = intent.getDataString();
                    if (name != null) {
                        name = name.substring(8);
                    }
                    mAppsUtils.addToOrderLast(name);
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    name = intent.getDataString();
                    if (name != null) {
                        //name = "package:包名" 故裁减之
                        name = name.substring(8);
                    }
                    mAppsUtils.removeItem(name);
                    break;
                default:
                    break;
            }
        }
    }
    private class ShoutCutChangedChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case INSTALL_SHORTCUT:
                    installAction(intent);
                    break;
                case UNINSTALL_SHORT_CUT:
                    long id = intent.getLongExtra(SHORT_CUT_APP_ID, -1);
                    mThreadPools.submit(new RemovedTask(id));
                    break;
                default:
                    break;
            }
        }
    }

    private class AddedTask implements Runnable {

        private AppDetailBean mDetailBean;


        AddedTask(AppDetailBean detailBean) {
            mDetailBean = detailBean;
        }

        @Override
        public void run() {
            String newerUrl = mDetailBean.getUrl();
            Realm realm = Realm.getDefaultInstance();
            AppDetailBean bean = realm.where(AppDetailBean.class)
                    .equalTo("id", mDetailBean.getId()).findFirst();
            if (bean != null) {
                String older = bean.getUrl();
                realm.beginTransaction();
                bean.setUrl(mDetailBean.getUrl());
                bean.setTitle(mDetailBean.getTitle());
                bean.setImage(mDetailBean.getImage());
                bean.setBackCode(mDetailBean.getBackCode());
                realm.commitTransaction();
                mAppsUtils.modifyItem(older, newerUrl);
            } else {
                realm.beginTransaction();
                realm.copyToRealm(mDetailBean);
                realm.commitTransaction();
                mAppsUtils.addToOrderFirst(newerUrl);
            }
            realm.close();
        }
    }

    private class RemovedTask implements Runnable {

        private long id;

        RemovedTask(long id) {
            this.id = id;
        }

        @Override
        public void run() {
            Realm realm = Realm.getDefaultInstance();
            AppDetailBean bean = realm.where(AppDetailBean.class)
                    .equalTo("id", id).findFirst();
            if (bean != null) {
                String url = bean.getUrl();
                realm.beginTransaction();
                bean.deleteFromRealm();
                realm.commitTransaction();
                mAppsUtils.removeItem(url);
            }
            realm.close();
        }
    }
}
