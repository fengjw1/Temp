package com.mp3.launcher4.proxys;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;

import com.mp3.launcher4.LauncherApplication;
import com.mp3.launcher4.R;
import com.mp3.launcher4.adapters.TvEpgAdapter;
import com.mp3.launcher4.adapters.TvSourceAdapter;
import com.mp3.launcher4.beans.TvSourceBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.customs.layoutmanagers.HorizontalLayoutManager;
import com.mp3.launcher4.customs.views.SpecialLimitRecyclerView;
import com.mp3.launcher4.customs.views.complex.BaseComplexProxy;
import com.mp3.launcher4.customs.views.complex.ComplexAdapter;
import com.mp3.launcher4.customs.views.complex.holds.TvHolder;
import com.mp3.launcher4.holders.TvSourceHolder;
import com.mp3.launcher4.utils.CommonUtils;
import com.mp3.launcher4.utils.ImageUtils;
import com.mp3.launcher4.utils.TvUtils;
import com.mstar.android.tv.TvChannelManager;
import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tv.TvEpgManager;
import com.mstar.android.tv.TvIsdbChannelManager;
import com.mstar.android.tvapi.common.vo.ProgramInfo;
import com.mstar.android.tvapi.dtv.vo.EpgEventInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author longzj
 */
public class SourceProxy extends BaseComplexProxy<TvHolder> {

    private final static String BOARD_T8C1 = "T8C1";
    private final static String BOARD_T5C1 = "T5C1";
    private final static String BOARD_T8E = "T8E";
    private final static int HDMI3_POSITION = 4;

    public final static short T_MediumSetting_IDX = 0x0C;

    private static final int MSG_DELAY_SCROLL = 0;
    private static final int MSG_DELAY_START_EPG_TASK = 1;
    private static final int MSG_DELAY_INIT_SURFACE_VIEW = 2;
    private static final int MSG_DELAY_RESET_NAME = 3;
    private static final int MSG_DELAY_RETRY_INIT = 4;
    private Handler mHandler;

    private List<TvSourceBean> mTvSourceList;
    private List<EpgEventInfo> mEpgList;
    private TvUtils mTvUtils;


    private boolean mScrollDelay = false;
    private int mLastFocusedAdapterPosition;
    private int mCurrentTvPosition;
    private boolean isForceRefresh;

    private BroadcastReceiver mTimeReceiver;
    private IntentFilter mIntentFilter;
    private EpgTask mEpgTask;

    public SourceProxy(Context context) {
        super(context, ComplexAdapter.TYPE_TV);
        initProperty(context);
    }

    @Override
    protected void onAttachedToRecycler() {
        super.onAttachedToRecycler();
        try {
            getContext().registerReceiver(mTimeReceiver, mIntentFilter);
        } catch (Exception ignored) {

        }

    }

    @Override
    protected void onDetachToRecycler() {
        super.onDetachToRecycler();
        if (mTimeReceiver != null) {
            getContext().unregisterReceiver(mTimeReceiver);
        }
        mScrollDelay = false;
    }

    @Override
    public boolean onBindViews(TvHolder holder) {
        if (mHandler == null) {
            initHandler();
        }
        TvSourceBean bean = mTvSourceList.get(mCurrentTvPosition);
        if (getHolder() == null) {
            initSurface(holder);
            initEpgRecycler(holder.getEpgRecycler());
            initSourceRecycler(holder.getSourceRecycler());
            int inputSource = bean.getInputsource();
            if (inputSource != TvSourceBean.SOURCE_USB) {
                sendMessageDelay(MSG_DELAY_INIT_SURFACE_VIEW, 200);
            }
            sendMessageDelay(MSG_DELAY_RETRY_INIT, 4000);
            holder.getTvName().setText(bean.getTvNameId());
            holder.getExpanded().setText("");
            return true;
        }

        return false;
    }

    @Override
    public boolean isGroupAbove() {
        return false;
    }

