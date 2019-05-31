package com.mp3.launcher4.proxys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

import com.mp3.launcher4.activities.DetailAppActivity;
import com.mp3.launcher4.adapters.InstalledAdapter;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.customs.views.complex.holds.TitleRecyclerHolder;
import com.mp3.launcher4.holders.BaseAppHolder;
import com.mp3.launcher4.holders.HomeInstalledHolder;
import com.mp3.launcher4.utils.AppDeleteDialog;
import com.mp3.launcher4.utils.AppsUtils;
import com.mp3.launcher4.utils.CommonUtils;
import com.mp3.launcher4.utils.ImageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author longzj
 */
public class InstalledProxy extends BaseTitleRecyclerProxy<AppDetailBean> {

    private static final int MAX_SHOWN_NUM = 11;
    private RequestInstalledTask mTask;
    private AppsUtils mAppsUtils;
    private int mOriginalItemPosition = -1;
    private int mMovedItemPosition = -1;

    private AppChangedReceiver mAppChangedReceiver;
    private List<OnMenuStateChangeListener> mStateChangeListenerList;

    public InstalledProxy(Context context, int titleId) {
        super(context, titleId);
        mAppsUtils = AppsUtils.getInstance(context.getApplicationContext());
        mAppChangedReceiver = new AppChangedReceiver();
        mStateChangeListenerList = new ArrayList<>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppsUtils.NOTIFY_APP_ADDED);
        intentFilter.addAction(AppsUtils.NOTIFY_APP_MODIFY);
        intentFilter.addAction(AppsUtils.NOTIFY_APP_REMOVED);
        context.registerReceiver(mAppChangedReceiver, intentFilter);
    }

    private boolean handleKeyAction(final HomeInstalledHolder homeInstalledHolder, int keycode, int pos) {
        int state = homeInstalledHolder.getMenuState();
        if (keycode == KeyEvent.KEYCODE_MENU
                && state == HomeInstalledHolder.STATE_NORMAL) {
            mMovedItemPosition = mOriginalItemPosition = homeInstalledHolder.getAdapterPosition();
            homeInstalledHolder.switchState(HomeInstalledHolder.STATE_MENU);
            for (OnMenuStateChangeListener listener : mStateChangeListenerList) {
                listener.onMenuStateChanged(HomeInstalledHolder.STATE_MENU);
            }
            return true;
        }

        boolean isBackValid = state != HomeInstalledHolder.STATE_NORMAL
                && keycode == KeyEvent.KEYCODE_BACK;
        if (isBackValid) {
            homeInstalledHolder.switchState(HomeInstalledHolder.STATE_NORMAL);
            restoreItemPosition();
            for (OnMenuStateChangeListener listener : mStateChangeListenerList) {
                listener.onMenuStateChanged(HomeInstalledHolder.STATE_NORMAL);
            }
            return true;
        }

        if (state == HomeInstalledHolder.STATE_MENU) {
            if (keycode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                homeInstalledHolder.setFocusState(keycode);
                return true;
            }
            if (keycode == KeyEvent.KEYCODE_ENTER
                    || keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
                int focusState = homeInstalledHolder.getFocusState();
                if (focusState == HomeInstalledHolder.FOCUSED_MOVE) {
                    homeInstalledHolder.switchState(HomeInstalledHolder.STATE_MOVED);
                    notifyItemRangeChanged(0, getData().size(),
                            new Integer[]{BaseAppHolder.SHADOW_STATE_DARK, mOriginalItemPosition});
                    for (OnMenuStateChangeListener listener : mStateChangeListenerList) {
                        listener.onMenuStateChanged(HomeInstalledHolder.STATE_MOVED);
                    }
                } else {
                    AppDeleteDialog deleteDialog = new AppDeleteDialog(getContext(),
                            getData().get(pos));
                    deleteDialog.setOnDeleteListener(new AppDeleteDialog.OnDeleteListener() {
                        @Override
                        public void onDelete() {
                            homeInstalledHolder.clearState();
                        }
                    });
                    deleteDialog.show();
                }
            }
            return true;
        } else if (state == HomeInstalledHolder.STATE_MOVED) {
            if (keycode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mMovedItemPosition = handleItemMoved(pos, keycode);
            }
            if (keycode == KeyEvent.KEYCODE_DPAD_CENTER
                    || keycode == KeyEvent.KEYCODE_ENTER) {
                if (mMovedItemPosition != mOriginalItemPosition) {
                    mAppsUtils.writeOrder(getData());
                }
                homeInstalledHolder.switchState(HomeInstalledHolder.STATE_NORMAL);
                notifyItemRangeChanged(0, getData().size(),
                        new Integer[]{BaseAppHolder.SHADOW_STATE_LIGHT, mMovedItemPosition});
                for (OnMenuStateChangeListener listener : mStateChangeListenerList) {
                    listener.onMenuStateChanged(HomeInstalledHolder.STATE_NORMAL);
                }
            }
            return true;
        }
        return false;
    }

    private int handleItemMoved(int pos, int keycode) {
        final List<AppDetailBean> list = getData();
        int nextPos = keycode == KeyEvent.KEYCODE_DPAD_LEFT ? pos - 1 : pos + 1;
        if (nextPos < 0 || nextPos >= list.size() - 1) {
            return pos;
        }
        final RecyclerView recyclerView = getHolder().getRecyclerView();
        Collections.swap(list, pos, nextPos);
        notifyItemMove(pos, nextPos);
        return nextPos;
    }

    private void restoreItemPosition() {
        if (mMovedItemPosition == mOriginalItemPosition) {
            notifyItemRangeChanged(0, getData().size(),
                    new Integer[]{BaseAppHolder.SHADOW_STATE_LIGHT, mOriginalItemPosition});
            mMovedItemPosition = -1;
            mOriginalItemPosition = -1;
            return;
        }
        final List<AppDetailBean> list = getData();
        AppDetailBean bean = list.get(mMovedItemPosition);
        list.remove(mMovedItemPosition);
        list.add(mOriginalItemPosition, bean);
        notifyItemMove(mMovedItemPosition, mOriginalItemPosition);
        notifyItemRangeChanged(0, getData().size(),
                new Integer[]{BaseAppHolder.SHADOW_STATE_LIGHT, mOriginalItemPosition});
        mMovedItemPosition = -1;
        mOriginalItemPosition = -1;
    }

    @Override
    public void requestData() {
        super.requestData();
        if (mTask != null) {
            mTask.cancel(true);
        }
        mTask = new RequestInstalledTask();
        mTask.execute();
    }

    @Override
    public void cancelRequest() {
        super.cancelRequest();
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mAppChangedReceiver != null) {
            getContext().unregisterReceiver(mAppChangedReceiver);
        }
        if (mStateChangeListenerList != null) {
            mStateChangeListenerList.clear();
        }
    }

    @Override
    protected boolean ignoreNetworkChange() {
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
        return new InstalledAdapter(getContext(), data);
    }

    @Override
    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int left = ImageUtils.dp2Px(getContext().getApplicationContext(), 11);
                int right = ImageUtils.dp2Px(getContext().getApplicationContext(), 11);
                int pos = parent.getChildAdapterPosition(view);
                if (pos == 0) {
                    left = 0;
                }
                if (pos == parent.getAdapter().getItemCount() - 1) {
                    right = 0;
                }
