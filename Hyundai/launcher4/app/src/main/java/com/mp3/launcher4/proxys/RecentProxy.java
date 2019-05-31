package com.mp3.launcher4.proxys;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mp3.launcher4.adapters.BaseAppAdapter;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.utils.AppsUtils;
import com.mp3.launcher4.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author longzj
 */
public class RecentProxy extends BaseTitleRecyclerProxy<AppDetailBean> {

    private static final int MAX_SHOWN_NUM = 10;
    private AppsUtils mAppsUtils;
    private RecentTask mRecentTask;
    private RecentBroadCast mRecentBroadCast;

    public RecentProxy(Context context, int titleId) {
        super(context, titleId);
        mAppsUtils = AppsUtils.getInstance(context.getApplicationContext());
        mRecentBroadCast = new RecentBroadCast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppsUtils.NOTIFY_RECENT_CHANGED);
        getContext().registerReceiver(mRecentBroadCast, intentFilter);
    }

    @Override
    public boolean isGroupAbove() {
        return true;
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
    protected BaseAdapter<AppDetailBean> initAdapter(List<AppDetailBean> data) {
        return new BaseAppAdapter(getContext(), data);
    }

    @Override
    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int right = ImageUtils.dp2Px(getContext().getApplicationContext(), 23);
                int bottom = ImageUtils.dp2Px(getContext().getApplicationContext(), 80);
                outRect.set(0, 0, right, bottom);
            }
        };
    }

    @Override
    public void onItemClicked(View view, int position) {
        super.onItemClicked(view, position);
        mAppsUtils.startApp(getContext(),
                getData().get(position),
                true);
    }

    @Override
    public void requestData() {
        super.requestData();
        startTask();
    }

    @Override
    public void cancelRequest() {
        super.cancelRequest();
        stopTask();
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mRecentBroadCast != null) {
            getContext().unregisterReceiver(mRecentBroadCast);
        }
    }

    @Override
    protected boolean ignoreNetworkChange() {
        return true;
    }

    private void stopTask() {
        if (mRecentTask != null) {
            mRecentTask.cancel(true);
        }
    }

    private void startTask() {
        stopTask();
        mRecentTask = new RecentTask();
        mRecentTask.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class RecentTask extends AsyncTask<Void, Void, List<AppDetailBean>> {

        @Override
        protected List<AppDetailBean> doInBackground(Void... voids) {
            List<AppDetailBean> data;
            while (true) {
                data = mAppsUtils.getRecentApps();
                if (data == null || data.isEmpty()) {
                    continue;
                }
                return data;
            }
        }

        @Override
        protected void onPostExecute(List<AppDetailBean> appDetailBeans) {
            super.onPostExecute(appDetailBeans);
            getData().clear();
            getData().addAll(appDetailBeans);
            notifyDataSetChanged();
        }
    }

    private class RecentBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            requestData();
            getHolder().getRecyclerView().clearFocus();
        }
    }
}
