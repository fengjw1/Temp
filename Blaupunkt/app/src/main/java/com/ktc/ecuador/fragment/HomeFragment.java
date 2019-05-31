package com.ktc.ecuador.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ktc.ecuador.LauncherApplication;
import com.ktc.ecuador.R;
import com.ktc.ecuador.activity.HomeActivity;
import com.ktc.ecuador.adapter.HomeSourceAdapter;
import com.ktc.ecuador.adapter.MyAppsAdapter;
import com.ktc.ecuador.data.protocal.MyAppBean;
import com.ktc.ecuador.data.protocal.ResponseBean;
import com.ktc.ecuador.data.protocal.TvSourceBean;
import com.ktc.ecuador.utils.CommonUtils;
import com.ktc.ecuador.utils.FontUtils;
import com.ktc.ecuador.utils.NetWorkUtils;
import com.ktc.ecuador.utils.TvUtils;
import com.ktc.ecuador.view.AppDeleteDialog;
import com.ktc.ecuador.view.AppPredictRecyclerView;
import com.ktc.ecuador.view.CenterRecyclerView;
import com.ktc.ecuador.view.KItemDecoration;
import com.ktc.ecuador.view.holders.TvHolder;
import com.ktc.ecuador.view.layoutManagers.HorizontalLayoutManager;
import com.ktc.ecuador.viewmodel.HomeViewModel;
import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tv.TvIsdbChannelManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final int MSG_DELAY_SURFACE_VIEW = 1;
    private static final int MSG_DELAY_INIT_SURFACE_VIEW = 2;
    private final static String BOARD_T8C1 = "T8C1";
    private final static String BOARD_T8E = "T8E";
    private final static int HDMI3_POSITION = 4;
    private static int MAX_APPS_SHOW = 9;
    private View baseView;
    private CenterRecyclerView mHomeSourceRecycler;
    private TextView mHomeAppTittle;
    private TextView mHomeAppMoveTips;
    private TextView mHomeAppName;
    private AppPredictRecyclerView mHomeAppRecycler;
    private ArrayList<MyAppBean> myAppList = new ArrayList<>();
    private MyAppsAdapter myAppsAdapter;
    private HomeViewModel homeViewModel;
    private HomeSourceAdapter mHomeSourceAdapter;
    private TvHolder holder;
    private List<TvSourceBean> mTvSourceList;
    private TvUtils mTvUtils;
    private int mCurrTvSource;
    private int mLastFocusedAdapterPosition;
    private int mCurrentTvPosition;
    private Handler mHandler;
    private HorizontalLayoutManager mHorizontalLayoutManager;

    private ArrayList<MyAppBean> oldData = new ArrayList<>();

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            myAppList.clear();
            homeViewModel.myAppsLiveData.onDataChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseView = inflater.inflate(R.layout.fragment_home, null);
        initView();
        initData();
        initRecycler();
        initAdapter();
        initHandler();
        return baseView;
    }

    private void initView() {
        mHomeSourceRecycler = baseView.findViewById(R.id.home_rv_source);
        mHomeAppTittle = baseView.findViewById(R.id.home_tv_my_app_title);
        mHomeAppName = baseView.findViewById(R.id.home_app_name);
        mHomeAppMoveTips = baseView.findViewById(R.id.home_tv_my_app_move_tip);
        mHomeAppRecycler = baseView.findViewById(R.id.home_rv_local_apps);

        FontUtils fontUtils = FontUtils.getInstance(getContext());
        fontUtils.setRegularFont(mHomeAppMoveTips);
        fontUtils.setRegularFont(mHomeAppTittle);
        fontUtils.setRegularFont(mHomeAppName);

    }

    private void initData() {
        initSourceList();
        mTvUtils = TvUtils.getInstance(LauncherApplication.mContext);
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
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        homeViewModel.getMyAppsLiveData().observe(this, myAppsObserver);

    }

    private void initRecycler() {
        mHomeSourceRecycler.addItemDecoration(new KItemDecoration(12, 27, 12, 27));
        mHorizontalLayoutManager = new HorizontalLayoutManager(getContext());
        mHomeAppRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mHomeAppRecycler.addItemDecoration(new KItemDecoration(0, 30, 0, 30));
        mHomeAppRecycler.setItemAnimator(new DefaultItemAnimator());
        mHomeAppRecycler.setNestedScrollingEnabled(false);

    }

    private void initAdapter() {
        mHomeSourceAdapter = new HomeSourceAdapter(getContext(), mTvSourceList);
        mHomeSourceRecycler.setAdapter(mHomeSourceAdapter);

        mHomeSourceAdapter.setOnItemSelectListener(new HomeSourceAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelect(View view, int position) {
                View focusView = mHomeSourceRecycler.getFocusedChild();
                int focusPosition = mHomeSourceRecycler.getChildLayoutPosition(focusView);
                holder = (TvHolder) mHomeSourceRecycler.getChildViewHolder(focusView);
                mCurrentTvPosition = focusPosition;
                if (mTvSourceList.get(focusPosition).getInputsource() != TvSourceBean.SOURCE_USB) {
                    mCurrTvSource = mTvSourceList.get(focusPosition).getInputsource();
                    updateSourceInputType(mTvSourceList.get(mCurrentTvPosition).getInputsource());
                    if (mHandler.hasMessages(MSG_DELAY_SURFACE_VIEW) || mHandler.hasMessages(MSG_DELAY_INIT_SURFACE_VIEW)) {
                        mHandler.removeMessages(MSG_DELAY_SURFACE_VIEW);
                        mHandler.removeMessages(MSG_DELAY_INIT_SURFACE_VIEW);
                    }
                    sendMessageDelay(MSG_DELAY_SURFACE_VIEW, 1500);
                }
            }

            @Override
            public void omItemUnSelect(View view, int position) {
                if (mHandler.hasMessages(MSG_DELAY_SURFACE_VIEW)) {
                    mHandler.removeMessages(MSG_DELAY_SURFACE_VIEW);
                }
                mTvUtils.pauseTv();
            }
        });

        mHomeSourceAdapter.setListener(new HomeSourceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                TvSourceBean bean = mTvSourceList.get(position);
                mCurrentTvPosition = position;
                if (bean.getInputsource() != TvSourceBean.SOURCE_USB) {
                    mTvUtils.changeSource(bean.getInputsource(),0);
                    CommonUtils.startTvApp(getContext(), false, true, bean.getInputsource());
                    if (mHandler.hasMessages(MSG_DELAY_SURFACE_VIEW) || mHandler.hasMessages(MSG_DELAY_INIT_SURFACE_VIEW)) {
                        mHandler.removeMessages(MSG_DELAY_SURFACE_VIEW);
                        mHandler.removeMessages(MSG_DELAY_INIT_SURFACE_VIEW);
                    }
                    mTvUtils.fullScreen(100);
                } else {
                    CommonUtils.startAppForPkg(getContext(), "com.jrm.localmm");
                }
            }
        });

        mHomeSourceAdapter.setOnKeyDownListener(new HomeSourceAdapter.OnKeyDownListener() {
            @Override
            public boolean onKey(int position, int keyCode) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    EventBus.getDefault().post("up");
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                    TvSourceBean bean = mTvSourceList.get(position);
                    mCurrentTvPosition = position;
                    if (bean.getInputsource() != TvSourceBean.SOURCE_USB) {
                        mTvUtils.changeSource(bean.getInputsource(),0);
                        CommonUtils.startTvApp(getContext(), false, true, bean.getInputsource());
                        if (mHandler.hasMessages(MSG_DELAY_SURFACE_VIEW) || mHandler.hasMessages(MSG_DELAY_INIT_SURFACE_VIEW)) {
                            mHandler.removeMessages(MSG_DELAY_SURFACE_VIEW);
                            mHandler.removeMessages(MSG_DELAY_INIT_SURFACE_VIEW);
                        }
                        mTvUtils.fullScreen(100);
                    } else {
                        CommonUtils.startAppForPkg(getContext(), "com.jrm.localmm");
                    }
                    return true;
                }
                return false;
            }
        });

        myAppsAdapter = new MyAppsAdapter(getContext(), myAppList, true);
        mHomeAppRecycler.setAdapter(myAppsAdapter);

        myAppsAdapter.setOnItemSelectListener(new MyAppsAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelect(int position, View view) {
                boolean isLeft = mLastFocusedAdapterPosition >= position;
                if (position == myAppList.size() - 1) {
                    mHomeAppName.setText(R.string.str_home_apps_view_all);
                    mHomeAppMoveTips.setVisibility(View.GONE);
                } else {
                    mHomeAppName.setText(myAppList.get(position).getAppName());
                }
                mHomeAppName.setVisibility(View.VISIBLE);
                mLastFocusedAdapterPosition = position;
                int offset = mHorizontalLayoutManager.calculateHorizontalOffset(isLeft);
                if (offset != 0) {
                    mHomeAppRecycler.smoothScrollBy(offset, 0);
                }

            }

            @Override
            public void onItemUnSelect(int position, View view) {
                if (position == myAppList.size() - 1) {
                    mHomeAppMoveTips.setVisibility(View.VISIBLE);
                }
                mHomeAppName.setVisibility(View.GONE);

            }

            @Override
            public void onItemEnterMoveState() {
                mHomeAppMoveTips.setText(R.string.tips_move);
            }

            @Override
            public void onItemExitMoveState() {
                mHomeAppMoveTips.setText(R.string.app_my_apps_move_tip);
            }
        });
        myAppsAdapter.setFocusChangeListener(new MyAppsAdapter.OnFocusChangeListener() {
            @Override
            public void onFocusChange(boolean hasFocus) {
                if (hasFocus) {
                    mHomeAppTittle.setAlpha(1.0f);
                    mHomeAppMoveTips.setVisibility(View.VISIBLE);
                } else {
                    mHomeAppTittle.setAlpha(0.6f);
                    mHomeAppMoveTips.setVisibility(View.INVISIBLE);
                }
            }
        });
        myAppsAdapter.setOnItemClickListener(new MyAppsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (myAppList.size() < 10){
                    MAX_APPS_SHOW = myAppList.size() - 1;
                }
                if (position == MAX_APPS_SHOW) {
                    HomeActivity activity = (HomeActivity) getActivity();
                    activity.tabHost.onTabChangedNew(activity.appsTagTd);
                    ((AppsFragment) activity.tabHost.mFragment).isFromHome = true;
                } else if (CommonUtils.isLocalApp(getContext(), myAppList.get(position).getIntentUrl())) {
                    homeViewModel.onStartApplication(myAppList.get(position).getAppPackageName());
                } else {
                    NetWorkUtils.gotoHtml5Page(getActivity(),
                            myAppList.get(position).getIntentUrl()
                            , myAppList.get(position).getBackCode());
                }

            }
        });

        myAppsAdapter.setOnKeyDownListener(new MyAppsAdapter.OnKeyDownListener() {
            @Override
            public boolean onKey(int position, int keyCode) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    initSource();
                } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (myAppList.size() < 10){
                        MAX_APPS_SHOW = myAppList.size() - 1;
                    }
                    if (position == MAX_APPS_SHOW) {
                        HomeActivity activity = (HomeActivity) getActivity();
                        activity.tabHost.onTabChangedNew(activity.appsTagTd);
                        ((AppsFragment) activity.tabHost.mFragment).isFromHome = true;
                    } else if (CommonUtils.isLocalApp(getContext(), myAppList.get(position).getIntentUrl())) {
                        homeViewModel.onStartApplication(myAppList.get(position).getAppPackageName());
                    } else {
                        NetWorkUtils.gotoHtml5Page(getActivity(),
                                myAppList.get(position).getIntentUrl()
                                , myAppList.get(position).getBackCode());
                    }
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
                        MyAppBean myAppBean;
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


    }

    private void initHandler() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (holder == null) {
                    return true;
                }
                switch (msg.what) {
                    case MSG_DELAY_INIT_SURFACE_VIEW:
                        mTvUtils.bindSurfaceView(holder.getSurfaceView());
                        mTvUtils.startTv(mTvSourceList.get(mCurrentTvPosition).getInputsource());
                        break;

                    case MSG_DELAY_SURFACE_VIEW:
                        mTvUtils.bindSurfaceView(holder.getSurfaceView());
                        mTvUtils.startTv(mCurrTvSource);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void initSourceList() {
        mTvSourceList = new ArrayList<>(7);
        mTvSourceList.add(new TvSourceBean(R.string.source_air,
                TvCommonManager.INPUT_SOURCE_DTV));
        mTvSourceList.add(new TvSourceBean(R.string.source_cable,
                TvCommonManager.INPUT_SOURCE_ATV));
        mTvSourceList.add(new TvSourceBean(R.string.source_hdmi1,
                TvCommonManager.INPUT_SOURCE_HDMI));
        mTvSourceList.add(new TvSourceBean(R.string.source_hdmi2,
                TvCommonManager.INPUT_SOURCE_HDMI2));
        mTvSourceList.add(new TvSourceBean(R.string.source_usb,
                TvSourceBean.SOURCE_USB));
        mTvSourceList.add(new TvSourceBean(R.string.source_av,
                TvCommonManager.INPUT_SOURCE_CVBS));

        String boardType = SystemProperties.get("ktc.board.type").toUpperCase();
        boolean hasYpbpr = SystemProperties.getBoolean("ktc.YPBPR.type", false);
        if (boardType.equals(BOARD_T8C1)) {
            mTvSourceList.add(HDMI3_POSITION, new TvSourceBean(R.string.source_hdmi3,
                    TvCommonManager.INPUT_SOURCE_HDMI3));
            mTvSourceList.add(new TvSourceBean(R.string.source_vga,
                    TvCommonManager.INPUT_SOURCE_VGA));
            if (hasYpbpr) {
                mTvSourceList.add(new TvSourceBean(R.string.source_Ypbpr,
                        TvCommonManager.INPUT_SOURCE_YPBPR));
            }
        } else if (boardType.equals(BOARD_T8E)) {
            mTvSourceList.add(HDMI3_POSITION, new TvSourceBean(R.string.source_hdmi3,
                    TvCommonManager.INPUT_SOURCE_HDMI3));
            mTvSourceList.add(new TvSourceBean(R.string.source_vga,
                    TvCommonManager.INPUT_SOURCE_VGA));
            mTvSourceList.add(new TvSourceBean(R.string.source_Ypbpr,
                    TvCommonManager.INPUT_SOURCE_YPBPR));
        } else if (hasYpbpr) {
            mTvSourceList.add(new TvSourceBean(R.string.source_Ypbpr,
                    TvCommonManager.INPUT_SOURCE_YPBPR));
        }
    }

    private void updateSourceInputType(int inputSourceTypeIndex) {
        ContentValues vals = new ContentValues();
        vals.put("enInputSourceType", inputSourceTypeIndex);
        try {
            LauncherApplication.mContext.getContentResolver().update(
                    Uri.parse("content://mstar.tv.usersetting/systemsetting"),
                    vals, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void sendMessageDelay(int what, long time) {
        if (mHandler == null) {
            return;
        }
        mHandler.removeMessages(what);
        mHandler.sendEmptyMessageDelayed(what, time);
    }

    private void initSource() {
        setCurrentTvPosition();
        if (mHomeSourceRecycler != null) {
            int firstPos = mHomeSourceRecycler.getChildAdapterPosition(mHomeSourceRecycler.getChildAt(0));
            final int childCount = mHomeSourceRecycler.getChildCount();
            final View lastView = mHomeSourceRecycler.getChildAt(mCurrentTvPosition - firstPos);
            mHomeSourceRecycler.smoothScrollToPosition(mCurrentTvPosition);
            mHomeSourceRecycler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (lastView != null) {
                        lastView.requestFocus();
                    } else {
                        if (childCount > 0) {
                            mHomeSourceRecycler.getChildAt(0).requestFocus();
                        }
                    }
                }
            }, 200);
            if (mHandler.hasMessages(MSG_DELAY_INIT_SURFACE_VIEW)) {
                mHandler.removeMessages(MSG_DELAY_INIT_SURFACE_VIEW);
            }
            sendMessageDelay(MSG_DELAY_INIT_SURFACE_VIEW, 1500);

        }
    }

    private boolean canSwap(RecyclerView.Adapter adapter, int position, int keyCode) {
        if (adapter == null || adapter.getItemCount() <= 0 || position < 0) {
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            //与左边元素交换 只需要判断position是否为0
            return 0 != position;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            return position + 1 != myAppList.size() - 1;
        }
        return false;
    }

    private void setCurrentTvPosition() {
        int currentTvSource = queryInputSource();
        if (currentTvSource == TvCommonManager.INPUT_SOURCE_DTV || currentTvSource == TvCommonManager.INPUT_SOURCE_ATV) {
            if (queryAntennaType() == TvIsdbChannelManager.DTV_ANTENNA_TYPE_AIR) {
                currentTvSource = TvCommonManager.INPUT_SOURCE_DTV;
            } else if (queryAntennaType() == TvIsdbChannelManager.DTV_ANTENNA_TYPE_CABLE) {
                currentTvSource = TvCommonManager.INPUT_SOURCE_ATV;
            }
        }

        if (mTvSourceList.get(mCurrentTvPosition).getInputsource() == TvSourceBean.SOURCE_USB) {
            return;
        }
        for (int position = 0; position < mTvSourceList.size(); position++) {
            if (currentTvSource == mTvSourceList.get(position).getInputsource()) {
                mCurrentTvPosition = position;
                break;
            }
        }
    }

    private int queryInputSource() {
        Cursor cursor = LauncherApplication.mContext.getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/systemsetting"), null, null, null, null);

        int antennaType = 0;
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                antennaType = cursor.getInt(cursor.getColumnIndex("enInputSourceType"));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return antennaType;
    }

    private int queryAntennaType() {
        Cursor cursor = LauncherApplication.mContext.getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/mediumsetting/"), null, null, null, null);
        int antennaType = 0;
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                antennaType = cursor.getInt(cursor.getColumnIndex("AntennaType"));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return antennaType;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHomeSourceRecycler != null && mHomeSourceRecycler.findFocus() != null) {
            initSource();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String command) {
        if ("ktc".equals(command)) {
            initSource();
        }

    }


}