//                RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
//                if (view.equals(parent.getFocusedChild()) && holder instanceof HomeInstalledHolder) {
//                    int menuState = ((HomeInstalledHolder) holder).getMenuState();
//                    if (menuState == HomeInstalledHolder.STATE_MOVED) {
//                        left = 0;
//                        right = 0;
//                    }
//                }
                int bottom = ImageUtils.dp2Px(getContext().getApplicationContext(), 90);
                outRect.set(left, 0, right, bottom);
            }
        };
    }

    @Override
    public void onItemSelected(View view, int position) {
        super.onItemSelected(view, position);
        for (OnMenuStateChangeListener listener : mStateChangeListenerList) {
            listener.onMenuStateChanged(HomeInstalledHolder.STATE_NORMAL);
        }
    }

    @Override
    public void onItemUnselected(View view, int position) {
        super.onItemUnselected(view, position);
        for (OnMenuStateChangeListener listener : mStateChangeListenerList) {
            listener.onMenuStateChanged(HomeInstalledHolder.STATE_NONE);
        }
    }

    @Override
    public boolean onLeftFirstAttached() {
        final TitleRecyclerHolder holder = getHolder();
        final RecyclerView recyclerView = holder.getRecyclerView();
        HomeInstalledHolder installedHolder = (HomeInstalledHolder) recyclerView.getChildViewHolder(recyclerView.getFocusedChild());
        int state = installedHolder.getMenuState();
        return state == HomeInstalledHolder.STATE_NORMAL && super.onLeftFirstAttached();
    }

    @Override
    public void onItemClicked(View view, int position) {
        super.onItemClicked(view, position);
        if (position != getData().size() - 1) {
            mAppsUtils.startApp(getContext(),
                    getData().get(position),
                    true);
        } else {
            CommonUtils.startActivityForClass((Activity) getContext(), DetailAppActivity.class);
        }
    }

    @Override
    public boolean onDispatchKey(View view, KeyEvent event, int keycode) {
        if (isNotifyAnimRunning()) {
            return true;
        }
        TitleRecyclerHolder holder = getHolder();
        if (holder == null) {
            return super.onDispatchKey(view, event, keycode);
        }
        final RecyclerView recyclerView = holder.getRecyclerView();
        RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
        if (viewHolder instanceof HomeInstalledHolder
                && KeyEvent.ACTION_DOWN == event.getAction()) {
            HomeInstalledHolder installedHolder = (HomeInstalledHolder) viewHolder;
            if (handleKeyAction(installedHolder, keycode, installedHolder.getAdapterPosition())) {
                return true;
            }
        }
        return super.onDispatchKey(view, event, keycode);
    }

    public void addMenuStateChangeListener(OnMenuStateChangeListener listener) {
        if (!mStateChangeListenerList.contains(listener)) {
            mStateChangeListenerList.add(listener);
        }
    }

    public interface OnMenuStateChangeListener {
        /**
         * 状态变化时调用
         *
         * @param state 状态值，参考HomeInstalledHolder内定义的三个参数
         */
        void onMenuStateChanged(int state);
    }

    @SuppressLint("StaticFieldLeak")
    private class RequestInstalledTask extends AsyncTask<Void, Void, List<AppDetailBean>> {

        @Override
        protected List<AppDetailBean> doInBackground(Void... voids) {
            List<AppDetailBean> data;
            while (true) {
                data = mAppsUtils.getInstalledApps(MAX_SHOWN_NUM);
                if (data == null || data.isEmpty()) {
                    continue;
                }
                data.add(new AppDetailBean());
                return data;
            }

        }

        @Override
        protected void onPostExecute(List<AppDetailBean> appBeans) {
            getData().clear();
            getData().addAll(appBeans);
            notifyAllReload();
        }
    }

    private class AppChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String url = intent.getStringExtra(AppsUtils.CHANGED_URL);
            int pos = intent.getIntExtra(AppsUtils.CHANGED_POS, -1);
            if (action == null || url == null || pos >= MAX_SHOWN_NUM - 1) {
                return;
            }

            switch (action) {
                case AppsUtils.NOTIFY_APP_ADDED:
                    add(url, pos);
                    break;
                case AppsUtils.NOTIFY_APP_REMOVED:
                    remove(url);
                    break;
                case AppsUtils.NOTIFY_APP_MODIFY:
                    change(url);
                    break;
                default:
                    break;
            }
        }

        void add(String url, int pos) {
            final List<AppDetailBean> list = getData();
            AppDetailBean bean = mAppsUtils.getAppInfo(url);
            if (bean != null) {
                list.add(pos, bean);
                notifyItemInsert(pos);
                notifyAllReload();
            }
        }

        void remove(String url) {
            final List<AppDetailBean> list = getData();
            int pos = getAppIndex(url);
            if (pos != -1) {
                list.remove(pos);
                notifyItemRemoved(pos);
            }
        }

        void change(String url) {
            final List<AppDetailBean> list = getData();
            int pos = getAppIndex(url);
            if (pos != -1) {
                AppDetailBean bean = mAppsUtils.getAppInfo(mAppsUtils.getOrderItem(pos));
                if (bean != null) {
                    list.set(pos, bean);
                    notifyItemChanged(pos);
                }
            }
        }

        int getAppIndex(String url) {
            int index = 0;
            for (AppDetailBean bean : getData()) {
                String tmpUrl = bean.getUrl();
                if (tmpUrl != null && tmpUrl.equals(url)) {
                    return index;
                }
                index++;
            }
            return -1;
        }
    }
}
