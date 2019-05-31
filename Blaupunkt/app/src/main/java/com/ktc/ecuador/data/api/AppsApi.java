package com.ktc.ecuador.data.api;


import com.ktc.ecuador.data.protocal.CategoryBean;
import com.ktc.ecuador.data.protocal.CategoryDetail;
import com.ktc.ecuador.data.protocal.Poster;
import com.ktc.ecuador.data.protocal.SearchResultApp;
import com.ktc.ecuador.data.protocal.StoreApp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface AppsApi {
    //
    @GET("api/v1/misc/endpoint/?endpointType=blaupunkt-ui5")
    Call<Poster> getPosterData();

    //
    @GET("api/v1/recommend/app/?categoryId=1&authentication=1")
    Call<StoreApp> getStoreData(@Query("lang") String lang);
    //

    /**
     * 获取分类
     *
     * @param language 语言
     * @return StoreAppResponse
     */
    @GET("api/v1/category/video/")
    Call<CategoryBean> getCategories(@Query("lang") String language);

    /**
     * 获取对应分类下的应用详情
     *
     * @param categoryId 对应的id
     * @return CategoryDetailResponse
     */
    @GET("api/v1/recommend/video/")
    Call<CategoryDetail> getCategoryDetail(@Query("categoryId") int categoryId, @Query("lang") String language);

    /**
     * 搜索应用api, ?lang=es_EC
     *
     * @param searchterm 搜索关键字
     * @return 搜索结果
     */
    @GET("api/v1/search/app/")
    Call<SearchResultApp> getAppsSearchResult(@Query("searchterm") String searchterm, @Query("lang") String language);


    /**
     * 搜索推荐api
     *
     * @param searchterm 关键字
     * @param language   语言
     * @param categoryId id
     * @return 搜索结果
     */
    @GET("api/v1/search/video/")
    Call<CategoryDetail> getDemandSearchResult(@Query("searchterm") String searchterm,
                                               @Query("lang") String language,
                                               @Query("categoryId") int categoryId);

}
