package com.ktc.ecuador.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ktc.ecuador.R;
import com.ktc.ecuador.adapter.CategoryDetailAdapter;
import com.ktc.ecuador.adapter.DemandPageAdapter;
import com.ktc.ecuador.adapter.KindRecommendAdapter;
import com.ktc.ecuador.adapter.MovieRecommendAdapter;
import com.ktc.ecuador.adapter.VideoRecommendAdapter;
import com.ktc.ecuador.data.protocal.CategoryBean;
import com.ktc.ecuador.data.protocal.CategoryDetail;
import com.ktc.ecuador.data.protocal.ResponseBean;
import com.ktc.ecuador.utils.CommonUtils;
import com.ktc.ecuador.utils.FontUtils;
import com.ktc.ecuador.utils.LogcatUtil;
import com.ktc.ecuador.view.KItemDecoration;
import com.ktc.ecuador.view.PredictRecyclerView;
import com.ktc.ecuador.view.indicators.AVLoadingIndicatorView;
import com.ktc.ecuador.viewmodel.HomeViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class DemandFragment extends Fragment implements ViewPager.OnPageChangeListener {
    private static final int ALL_MOVIE_SPAN_COUNT = 6;
    private static final int ALL_VIDEO_SPAN_COUNT = 4;
    private View baseView;
    private List<View> pagerViews = new ArrayList<>();
    private ViewPager demand_vp_root;
    private TextView demand_tv_movie_title;
    private TextView demand_tv_movie_name;
    private TextView demand_tv_movie_desc;
    private MovieRecommendAdapter movieRecommendAdapter;
    private TextView demand_tv_video_title;
    private TextView demand_tv_video_name;
    private TextView demand_tv_video_desc;
    private VideoRecommendAdapter videoRecommendAdapter;
    private TextView demand_tv_kinds_title;
    private KindRecommendAdapter kindRecommendAdapter;
    private HomeViewModel homeViewModel;
    private CategoryBean categoryBean;
    private List<CategoryDetail.CategoryItem> movieCategoryDetailList = new ArrayList<>();
    private List<CategoryDetail.CategoryItem> videoCategoryDetailList = new ArrayList<>();
    private List<CategoryBean.CategoryItem> kindCategoryItemList = new ArrayList<>();
    private List<CategoryDetail.CategoryItem> categoryDetailList = new ArrayList<>();
    private int detailCategoryId = -1;
    private TextView demand_tv_detail_title;
    private TextView demand_tv_detail_name;
    private TextView demand_tv_detail_desc;
    private PredictRecyclerView demand_rv_detail;
    private CategoryDetailAdapter categoryDetailAdapter;
    private GridLayoutManager categoryDetailLayoutManager;
    private AVLoadingIndicatorView demand_load_detail;
    private PredictRecyclerView demand_rv_kinds;
    private PredictRecyclerView demand_rv_video;
    private PredictRecyclerView demand_rv_movie;
    private boolean isTopColumn = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseView = inflater.inflate(R.layout.fragment_demand, container, false);
        initView();
        loadData();
        initViewPager();
        initMovieRow();
        initVideoRow();
        initKindRow();
        initDetailPage();
        initFontStyle();
        return baseView;
    }


    private void initView() {
        demand_vp_root = baseView.findViewById(R.id.demand_vp_root);
        demand_vp_root.addOnPageChangeListener(this);

        pagerViews.add(View.inflate(getContext(), R.layout.layout_demand, null));
        pagerViews.add(View.inflate(getContext(), R.layout.layout_demand_detail, null));


        View categoryPageView = pagerViews.get(0);

        demand_tv_movie_title = categoryPageView.findViewById(R.id.demand_tv_movie_title);
        demand_tv_movie_name = categoryPageView.findViewById(R.id.demand_tv_movie_name);
        demand_tv_movie_desc = categoryPageView.findViewById(R.id.demand_tv_movie_desc);
        demand_rv_movie = categoryPageView.findViewById(R.id.demand_rv_movie);
        demand_tv_video_title = categoryPageView.findViewById(R.id.demand_tv_video_title);
        demand_tv_video_name = categoryPageView.findViewById(R.id.demand_tv_video_name);
        demand_tv_video_desc = categoryPageView.findViewById(R.id.demand_tv_video_desc);
        demand_rv_video = categoryPageView.findViewById(R.id.demand_rv_video);
        demand_tv_kinds_title = categoryPageView.findViewById(R.id.demand_tv_kinds_title);
        demand_rv_kinds = categoryPageView.findViewById(R.id.demand_rv_kinds);

        View categoryDetailPageView = pagerViews.get(1);
        demand_tv_detail_title = categoryDetailPageView.findViewById(R.id.demand_tv_detail_title);
        demand_tv_detail_name = categoryDetailPageView.findViewById(R.id.demand_tv_detail_name);
        demand_tv_detail_desc = categoryDetailPageView.findViewById(R.id.demand_tv_detail_desc);
        demand_rv_detail = categoryDetailPageView.findViewById(R.id.demand_rv_detail);
        demand_load_detail = categoryDetailPageView.findViewById(R.id.demand_load_detail);

    }

    private void loadData() {
        Observer<ResponseBean<CategoryBean>> categoryListObserver = new Observer<ResponseBean<CategoryBean>>() {
            @Override
            public void onChanged(@Nullable ResponseBean<CategoryBean> responseBean) {
                if (responseBean != null && responseBean.isSuccess()) {
                    ArrayList<CategoryBean.CategoryItem> newData = new ArrayList<>();
                    categoryBean = responseBean.getResult();
                    newData.addAll(categoryBean.getCategories());
                    newData.remove(0);
                    newData.remove(newData.size() - 1);
                    if (!kindCategoryItemList.equals(newData)) {
                        kindCategoryItemList.clear();
                        kindCategoryItemList.addAll(newData);
                        kindRecommendAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
        Observer<ResponseBean<CategoryDetail>> categoryDetailObserver = new Observer<ResponseBean<CategoryDetail>>() {
            @Override
            public void onChanged(@Nullable ResponseBean<CategoryDetail> responseBean) {
                if (responseBean != null && responseBean.isSuccess()) {
                    ArrayList<CategoryDetail.CategoryItem> newMovieData = new ArrayList<>();
                    ArrayList<CategoryDetail.CategoryItem> newVideoData = new ArrayList<>();
                    ArrayList<CategoryDetail.CategoryItem> newCategoryData = new ArrayList<>();
                    if (responseBean.getResult().getCategoryId() == 1) {
                        for (CategoryDetail.CategoryItem item : responseBean.getResult().getList()) {
                            if (!"0".equals(item.getType_id())) {
                                newMovieData.add(item);
                            }
                        }
                        if (!movieCategoryDetailList.equals(newMovieData)) {
                            movieCategoryDetailList.clear();
                            movieCategoryDetailList.addAll(newMovieData);
                            movieRecommendAdapter.notifyDataSetChanged();
                        }
                    } else if (responseBean.getResult().getCategoryId() == 99) {
                        String isMovie = "1";
                        for (CategoryDetail.CategoryItem item : responseBean.getResult().getList()) {
                            if (!isMovie.equals(item.getType_id()) && !"0".equals(item.getType_id())) {
                                newVideoData.add(item);
                            }
                        }
                        if (!videoCategoryDetailList.equals(newVideoData)) {
                            videoCategoryDetailList.clear();
                            videoCategoryDetailList.addAll(newVideoData);
                            videoRecommendAdapter.notifyDataSetChanged();
                        }
                    } else {
                        for (CategoryDetail.CategoryItem item : responseBean.getResult().getList()) {
                            if (!"0".equals(item.getType_id())) {
                                newCategoryData.add(item);
                            }
                        }
                        if (!categoryDetailList.equals(newCategoryData)) {
                            categoryDetailList.clear();
                            categoryDetailList.addAll(newCategoryData);
                            categoryDetailAdapter.notifyDataSetChanged();
                            LogcatUtil.e("categoryIsNotEquals");
                        }
                    }
                }
            }
        };
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        homeViewModel.getCategoryListLiveData().observe(this, categoryListObserver);
        homeViewModel.getCategoryDetailLiveData().observe(this, categoryDetailObserver);
        homeViewModel.getCategoryList();
        getCategoryDetailData();
    }

    /**
     * 初始化viewpager
     */
    private void initViewPager() {
        DemandPageAdapter pageAdapter = new DemandPageAdapter(getContext(), pagerViews);
        demand_vp_root.setAdapter(pageAdapter);
    }

    /**
     * movie 推荐行数据处理
     */
    private void initMovieRow() {
        movieRecommendAdapter = new MovieRecommendAdapter(getContext(), movieCategoryDetailList);
        demand_rv_movie.setAdapter(movieRecommendAdapter);
        demand_rv_movie.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        demand_rv_movie.setItemAnimator(null);
        demand_rv_movie.addItemDecoration(new KItemDecoration(25, 0, 25, 20));
        demand_rv_movie.setNestedScrollingEnabled(false);
        movieRecommendAdapter.setOnItemClickListener(new MovieRecommendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (movieCategoryDetailList.isEmpty()){
                    return;
                }
                if (position == movieRecommendAdapter.getItemCount() - 1 && categoryBean != null && movieCategoryDetailList != null) {
                    clearDetailPage();
                    detailCategoryId = categoryBean.getCategories().get(0).getId();
                    demand_vp_root.setCurrentItem(1, true);
                } else {
                    CommonUtils.startMP3Browser(getContext(), movieCategoryDetailList.get(position).getUrl(), movieCategoryDetailList.get(position).getBack_keycode());
                }
            }
        });
        movieRecommendAdapter.setOnKeyDownListener(new MovieRecommendAdapter.OnKeyDownListener() {
            @Override
            public boolean onKey(int position, int keyCode) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    EventBus.getDefault().post("up");
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (movieCategoryDetailList.isEmpty()){
                        return true;
                    }
                    if (position == movieRecommendAdapter.getItemCount() - 1 && categoryBean != null && movieCategoryDetailList != null) {
                        clearDetailPage();
                        detailCategoryId = categoryBean.getCategories().get(0).getId();
                        demand_vp_root.setCurrentItem(1, true);
                    } else {
                        CommonUtils.startMP3Browser(getContext(), movieCategoryDetailList.get(position).getUrl(), movieCategoryDetailList.get(position).getBack_keycode());
                    }
                    return true;
                }
                return false;
            }
        });
        movieRecommendAdapter.setOnItemSelectListener(new MovieRecommendAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelect(int position) {
                if (position != movieRecommendAdapter.getItemCount() - 1) {
                    if (position < movieCategoryDetailList.size() && movieCategoryDetailList.get(position) != null) {
                        demand_tv_movie_name.setText(movieCategoryDetailList.get(position).getTitle());
                        demand_tv_movie_desc.setText(movieCategoryDetailList.get(position).getDescription());
                    } else {
                        demand_tv_movie_name.setText("");
                        demand_tv_movie_desc.setText("");
                    }
                } else if (position == movieRecommendAdapter.getItemCount() - 1) {
                    demand_tv_movie_name.setText(getText(R.string.str_recommend_movie_view_all));
                    demand_tv_movie_desc.setText("");
                }
            }
        });
        movieRecommendAdapter.setFocusChangeListener(new MovieRecommendAdapter.OnFocusChangeListener() {
            @Override
            public void onFocusChange(boolean hasFocus) {
                if (hasFocus) {
                    demand_tv_movie_title.setAlpha(1.0f);
                } else {
                    demand_tv_movie_title.setAlpha(0.6f);
                }
                if (hasFocus && !TextUtils.isEmpty(demand_tv_movie_name.getText().toString())) {
                    demand_tv_movie_name.setVisibility(View.VISIBLE);
                    demand_tv_movie_desc.setVisibility(View.VISIBLE);
                } else {
                    demand_tv_movie_name.setVisibility(View.GONE);
                    demand_tv_movie_desc.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * video 推荐数据处理
     */
    private void initVideoRow() {
        videoRecommendAdapter = new VideoRecommendAdapter(getContext(), videoCategoryDetailList);
        demand_rv_video.setAdapter(videoRecommendAdapter);
        demand_rv_video.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        demand_rv_video.setItemAnimator(null);
        demand_rv_video.addItemDecoration(new KItemDecoration(25, 0, 25, 30));
        demand_rv_video.setNestedScrollingEnabled(false);
        videoRecommendAdapter.setOnItemClickListener(new VideoRecommendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (videoCategoryDetailList.isEmpty()){
                    return;
                }
                CommonUtils.startMP3Browser(getContext(), videoCategoryDetailList.get(position).getUrl(), videoCategoryDetailList.get(position).getBack_keycode());

            }
        });

        videoRecommendAdapter.setOnKeyDownListener(new VideoRecommendAdapter.OnKeyDownListener() {
            @Override
            public boolean onKey(int position, int keyCode) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (videoCategoryDetailList.isEmpty()){
                        return true;
                    }
                    CommonUtils.startMP3Browser(getContext(), videoCategoryDetailList.get(position).getUrl(), videoCategoryDetailList.get(position).getBack_keycode());
                    return true;
                }
                return false;
            }
        });
        videoRecommendAdapter.setOnItemSelectListener(new VideoRecommendAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelect(int position) {
                if (videoCategoryDetailList.size() > 0 && videoCategoryDetailList.get(position) != null) {
                    demand_tv_video_name.setText(videoCategoryDetailList.get(position).getTitle());
                    demand_tv_video_desc.setText(videoCategoryDetailList.get(position).getDescription());
                }
            }
        });
        videoRecommendAdapter.setFocusChangeListener(new VideoRecommendAdapter.OnFocusChangeListener() {
            @Override
            public void onFocusChange(boolean hasFocus) {
                if (hasFocus) {
                    demand_tv_video_title.setAlpha(1.0f);
                } else {
                    demand_tv_video_title.setAlpha(0.6f);
                }
                if (hasFocus && !TextUtils.isEmpty(demand_tv_video_name.getText().toString())) {
                    demand_tv_video_name.setVisibility(View.VISIBLE);
                    demand_tv_video_desc.setVisibility(View.VISIBLE);
                } else {
                    demand_tv_video_name.setVisibility(View.GONE);
                    demand_tv_video_desc.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 分类行UI初始化
     */
    private void initKindRow() {
        kindRecommendAdapter = new KindRecommendAdapter(getContext(), kindCategoryItemList);
        demand_rv_kinds.setAdapter(kindRecommendAdapter);
        demand_rv_kinds.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        demand_rv_kinds.setItemAnimator(null);
        demand_rv_kinds.addItemDecoration(new KItemDecoration(25, 0, 25, 20));
        demand_rv_kinds.setNestedScrollingEnabled(false);
        kindRecommendAdapter.setOnItemClickListener(new KindRecommendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position < kindCategoryItemList.size() && kindCategoryItemList.get(position) != null) {
                    clearDetailPage();
                    detailCategoryId = kindCategoryItemList.get(position).getId();
                    demand_tv_detail_title.setText(kindCategoryItemList.get(position).getDisplay());
                    demand_vp_root.setCurrentItem(1, true);
                }
            }
        });
        kindRecommendAdapter.setFocusChangeListener(new KindRecommendAdapter.OnFocusChangeListener() {
            @Override
            public void onFocusChange(boolean hasFocus) {
                if (hasFocus) {
                    demand_tv_kinds_title.setAlpha(1.0f);
                } else {
                    demand_tv_kinds_title.setAlpha(0.6f);
                }
            }
        });
    }

    /**
     * 详情UI处理
     */
    private void initDetailPage() {
        demand_rv_detail.setEmptyView(demand_load_detail);
        demand_rv_detail.setItemAnimator(null);
        demand_rv_detail.addItemDecoration(new KItemDecoration(25, 20, 25, 0));
        demand_load_detail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    demand_vp_root.setCurrentItem(0, true);
                }
                return true;
            }
        });
    }

    private void initFontStyle() {
        FontUtils.getInstance(getContext()).setRegularFont(demand_tv_movie_title);
        FontUtils.getInstance(getContext()).setMediumFont(demand_tv_movie_name);
        FontUtils.getInstance(getContext()).setRegularFont(demand_tv_movie_desc);
        FontUtils.getInstance(getContext()).setRegularFont(demand_tv_video_title);
        FontUtils.getInstance(getContext()).setMediumFont(demand_tv_video_name);
        FontUtils.getInstance(getContext()).setRegularFont(demand_tv_video_desc);
        FontUtils.getInstance(getContext()).setRegularFont(demand_tv_kinds_title);
        FontUtils.getInstance(getContext()).setRegularFont(demand_tv_detail_title);
        FontUtils.getInstance(getContext()).setMediumFont(demand_tv_detail_name);
        FontUtils.getInstance(getContext()).setRegularFont(demand_tv_detail_desc);
    }

    /**
     * 获取电影和View推荐内容
     */
    private void getCategoryDetailData() {
        homeViewModel.getCategoryDetail(1);
        homeViewModel.getCategoryDetail(99);
    }

    /**
     * 切换到第二页 需要reset
     */
    private void clearDetailPage() {
        categoryDetailList.clear();
        demand_tv_detail_name.setText("");
        demand_tv_detail_desc.setText("");
        demand_rv_detail.removeAllViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pagerViews.clear();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 1) {
            //切换到第二页
            initCategoryDetailPage();
        } else {
            if (detailCategoryId == categoryBean.getCategories().get(0).getId()) {
                demand_rv_movie.getChildAt(0).requestFocus();
            } else {
                for (int i = 0; i < kindCategoryItemList.size(); i++) {
                    if (kindCategoryItemList.get(i).getId() == detailCategoryId) {
                        demand_rv_kinds.getChildAt(i).requestFocus();
                    }
                }
            }
        }
    }

    /**
     * 初始化第二页
     */
    private void initCategoryDetailPage() {
        if (detailCategoryId == categoryBean.getCategories().get(0).getId()) {
            demand_tv_detail_title.setText(R.string.demand_detail_title_movie);
            categoryDetailList.addAll(movieCategoryDetailList);
            categoryDetailAdapter = new CategoryDetailAdapter(getContext(), categoryDetailList, true);
            categoryDetailLayoutManager = new GridLayoutManager(getContext(), ALL_MOVIE_SPAN_COUNT, GridLayoutManager.VERTICAL, false);
            demand_rv_detail.setLayoutManager(categoryDetailLayoutManager);
        } else {
            categoryDetailAdapter = new CategoryDetailAdapter(getContext(), categoryDetailList, false);
            categoryDetailLayoutManager = new GridLayoutManager(getContext(), ALL_VIDEO_SPAN_COUNT, GridLayoutManager.VERTICAL, false);
            demand_rv_detail.setLayoutManager(categoryDetailLayoutManager);
            homeViewModel.getCategoryDetail(detailCategoryId);
        }
        demand_rv_detail.setAdapter(categoryDetailAdapter);
        categoryDetailAdapter.setOnItemSelectListener(new CategoryDetailAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelect(int position) {
                isTopColumn = position < categoryDetailLayoutManager.getSpanCount();
                demand_tv_detail_name.setText(categoryDetailList.get(position).getTitle());
                demand_tv_detail_desc.setText(categoryDetailList.get(position).getDescription());
            }
        });
        categoryDetailAdapter.setOnItemClickListener(new CategoryDetailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (categoryDetailList.isEmpty()){
                    return;
                }
                CommonUtils.startMP3Browser(getContext(), categoryDetailList.get(position).getUrl(), categoryDetailList.get(position).getBack_keycode());
            }
        });
        categoryDetailAdapter.setOnKeyDownListener(new CategoryDetailAdapter.OnKeyDownListener() {
            @Override
            public boolean onKey(int position, int keyCode) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    demand_vp_root.setCurrentItem(0, true);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (categoryDetailList.isEmpty()){
                        return true;
                    }
                    CommonUtils.startMP3Browser(getContext(), categoryDetailList.get(position).getUrl(), categoryDetailList.get(position).getBack_keycode());
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && isTopColumn) {
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}