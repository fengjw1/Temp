package com.mp3.launcher4.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.beans.RecentBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * @author longzj
 */
public class AppsUtils {

    public final static String NOTIFY_RECENT_CHANGED = "action.recent.CHANGED";
    public final static String NOTIFY_APP_MODIFY = "action.app.MODIFY";
    public final static String NOTIFY_APP_REMOVED = "action.app.REMOVED";
    public final static String NOTIFY_APP_ADDED = "action.app.ADDED";
    public final static String CHANGED_URL = "CHANGED_URL";
    public final static String CHANGED_POS = "CHANGED_POS";
    public static final int NO_LIMIT = -1;
    private static final String JSON_RECENT = "recent.json";
    private static final String JSON_ORDER = "order.json";
    private final static int MAX_RECENT_SIZE = 10;
    private final static String DEFAULT_RECENT = "com.mstar.tv.tvplayer.ui";
    @SuppressLint("StaticFieldLeak")
    private static AppsUtils mInstance;
    private PackageManager mPackageManager;
    private Context mContext;
    private Gson mGson;
    private List<String> mRecentList;
    private List<String> mOrderList;
    private ExecutorService mExecutors;

    private AppsUtils(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mGson = new Gson();
        mExecutors = Executors.newSingleThreadExecutor();
        mRecentList = Collections.synchronizedList(new ArrayList<String>());
        mOrderList = Collections.synchronizedList(new ArrayList<String>());
    }

