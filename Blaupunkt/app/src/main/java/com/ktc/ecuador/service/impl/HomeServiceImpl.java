package com.ktc.ecuador.service.impl;

import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import com.ktc.ecuador.LauncherApplication;
import com.ktc.ecuador.data.Constants;
import com.ktc.ecuador.data.api.AppsApi;
import com.ktc.ecuador.data.protocal.CategoryBean;
import com.ktc.ecuador.data.protocal.CategoryDetail;
import com.ktc.ecuador.data.protocal.MyAppBean;
import com.ktc.ecuador.data.protocal.Poster;
import com.ktc.ecuador.data.protocal.ResponseBean;
import com.ktc.ecuador.data.protocal.SearchResponseBean;
import com.ktc.ecuador.data.protocal.SearchResultApp;
import com.ktc.ecuador.data.protocal.ShortCutApp;
import com.ktc.ecuador.data.protocal.StoreApp;
import com.ktc.ecuador.service.HomeService;
import com.ktc.ecuador.utils.CloseUtil;
import com.ktc.ecuador.utils.CommonUtils;
import com.ktc.ecuador.utils.RealmUtil;
import com.ktc.ecuador.utils.RetrofitFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeServiceImpl implements HomeService {
    private static final String isEmpty = "0";
    private Boolean isAppSearched = false;

    @Override
    public void getMyApps(final MutableLiveData liveData, final PackageManager packageManager) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Intent mIntent = new Intent(Intent.ACTION_MAIN, null);
                mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> appResolveInfos = packageManager.queryIntentActivities(mIntent, 0);
                ArrayList<MyAppBean> myAppBeanList = new ArrayList<>();
                String packageName;
                MyAppBean myAppBean;
                for (int i = 0; i < appResolveInfos.size(); i++) {
                    packageName = appResolveInfos.get(i).activityInfo.packageName;
                    if (filterApk(packageName)) {
                        continue;
                    }
                    myAppBean = new MyAppBean();
                    myAppBean.setAppName(appResolveInfos.get(i).loadLabel(packageManager).toString());
                    myAppBean.setIntentUrl(appResolveInfos.get(i).activityInfo.packageName);
                    myAppBean.setAppPackageName(appResolveInfos.get(i).activityInfo.packageName);

                    if (packageName.equals("com.foxxum.atvdownloader")){
                        myAppBeanList.add(0, myAppBean);
                    }else {
                        myAppBeanList.add(myAppBean);
                    }
                }
                //查询shortcut快捷方式
                Realm realm = RealmUtil.getRealm();
                RealmResults<ShortCutApp> shortCutApps = realm.where(ShortCutApp.class).findAll();
                if (shortCutApps != null && shortCutApps.size() > 0) {
                    for (int i = 0; i < shortCutApps.size(); i++) {
                        ShortCutApp shortCutApp = shortCutApps.get(i);
                        if (shortCutApp != null) {
                            myAppBean = new MyAppBean();
                            myAppBean.setAppName(shortCutApp.getAppName());
                            myAppBean.setIconRes(shortCutApp.getIconRes());
                            myAppBean.setIntentUrl(shortCutApp.getIntentUrl());
                            myAppBean.setBackCode(shortCutApp.getBackCode());
                            myAppBean.setId(shortCutApp.getId());
                            myAppBeanList.add(myAppBean);
                        }
                    }
                }
                if (realm != null) {
                    realm.close();
                }

                //如果没有sort文件，则将现在的结果保存一下，如果有的话 进行排序后返回
                File outFile = new File(LauncherApplication.getInstance().getFilesDir(), Constants.SORT_APP_STORE_FILE_NAME);
                if (!outFile.exists()) {
                    List<String> sortDataList = new ArrayList<>(myAppBeanList.size());
                    for (int i = 0; i < myAppBeanList.size(); i++) {
                        myAppBean = myAppBeanList.get(i);
                        if (CommonUtils.isLocalApp(LauncherApplication.mContext, myAppBean.getIntentUrl())) {
                            sortDataList.add(myAppBean.getAppPackageName());
                        } else {
                            sortDataList.add(String.valueOf(myAppBean.getId()));
                        }
                    }
                    storeAppSortResult(sortDataList);
                } else {
                    //fengjw
                    MyAppBean.sortList = restoreAppSortResult();
                    MyAppBean bean = myAppBeanList.get(0);
                    myAppBeanList.remove(0);
                    Collections.sort(myAppBeanList);
                    myAppBeanList.add(0, bean);
                }
                ResponseBean<List<MyAppBean>> responseBean = new ResponseBean<>();
                responseBean.setSuccess(true);
                responseBean.setResult(myAppBeanList);
                liveData.postValue(responseBean);
            }
        });
    }

    @Override
    public void getStoreApps(final MutableLiveData storeAppsLiveData) {
        AppsApi service = RetrofitFactory.getInstance(Constants.APPS_SERVER_ADDRESS).create
                (AppsApi.class);
        final ResponseBean responseBean = new ResponseBean();

        service.getPosterData().enqueue(new Callback<Poster>() {
            @Override
            public void onResponse(Call<Poster> call, Response<Poster> response) {
                if (response.body() == null) {
                    return;
                }
                responseBean.setSuccess(true);
                responseBean.setResult(response.body());
                storeAppsLiveData.postValue(responseBean);
            }

            @Override
            public void onFailure(Call<Poster> call, Throwable t) {
                responseBean.setSuccess(false);
                responseBean.setMessage(Constants.NETWORK_OUT_TIME);
                storeAppsLiveData.postValue(responseBean);
            }
        });
        service.getStoreData(LauncherApplication.LANGUAGE).enqueue(new Callback<StoreApp>() {
            @Override
            public void onResponse(Call<StoreApp> call, Response<StoreApp> response) {
                if (response.body() == null || isEmpty.equals(response.body().getList().get(0).getId())) {
                    return;
                }
                responseBean.setSuccess(true);
                responseBean.setResult(response.body().getList());
                storeAppsLiveData.postValue(responseBean);
            }

            @Override
            public void onFailure(Call<StoreApp> call, Throwable t) {
                responseBean.setSuccess(false);
                responseBean.setMessage(Constants.NETWORK_OUT_TIME);
                storeAppsLiveData.postValue(responseBean);
            }
        });

    }

    @Override
    public void getCategoryList(final MutableLiveData categoryListLiveData) {
        AppsApi service = RetrofitFactory.getInstance(Constants.APPS_SERVER_ADDRESS).create
                (AppsApi.class);
        final ResponseBean responseBean = new ResponseBean();

        service.getCategories(LauncherApplication.LANGUAGE).enqueue(new Callback<CategoryBean>() {
            @Override
            public void onResponse(Call<CategoryBean> call, Response<CategoryBean> response) {
                responseBean.setSuccess(true);
                responseBean.setResult(response.body());
                categoryListLiveData.postValue(responseBean);
            }

            @Override
            public void onFailure(Call<CategoryBean> call, Throwable t) {
                responseBean.setSuccess(false);
                responseBean.setMessage(Constants.NETWORK_OUT_TIME);
                categoryListLiveData.postValue(responseBean);
            }
        });
    }

    @Override
    public void getCategoryDetail(final MutableLiveData categoryDetailLiveData, int id) {
        AppsApi service = RetrofitFactory.getInstance(Constants.APPS_SERVER_ADDRESS).create
                (AppsApi.class);
        final ResponseBean responseBean = new ResponseBean();

        service.getCategoryDetail(id, LauncherApplication.LANGUAGE).enqueue(new Callback<CategoryDetail>() {
            @Override
            public void onResponse(Call<CategoryDetail> call, Response<CategoryDetail> response) {
                if (response.body() == null || isEmpty.equals(response.body().getList().get(0).getType_id())) {
                    return;
                }
                responseBean.setSuccess(true);
                responseBean.setResult(response.body());
                categoryDetailLiveData.postValue(responseBean);
            }

            @Override
            public void onFailure(Call<CategoryDetail> call, Throwable t) {
                responseBean.setSuccess(false);
                responseBean.setMessage(Constants.NETWORK_OUT_TIME);
                categoryDetailLiveData.postValue(responseBean);
            }
        });
    }

    @Override
    public void getSearchResult(final MutableLiveData searchResultLiveData, String key) {
        AppsApi service = RetrofitFactory.getInstance(Constants.APPS_SERVER_ADDRESS).create
                (AppsApi.class);
        int[] categoryId = new int[]{1, 2, 3, 5, 6, 7};
        final List<Call<CategoryDetail>> mCallList = new ArrayList<>();
        Call<CategoryDetail> mDemandCall;
        final List<SearchResponseBean> mData = new ArrayList<>();

        service.getAppsSearchResult(key, LauncherApplication.LANGUAGE).enqueue(new Callback<SearchResultApp>() {
            @Override
            public void onResponse(Call<SearchResultApp> call, Response<SearchResultApp> response) {
                SearchResponseBean responseBean = new SearchResponseBean();
                isAppSearched = true;
                if (response.body() == null || isEmpty.equals(response.body().getList().get(0).getId())||"noId".equals(response.body().getList().get(0).getId())) {
                    if (mCallList.isEmpty() && isAppSearched) {
                        searchResultLiveData.postValue(mData);
                    }
                    return;
                }
                responseBean.setSuccess(true);
                responseBean.setAppResult(response.body().getList());
                mData.add(responseBean);
                if (mCallList.isEmpty() && isAppSearched) {
                    searchResultLiveData.postValue(mData);
                }

            }

            @Override
            public void onFailure(Call<SearchResultApp> call, Throwable t) {
                SearchResponseBean responseBean = new SearchResponseBean();
                isAppSearched = true;
                responseBean.setSuccess(false);
                responseBean.setMessage(Constants.NETWORK_OUT_TIME);
                if (mCallList.isEmpty() && isAppSearched) {
                    searchResultLiveData.postValue(mData);
                }

            }
        });
        for (int i = 0; i < categoryId.length; i++) {
            mDemandCall = service.getDemandSearchResult(key, LauncherApplication.LANGUAGE, categoryId[i]);
            mCallList.add(mDemandCall);
            if (mDemandCall.isExecuted()) {
                mDemandCall.cancel();
                mDemandCall.clone();
            }
            mDemandCall.enqueue(new Callback<CategoryDetail>() {
                @Override
                public void onResponse(Call<CategoryDetail> call, Response<CategoryDetail> response) {
                    SearchResponseBean responseBean = new SearchResponseBean();
                    mCallList.remove(call);
                    if (response.body() == null || isEmpty.equals(response.body().getList().get(0).getType_id())) {
                        if (mCallList.isEmpty() && isAppSearched) {
                            searchResultLiveData.postValue(mData);
                        }
                        return;
                    }
                    responseBean.setSuccess(true);
                    responseBean.setCategoryResult(response.body().getList());
                    mData.add(responseBean);
                    if (mCallList.isEmpty() && isAppSearched) {
                        searchResultLiveData.postValue(mData);
                    }


                }

                @Override
                public void onFailure(Call<CategoryDetail> call, Throwable t) {
                    SearchResponseBean responseBean = new SearchResponseBean();
                    mCallList.remove(call);
                    responseBean.setSuccess(false);
                    responseBean.setMessage(Constants.NETWORK_OUT_TIME);
                    if (mCallList.isEmpty() && isAppSearched) {
                        searchResultLiveData.postValue(mData);
                    }

                }
            });
        }


    }

    @Override
    public void storeAppSortResult(final List<String> dataList) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                File outFile = new File(LauncherApplication.getInstance().getFilesDir(), Constants.SORT_APP_STORE_FILE_NAME);
                ObjectOutputStream oos = null;
                try {
                    if (!outFile.exists()) {
                        boolean createResult = outFile.createNewFile();
                        if (!createResult) {
                            return;
                        }
                    }
                    oos = new ObjectOutputStream(new FileOutputStream(outFile));
                    oos.writeObject(dataList);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    CloseUtil.close(oos);
                }
            }
        });
    }

    @Override
    public List<String> restoreAppSortResult() {
        List<String> dataList = new ArrayList<>();
        File outFile = new File(LauncherApplication.getInstance().getFilesDir(), Constants.SORT_APP_STORE_FILE_NAME);
        ObjectInputStream ois = null;
        try {
            if (outFile.exists()) {
                ois = new ObjectInputStream(new FileInputStream(outFile));
                dataList = (List<String>) ois.readObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(ois);
        }
        return dataList;
    }

    /**
     * 过滤掉不需要显示的应用 @ packagenName @ return boolean
     */
    public boolean filterApk(String packagenName) {
        if (packagenName.equals("com.android.gallery3d")) {
            return true;
        } else if (packagenName.equals("com.android.contacts")) {
            return true;
        } else if (packagenName.equals("com.android.phone")) {
            return true;
        } else if (packagenName.equals("mstar.factorymenu.ui")) {
            return true;
        } else if (packagenName.equals("com.broadcom.bluetoothmonitor")) {
            return true;
        } else if (packagenName.equals("com.awox.quickcontrolpoint")) {
            return true;
        } else if (packagenName.equals("com.awox.renderer3")) {
            return true;
        } else if (packagenName.equals("com.android.dummyactivity")) {
            return true;
        } else if (packagenName.equals("com.awox.server")) {
            return true;
        } else if (packagenName.equals("com.android.camera2")) {
            return true;
        } else if (packagenName.equals("com.android.deskclock")) {
            return true;
        } else if (packagenName.equals("com.android.calculator2")) {
            return true;
        } else if (packagenName.equals("com.android.music")) {
            return true;
        } else if (packagenName.equals("com.mstar.android.tv.app")) {
            return true;
        } else if (packagenName.equals("com.ktc.launcher")) {
            return true;
        } else if (packagenName.equals("com.google.android.googlequicksearchbox")) {
            return true;
        } else if (packagenName.equals("com.ecuador.ktc")) {
            return true;
        } else if (packagenName.equals("com.access_company.nfbe.content_shell_apk")) {
            return true;
        } else if (packagenName.equals("com.android.tv.settings")) {
            return true;
        } else if (packagenName.equals("com.ktc.ecuador")) {
            return true;
        } else if (packagenName.equals("com.foxxum.downloader")) {
            return true;
        }

        return false;
    }
}
