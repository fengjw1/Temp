package com.mp3.launcher4.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.activities.bases.BaseActivity;
import com.mp3.launcher4.adapters.AllMovieAdapter;
import com.mp3.launcher4.beans.CategoryBean;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.customs.views.indicators.AVLoadingIndicatorView;
import com.mp3.launcher4.holders.CategoryOtherHolder;
import com.mp3.launcher4.holders.VideoHolder;
import com.mp3.launcher4.networks.NetworkSupports;
import com.mp3.launcher4.networks.responses.CategoryDetailResponse;
import com.mp3.launcher4.utils.AnimatorUtils;
import com.mp3.launcher4.utils.CommonUtils;
import com.mp3.launcher4.utils.FontUtils;
import com.mp3.launcher4.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author zhouxw
 */
public class DetailCategoryActivity<T> extends BaseActivity {


    private RecyclerView mRecyclerView;
    private BaseAdapter<CategoryDetailBean> mDemandAdapter;
    private Call<CategoryDetailResponse> mCall;
    private SparseArray<View> mViewSparseArray;
    private AnimatorSet mFocusOnDemandAnimSet;
    private AnimatorSet mUnfocusDemandAnimSet;
    private int mCategoryId;
    private ImageView imgDetailIcon;
    private TextView tvDetailCategoryName;
    private TextView tvDetailCategorySecondName;
    private AVLoadingIndicatorView mAVLoadingIndicatorView;
    private String mCategoryName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_category);
        initProperty();
        initWidget();
        initRecyclerView();
        initAdapter();
        requestOnDemandData();

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onCheckNetworkState(boolean hasNetwork) {

    }

    private void initProperty() {
        mViewSparseArray = new SparseArray<>(2);
        Intent intent = getIntent();
        CategoryBean categoryBean = intent.getParcelableExtra("category");
        if (categoryBean == null) {
            finish();
            return;
        }
        mCategoryId = categoryBean.getId();
        mCategoryName = categoryBean.getDisplay();
    }

    private void initAdapter() {
        initCategoryAdapter();
        imgDetailIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_demand_hl, null));
        String label = getResources().getString(R.string.slide_on_demand) + " |";
        tvDetailCategoryName.setText(label);
        tvDetailCategorySecondName.setText(mCategoryName);
    }

    private void initWidget() {
        mRecyclerView = findViewById(R.id.detail_demand_rv);
        imgDetailIcon = findViewById(R.id.detail_icon);
        tvDetailCategoryName = findViewById(R.id.detail_categoryName);
        tvDetailCategorySecondName = findViewById(R.id.detail_category_secondName);
        mAVLoadingIndicatorView = findViewById(R.id.detail_indicator);

        imgDetailIcon.setVisibility(View.VISIBLE);
        tvDetailCategoryName.setVisibility(View.VISIBLE);
        tvDetailCategorySecondName.setVisibility(View.VISIBLE);
        FontUtils fontUtils = FontUtils.getInstance(getApplicationContext());
        fontUtils.setBoldFont(tvDetailCategoryName);
        fontUtils.setLightFont(tvDetailCategorySecondName);

    }

    private void initRecyclerView() {
        mRecyclerView.setFocusable(false);
        mRecyclerView.setFocusableInTouchMode(false);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 6, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

    }

    private void removeItemDecoration() {
        int itemDecCount = mRecyclerView.getItemDecorationCount();
        for (int index = 0; index < itemDecCount; index++) {
            mRecyclerView.removeItemDecorationAt(index);
        }
    }


    private void initCategoryAdapter() {
        final List<CategoryDetailBean> demandList = new ArrayList<>();
        mDemandAdapter = new AllMovieAdapter(this, demandList);
        removeItemDecoration();
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = ImageUtils.dp2Px(DetailCategoryActivity.this, 10);
                outRect.right = ImageUtils.dp2Px(DetailCategoryActivity.this, 10);
                outRect.top = ImageUtils.dp2Px(DetailCategoryActivity.this, 20);
                outRect.bottom = ImageUtils.dp2Px(DetailCategoryActivity.this, 20);
            }
        });
        mDemandAdapter.setOnItemSelectListener(new BaseAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelected(View view, int position) {
                if (mRecyclerView.getChildViewHolder(view) instanceof VideoHolder) {
                    focusMovieAnim(view);
                } else {
                    focusOtherCategoryAnim(view);
                    reloadOtherCategoryImage(view, true);
                }

            }

            @Override
            public void onItemUnselected(View view, int position) {
                if (mRecyclerView.getChildViewHolder(view) instanceof VideoHolder) {
                    unfocusMovieAnim(view);
                } else {
                    unfocusOtherCategoryAnim(view);
                    reloadOtherCategoryImage(view, false);
                }
            }
        });

        mDemandAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                CommonUtils.startMP3Browser(DetailCategoryActivity.this, demandList.get(position).getUrl(), demandList.get(position).getBackCode());
            }
        });

        mRecyclerView.setAdapter(mDemandAdapter);
    }

    protected void requestOnDemandData() {
        if (mCall != null && mCall.isExecuted()) {
            mCall.cancel();
        }
        mCall = NetworkSupports.getInstance().createCategoryDetailCall(mCategoryId);
        final List<CategoryDetailBean> data = mDemandAdapter.getData();
        if (!data.isEmpty()) {
            data.clear();
            mDemandAdapter.notifyDataSetChanged();
        }
        final AVLoadingIndicatorView indicatorView = getIndicatorView();
        indicatorView.setVisibility(View.GONE);
        indicatorView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (data.isEmpty()) {
                    indicatorView.setVisibility(View.VISIBLE);
                }
            }
        }, 400);
        mCall.enqueue(new Callback<CategoryDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoryDetailResponse> call, Response<CategoryDetailResponse> response) {
                if (response.isSuccessful()) {
                    CategoryDetailResponse responseBody = response.body();

                    if (responseBody != null) {
                        notifyDataChanged(responseBody.getList());
                        getIndicatorView().setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoryDetailResponse> call, @NonNull Throwable t) {

            }
        });
    }

    public AVLoadingIndicatorView getIndicatorView() {
        return mAVLoadingIndicatorView;
    }

    /**
     * 默认通知更新方式
     *
     * @param data 数据集
     */
    protected void notifyDataChanged(List<CategoryDetailBean> data) {
        if (data == null) {
            return;
        }
        List<CategoryDetailBean> baseData = mDemandAdapter.getData();
        if (baseData.contains(data)) {
            return;
        }
        int startPos = baseData.size();
        baseData.addAll(data);
        int endPos = baseData.size();
        if (endPos == 1) {
            mDemandAdapter.notifyItemChanged(0);
        } else {
            mDemandAdapter.notifyItemRangeInserted(startPos, endPos);
        }

    }

    /**
     * 当电影item的焦点消失时执行动画
     *
     * @param view item对应的视图
     */
    private void unfocusMovieAnim(View view) {
        AnimatorSet set = new AnimatorSet();
        Animator transZAnim = AnimatorUtils.createElevationAnim(view.findViewById(R.id.category_cover), 0);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(view.findViewById(R.id.card), 1.0f);
        Animator titleAlphaAnim = AnimatorUtils.createAlphaAnim(view.findViewById(R.id.category_title), 0.0f);
        set.playTogether(transZAnim, scaleAnim, titleAlphaAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new AccelerateInterpolator());
        set.start();
    }

    /**
     * 当电影item获取焦点时执行动画
     *
     * @param view item对应的视图
     */
    private void focusMovieAnim(View view) {
        AnimatorSet set = new AnimatorSet();
        Animator transZAnim = AnimatorUtils.createElevationAnim(view.findViewById(R.id.category_cover), AnimatorUtils.BASE_Z);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(view.findViewById(R.id.card), AnimatorUtils.BASE_RECTANGLE_SCALE);
        Animator titleAlphaAnim = AnimatorUtils.createAlphaAnim(view.findViewById(R.id.category_title), 1.0f);
        set.playTogether(transZAnim, scaleAnim, titleAlphaAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    /**
     * 当其他分类的item失去焦点时
     *
     * @param view 视图
     */
    private void unfocusOtherCategoryAnim(View view) {
        View focusedAnimView = mViewSparseArray.get(0);
        if (focusedAnimView != null && focusedAnimView.equals(view)) {
            mFocusOnDemandAnimSet.cancel();
        }
        mUnfocusDemandAnimSet = new AnimatorSet();
        CategoryOtherHolder holder = (CategoryOtherHolder) mRecyclerView.getChildViewHolder(view);
        Animator otherAnim = AnimatorUtils.createUnfocusOtherAnim(holder, 1.0f);
        Animator otherAlphaAnim = AnimatorUtils.createAlphaAnim(holder.getTitle(), 0.4f);
        mUnfocusDemandAnimSet.playTogether(otherAnim, otherAlphaAnim);
        mUnfocusDemandAnimSet.setDuration(AnimatorUtils.BASE_TIME);
        if (mUnfocusDemandAnimSet != null) {
            mViewSparseArray.put(1, view);
            mUnfocusDemandAnimSet.start();
        }
    }

    /**
     * 当其他分类的item获得焦点时
     *
     * @param view 视图
     */
    private void focusOtherCategoryAnim(View view) {
        View unfocusedAnimView = mViewSparseArray.get(1);
        if (unfocusedAnimView != null && unfocusedAnimView.equals(view)) {
            mUnfocusDemandAnimSet.cancel();
        }
        mFocusOnDemandAnimSet = new AnimatorSet();
        CategoryOtherHolder holder = (CategoryOtherHolder) mRecyclerView.getChildViewHolder(view);
        Animator otherAnim = AnimatorUtils.createFocusOnOtherAnim(holder, AnimatorUtils.BASE_RECTANGLE_SCALE);
        Animator otherAlphaAnim = AnimatorUtils.createAlphaAnim(holder.getTitle(), 1.0f);
        mFocusOnDemandAnimSet.playTogether(otherAnim);
        mFocusOnDemandAnimSet.setDuration(AnimatorUtils.BASE_TIME);
        if (mFocusOnDemandAnimSet != null) {
            mViewSparseArray.put(0, view);
            mFocusOnDemandAnimSet.start();
        }
    }

    /**
     * 重设其他分类视图中图的大小
     *
     * @param view        视图
     * @param hasSelected 是否选中
     */
    private void reloadOtherCategoryImage(View view, boolean hasSelected) {
        CategoryOtherHolder holder = (CategoryOtherHolder) mRecyclerView.getChildViewHolder(view);
        TextView title = holder.getTitle();
        int heightDp = hasSelected ? 107 : 50;
        ViewGroup.LayoutParams params = title.getLayoutParams();
        params.height = ImageUtils.dp2Px(getApplicationContext(), heightDp);
        title.setMaxLines(hasSelected ? 5 : 2);
    }


}