    public static AppsUtils getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AppsUtils.class) {
                if (mInstance == null) {
                    mInstance = new AppsUtils(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    public void init() {
        mExecutors.execute(new ReadOrderListImpl());
        mExecutors.execute(new ReadRecentListImpl());
    }

    public List<AppDetailBean> getRecentApps() {
        return getAppsInternal(mRecentList, MAX_RECENT_SIZE);
    }

    public List<AppDetailBean> getInstalledApps(int limit) {
        return getAppsInternal(mOrderList, limit);
    }

    private List<AppDetailBean> getAppsInternal(List<String> appList, int limit) {

        List<AppDetailBean> list = new ArrayList<>();
        int size = appList.size();
        size = limit > 0 && limit < size ? limit : size;
        for (int index = 0; index < size; index++) {
            String url = appList.get(index);
            AppDetailBean appDetailBean;
            appDetailBean = getAppInfo(url);
            if (appDetailBean != null) {
                list.add(appDetailBean);
            }
        }
        return list;
    }

    private AppDetailBean getLocalApp(String url) {
        AppDetailBean bean = new AppDetailBean();
        bean.setUrl(url);
        String label = null;
        try {
            label = mPackageManager.getApplicationLabel(mPackageManager
                    .getApplicationInfo(url, ApplicationInfo.FLAG_INSTALLED))
                    .toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(label)) {
            bean.setTitle(label);
        }
        return bean;
    }

    public AppDetailBean getAppInfo(String url) {
        AppDetailBean appDetailBean;
        if (CommonUtils.isLocalApp(mContext, url)) {
            appDetailBean = getLocalApp(url);
        } else {
            Realm realm = Realm.getDefaultInstance();
            appDetailBean = realm.where(AppDetailBean.class).equalTo("url", url).findFirst();
            if (appDetailBean != null) {
                appDetailBean = appDetailBean.renew();
            } else {
                RecentBean bean = realm.where(RecentBean.class).equalTo("url", url).findFirst();
                if (bean != null) {
                    appDetailBean = bean.createAppDetailBean();
                }
            }
            realm.close();
        }
        return appDetailBean;
    }

    public void writeOrder(List<AppDetailBean> data) {
        List<String> list = new ArrayList<>();
        for (AppDetailBean bean : data) {
            String url = bean.getUrl();
            if (url != null) {
                list.add(url);
            }
        }
        mExecutors.execute(new WriteOrderListImpl(list));
    }

    private void writeRecent(AppDetailBean appDetailBean) {
        if (appDetailBean == null) {
            return;
        }
        String url = appDetailBean.getUrl();
        if (url == null) {
            return;
        }
        int pos = mRecentList.indexOf(url);
        if (mRecentList.contains(url)) {
            if (pos == 0) {
                return;
            }

        } else if (mRecentList.size() >= MAX_RECENT_SIZE) {
            pos = mRecentList.size() - 1;
        }
        String removedUrl = null;
        if (pos != -1) {
            removedUrl = mRecentList.get(pos);
            mRecentList.remove(pos);
        }
        mRecentList.add(0, url);
        mExecutors.execute(new WriteRecentListImpl(removedUrl, appDetailBean.createRecentBean()));
    }

    private String readJsonString(String name) {
        File file = checkFile(name);
        if (file == null) {
            return null;
        }
        try {
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader, 1024);
            StringBuilder builder = new StringBuilder();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                builder.append(line);
            }
            bufferedReader.close();
            reader.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeJsonString(String path, String json) {
        File file = checkFile(path);
        if (file == null) {
            return;
        }
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File checkFile(String name) {
        File file = new File(mContext.getFilesDir(), name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    public String getRecentItem(int pos) {
        return mRecentList.get(pos);
    }

    public String getOrderItem(int pos) {
        return mOrderList.get(pos);
    }

    private List<ResolveInfo> filter(List<ResolveInfo> sourceData) {
        List<ResolveInfo> filterData = new ArrayList<>();
        List<String> filterList = Arrays.asList(CommonUtils.PACKAGE_FILTER_LIST);
        for (ResolveInfo info : sourceData) {
            if (filterList.contains(info.activityInfo.packageName)) {
                continue;
            }
            filterData.add(info);
        }

        return filterData;
    }

    private List<String> getNativeList() {
        if (mPackageManager == null) {
            return null;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = mPackageManager.queryIntentActivities(intent, 0);
        resolveInfoList = filter(resolveInfoList);
        List<String> data = new ArrayList<>();
        for (ResolveInfo info : resolveInfoList) {
            String packageName = info.activityInfo.packageName;
            data.add(packageName);
        }
        return data;
    }

    private List<String> getShortcutList() {
        List<String> beans = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<AppDetailBean> results = realm.where(AppDetailBean.class).findAll();
        if (results != null && !results.isEmpty()) {
            for (AppDetailBean bean : results) {
                beans.add(bean.getUrl());
            }
        }
        realm.close();
        return beans;
    }

    private void writeDefaultOrder() {
        List<String> list = new ArrayList<>();
        list.addAll(getShortcutList());
        list.addAll(getNativeList());
        mExecutors.execute(new WriteOrderListImpl(list));
    }

    public void removeItem(String url) {
        if (mOrderList.contains(url)) {
            mOrderList.remove(url);
            mExecutors.execute(new WriteOrderListImpl(null));
            Intent intent = new Intent(NOTIFY_APP_REMOVED);
            intent.putExtra(CHANGED_POS, mOrderList.indexOf(url));
            intent.putExtra(CHANGED_URL, url);
            mContext.sendBroadcast(intent);
        }
        if (mRecentList.contains(url)) {
            mRecentList.remove(url);
            mExecutors.execute(new WriteRecentListImpl(url, null));
            Intent intent = new Intent(NOTIFY_RECENT_CHANGED);
            mContext.sendBroadcast(intent);
        }
    }

    private void addToOrder(String url, int pos) {
        if (url == null) {
            return;
        }
        if (!mOrderList.contains(url)) {
            if (pos >= 0) {
                mOrderList.add(pos, url);
            } else {
                mOrderList.add(url);
                pos = mOrderList.size() - 1;
            }
            mExecutors.execute(new WriteOrderListImpl(null));
            Intent intent = new Intent(NOTIFY_APP_ADDED);
            intent.putExtra(CHANGED_URL, url);
            intent.putExtra(CHANGED_POS, pos);
            mContext.sendBroadcast(intent);
        }
    }

    public void addToOrderFirst(String url) {
        addToOrder(url, 0);
    }

    public void addToOrderLast(String url) {
        addToOrder(url, -1);
    }

    public void modifyItem(String older, String newer) {
        if (mOrderList.contains(older)) {
            int pos = mOrderList.indexOf(older);
            mOrderList.set(pos, newer);
            Intent intent = new Intent(NOTIFY_APP_MODIFY);
            intent.putExtra(CHANGED_URL, older);
            intent.putExtra(CHANGED_POS, pos);
            mContext.sendBroadcast(intent);
        } else if (CommonUtils.isLocalApp(mContext, newer)) {
            addToOrderLast(newer);
        } else {
            addToOrderLast(newer);
        }
        if (mRecentList.contains(older)) {
            int pos = mRecentList.indexOf(older);
            mRecentList.set(pos, newer);
            Intent intent = new Intent(NOTIFY_APP_MODIFY);
            intent.putExtra(CHANGED_URL, older);
            mContext.sendBroadcast(intent);
        }
    }

    public void startApp(Context context, AppDetailBean bean, boolean writeToRecent) {
        String url = bean.getUrl();
        if (url == null) {
            return;
        }
        if (CommonUtils.isLocalApp(context, url)) {
            CommonUtils.startAppForPkg(context, url);
        } else {
            CommonUtils.startMP3Browser(context, url, bean.getBackCode());
        }
        if (writeToRecent) {
            writeRecent(bean);
        }
    }

    private class ReadOrderListImpl implements Runnable {

        @Override
        public void run() {
            String jsonString = readJsonString(JSON_ORDER);
            if (TextUtils.isEmpty(jsonString)) {
                writeDefaultOrder();
                return;
            }
            List<String> data = mGson.fromJson(jsonString,
                    TypeToken.get(List.class).getType());
            if (data != null && !data.isEmpty()) {
                mOrderList.addAll(data);
            } else {
                writeDefaultOrder();
            }
        }
    }

    private class WriteOrderListImpl implements Runnable {

        List<String> list;
        private String mRemovedId;

        WriteOrderListImpl(List<String> list) {
            this.list = list;
        }

        @Override
        public void run() {
            int oldListSize = mOrderList.size();
            if (list != null && !list.isEmpty()) {
                String oldUrl = null;
                String newUrl;
                for (int index = 0; index < list.size(); index++) {
                    newUrl = list.get(index);
                    if (oldListSize > index) {
                        oldUrl = mOrderList.get(index);
                    }
                    if (TextUtils.isEmpty(newUrl) || newUrl.equals(oldUrl)) {
                        continue;
                    }
                    if (index < oldListSize) {
                        mOrderList.set(index, newUrl);
                    } else {
                        mOrderList.add(newUrl);
                    }
                }
            }
            String json = mGson.toJson(mOrderList);
            writeJsonString(JSON_ORDER, json);
        }
    }

    private class ReadRecentListImpl implements Runnable {

        @Override
        public void run() {
            String jsonString = readJsonString(JSON_RECENT);
            if (TextUtils.isEmpty(jsonString)) {
                writeRecent(getAppInfo(DEFAULT_RECENT));
                return;
            }
            List<String> data = mGson.fromJson(jsonString,
                    TypeToken.get(List.class).getType());
            if (data != null && !data.isEmpty()) {
                mRecentList.addAll(data);
            } else {
                writeRecent(getAppInfo(DEFAULT_RECENT));
            }
        }
    }

    private class WriteRecentListImpl implements Runnable {

        private String mOlderUrl;
        private RecentBean mRecentBean;

        WriteRecentListImpl(String olderUrl, RecentBean recentBean) {
            mOlderUrl = olderUrl;
            mRecentBean = recentBean;
        }

        @Override
        public void run() {
            Realm realm = Realm.getDefaultInstance();
            if (mOlderUrl != null) {
                RecentBean bean = realm.where(RecentBean.class).equalTo("url", mOlderUrl).findFirst();
                if (bean != null) {
                    realm.beginTransaction();
                    bean.deleteFromRealm();
                    realm.commitTransaction();
                }
            }
            if (mRecentBean != null) {
                RecentBean bean = realm.where(RecentBean.class)
                        .equalTo("id", mRecentBean.getId()).findFirst();
                if (bean == null && !CommonUtils.isLocalApp(mContext, mRecentBean.getUrl())) {
                    realm.beginTransaction();
                    realm.copyToRealm(mRecentBean);
                    realm.commitTransaction();
                }
                Intent intent = new Intent(NOTIFY_RECENT_CHANGED);
                mContext.sendBroadcast(intent);
            }
            realm.close();

            String json = mGson.toJson(mRecentList);
            writeJsonString(JSON_RECENT, json);
        }
    }

}
