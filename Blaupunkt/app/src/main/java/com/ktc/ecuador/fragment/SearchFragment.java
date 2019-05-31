package com.ktc.ecuador.fragment;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ktc.ecuador.R;
import com.ktc.ecuador.adapter.SearchResultAdapter;
import com.ktc.ecuador.data.protocal.CategoryDetail;
import com.ktc.ecuador.data.protocal.ItemBean;
import com.ktc.ecuador.data.protocal.SearchResponseBean;
import com.ktc.ecuador.utils.CommonUtils;
import com.ktc.ecuador.utils.FontUtils;
import com.ktc.ecuador.utils.ImageUtils;
import com.ktc.ecuador.view.KItemDecoration;
import com.ktc.ecuador.view.SearchRecyclerView;
import com.ktc.ecuador.view.SearchView;
import com.ktc.ecuador.view.indicators.AVLoadingIndicatorView;
import com.ktc.ecuador.view.layoutManagers.FocusLinerLayoutManager;
import com.ktc.ecuador.viewmodel.HomeViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {
    private static final int RC_SEARCH = 0x01;
    private View baseView;
    private AVLoadingIndicatorView mSearchIndicator;
    private SearchView mSearchView;
    private TextView mSearchTips;
    private TextView mSearchRightFirst;
    private TextView mSearchRightFirstName;
    private TextView mSearchRightFirsDescription;
    private TextView mSearchRightVideoDescription;
    private View mSearchLine;
    private EditText mSearchEditText;
    private RelativeLayout mSearchcontent;
    private RelativeLayout mSearchRightContent;
    private SearchRecyclerView mSearchrecycler;
    private ImageView mSearchRightImage;
    private HomeViewModel mHomeViewModel;
    private List<ItemBean> searchResult;
    private SearchResultAdapter mAdapter;
    private boolean isHidden= false;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RC_SEARCH) {
                String searchKey = (String) msg.obj;
                mSearchIndicator.setVisibility(View.VISIBLE);
                mSearchTips.setVisibility(View.GONE);
                mHomeViewModel.getSearchResult(searchKey);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseView = inflater.inflate(R.layout.fragment_search, null);
        initView();
        initData();
        initRecyclerView();
        initAdapter();
        initEdit();
        isHidden = true;
        return baseView;
    }


    private  void clearData(){
        searchResult.clear();
      mSearchcontent.setVisibility(View.GONE);
      mSearchTips.setText(getContext().getResources().getString(R.string.search_tips));
      mSearchEditText.setText("");
      mSearchTips.setVisibility(View.VISIBLE);
  }

    @Override
    public void onResume() {
        super.onResume();
        if (isHidden) {
            clearData();
            isHidden=false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mHandler.hasMessages(RC_SEARCH)) {
            mHandler.removeMessages(RC_SEARCH);
        }
    }

    private void initView() {
        mSearchIndicator = baseView.findViewById(R.id.search_indicator);
        mSearchView = baseView.findViewById(R.id.search_view);
        mSearchTips = baseView.findViewById(R.id.search_tv_tips);
        mSearchRightFirst = baseView.findViewById(R.id.searchView_right_first);
        mSearchRightFirstName = baseView.findViewById(R.id.searchView_right_name);
        mSearchRightFirsDescription = baseView.findViewById(R.id.searchView_right_description);
        mSearchRightVideoDescription = baseView.findViewById(R.id.searchView_right_video_description);
        mSearchRightContent = baseView.findViewById(R.id.searchView_right_content);
        mSearchrecycler = baseView.findViewById(R.id.searchView_recycler);
        mSearchRightImage = baseView.findViewById(R.id.searchView_right_image);
        mSearchEditText = baseView.findViewById(R.id.searchView_et);
        mSearchLine = baseView.findViewById(R.id.searchView_right_line);
        mSearchcontent = baseView.findViewById(R.id.searchView_content);
        FontUtils fontUtils = FontUtils.getInstance(getContext());
        fontUtils.setRegularFont(mSearchTips);
        fontUtils.setRegularFont(mSearchRightFirst);
        fontUtils.setRegularFont(mSearchRightFirstName);
        fontUtils.setRegularFont(mSearchRightFirsDescription);
        fontUtils.setRegularFont(mSearchRightVideoDescription);


        mSearchView.addListener(new SearchView.SearchViewListener() {
            @Override
            public void backPress() {

            }

            @Override
            public void upPress() {
                EventBus.getDefault().post("up");
            }
        });
    }

    private void initData() {
        searchResult = new ArrayList<>();
        Observer<List<SearchResponseBean>> searchResultObserver = new Observer<List<SearchResponseBean>>() {
            @Override
            public void onChanged(@Nullable List<SearchResponseBean> searchResponseBean) {
                if (searchResponseBean == null) {
                    return;
                }
                for (SearchResponseBean responseBean : searchResponseBean) {
                    if (responseBean.isSuccess()) {
                        mSearchIndicator.setVisibility(View.GONE);
                        mSearchTips.setVisibility(View.GONE);
                        mSearchcontent.setVisibility(View.VISIBLE);
                        if (responseBean.getAppResult() != null) {
                            searchResult.addAll(responseBean.getAppResult());
                        }
                        if (responseBean.getCategoryResult() != null) {
                            searchResult.addAll(responseBean.getCategoryResult());
                        }
                    }
                }
                if (searchResult.isEmpty()) {
                    mSearchIndicator.setVisibility(View.GONE);
                    mSearchTips.setVisibility(View.VISIBLE);
                    mSearchTips.setText(getResources().getString(R.string.tips_no_result));
                }

                mAdapter.notifyDataSetChanged();

            }
        };
        mHomeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        mHomeViewModel.getSearchResultLiveData().observe(this, searchResultObserver);

    }

    private void initRecyclerView() {
        mSearchrecycler.setLayoutManager(new FocusLinerLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mSearchrecycler.addItemDecoration(new KItemDecoration(10, 0, 10, 0));
        mSearchrecycler.setHasFixedSize(true);
    }

    private void initAdapter() {
        mAdapter = new SearchResultAdapter(getContext(), searchResult);
        mSearchrecycler.setAdapter(mAdapter);
        mAdapter.setOnItemSelectListener(new SearchResultAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelect(int position, int type) {
                handleData(true, type, position);

            }

            @Override
            public void omItemUnSelect(int position) {
                handleData(false, 0, position);

            }
        });
        mAdapter.setOnItemClickListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ItemBean result = searchResult.get(position);
                CommonUtils.startMP3Browser(getContext(), result.getUrl(), result.getBack_keycode());
            }
        });

    }

    private void initEdit() {
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mSearchEditText.setSelection(charSequence.length());
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                if (mHandler.hasMessages(RC_SEARCH)) {
                    mHandler.removeMessages(RC_SEARCH);
                }
                if (editable.length() >= 3) {
                    searchResult.clear();
                    Message msg = Message.obtain();
                    msg.what = RC_SEARCH;
                    msg.obj = editable.toString();
                    mHandler.sendMessageDelayed(msg, 1500);
                }

            }
        });
    }

    private void handleData(boolean selected, int type, int position) {
        if (selected) {
            mSearchRightContent.setVisibility(View.VISIBLE);
            mSearchLine.setVisibility(View.VISIBLE);
            RequestOptions requestOptions = new RequestOptions();
            int imageVisible = View.VISIBLE;
            int rightFirstVisible = View.GONE;
            int rightNameVisible = View.GONE;
            int rightDescriptionVisible = View.GONE;
            int rightVideoDescriptionVisible = View.GONE;
            switch (type) {
                case SearchResultAdapter.TYPE_APPS: {
                    requestOptions = requestOptions.centerCrop()
                            .override(ImageUtils.dp2Px(getContext(), 384), ImageUtils.dp2Px(getContext(), 216))
                            .placeholder(R.drawable.placeholder_app_item);
                    break;
                }
                case SearchResultAdapter.TYPE_MOVIE: {
                    CategoryDetail.CategoryItem categoryItem = (CategoryDetail.CategoryItem) searchResult.get(position);
                    rightFirstVisible = View.VISIBLE;
                    rightNameVisible = View.VISIBLE;
                    rightDescriptionVisible = View.VISIBLE;
                    mSearchRightFirst.setText(categoryItem.getTitle());
                    mSearchRightFirsDescription.setText(categoryItem.getDescription());
                    requestOptions = requestOptions.centerCrop()
                            .override(ImageUtils.dp2Px(getContext(), 180), ImageUtils.dp2Px(getContext(), 270))
                            .placeholder(R.drawable.placeholder_movie_item);
                    break;
                }
                case SearchResultAdapter.TYPE_VIDEO: {
                    requestOptions = requestOptions.centerCrop()
                            .override(ImageUtils.dp2Px(getContext(), 375), ImageUtils.dp2Px(getContext(), 210))
                            .placeholder(R.drawable.placeholder_video_item);
                    CategoryDetail.CategoryItem categoryItem = (CategoryDetail.CategoryItem) searchResult.get(position);
                    mSearchRightVideoDescription.setText(categoryItem.getDescription());
                    rightVideoDescriptionVisible = View.VISIBLE;

                    break;
                }
                default:
            }
            mSearchRightFirst.setVisibility(rightFirstVisible);
            mSearchRightFirstName.setVisibility(rightNameVisible);
            mSearchRightFirsDescription.setVisibility(rightDescriptionVisible);
            mSearchRightVideoDescription.setVisibility(rightVideoDescriptionVisible);
            mSearchRightImage.setVisibility(imageVisible);
            Glide.with(getContext()).load(searchResult.get(position).getImage()).apply(requestOptions).into(mSearchRightImage);

        } else {
            mSearchRightContent.setVisibility(View.GONE);
            mSearchLine.setVisibility(View.GONE);
        }


    }
}