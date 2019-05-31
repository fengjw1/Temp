package com.ktc.ecuador.data.protocal;

import com.ktc.ecuador.LauncherApplication;
import com.ktc.ecuador.utils.CommonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import io.realm.RealmObject;

public class MyAppBean extends RealmObject implements Serializable, Comparable<MyAppBean> {
    public static List<String> sortList = null;
    private String appName;
    private String appPackageName;
    private long id = -1;
    private String iconRes;
    private String intentUrl;
    private int backCode;

    public MyAppBean() {

    }


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIconRes() {
        return iconRes;
    }

    public void setIconRes(String iconRes) {
        this.iconRes = iconRes;
    }

    public int getBackCode() {
        return backCode;
    }

    public void setBackCode(int backCode) {
        this.backCode = backCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, appPackageName, id, iconRes, intentUrl, backCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyAppBean myAppBean = (MyAppBean) o;
        return id == myAppBean.id &&
                backCode == myAppBean.backCode &&
                Objects.equals(appName, myAppBean.appName) &&
                Objects.equals(appPackageName, myAppBean.appPackageName) &&
                Objects.equals(iconRes, myAppBean.iconRes) &&
                Objects.equals(intentUrl, myAppBean.intentUrl);
    }

    @Override
    public String toString() {
        return "MyAppBean{" +
                "appName='" + appName + '\'' +
                ", appPackageName='" + appPackageName + '\'' +
                ", id=" + id +
                ", iconRes='" + iconRes + '\'' +
                ", intentUrl='" + intentUrl + '\'' +
                ", backCode=" + backCode +
                '}';
    }

    @Override
    public int compareTo(MyAppBean comObj) {
        if (sortList == null || sortList.size() == 0) {
            return 0;
        }
        int localIndex = CommonUtils.isLocalApp(LauncherApplication.mContext, intentUrl) ? sortList.indexOf(appPackageName) : sortList.indexOf(String.valueOf(id));
        int compIndex = CommonUtils.isLocalApp(LauncherApplication.mContext, comObj.getIntentUrl()) ? sortList.indexOf(comObj.getAppPackageName()) : sortList.indexOf(String.valueOf(comObj.getId()));
        return localIndex - compIndex;
    }

    public String getIntentUrl() {
        return intentUrl;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setIntentUrl(String intentUrl) {
        this.intentUrl = intentUrl;
    }
}
