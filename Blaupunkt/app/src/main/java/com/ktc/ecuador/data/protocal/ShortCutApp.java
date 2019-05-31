package com.ktc.ecuador.data.protocal;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class ShortCutApp extends RealmObject {
    @PrimaryKey
    private long id;
    @Required
    private String iconRes = "";
    @Required
    private String intentUrl = "";
    @Required
    private String appName = "";

    private int backCode;

    public int getBackCode() {
        return backCode;
    }

    public void setBackCode(int backCode) {
        this.backCode = backCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIconRes() {
        return iconRes;
    }

    public void setIconRes(String iconRes) {
        this.iconRes = iconRes;
    }

    public String getIntentUrl() {
        return intentUrl;
    }

    public void setIntentUrl(String intentUrl) {
        this.intentUrl = intentUrl;
    }

}
