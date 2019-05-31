package com.mp3.launcher4.proxys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

import com.mp3.launcher4.activities.DetailCategoryActivity;
import com.mp3.launcher4.adapters.CategoryAdapter;
import com.mp3.launcher4.beans.CategoryBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.customs.views.complex.holds.TitleRecyclerHolder;
import com.mp3.launcher4.networks.NetworkSupports;
import com.mp3.launcher4.networks.responses.CategoriesResponse;
import com.mp3.launcher4.utils.CommonUtils;
import com.mp3.launcher4.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author longzj
 */
public class CategoryProxy extends BaseTitleRecyclerProxy<CategoryBean> {

    private static final int MAX_SHOWN_NUM = 6;
    private Call<CategoriesResponse> mCategoriesCall;


    public CategoryProxy(Context context, int titleId) {
        super(context, titleId);
    }

    @Override
    public boolean onBindViews(TitleRecyclerHolder holder) {
        if (super.onBindViews(holder)) {
            holder.getRecyclerView().addLimitedKey(KeyEvent.KEYCODE_DPAD_DOWN);
            return true;
        }
        return false;
    }

    @Override
    public boolean isGroupAbove() {
        return true;
    }

    @Override
    protected List<CategoryBean> preloadData() {
        List<CategoryBean> data = new ArrayList<>();
        for (int i = 0; i < PRELOAD_NUM; i++) {
            data.add(new CategoryBean());
        }
        return data;
    }

    @Override
    protected BaseAdapter<CategoryBean> initAdapter(List<CategoryBean> data) {
        return new CategoryAdapter(getContext(), data);
    }

    @Override
    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int right = ImageUtils.dp2Px(getContext().getApplicationContext(), 23);
                outRect.set(0, 0, right, 0);
            }
        };
    }

    @Override
    public void onItemClicked(View view, int position) {
        super.onItemClicked(view, position);
        CategoryBean bean = getData().get(position);
        if (bean.getLabel() != null) {
            Intent intent = new Intent(getContext(), DetailCategoryActivity.class);
            intent.putExtra("category", bean);
            CommonUtils.startActivityForIntent((Activity) getContext(), intent);
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
        if (mCategoriesCall != null) {
            mCategoriesCall.cancel();
        }
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
                ListIterator<CategoryBean> iterator = categoryBeanList.listIterator();
                while (iterator.hasNext()) {
                    CategoryBean bean = iterator.next();
                    if (bean.getLabel().equals(VideoProxy.LABEL_MOVIES)
                            || bean.getLabel().equals(TrendingProxy.LABEL_RECOMMENDED)) {
                        iterator.remove();
                    }
                }
                final List<CategoryBean> data = getData();
                data.clear();
                data.addAll(categoryBeanList);
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<CategoriesResponse> call, @NonNull Throwable t) {

            }
        });
    }
}
