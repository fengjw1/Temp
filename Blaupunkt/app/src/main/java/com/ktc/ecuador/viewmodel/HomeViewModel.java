package com.ktc.ecuador.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.ktc.ecuador.data.Constants;
import com.ktc.ecuador.data.protocal.CategoryBean;
import com.ktc.ecuador.data.protocal.CategoryDetail;
import com.ktc.ecuador.data.protocal.MyAppBean;
import com.ktc.ecuador.data.protocal.ResponseBean;
import com.ktc.ecuador.data.protocal.SearchResponseBean;
import com.ktc.ecuador.data.protocal.StoreApp;
import com.ktc.ecuador.livedata.LocalAppLiveData;
import com.ktc.ecuador.livedata.TimeTickLiveData;
import com.ktc.ecuador.service.HomeService;
import com.ktc.ecuador.service.impl.HomeServiceImpl;
import com.ktc.ecuador.utils.NetWorkUtils;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;

import java.util.List;


public class HomeViewModel extends AndroidViewModel {
    //获取本地应用data
    public LocalAppLiveData myAppsLiveData = null;
    private HomeService homeService;
    //时间提供者data
    private TimeTickLiveData mTimeTickLiveData = new TimeTickLiveData(getApplication());
    //获取推荐应用data
    private MutableLiveData<ResponseBean<List<StoreApp.ListBean>>> storeAppsLiveData = new MutableLiveData<>();
    //获取所有CategoryList列表
    private MutableLiveData<ResponseBean<CategoryBean>> categoryListLiveData = new MutableLiveData<>();
    //获取所有CategoryList列表
    private MutableLiveData<ResponseBean<CategoryDetail>> categoryDetailLiveData = new MutableLiveData<>();
    //获取search结果列表
    private MutableLiveData<List<SearchResponseBean>> searchResultLiveData = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        homeService = new HomeServiceImpl();
        myAppsLiveData = new LocalAppLiveData(getApplication(), homeService);
    }

    public TimeTickLiveData getTimeTickLiveData() {
        return mTimeTickLiveData;
    }

    public LiveData<ResponseBean<List<StoreApp.ListBean>>> getStoreAppsLiveData() {
        return storeAppsLiveData;
    }

    public LiveData<ResponseBean<List<MyAppBean>>> getMyAppsLiveData() {
        return myAppsLiveData;
    }

    public LiveData<ResponseBean<CategoryBean>> getCategoryListLiveData() {
        return categoryListLiveData;
    }

    public LiveData<ResponseBean<CategoryDetail>> getCategoryDetailLiveData() {
        return categoryDetailLiveData;
    }

    public LiveData<List<SearchResponseBean>> getSearchResultLiveData() {
        return searchResultLiveData;
    }

    /**
     * 获取推荐应用
     */
    public void getStoreApps() {
        if (checkNetWork()) {
            homeService.getStoreApps(storeAppsLiveData);
        }
    }

    /**
     * 检测网络是否可用
     */
    private boolean checkNetWork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplication().getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    //启动本地应用
    public void onStartApplication(String packageName) {
        Intent mIntent = getApplication()
                .getPackageManager()
                .getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (packageName.equals(Constants.MTvPlayer_Package_Name)) {
                try {
                    TvManager.getInstance()
                            .setTvosCommonCommand("SetAutoSleepOnStatus");
                } catch (TvCommonException e) {
                    e.printStackTrace();
                }
            }
            try {
                getApplication().startActivity(mIntent);
            } catch (ActivityNotFoundException anf) {
            }
        }
    }

    public void onGoToNetPage(String url, int back_keycode) {
        NetWorkUtils.gotoHtml5Page(getApplication(), url, back_keycode);
    }

    /**
     * 获取categories
     */
    public void getCategoryList() {
        if (checkNetWork()) {
            homeService.getCategoryList(categoryListLiveData);
        }
    }

    public void getCategoryDetail(int id) {
        if (checkNetWork()) {
            homeService.getCategoryDetail(categoryDetailLiveData, id);
        }
    }

    /**
     * 获取search结果
     *
     * @param key search关键字
     */
    public void getSearchResult(String key) {
        if (checkNetWork()) {
            homeService.getSearchResult(searchResultLiveData, key);
        }
    }

    public void storeAppsSortData(List<String> dataList) {
        homeService.storeAppSortResult(dataList);
    }

    public List<String> restoreAppsSortData() {
        return homeService.restoreAppSortResult();
    }
}
