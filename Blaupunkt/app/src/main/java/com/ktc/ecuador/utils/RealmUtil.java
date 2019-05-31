package com.ktc.ecuador.utils;

import io.realm.Realm;

public class RealmUtil {
    static Realm mRealm = null;

    public static Realm getRealm() {
        mRealm = Realm.getDefaultInstance();
        return mRealm;
    }

    public static void realmClose() {
        if (mRealm != null)
            mRealm.close();
        mRealm = null;
    }
}
