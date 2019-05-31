package com.mp3.launcher4.proxys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mp3.launcher4.activities.DetailCategoryActivity;
import com.mp3.launcher4.adapters.HomeVideoAdapter;
import com.mp3.launcher4.beans.CategoryBean;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.networks.NetworkSupports;
import com.mp3.launcher4.networks.responses.CategoriesResponse;
import com.mp3.launcher4.networks.responses.CategoryDetailResponse;
import com.mp3.launcher4.utils.CommonUtils;
import com.mp3.launcher4.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author longzj
 */
public class VideoProxy extends BaseTitleRecyclerProxy<CategoryDetailBean> {

    static final String LABEL_MOVIES = "MOVIES";
    private static final int MAX_SHOWN_NUM = 9;
    private static final int DEFAULT_ID = 1;
    private Call<CategoryDetailResponse> mVideoCall;
    private Call<CategoriesResponse> mCategoriesCall;
    private CategoryBean mCategoryBean;

    public VideoProxy(Context context, int titleId) {
        super(context, titleId);
    }

    private void startCategoryCall() {
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
                    if (LABEL_MOVIES.equals(bean.getLabel())) {
                        mCategoryBean = bean;
                        startVideoCall(bean.getId());
                        break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoriesResponse> call, @NonNull Throwable t) {
                startVideoCall(DEFAULT_ID);
            }
        });
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
        return new HomeVideoAdapter(getContext(), data);
    }

    @Override
    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int right = ImageUtils.dp2Px(getContext().getApplicationContext(), 23);
                int bottom = ImageUtils.dp2Px(getContext().getApplicationContext(), 110);
                outRect.set(0, 0, right, bottom);
            }
        };
    }

    @Override
    public void onItemClicked(View view, int position) {
        super.onItemClicked(view, position);
        final List<CategoryDetailBean> data = getData();
        CategoryDetailBean bean = data.get(position);
        if (mCategoryBean != null && position == data.size() - 1) {
            Intent intent = new Intent(getContext(), DetailCategoryActivity.class);
            intent.putExtra("category", mCategoryBean);
            CommonUtils.startActivityForIntent((Activity) getContext(), intent);
        } else {
            CommonUtils.startMP3Browser(getContext(), bean.getUrl(), bean.getBackCode());
        }
    }

    @Override
    public void requestData() {
        super.requestData();
        startCategoryCall();
    }

    @Override
    public void cancelRequest() {
        super.cancelRequest();
        if (mVideoCall != null) {
            mVideoCall.cancel();
        }
    }

    private void startVideoCall(int id) {
        if (mVideoCall == null) {
            mVideoCall = NetworkSupports.getInstance()
                    .createCategoryDetailCall(id);
        } else if (mVideoCall.isExecuted()
                || mVideoCall.isCanceled()) {
            mVideoCall = mVideoCall.clone();
        }
        mVideoCall.enqueue(new Callback<CategoryDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoryDetailResponse> call, @NonNull Response<CategoryDetailResponse> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                final List<CategoryDetailBean> list = getData();
                CategoryDetailResponse detailResponse = response.body();
                if (detailResponse == null) {
                    return;
                }
                list.clear();
                List<CategoryDetailBean> responseList = detailResponse.getList();
                int size = responseList.size();
                size = size < MAX_SHOWN_NUM ? size : MAX_SHOWN_NUM;
                for (int count = 0; count < size; count++) {
                    list.add(responseList.get(count));
                }
                list.add(new CategoryDetailBean());
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<CategoryDetailResponse> call, @NonNull Throwable t) {
                resetRequestedFlag();
            }
        });
    }
}