    @Override
    public boolean refocus(boolean isLastChange) {
        final TvHolder holder = getHolder();
        if (holder == null) {
            return true;
        }
        RecyclerView recyclerView = holder.getSourceRecycler();
        if (recyclerView == null) {
            return false;
        }
        if (recyclerView.isFocused()) {
            return true;
        }
        int firstPos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0));
        int childCount = recyclerView.getChildCount();
        View lastFocusedView = recyclerView.getChildAt(mLastFocusedAdapterPosition - firstPos);
        if (lastFocusedView == null) {
            if (isLastChange && childCount > 0) {
                recyclerView.getChildAt(0).requestFocus();
                return true;
            }
            return false;
        }
        return lastFocusedView.requestFocus();
    }

    @Override
    public void requestData() {
        super.requestData();
        sendMessageDelay(MSG_DELAY_RETRY_INIT, 1000);
    }

    @Override
    public void cancelRequest() {
        super.cancelRequest();
        stopEpgTask();
        if (mHandler != null) {
            mHandler.removeMessages(MSG_DELAY_SCROLL);
            mHandler.removeMessages(MSG_DELAY_START_EPG_TASK);
            mHandler.removeMessages(MSG_DELAY_INIT_SURFACE_VIEW);
            mHandler.removeMessages(MSG_DELAY_RESET_NAME);
            mHandler.removeMessages(MSG_DELAY_RETRY_INIT);
        }
        mTvUtils.pauseTv();
    }

    @Override
    public void onActivityResume() {
        sendMessageDelay(MSG_DELAY_RETRY_INIT, 1000);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        mTvUtils.destroyTv();
    }

    @Override
    public long getScrollDelayTime() {
        if (mScrollDelay) {
            return 300;
        } else {
            return 0;
        }
    }

    @Override
    public void beforeGroupScrolling() {
        super.beforeGroupScrolling();
        mTvUtils.pauseTv();
    }

    private void initSurface(TvHolder holder) {
        SurfaceView surfaceView = holder.getSurfaceView();
        surfaceView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                float transZ = hasFocus ? 6.0f : 0.0f;
                v.setTranslationZ(transZ);
            }
        });
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TvSourceBean bean = mTvSourceList.get(mCurrentTvPosition);
                CommonUtils.startTvApp(getContext(), false, true,
                        bean.getInputsource());
            }
        });
        surfaceView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int action = event.getAction();
                if (action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        getComplexRecycler().leaveFromLeft();
                        return true;
                    }
                    return keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_UP;
                }
                return false;
            }
        });
    }

    private void initTimeReceiver() {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        mIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mTimeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sendMessageDelay(MSG_DELAY_START_EPG_TASK, 0);
            }
        };
    }

    private void recordLastFocusedPosition(View view) {
        final TvHolder holder = getHolder();
        if (view == null || holder == null) {
            return;
        }
        int lastFocusedPosition = holder.getSourceRecycler().getChildAdapterPosition(view);
        if (lastFocusedPosition < 0) {
            return;
        }
        mLastFocusedAdapterPosition = lastFocusedPosition;
    }

    private void initProperty(final Context context) {
        initTimeReceiver();
        initSourceList();
        mEpgList = new ArrayList<>();
        mTvUtils = TvUtils.getInstance(context.getApplicationContext());
    }

    private void initHandler() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                TvHolder holder = getHolder();
                if (holder == null) {
                    return true;
                }
                setCurrentTvPosition();
                switch (msg.what) {
                    case MSG_DELAY_SCROLL:
                        getComplexRecycler().scrollDown();
                        break;
                    case MSG_DELAY_START_EPG_TASK:
                        startEpgTask();
                        break;
                    case MSG_DELAY_RESET_NAME:
                        initName(holder, mTvSourceList.get(mCurrentTvPosition));
                        break;
                    case MSG_DELAY_INIT_SURFACE_VIEW:
                        mTvUtils.bindSurfaceView(holder.getSurfaceView());
                        mTvUtils.startTv(mTvSourceList.get(mCurrentTvPosition).getInputsource());
                        mScrollDelay = true;
                        break;
                    case MSG_DELAY_RETRY_INIT:
                        clickAction(holder, mCurrentTvPosition);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void initEpgRecycler(RecyclerView recyclerView) {
        final Context context = getContext();
        recyclerView.setFocusable(false);
        recyclerView.setFocusableInTouchMode(false);
        TvEpgAdapter epgAdapter = new TvEpgAdapter(context, mEpgList);
        RecyclerView.ItemDecoration epgDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int bottom = ImageUtils.dp2Px(context.getApplicationContext(), 7);
                outRect.set(0, 0, 0, bottom);
            }
        };
        LinearLayoutManager manager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.addItemDecoration(epgDecoration);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(epgAdapter);
    }

    private void initSourceList() {
        mTvSourceList = new ArrayList<>(7);
        mTvSourceList.add(new TvSourceBean(R.string.source_air,
                TvCommonManager.INPUT_SOURCE_DTV,
                R.drawable.ic_source_tv));
        mTvSourceList.add(new TvSourceBean(R.string.source_cable,
                TvCommonManager.INPUT_SOURCE_ATV,
                R.drawable.ic_source_tv));
        mTvSourceList.add(new TvSourceBean(R.string.source_hdmi1,
                TvCommonManager.INPUT_SOURCE_HDMI,
                R.drawable.ic_source_hdmi));
        mTvSourceList.add(new TvSourceBean(R.string.source_hdmi2,
                TvCommonManager.INPUT_SOURCE_HDMI2,
                R.drawable.ic_source_hdmi));
        mTvSourceList.add(new TvSourceBean(R.string.source_usb,
                TvSourceBean.SOURCE_USB,
                R.drawable.ic_source_usb));
        mTvSourceList.add(new TvSourceBean(R.string.source_av,
                TvCommonManager.INPUT_SOURCE_CVBS,
                R.drawable.ic_source_av));

        String boardType = SystemProperties.get("ktc.board.type").toUpperCase();
        boolean hasYpbpr = SystemProperties.getBoolean("ktc.YPBPR.type", false);
        if (boardType.equals(BOARD_T8C1)) {
            mTvSourceList.add(HDMI3_POSITION, new TvSourceBean(R.string.source_hdmi3,
                    TvCommonManager.INPUT_SOURCE_HDMI3,
                    R.drawable.ic_source_hdmi));
            mTvSourceList.add(new TvSourceBean(R.string.source_vga,
                    TvCommonManager.INPUT_SOURCE_VGA,
                    R.drawable.ic_source_vga));
            if (hasYpbpr) {
                mTvSourceList.add(new TvSourceBean(R.string.source_Ypbpr,
                        TvCommonManager.INPUT_SOURCE_YPBPR,
                        R.drawable.ic_source_ypbpr));
            }
        } else if (boardType.equals(BOARD_T8E)) {
            mTvSourceList.add(HDMI3_POSITION, new TvSourceBean(R.string.source_hdmi3,
                    TvCommonManager.INPUT_SOURCE_HDMI3,
                    R.drawable.ic_source_hdmi));
            mTvSourceList.add(new TvSourceBean(R.string.source_vga,
                    TvCommonManager.INPUT_SOURCE_VGA,
                    R.drawable.ic_source_vga));
            mTvSourceList.add(new TvSourceBean(R.string.source_Ypbpr,
                    TvCommonManager.INPUT_SOURCE_YPBPR,
                    R.drawable.ic_source_ypbpr));
        } else if (hasYpbpr) {
            mTvSourceList.add(new TvSourceBean(R.string.source_Ypbpr,
                    TvCommonManager.INPUT_SOURCE_YPBPR,
                    R.drawable.ic_source_ypbpr));
        }
    }

    private void sendMessageDelay(int what, long time) {
        if (mHandler == null) {
            return;
        }
        mHandler.removeMessages(what);
        mHandler.sendEmptyMessageDelayed(what, time);
    }

    private void initSourceRecycler(final SpecialLimitRecyclerView recyclerView) {
        final Context context = getContext();
        HorizontalLayoutManager sourceLayoutManager = new HorizontalLayoutManager(getContext());
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                recordLastFocusedPosition(recyclerView.getFocusedChild());
            }
        };
        RecyclerView.ItemDecoration sourceDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int right = ImageUtils.dp2Px(context.getApplicationContext(), 23);
                int top = ImageUtils.dp2Px(context.getApplicationContext(), 68);
                int bottom = ImageUtils.dp2Px(context.getApplicationContext(), 70);
                outRect.set(0, top, right, bottom);
            }
        };
        TvSourceAdapter sourceAdapter = new TvSourceAdapter(context, mTvSourceList);
        sourceAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                final TvHolder holder = getHolder();
                if (holder == null) {
                    return;
                }
                if (mCurrentTvPosition == position) {
                    return;
                }
                clickAction(holder, position);
            }
        });

        sourceAdapter.setOnItemSelectListener(new BaseAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelected(View view, int position) {
                TvHolder holder = getHolder();
                if (holder == null) {
                    return;
                }
                getComplexRecycler().scrollSelf();
                RecyclerView sourceRecycle = holder.getSourceRecycler();
                HorizontalLayoutManager manager = (HorizontalLayoutManager) sourceRecycle.getLayoutManager();
                boolean isLeft = mLastFocusedAdapterPosition >= position;
                mLastFocusedAdapterPosition = position;
                int offset = manager.calculateHorizontalOffset(isLeft);
                if (offset != 0) {
                    sourceRecycle.smoothScrollBy(offset, 0);
                }
                TvSourceHolder tvSourceHolder = (TvSourceHolder) recyclerView.getChildViewHolder(view);
                tvSourceHolder.focused();
            }

            @Override
            public void onItemUnselected(View view, int position) {
                TvHolder holder = getHolder();
                if (holder == null) {
                    return;
                }
                RecyclerView sourceRecycle = holder.getSourceRecycler();
                TvSourceHolder tvSourceHolder = (TvSourceHolder) sourceRecycle.getChildViewHolder(view);
                tvSourceHolder.unfocused();
            }
        });
        sourceAdapter.setOnItemDispatchKeyListener(new BaseAdapter.OnItemDispatchKeyListener() {
            @Override
            public boolean onDispatchKey(View view, KeyEvent event, int keycode) {
                int action = event.getAction();
                if (action == KeyEvent.ACTION_DOWN
                        && keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    mScrollDelay = true;
                    mTvUtils.pauseTv();
                    sendMessageDelay(MSG_DELAY_SCROLL, getScrollDelayTime());
                    return true;
                }
                return false;
            }
        });

        recyclerView.setOnFirstLeftListener(new SpecialLimitRecyclerView.OnFirstLeftListener() {
            @Override
            public boolean onLeftFirstAttached() {
                getComplexRecycler().leaveFromLeft();
                return true;
            }
        });
        recyclerView.addOnScrollListener(onScrollListener);
        recyclerView.addItemDecoration(sourceDecoration);
        recyclerView.setLayoutManager(sourceLayoutManager);
        recyclerView.setAdapter(sourceAdapter);
    }

    private void setCurrentTvPosition() {
        int systemAutoTime = 0;
        int currentTvSource = queryInputSource();
        if (currentTvSource == TvCommonManager.INPUT_SOURCE_DTV || currentTvSource == TvCommonManager.INPUT_SOURCE_ATV) {
            if (queryAntennaType() == TvIsdbChannelManager.DTV_ANTENNA_TYPE_AIR) {
                currentTvSource = TvCommonManager.INPUT_SOURCE_DTV;
            } else if (queryAntennaType() == TvIsdbChannelManager.DTV_ANTENNA_TYPE_CABLE) {
                currentTvSource = TvCommonManager.INPUT_SOURCE_ATV;
            }
        }
        for (int position = 0; position < mTvSourceList.size(); position++) {
            if (currentTvSource == mTvSourceList.get(position).getInputsource()) {
                mCurrentTvPosition = position;
                break;
            }
        }
    }

    private int queryAntennaType() {
        Cursor cursor = LauncherApplication.mAppContext.getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/mediumsetting/"), null, null, null, null);
        cursor.moveToFirst();
        int antennaType = 0;
        antennaType = cursor.getInt(cursor.getColumnIndex("AntennaType"));
        cursor.close();
        return antennaType;
    }

    private int queryInputSource() {
        Cursor cursor = LauncherApplication.mAppContext.getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/systemsetting"), null, null, null, null);
        cursor.moveToFirst();
        int antennaType = 0;
        antennaType = cursor.getInt(cursor.getColumnIndex("enInputSourceType"));
        cursor.close();
        return antennaType;
    }

    private void updateSourceInputType(int inputSourceTypeIdex) {
        long ret = -1;

        ContentValues vals = new ContentValues();
        vals.put("enInputSourceType", inputSourceTypeIdex);
        try {
            ret = LauncherApplication.mAppContext.getContentResolver().update(
                    Uri.parse("content://mstar.tv.usersetting/systemsetting"),
                    vals, null, null);
        } catch (SQLException e) {
        }

    }


    private void clickAction(TvHolder holder, int position) {
        TvSourceBean bean = mTvSourceList.get(position);
        if (bean.getInputsource() != TvSourceBean.SOURCE_USB) {
            mTvUtils.startTv(bean.getInputsource());
            updateSourceInputType(bean.getInputsource());
            mScrollDelay = true;
            sendMessageDelay(MSG_DELAY_RESET_NAME, 3000);
            mCurrentTvPosition = position;
            holder.getTvName().setText(bean.getTvNameId());
            holder.getExpanded().setText("");
            mEpgList.clear();
            holder.notifyEpgDataChanged();
        } else {
            CommonUtils.startAppForPkg(getContext(), "com.jrm.localmm");
        }
        if (TvUtils.isSourceDtv(bean.getInputsource())) {
            sendMessageDelay(MSG_DELAY_START_EPG_TASK, 6000);
        }
    }

    private void initName(TvHolder holder, TvSourceBean bean) {
        if (holder == null) {
            return;
        }
        String channelName = "";
        int source = bean.getInputsource();
        if (TvUtils.isSourceDtv(source)) {
            channelName = TvChannelManager.getInstance().getCurrentProgramInfo().serviceName;
        } else if (source == TvCommonManager.INPUT_SOURCE_ATV) {
            channelName = TvChannelManager.getInstance().getCurrentProgramInfo().serviceName;
        }
        if (channelName != null) {
            holder.setExpanded(channelName);
        }

        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return mScrollDelay;
            }
        });
    }

    private void stopEpgTask() {
        if (mEpgTask != null) {
            mEpgTask.cancel(true);
        }
    }

    private void startEpgTask() {
        stopEpgTask();
        mEpgTask = new EpgTask();
        mEpgTask.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class EpgTask extends AsyncTask<Void, Void, List<EpgEventInfo>> {

        private final static int MAX_EPG_COUNT = 4;

        @Override
        protected List<EpgEventInfo> doInBackground(Void... voids) {
            int source = TvCommonManager.getInstance().getCurrentTvInputSource();
            if (!TvUtils.isSourceDtv(source)) {
                return null;
            }
            return getEpgEventListInternal(TvChannelManager
                    .getInstance().getCurrentProgramInfo());
        }

        @Override
        protected void onPostExecute(List<EpgEventInfo> epgEventInfos) {
            super.onPostExecute(epgEventInfos);
            final TvHolder holder = getHolder();
            if (epgEventInfos == null || holder == null) {
                return;
            }
            mEpgList.clear();
            mEpgList.addAll(epgEventInfos);
            holder.notifyEpgDataChanged();
        }

        private List<EpgEventInfo> getEpgEventListInternal(ProgramInfo programInfo) {
            Time nextEventBaseTime = new Time();
            nextEventBaseTime.setToNow();
            return TvEpgManager.getInstance().getEventInfo(
                    programInfo.serviceType, programInfo.number,
                    nextEventBaseTime, MAX_EPG_COUNT);
        }
    }
}
