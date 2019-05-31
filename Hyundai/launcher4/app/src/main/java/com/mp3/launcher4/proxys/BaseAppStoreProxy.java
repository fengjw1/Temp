package com.mp3.launcher4.proxys;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.networks.NetworkSupports;
import com.mp3.launcher4.networks.responses.StoreAppResponse;
import com.mp3.launcher4.utils.AppsUtils;
import com.mp3.launcher4.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author longzj
 */
public abstract class BaseAppStoreProxy extends BaseTitleRecyclerProxy<AppDetailBean> {

    private static final int MAX_SHOWN_NUM = 10;
    private Call<StoreAppResponse> mStoreCall;
    private AppsUtils mAppsUtils;

    public BaseAppStoreProxy(Context context, int titleId) {
        super(context, titleId);
        mAppsUtils = AppsUtils.getInstance(context.getApplicationContext());
    }

    @Override
    protected List<AppDetailBean> preloadData() {
        List<AppDetailBean> data = new ArrayList<>();
        for (int i = 0; i < PRELOAD_NUM; i++) {
            data.add(new AppDetailBean());
        }
        return data;
    }


    @Override
    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int right = ImageUtils.dp2Px(getContext().getApplicationContext(), 23);
                int bottom = ImageUtils.dp2Px(getContext().getApplicationContext(), 70);
                outRect.set(0, 0, right, bottom);
            }
        };
    }

    @Override
    public void onItemClicked(View view, int position) {
        super.onItemClicked(view, position);
    }

    @Override
    public void requestData() {
        super.requestData();
        doStoreListCall();
    }

    @Override
    public void cancelRequest() {
        super.cancelRequest();
        if (mStoreCall != null) {
            mStoreCall.cancel();
        }
    }

    private void doStoreListCall() {
        if (mStoreCall == null) {
            mStoreCall = NetworkSupports.getInstance()
                    .createStoreCall(getId());
        } else if (mStoreCall.isExecuted()
                || mStoreCall.isCanceled()) {
            mStoreCall = mStoreCall.clone();
        }
        mStoreCall.enqueue(new Callback<StoreAppResponse>() {
            @Override
            public void onResponse(@NonNull Call<StoreAppResponse> call, @NonNull Response<StoreAppResponse> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                final List<AppDetailBean> list = getData();
                StoreAppResponse storeAppResponse = response.body();
                if (storeAppResponse == null) {
                    return;
                }
                list.clear();
                List<AppDetailBean> responseList = storeAppResponse.getList();
                int size = responseList.size();
                size = size < MAX_SHOWN_NUM ? size : MAX_SHOWN_NUM;
                if (getId() == 1) {
                    list.add(new AppDetailBean());
                }
                for (int count = 0; count < size; count++) {
                    list.add(responseList.get(count));
                }
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<StoreAppResponse> call, @NonNull Throwable t) {
                resetRequestedFlag();
            }
        });
    }

    protected abstract int getId();

    public AppsUtils getAppsUtils() {
        return mAppsUtils;
    }
}
