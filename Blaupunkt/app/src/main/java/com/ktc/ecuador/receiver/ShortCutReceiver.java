package com.ktc.ecuador.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.ktc.ecuador.data.Constants;
import com.ktc.ecuador.data.protocal.ShortCutApp;
import com.ktc.ecuador.utils.LogcatUtil;
import com.ktc.ecuador.utils.RealmUtil;

import io.realm.Realm;
import io.realm.Realm.Transaction;

public class ShortCutReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, final Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action))
            return;
        if (Constants.INSTALL_SHORTCUT.equals(action)) {
            //添加shortcut
            addShortCut(intent);
        } else if (Constants.UNINSTALL_SHORT_CUT.equals(action)) {
            //删除shortcut
            deleteShortCut(intent);
        }
        abortBroadcast();
    }

    private void addShortCut(final Intent intent) {
        Realm realm = RealmUtil.getRealm();
        final long id = intent.getLongExtra(Constants.SHORT_CUT_APP_ID, -1);
        final String imgUrl = intent.getStringExtra(Constants.SHORT_CUT_ICON);
        final String intentUrl = intent.getStringExtra(Constants.SHORT_CUT_INTENT_URL);
        final String appName = intent.getStringExtra(Constants.SHORT_CUT_NAME);
        final int backCode = intent.getIntExtra(Constants.SHORT_CUT_BACK_KEYCODE, -1);
        ShortCutApp shortCutApp = realm.where(ShortCutApp.class).equalTo("id", id).findFirst();
        LogcatUtil.d("imgUrl==" + imgUrl + "intentUrl==" + intentUrl + "appName==" + appName + "backCode==" + backCode);
        if (shortCutApp != null
                || TextUtils.isEmpty(imgUrl)
                || TextUtils.isEmpty(intentUrl)
                || TextUtils.isEmpty(appName)
                || backCode == -1)
            return;
        realm.executeTransaction(new Transaction() {
            @Override
            public void execute(Realm realm) {
                ShortCutApp shortCutApp = realm.createObject(ShortCutApp.class, id);
                shortCutApp.setIconRes(imgUrl);
                shortCutApp.setIntentUrl(intentUrl);
                shortCutApp.setAppName(appName);
                shortCutApp.setBackCode(backCode);
            }
        });
        RealmUtil.realmClose();
    }

    private void deleteShortCut(Intent intent) {
        Realm realm = RealmUtil.getRealm();
        long id = intent.getLongExtra(Constants.SHORT_CUT_APP_ID, -1);
        final ShortCutApp shortCutApp = realm.where(ShortCutApp.class).equalTo("id", id).findFirst();
        if (shortCutApp != null) {
            realm.executeTransaction(new Transaction() {
                @Override
                public void execute(Realm realm) {
                    shortCutApp.deleteFromRealm();
                }
            });
        }
        RealmUtil.realmClose();
    }
}
