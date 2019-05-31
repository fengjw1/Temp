package com.mp3.launcher4.proxys;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mp3.launcher4.adapters.TrendingAdapter;
import com.mp3.launcher4.beans.CategoryBean;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.holders.CategoryOtherHolder;
import com.mp3.launcher4.networks.NetworkSupports;
import com.mp3.launcher4.networks.responses.BannerResponse;
import com.mp3.launcher4.networks.responses.CategoriesResponse;
import com.mp3.launcher4.networks.responses.CategoryDetailResponse;
import com.mp3.launcher4.utils.CommonUtils;
import com.mp3.launcher4.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.security.KeyStore.getApplicationContext;

/**
 * @author longzj
 */
public class TrendingProxy extends BaseTitleRecyclerProxy<CategoryDetailBean> {

    public static final String LABEL_RECOMMENDED = "RECOMMENDED";
    private static final float BANNER_SCALE_RATIO = 1.06f;
    private static final float MOVIE_SCALE_RATIO = 1.2f;
    private static final int MAX_SHOWN_NUM = 10;
    private static final int DEFAULT_ID = 99;
    private Call<BannerResponse> mBannerResponseCall;
    private Call<CategoryDetailResponse> mRecommendCall;
    private Call<CategoriesResponse> mCategoriesCall;
    private boolean isBannerLoaded = false;
    private boolean isRecommendLoaded = false;


    public TrendingProxy(Context context, int titleId) {
        super(context, titleId);
    }

    @Override
    protected List<CategoryDetailBean> preloadData() {
        List<CategoryDetailBean> data = new ArrayList<>();
        for (int i = 0; i < PRELOAD_NUM; i++) {
            data.add(new CategoryDetailBean());
        }
        return data;
    }

    @Override
    protected BaseAdapter<CategoryDetailBean> initAdapter(List<CategoryDetailBean> data) {
        return new TrendingAdapter(getContext(), data);
    }

