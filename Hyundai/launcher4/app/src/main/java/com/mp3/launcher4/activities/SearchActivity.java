package com.mp3.launcher4.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.activities.bases.BaseActivity;
import com.mp3.launcher4.adapters.AllAppAdapter;
import com.mp3.launcher4.adapters.AllMovieAdapter;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.customs.layoutmanagers.FocusLinearLayoutManager;
import com.mp3.launcher4.customs.views.SearchView;
import com.mp3.launcher4.customs.views.indicators.AVLoadingIndicatorView;
import com.mp3.launcher4.holders.CategoryOtherHolder;
import com.mp3.launcher4.holders.VideoHolder;
import com.mp3.launcher4.networks.NetworkSupports;
import com.mp3.launcher4.networks.responses.CategoryDetailResponse;
import com.mp3.launcher4.networks.responses.StoreAppResponse;
import com.mp3.launcher4.utils.AnimatorUtils;
import com.mp3.launcher4.utils.AppsUtils;
import com.mp3.launcher4.utils.CommonUtils;
import com.mp3.launcher4.utils.FontUtils;
import com.mp3.launcher4.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author zhouxw
 */
public class SearchActivity extends BaseActivity {

    private final static int MIN_CHAR_COUNT = 3;
    private static final int RC_SEARCH = 1;
    private final static int INTERVAL = 1000;
    private View mSearchResultParent;
    private TextView mPreSearchTipsText;
    private TextView searchDemandLabel;
    private TextView searchAppLabel;
    private RecyclerView mAppRecyclerView;
    private RecyclerView mDemandRecyclerView;
    private EditText mSearchEdit;
    private List<AppDetailBean> mAppDetailList;
    private AllAppAdapter mAppAdapter;
    private List<CategoryDetailBean> mDemandList;
    private BaseAdapter mDemandAdapter;
    private int categoryindex = 0;
    private Boolean isAppSearched = false;
    private List<Call<CategoryDetailResponse>> mCallList;
    private SparseArray<View> mViewSparseArray;
    private AnimatorSet mFocusOnDemandAnimSet;
    private AnimatorSet mUnfocusDemandAnimSet;
    private AVLoadingIndicatorView mIndicatorView;
    private Call<StoreAppResponse> mAppSearchCall;
    private Call<CategoryDetailResponse> mDemandSearchCall;
    private ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    private int[] categoryId = new int[]{1, 2, 3, 5, 6, 7};
    private int mCategoryId;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RC_SEARCH) {
                String searchKey = (String) msg.obj;
                startSearch(searchKey);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initProperty();
        initWidget();
    }

    @Override
    protected void onCheckNetworkState(boolean hasNetwork) {
        String key = mSearchEdit.getText().toString();
        handleOnTextChange(key.length(), key);
    }

    void initProperty() {
        mViewSparseArray = new SparseArray<>(2);
        mCallList = new ArrayList<>();
        Intent intent = getIntent();
        mCategoryId = intent.getIntExtra("categoryId+", -1);
        if (mCategoryId == -1) {
            mCategoryId = 1;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAppSearchCall != null) {
            mAppSearchCall.cancel();
        }
        if (mDemandSearchCall != null) {
            mDemandSearchCall.cancel();
        }
        if (mHandler != null) {
            mHandler.removeMessages(RC_SEARCH);
        }
    }

    void initWidget() {
        mSearchResultParent = findViewById(R.id.search_result_parent);
        mPreSearchTipsText = (TextView) findViewById(R.id.search_tv_tips);
        mAppRecyclerView = (RecyclerView) findViewById(R.id.search_apps_rv);
        mDemandRecyclerView = (RecyclerView) findViewById(R.id.search_demand_rv);
        mIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.search_indicator);
        mSearchEdit = (EditText) findViewById(R.id.searchView_et);
        SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchAppLabel = (TextView) findViewById(R.id.search_app_label);
        searchDemandLabel = (TextView) findViewById(R.id.search_demand_label);
        initAppRecyclerView();
        initDemandRecyclerView();
        initEdit();
        searchView.addListener(new SearchView.SearchViewListener() {
            @Override
            public void backPress() {
                onBackPressed();
            }
        });
        FontUtils fontUtils = FontUtils.getInstance(getApplicationContext());
        fontUtils.setRegularFont(mPreSearchTipsText);
        fontUtils.setLightFont(searchAppLabel);
        fontUtils.setLightFont(searchDemandLabel);
    }

    void initAppRecyclerView() {
        mAppRecyclerView.setFocusable(false);
        mAppRecyclerView.setFocusableInTouchMode(false);
        mAppDetailList = new ArrayList<>();
        mAppAdapter = new AllAppAdapter(this, mAppDetailList);
        mAppRecyclerView.setLayoutManager(new FocusLinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        mAppRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.right = ImageUtils.dp2Px(SearchActivity.this, 15);
            }
        });
        mAppRecyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mAppRecyclerView.setAdapter(mAppAdapter);
        mAppAdapter.setOnItemSelectListener(new BaseAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelected(View view, int position) {
                executeAppAnimator(view, true);
            }

            @Override
            public void onItemUnselected(View view, int position) {
                executeAppAnimator(view, false);
            }
        });
        mAppAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                AppDetailBean bean = mAppDetailList.get(position);
                AppsUtils.getInstance(getApplicationContext()).startApp(SearchActivity.this,
                        bean, true);
            }
        });
    }

    void initDemandRecyclerView() {
        mDemandRecyclerView.setFocusable(false);
        mDemandRecyclerView.setFocusableInTouchMode(false);
        mDemandList = new ArrayList<>();
        mDemandAdapter = new AllMovieAdapter(this, mDemandList);
        initAdapter();
        mDemandAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                CategoryDetailBean bean = mDemandList.get(position);
                String url = bean.getUrl();
                int backCode = bean.getBackCode();
                if (url != null) {
                    CommonUtils.startMP3Browser(SearchActivity.this, url, backCode);
                }
            }
        });
        mDemandRecyclerView.setLayoutManager(new FocusLinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        mDemandRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.right = ImageUtils.dp2Px(SearchActivity.this, 20);
            }
        });
        mDemandRecyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mDemandRecyclerView.setAdapter(mDemandAdapter);
    }

    void initAdapter() {
        mDemandAdapter.setOnItemSelectListener(new BaseAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelected(View view, int position) {
                if (mDemandRecyclerView.getChildViewHolder(view) instanceof VideoHolder) {
                    focusMovieAnim(view);
                } else {
                    focusOtherCategoryAnim(view);
                    reloadOtherCategoryImage(view, true);
                }

            }

            @Override
            public void onItemUnselected(View view, int position) {
                if (mDemandRecyclerView.getChildViewHolder(view) instanceof VideoHolder) {
                    unfocusMovieAnim(view);
                } else {
                    unfocusOtherCategoryAnim(view);
                    reloadOtherCategoryImage(view, false);
                }
            }
        });

    }


    void initEdit() {
        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchEdit.setSelection(s.length());
            }

            @Override
            public void afterTextChanged(Editable s) {
                handleOnTextChange(s.length(), s.toString());
            }
        });
    }

    void handleOnTextChange(int length, String searchKey) {
        boolean hasNetwork = hasNetwork();
        if (!hasNetwork) {
            mSearchResultParent.setVisibility(View.GONE);
            mPreSearchTipsText.setVisibility(View.GONE);
            mIndicatorView.setVisibility(View.GONE);
            return;
        }
        int searchVisible;
        int tipsVisible;
        int indicatorVisible;
        boolean isLengthValid = length >= MIN_CHAR_COUNT;
        boolean isShowingResult = !mAppDetailList.isEmpty()
                || !mDemandList.isEmpty();
        if (isLengthValid) {
            searchVisible = View.GONE;
            tipsVisible = View.GONE;
            indicatorVisible = View.VISIBLE;
        } else if (isShowingResult) {
            searchVisible = View.VISIBLE;
            tipsVisible = View.GONE;
            indicatorVisible = View.GONE;
        } else {
            searchVisible = View.GONE;
            tipsVisible = View.VISIBLE;
            indicatorVisible = View.GONE;
        }
        mSearchResultParent.setVisibility(searchVisible);
        mPreSearchTipsText.setVisibility(tipsVisible);
        mIndicatorView.setVisibility(indicatorVisible);
        if (isLengthValid) {
            if (mHandler.hasMessages(RC_SEARCH)) {
                mHandler.removeMessages(RC_SEARCH);
            }
            Message msg = Message.obtain();
            msg.what = RC_SEARCH;
            msg.obj = searchKey;
            mHandler.sendMessageDelayed(msg, INTERVAL);
        }
    }

    void startSearch(final String key) {
        categoryindex = 0;
        mDemandList.clear();
        mAppDetailList.clear();
        isAppSearched = false;
        startSearchingApp(key);
        Runnable searchTask = new Runnable() {
            @Override
            public void run() {
                startSearchingDemand(key);
            }
        };

        for (int aCategoryId : categoryId) {
            singleThreadPool.execute(searchTask);
        }


    }

    /**
     * 启动异步搜索并加载app类数据
     *
     * @param key 关键字
     */
    void startSearchingApp(final String key) {
        mAppDetailList.clear();
        mAppAdapter.notifyDataSetChanged();
        if (mAppSearchCall != null) {
            mAppSearchCall.cancel();
        }
        mAppSearchCall = NetworkSupports.getInstance().createAppSearchCall(key);
        mAppSearchCall.enqueue(new Callback<StoreAppResponse>() {
            @Override
            public void onResponse(@NonNull Call<StoreAppResponse> call, @NonNull Response<StoreAppResponse> response) {
                StoreAppResponse app = response.body();
                if (app == null) {
                    return;
                }
                List<AppDetailBean> data = app.getList();
                if (data != null && data.get(0).getId() != 0) {
                    mAppDetailList.addAll(data);
                }
                isAppSearched = true;
            }

            @Override
            public void onFailure(@NonNull Call<StoreAppResponse> call, @NonNull Throwable t) {
                isAppSearched = true;
            }
        });
    }


    /**
     * 启动异步搜索并加载推荐数据
     *
     * @param key 关键字
     */
    void startSearchingDemand(final String key) {
        mCategoryId = categoryId[categoryindex++];
        mDemandSearchCall = NetworkSupports.getInstance().createDemandSearchCall(key, mCategoryId);
        mCallList.add(mDemandSearchCall);
        if (mDemandSearchCall.isExecuted()) {
            mDemandSearchCall.cancel();
            mDemandSearchCall.clone();
        }
        mDemandSearchCall.enqueue(new Callback<CategoryDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoryDetailResponse> call, @NonNull Response<CategoryDetailResponse> response) {
                mCallList.remove(call);
                CategoryDetailResponse app = response.body();
                if (app == null) {
                    if (mCallList.isEmpty()) {
                        handleResult(key);
                    }
                    return;
                }
                List<CategoryDetailBean> data = app.getList();
                final String empty = "0";
                if (data != null && !data.get(0).getType_id().equals(empty)) {
                    mDemandList.addAll(data);
                }
                if (mCallList.isEmpty()) {
                    handleResult(key);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoryDetailResponse> call, @NonNull Throwable t) {
                mCallList.remove(call);
                if (mCallList.isEmpty()) {
                    handleResult(key);
                }
            }
        });
    }


    private void handleResult(String key) {
        if (!mDemandList.isEmpty() && isAppSearched && !mAppDetailList.isEmpty()) {
            mDemandAdapter.notifyDataSetChanged();
            mAppAdapter.notifyDataSetChanged();
            mIndicatorView.setVisibility(View.GONE);
            mSearchResultParent.setVisibility(View.VISIBLE);
            searchDemandLabel.setVisibility(View.VISIBLE);
            searchAppLabel.setVisibility(View.VISIBLE);
            mDemandRecyclerView.setVisibility(View.VISIBLE);
            mAppRecyclerView.setVisibility(View.VISIBLE);
            String label = getResources().getString(R.string.search_title_demand) + " \"" + key + "\"";
            searchDemandLabel.setText(label);
            String appLabel = getResources().getString(R.string.search_title_apps) + " \"" + key + "\"";
            searchAppLabel.setText(appLabel);
        } else if (!mDemandList.isEmpty()) {
            mDemandAdapter.notifyDataSetChanged();
            mIndicatorView.setVisibility(View.GONE);
            mSearchResultParent.setVisibility(View.VISIBLE);
            searchDemandLabel.setVisibility(View.VISIBLE);
            searchAppLabel.setVisibility(View.GONE);
            mDemandRecyclerView.setVisibility(View.VISIBLE);
            mAppRecyclerView.setVisibility(View.GONE);
            String label = getResources().getString(R.string.search_title_demand) + " \"" + key + "\"";
            searchDemandLabel.setText(label);

        } else if (!mAppDetailList.isEmpty()) {
            mAppAdapter.notifyDataSetChanged();
            mIndicatorView.setVisibility(View.GONE);
            searchDemandLabel.setVisibility(View.GONE);
            mSearchResultParent.setVisibility(View.VISIBLE);
            searchAppLabel.setVisibility(View.VISIBLE);
            mDemandRecyclerView.setVisibility(View.GONE);
            mAppRecyclerView.setVisibility(View.VISIBLE);
            String appLabel = getResources().getString(R.string.search_title_apps) + " \"" + key + "\"";
            searchAppLabel.setText(appLabel);

        } else {
            mIndicatorView.setVisibility(View.GONE);
            mSearchResultParent.setVisibility(View.GONE);
            mPreSearchTipsText.setVisibility(View.VISIBLE);
            String result = getResources().getString(R.string.tips_no_result) + " \"" + key + "\"";
            mPreSearchTipsText.setText(result);
            mPreSearchTipsText.setTextColor(Color.WHITE);
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
        Animator transZAnim = AnimatorUtils.createElevationAnim(view.findViewById(R.id.category_cover), 1.1f);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(view.findViewById(R.id.card), 1.1f);
        Animator titleAlphaAnim = AnimatorUtils.createAlphaAnim(view.findViewById(R.id.category_title), 1.0f);
        set.playTogether(transZAnim, scaleAnim, titleAlphaAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    /**
     * 当app的item焦点变化时执行动画
     *
     * @param view        item对应的视图
     * @param hasSelected 是否被选中（得到焦点）
     */
    private void executeAppAnimator(View view, boolean hasSelected) {
        AnimatorSet set = new AnimatorSet();
        if (hasSelected) {
            Animator scaleAnim = AnimatorUtils.createCommonInScaleAnim(view.findViewById(R.id.card), 1.1f, 350);
            Animator tileAlphaAnim = AnimatorUtils.createAlphaAnim(view.findViewById(R.id.category_title), 1.0f);
            set.playTogether(scaleAnim, tileAlphaAnim);
        } else {
            Animator scaleAnim = AnimatorUtils.createCommonOutScaleAnim(view.findViewById(R.id.card), 1.0f, 300);
            Animator tileAlphaAnim = AnimatorUtils.createAlphaAnim(view.findViewById(R.id.category_title), 0.0f);
            set.playTogether(scaleAnim, tileAlphaAnim);
        }
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
        CategoryOtherHolder holder = (CategoryOtherHolder) mDemandRecyclerView.getChildViewHolder(view);
        Animator otherAnim = AnimatorUtils.createUnfocusOtherAnim(holder, 1.0f);
        Animator otherAlphaAnim = AnimatorUtils.createAlphaAnim(view.findViewById(R.id.other_title), 0.4f);
        mUnfocusDemandAnimSet.playTogether(otherAnim, otherAlphaAnim);
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
        CategoryOtherHolder holder = (CategoryOtherHolder) mDemandRecyclerView.getChildViewHolder(view);
        Animator otherAnim = AnimatorUtils.createFocusOnOtherAnim(holder, 1.15f);
        Animator otherAlphaAnim = AnimatorUtils.createAlphaAnim(view.findViewById(R.id.other_title), 1.0f);
        mFocusOnDemandAnimSet.playTogether(otherAnim, otherAlphaAnim);
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
        CategoryOtherHolder holder = (CategoryOtherHolder) mDemandRecyclerView.getChildViewHolder(view);
        TextView title = holder.getTitle();
        int heightDp = hasSelected ? 107 : 50;
        ViewGroup.LayoutParams params = title.getLayoutParams();
        params.height = ImageUtils.dp2Px(getApplicationContext(), heightDp);
        title.setMaxLines(hasSelected ? 5 : 2);
    }
}
