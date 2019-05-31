package com.mp3.launcher4.activities.bases;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;


/**
 * @author longzj
 */
public abstract class BaseActivity extends Activity {

    private ConnectivityManager mConnectivityManager;
    private NetStateReceiver mNetStateReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (keepWatchingNetworkStateChange()) {
            mConnectivityManager = (ConnectivityManager) getApplicationContext()
                    .getSystemService(CONNECTIVITY_SERVICE);
            initReceiver();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (keepWatchingNetworkStateChange()) {
            onCheckNetworkState(hasNetwork());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetStateReceiver != null) {
            unregisterReceiver(mNetStateReceiver);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event) || keyCode == KeyEvent.KEYCODE_TV_INPUT;
    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetStateReceiver = new NetStateReceiver();
        registerReceiver(mNetStateReceiver, intentFilter);
    }

    /**
     * 检查网络状态，分别在resume和广播中
     *
     * @param hasNetwork 是否有网
     */
    protected abstract void onCheckNetworkState(boolean hasNetwork);

    /**
     * 判断是否有网
     *
     * @return boolean
     */
    protected boolean hasNetwork() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * 是否打开网络检测
     *
     * @return 默认true
     */
    protected boolean keepWatchingNetworkStateChange() {
        return true;
    }

    private class NetStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (keepWatchingNetworkStateChange()) {
                onCheckNetworkState(hasNetwork());
            }
        }
    }
}
