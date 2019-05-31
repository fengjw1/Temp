package com.ktc.ecuador.service;

import android.arch.lifecycle.MutableLiveData;
import android.content.pm.PackageManager;

import java.util.List;

public interface HomeService {
    void getMyApps(MutableLiveData liveData, PackageManager packageManager);

    void getStoreApps(MutableLiveData storeAppsLiveData);

    void getCategoryList(MutableLiveData categoryListLiveData);

    void getCategoryDetail(MutableLiveData categoryDetailLiveData, int id);

    /**
     * 获取search结果
     *
     * @param searchResultLiveData 获取到的结果，APP和category信息
     * @param key                  search关键字
     */
    void getSearchResult(MutableLiveData searchResultLiveData, String key);

    void storeAppSortResult(List<String> dataList);

    List<String> restoreAppSortResult();
}