    @Override
    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                final Context context = getContext().getApplicationContext();
                int pos = parent.getChildAdapterPosition(view);
                int right = ImageUtils.dp2Px(context, 23);
                int bottom = ImageUtils.dp2Px(context, 100);
                outRect.set(0, 0, right, bottom);
            }
        };
    }

    @Override
    public void onItemSelected(View view, int position) {
        super.onItemSelected(view, position);
        reloadOtherCategoryImage(view,true);
    }

    @Override
    public void onItemUnselected(View view, int position) {
        super.onItemUnselected(view, position);
        reloadOtherCategoryImage(view,false);
    }

    @Override
    public void onItemClicked(View view, int position) {
        super.onItemClicked(view, position);
        final List<CategoryDetailBean> data = getData();
        CategoryDetailBean bean = data.get(position);
        if (bean == null) {
            return;
        }
        CommonUtils.startMP3Browser(getContext(), bean.getUrl(), bean.getBackCode());
    }

    @Override
    public void requestData() {
        super.requestData();
        startBannerCall();
        startCategoryCall();
    }

    /**
     * 重设其他分类视图中图的大小
     *
     * @param view        视图
     * @param hasSelected 是否选中
     */
    private void reloadOtherCategoryImage(View view, boolean hasSelected) {
        RecyclerView.ViewHolder holder = getHolder().getRecyclerView().getChildViewHolder(view);
        if (holder instanceof CategoryOtherHolder) {
            TextView title = ((CategoryOtherHolder) holder).getTitle();
            int heightDp = hasSelected ? 107 : 50;
            ViewGroup.LayoutParams params = title.getLayoutParams();
            params.height = ImageUtils.dp2Px(getApplicationContext(), heightDp);
            title.setMaxLines(hasSelected ? 5 : 2);
            title.setLayoutParams(params);
        }
    }


    @Override
    public void cancelRequest() {
        super.cancelRequest();
        if (mBannerResponseCall != null) {
            mBannerResponseCall.cancel();
        }
        if (mCategoriesCall != null) {
            mCategoriesCall.cancel();
        }
        if (mRecommendCall != null) {
            mRecommendCall.cancel();
        }
    }

    private void startBannerCall() {
        if (isBannerLoaded) {
            return;
        }
        if (mBannerResponseCall == null) {
            mBannerResponseCall = NetworkSupports.getInstance().createBannerCall();
        } else if (mBannerResponseCall.isExecuted()
                || mBannerResponseCall.isCanceled()) {
            mBannerResponseCall = mBannerResponseCall.clone();
        }
        mBannerResponseCall.enqueue(new Callback<BannerResponse>() {
            @Override
            public void onResponse(@NonNull Call<BannerResponse> call, @NonNull Response<BannerResponse> response) {
                if (!response.isSuccessful() || !isAttachToRecycle() || isParentScrolling()) {
                    return;
                }
                BannerResponse bannerResponse = response.body();
                CategoryDetailBean bean = new CategoryDetailBean();
                if (bannerResponse == null) {
                    return;
                }
                bean.setBackCode(0);
                bean.setImage(bannerResponse.getImage());
                bean.setTitle(bannerResponse.getTitle());
                bean.setUrl(bannerResponse.getUrl());
                final List<CategoryDetailBean> list = getData();
                list.remove(0);
                list.add(0, bean);
                notifyItemChanged(0);
                isBannerLoaded = true;
                setIgnoreUpdateData(isRecommendLoaded);
            }

            @Override
            public void onFailure(@NonNull Call<BannerResponse> call, @NonNull Throwable t) {
                resetRequestedFlag();
            }
        });
    }

    private void startRecommendCall(int id) {
        if (isRecommendLoaded) {
            return;
        }
        if (mRecommendCall == null) {
            mRecommendCall = NetworkSupports.getInstance().createCategoryDetailCall(id);
        } else if (mRecommendCall.isExecuted()
                || mRecommendCall.isCanceled()) {
            mRecommendCall = mRecommendCall.clone();
        }
        mRecommendCall.enqueue(new Callback<CategoryDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoryDetailResponse> call, @NonNull Response<CategoryDetailResponse> response) {
                handleResponse(response);
                isRecommendLoaded = true;
                setIgnoreUpdateData(isBannerLoaded);
            }

            @Override
            public void onFailure(@NonNull Call<CategoryDetailResponse> call, @NonNull Throwable t) {
                resetRequestedFlag();
            }
        });
    }

    private void startCategoryCall() {
        if (isRecommendLoaded) {
            return;
        }
        if (mCategoriesCall == null) {
            mCategoriesCall = NetworkSupports.getInstance().createCategoriesCall();
        } else if (mCategoriesCall.isCanceled() || mCategoriesCall.isExecuted()) {
            mCategoriesCall = mCategoriesCall.clone();
        }
        mCategoriesCall.enqueue(new Callback<CategoriesResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoriesResponse> call, @NonNull Response<CategoriesResponse> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                CategoriesResponse categoriesResponse = response.body();
                if (categoriesResponse == null) {
                    return;
                }
                List<CategoryBean> categoryBeanList = categoriesResponse.getCategories();
                for (CategoryBean bean : categoryBeanList) {
                    if (LABEL_RECOMMENDED.equals(bean.getLabel())) {
                        startRecommendCall(bean.getId());
                        break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoriesResponse> call, @NonNull Throwable t) {
                startRecommendCall(DEFAULT_ID);
            }
        });
    }



    private void handleResponse(Response<CategoryDetailResponse> response) {
        if (!response.isSuccessful()) {
            return;
        }
        CategoryDetailResponse detailResponse = response.body();
        if (detailResponse == null) {
            return;
        }
        List<CategoryDetailBean> list = getData();
        List<CategoryDetailBean> responseList = detailResponse.getList();
        int size = responseList.size();
        if (size == 0) {
            return;
        }
        ListIterator<CategoryDetailBean> iterator = list.listIterator();

        while (iterator.hasNext()) {
            if (iterator.nextIndex() == 0) {
                iterator.next();
                continue;
            }
            iterator.next();
            iterator.remove();
        }
        size = size < MAX_SHOWN_NUM ? size : MAX_SHOWN_NUM;
        for (int index = 0; index < size; index++) {
            list.add(responseList.get(index));
        }
        notifyDataSetChanged();
    }
}
