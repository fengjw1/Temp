package com.ktc.ecuador.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ktc.ecuador.R;
import com.ktc.ecuador.adapter.MyAppsAdapter;
import com.ktc.ecuador.adapter.StoreAppsAdapter;
import com.ktc.ecuador.data.Constants;
import com.ktc.ecuador.data.protocal.MyAppBean;
import com.ktc.ecuador.data.protocal.Poster;
import com.ktc.ecuador.data.protocal.ResponseBean;
import com.ktc.ecuador.data.protocal.StoreApp;
import com.ktc.ecuador.utils.CommonUtils;
import com.ktc.ecuador.utils.FontUtils;
import com.ktc.ecuador.utils.NetWorkUtils;
import com.ktc.ecuador.view.AppDeleteDialog;
import com.ktc.ecuador.view.AppPredictRecyclerView;
import com.ktc.ecuador.view.KItemDecoration;
import com.ktc.ecuador.viewmodel.HomeViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppsFragment extends Fragment {
    public boolean isFromHome = false;
    private View baseView;
    private TextView apps_tv_store_appName;
    private TextView apps_tv_store_title;
    private TextView apps_tv_my_app_title;
    private TextView apps_tv_my_app_move_tip;
    private ArrayList<MyAppBean> myAppList = new ArrayList<>();
    private ArrayList<MyAppBean> oldData = new ArrayList<>();
    private MyAppsAdapter myAppsAdapter;
    private AppPredictRecyclerView apps_rv_local_apps;
    private RecyclerView apps_rv_store_apps;
    private ArrayList<StoreApp.ListBean> storeApps = new ArrayList<>();
    private StoreAppsAdapter storeAppsAdapter;
    private HomeViewModel homeViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseView = inflater.inflate(R.layout.fragment_apps, null);
        initView();
        initData();
        return baseView;
    }


    private void initView() {
        apps_tv_store_appName = baseView.findViewById(R.id.apps_tv_store_appName);
        apps_tv_store_title = baseView.findViewById(R.id.apps_tv_store_title);
        apps_tv_my_app_title = baseView.findViewById(R.id.apps_tv_my_app_title);
        apps_tv_my_app_move_tip = baseView.findViewById(R.id.apps_tv_my_app_move_tip);

        apps_rv_local_apps = baseView.findViewById(R.id.apps_rv_local_apps);
        myAppsAdapter = new MyAppsAdapter(getContext(), myAppList, false);
        apps_rv_local_apps.setAdapter(myAppsAdapter);
        apps_rv_local_apps.setLayoutManager(new GridLayoutManager(getContext(), 4, GridLayoutManager.VERTICAL, false));
        apps_rv_local_apps.setItemAnimator(new DefaultItemAnimator());
        apps_rv_local_apps.addItemDecoration(new KItemDecoration(25, 0, 25, 60));
        apps_rv_local_apps.setNestedScrollingEnabled(false);
        apps_rv_local_apps.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (isFromHome) {
                    view.requestFocus();
                    isFromHome = false;
                }
            }
        });
        myAppsAdapter.setOnItemSelectListener(new MyAppsAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelect(int position, View view) {

            }

            @Override
            public void onItemUnSelect(int position, View view) {

            }

            @Override
            public void onItemEnterMoveState() {
                apps_tv_my_app_move_tip.setText(R.string.tips_move);
            }

            @Override
            public void onItemExitMoveState() {
                apps_tv_my_app_move_tip.setText(R.string.app_my_apps_move_tip);
            }
        });
        myAppsAdapter.setFocusChangeListener(new MyAppsAdapter.OnFocusChangeListener() {
            @Override
            public void onFocusChange(boolean hasFocus) {
                if (hasFocus) {
                    apps_tv_my_app_title.setAlpha(1.0f);
                    apps_tv_my_app_move_tip.setVisibility(View.VISIBLE);
                } else {
                    apps_tv_my_app_title.setAlpha(0.6f);
                    apps_tv_my_app_move_tip.setVisibility(View.INVISIBLE);
                }
            }
        });
        myAppsAdapter.setOnItemClickListener(new MyAppsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (CommonUtils.isLocalApp(getContext(), myAppList.get(position).getIntentUrl()))
                    CommonUtils.startAppForPkg(getContext(), myAppList.get(position).getAppPackageName());
                else
                    NetWorkUtils.gotoHtml5Page(getActivity(),
                            myAppList.get(position).getIntentUrl()
                            , myAppList.get(position).getBackCode());
            }
        });
        myAppsAdapter.setOnKeyDownListener(new MyAppsAdapter.OnKeyDownListener() {
            @Override
            public boolean onKey(int position, int keyCode) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (CommonUtils.isLocalApp(getContext(), myAppList.get(position).getIntentUrl()))
                        CommonUtils.startAppForPkg(getContext(), myAppList.get(position).getAppPackageName());
                    else
                        NetWorkUtils.gotoHtml5Page(getActivity(),
                                myAppList.get(position).getIntentUrl()
                                , myAppList.get(position).getBackCode());
                    return true;
                }
                return false;
            }

            @Override
            public void deleteEvent(final int position) {
                AppDeleteDialog deleteDialog = new AppDeleteDialog(getActivity(),
                        myAppList.get(position));
                deleteDialog.setOnDeleteListener(new AppDeleteDialog.OnDeleteListener() {
                    @Override
                    public void onDelete() {
                        /**
                         * 删除position数据
                         * 删除adapter
                         * 如果不是最后一个，更新position位置到最后一个位置的数据
                         */
                        myAppList.remove(position);
                        myAppsAdapter.notifyItemRemoved(position);
//                        if (position > myAppsAdapter.getItemCount() - 1)
//                            myAppsAdapter.notifyItemRangeChanged(position, myAppsAdapter.getItemCount() - position);
                        myAppsAdapter.exitMenuMode();
                    }
                });
                deleteDialog.show();
            }

            @Override
            public boolean onMoveEvent(int keyCode, int position) {
                /**
                 * 在move状态下，item的事件全部由此部分处理。特别注意position必须是holder.getAdapterPosition
                 * 而不是bind时的position
                 */
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (canSwap(myAppsAdapter, position, keyCode)) {
                        Collections.swap(myAppList, position, position - 1);
                        myAppsAdapter.notifyItemMoved(position, position - 1);
                    }
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (canSwap(myAppsAdapter, position, keyCode)) {
                        Collections.swap(myAppList, position, position + 1);
                        myAppsAdapter.notifyItemMoved(position, position + 1);
                    }
                }
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    int originPosition = myAppsAdapter.originPosition;
                    if (originPosition != position && originPosition != -1) {
                        MyAppBean myAppBean = myAppList.get(position);
                        myAppList.remove(position);
                        myAppList.add(originPosition, myAppBean);
                        myAppsAdapter.notifyItemMoved(position, originPosition);
                    }
                    myAppsAdapter.exitMovingMode();
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    myAppsAdapter.exitMovingMode();
                    //store op
                    int originPosition = myAppsAdapter.originPosition;
                    if (originPosition != position) {
                        List<String> sortDataList = new ArrayList<>(myAppList.size());
                        MyAppBean myAppBean = null;
                        for (int i = 0; i < myAppList.size(); i++) {
                            myAppBean = myAppList.get(i);
                            if (CommonUtils.isLocalApp(getContext(), myAppBean.getIntentUrl())) {
                                sortDataList.add(myAppBean.getAppPackageName());
                            } else {
                                sortDataList.add(String.valueOf(myAppBean.getId()));
                            }
                        }
                        homeViewModel.storeAppsSortData(sortDataList);
                        MyAppBean.sortList = sortDataList;
                    }
                }
                return true;
            }
        });
        StoreApp.ListBean storeApp = new StoreApp.ListBean();
        storeApp.setTitle(getString(R.string.apps_open_store));
        if (storeApps.isEmpty()) {
            storeApps.add(storeApp);
        }
        apps_rv_store_apps = baseView.findViewById(R.id.apps_rv_store_apps);
        storeAppsAdapter = new StoreAppsAdapter(getContext(), storeApps);
        apps_rv_store_apps.setAdapter(storeAppsAdapter);
        apps_rv_store_apps.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        apps_rv_store_apps.addItemDecoration(new KItemDecoration(25, 0, 25, 50));
        apps_rv_store_apps.setItemAnimator(new DefaultItemAnimator());
        apps_rv_store_apps.setNestedScrollingEnabled(false);
        storeAppsAdapter.setOnItemSelectListener(new StoreAppsAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelect(int position) {
                apps_tv_store_appName.setText(storeApps.get(position).getTitle());
            }
        });
        storeAppsAdapter.setFocusChangeListener(new StoreAppsAdapter.OnFocusChangeListener() {
            @Override
            public void onFocusChange(boolean hasFocus) {
                if (hasFocus) {
                    apps_tv_store_title.setAlpha(1.0f);
                    apps_tv_store_appName.setAlpha(1.0f);
                } else {
                    apps_tv_store_title.setAlpha(0.6f);
                    apps_tv_store_appName.setAlpha(0.6f);
                }
            }
        });
        storeAppsAdapter.setOnItemClickListener(new StoreAppsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position == 0) {
                    homeViewModel.onStartApplication(Constants.Store_Package_Name);
                } else if (position > 0) {
                    homeViewModel.onGoToNetPage(storeApps.get(position)
                            .getUrl(), storeApps.get(position).getBack_keycode());
                }
            }
        });
        storeAppsAdapter.setOnKeyDownListener(new StoreAppsAdapter.OnKeyDownListener() {
            @Override
            public boolean onKey(int position, int keyCode) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    EventBus.getDefault().post("up");
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (position == 0) {
                        homeViewModel.onStartApplication(Constants.Store_Package_Name);
                    } else if (position > 0) {
                        homeViewModel.onGoToNetPage(storeApps.get(position)
                                .getUrl(), storeApps.get(position).getBack_keycode());
                    }
                    return true;
                }
                return false;
            }
        });
        initFontStyle();
    }

    private void initData() {
        Observer<ResponseBean<List<MyAppBean>>> myAppsObserver = new Observer<ResponseBean<List<MyAppBean>>>() {
            @Override
            public void onChanged(@Nullable ResponseBean<List<MyAppBean>> responseBean) {
                if (responseBean != null && responseBean.isSuccess()) {
                    myAppList.clear();
                    myAppList.addAll(responseBean.getResult());
                    if (!myAppList.equals(oldData)) {
                        oldData.clear();
                        oldData.addAll(myAppList);
                        myAppsAdapter.notifyDataSetChanged();
                        baseView.requestLayout(); //必须重新layout 不然的话 安装APK后可能滚动计算出问题
                    }
                }
            }
        };
        Observer<ResponseBean<List<StoreApp.ListBean>>> storeAppsObserver = new Observer<ResponseBean<List<StoreApp.ListBean>>>() {
            @Override
            public void onChanged(@Nullable ResponseBean<List<StoreApp.ListBean>> responseBean) {
                if (responseBean != null && responseBean.isSuccess()) {
                    Object result = responseBean.getResult();
                    ArrayList<StoreApp.ListBean> oldStoreData = new ArrayList<>(storeApps);
                    if (result instanceof Poster) {
                        StoreApp.ListBean storeApp = new StoreApp.ListBean();
                        storeApp.setImage(((Poster) result).getImage());
                        storeApp.setTitle(((Poster) result).getTitle());
                        storeApp.setUrl(((Poster) result).getUrl());
                        if (!storeApps.contains(storeApp)) {
                            storeApps.add(1, storeApp);
                        }
                    } else {
                        for (StoreApp.ListBean listBean : responseBean.getResult()) {
                            if (!storeApps.contains(listBean)) {
                                storeApps.add(listBean);
                            }
                        }
                    }
                    if (!oldStoreData.equals(storeApps)) {
                        storeAppsAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        homeViewModel.getMyAppsLiveData().observe(this, myAppsObserver);
        homeViewModel.getStoreAppsLiveData().observe(this, storeAppsObserver);
        homeViewModel.getStoreApps();
    }

    private boolean canSwap(RecyclerView.Adapter adapter, int position, int keyCode) {
        if (adapter == null || adapter.getItemCount() <= 0 || position < 0)
            return false;
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            //与左边元素交换 只需要判断position是否为0
            return 0 != position;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            return position != adapter.getItemCount() - 1;
        }
        return false;
    }

    private void initFontStyle() {
        FontUtils.getInstance(getContext()).setRegularFont(apps_tv_store_appName);
        FontUtils.getInstance(getContext()).setRegularFont(apps_tv_store_title);
        FontUtils.getInstance(getContext()).setRegularFont(apps_tv_my_app_title);
    }

}
