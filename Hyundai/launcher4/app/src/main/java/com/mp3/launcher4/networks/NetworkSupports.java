package com.mp3.launcher4.networks;

import com.mp3.launcher4.LauncherApplication;
import com.mp3.launcher4.networks.responses.BannerResponse;
import com.mp3.launcher4.networks.responses.CategoriesResponse;
import com.mp3.launcher4.networks.responses.CategoryDetailResponse;
import com.mp3.launcher4.networks.responses.StoreAppResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author longzj
 */
public class NetworkSupports {

    private static NetworkSupports instance;
    private AppsApi mAppsApi;

    private NetworkSupports() {
        final long maxCache = 10 * 10 * 1024;
        Cache cache = new Cache(LauncherApplication.mAppContext.getCacheDir(), maxCache);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.retryOnConnectionFailure(true);
        builder.connectTimeout(10 * 1000, TimeUnit.MILLISECONDS);
        builder.cache(cache);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LauncherApplication.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();
        mAppsApi = retrofit.create(AppsApi.class);
    }

    public static NetworkSupports getInstance() {
        if (instance == null) {
            synchronized (NetworkSupports.class) {
                if (instance == null) {
                    instance = new NetworkSupports();
                }
            }
        }
        return instance;
    }

    public Call<StoreAppResponse> createStoreCall(int id) {
        return mAppsApi.getStoreData(id, LauncherApplication.LANGUAGE);
    }

    public Call<CategoriesResponse> createCategoriesCall() {
        return mAppsApi.getCategories(LauncherApplication.LANGUAGE);
    }

    public Call<BannerResponse> createBannerCall() {
        return mAppsApi.getBannerData();
    }

    public Call<CategoryDetailResponse> createCategoryDetailCall(int categoryId) {
        return mAppsApi.getCategoryDetail(categoryId, LauncherApplication.LANGUAGE);
    }

    public Call<CategoryDetailResponse> createDemandSearchCall(String key, int id) {
        return mAppsApi.getDemandSearchResult(key, LauncherApplication.LANGUAGE, id);
    }

    public Call<StoreAppResponse> createAppSearchCall(String key) {
        return mAppsApi.getAppsSearchResult(key, LauncherApplication.LANGUAGE);
    }


}
