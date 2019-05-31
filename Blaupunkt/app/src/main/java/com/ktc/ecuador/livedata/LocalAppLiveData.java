package com.ktc.ecuador.livedata;

import android.arch.lifecycle.MutableLiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.ktc.ecuador.data.protocal.MyAppBean;
import com.ktc.ecuador.data.protocal.ResponseBean;
import com.ktc.ecuador.service.HomeService;
import com.ktc.ecuador.utils.RealmUtil;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;

public class LocalAppLiveData extends MutableLiveData<ResponseBean<List<MyAppBean>>> implements RealmChangeListener<Realm> {
    private ApkInstallListener mApkInstallListener;
    private LocalAppLiveData instance;
    private Context mContext;
    private HomeService homeService;

    public LocalAppLiveData(Context context, HomeService homeService) {
        this.mContext = context;
        instance = this;
        this.homeService = homeService;
        mApkInstallListener = new ApkInstallListener();
    }

    //change to 1 from 0.
    @Override
    protected void onActive() {
        super.onActive();
        homeService.getMyApps(instance, mContext.getPackageManager());
        IntentFilter apkFilter = new IntentFilter();
        apkFilter.addAction("android.intent.action.PACKAGE_ADDED");
        apkFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        apkFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        mApkInstallListener = new ApkInstallListener();
        apkFilter.addDataScheme("package");
        mContext.registerReceiver(mApkInstallListener, apkFilter);

        RealmUtil.getRealm().addChangeListener(this);
    }

    // from 1 to 0
    @Override
    protected void onInactive() {
        super.onInactive();
        mContext.unregisterReceiver(mApkInstallListener);
        RealmUtil.getRealm().removeChangeListener(this);
    }

    public void onDataChanged() {
        Log.e("lzj", "onDataChanged: ");
        homeService.getMyApps(instance, mContext.getPackageManager());
    }

    @Override
    public void onChange(Realm realm) {
        homeService.getMyApps(instance, mContext.getPackageManager());
    }

    class ApkInstallListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            homeService.getMyApps(instance, mContext.getPackageManager());
        }
    }
}
