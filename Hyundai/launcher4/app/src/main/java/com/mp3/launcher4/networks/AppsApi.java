package com.mp3.launcher4.networks;

import com.mp3.launcher4.networks.responses.BannerResponse;
import com.mp3.launcher4.networks.responses.CategoriesResponse;
import com.mp3.launcher4.networks.responses.CategoryDetailResponse;
import com.mp3.launcher4.networks.responses.StoreAppResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * retrofit 相关接口定义
 *
 * @author longzj
 */
public interface AppsApi {

    /**
     * 获取推荐应用位应用信息
     *
     * @param language 语言
     * @param id       id
     * @return 请求接收后的推荐位app列表信息
     */
    @GET("api/v1/recommend/app/")
    Call<StoreAppResponse> getStoreData(@Query("categoryId") int id, @Query("lang") String language);

    /**
     * 获取分类
     *
     * @param language 语言
     * @return StoreAppResponse
     */
    @GET("api/v1/category/video/")
    Call<CategoriesResponse> getCategories(@Query("lang") String language);

    /**
     * 获取banner数据
     *
     * @return banner数据
     */
    @GET("api/v1/misc/endpoint/?endpointType=hyundai")
    Call<BannerResponse> getBannerData();


    /**
     * 获取对应分类下的应用详情
     *
     * @param categoryId 对应的id
     * @return CategoryDetailResponse
     */
    @GET("api/v1/recommend/video/")
    Call<CategoryDetailResponse> getCategoryDetail(@Query("categoryId") int categoryId, @Query("lang") String language);


    /**
     * 搜索应用api, ?lang=es_EC
     *
     * @param searchterm 搜索关键字
     * @return 搜索结果
     */
    @GET("api/v1/search/app/")
    Call<StoreAppResponse> getAppsSearchResult(@Query("searchterm") String searchterm, @Query("lang") String language);


    /**
     * 搜索推荐api
     *
     * @param searchterm 关键字
     * @param language   语言
     * @param categoryId id
     * @return 搜索结果
     */
    @GET("api/v1/search/video/")
    Call<CategoryDetailResponse> getDemandSearchResult(@Query("searchterm") String searchterm,
                                                       @Query("lang") String language,
                                                       @Query("categoryId") int categoryId);
}
