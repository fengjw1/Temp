package com.mp3.launcher4.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.activities.bases.BaseActivity;
import com.mp3.launcher4.adapters.DetailAppAdapter;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.customs.views.FocusCheckRecyclerView;
import com.mp3.launcher4.holders.AbstractHomeHolder;
import com.mp3.launcher4.holders.BaseAppHolder;
import com.mp3.launcher4.holders.HomeInstalledHolder;
import com.mp3.launcher4.utils.AppDeleteDialog;
import com.mp3.launcher4.utils.AppsUtils;
import com.mp3.launcher4.utils.FontUtils;
import com.mp3.launcher4.utils.ImageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhouxw
 */
public class DetailAppActivity extends BaseActivity {
    private FocusCheckRecyclerView mRecyclerView;
    private List<AppDetailBean> mAppDetailList;
    private DetailAppAdapter mAppAdapter;
    private AppsUtils mAppsUtils;
    private int mOriginalItemPosition = -1;
    private int mMovedItemPosition = -1;

    private TextView mTvDetailInfo;
    private RequestInstalledTask mTask;
    private AppChangedReceiver mAppChangedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_app);
        init();
        initWidget();
        initRecyclerView();
        initHeaderTips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAppChangedReceiver != null) {
            this.unregisterReceiver(mAppChangedReceiver);
        }
        if (mTask != null) {
            cancelRequest();
        }
    }

    @Override
    protected void onCheckNetworkState(boolean hasNetwork) {

    }

    private void init() {
        mAppChangedReceiver = new AppChangedReceiver();
        mAppDetailList = new ArrayList<>();
        Intent intent = getIntent();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppsUtils.NOTIFY_APP_ADDED);
        intentFilter.addAction(AppsUtils.NOTIFY_APP_MODIFY);
        intentFilter.addAction(AppsUtils.NOTIFY_APP_REMOVED);
        this.registerReceiver(mAppChangedReceiver, intentFilter);
        mAppsUtils = AppsUtils.getInstance(getApplicationContext());
    }

    private void initWidget() {
        mRecyclerView = findViewById(R.id.detail_app_rv);
        ImageView imgDetailIcon = findViewById(R.id.detail_icon);
        TextView tvDetailCategoryName = findViewById(R.id.detail_categoryName);
        TextView tvDetailCategorySecondName = findViewById(R.id.detail_category_secondName);
        mTvDetailInfo = findViewById(R.id.header_tips_text);

        imgDetailIcon.setVisibility(View.VISIBLE);
        tvDetailCategoryName.setVisibility(View.VISIBLE);
        tvDetailCategorySecondName.setVisibility(View.VISIBLE);
        mTvDetailInfo.setText(R.string.tips_menu);
        mTvDetailInfo.setVisibility(View.VISIBLE);
        imgDetailIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_app_store_hl, null));
        String label = getResources().getString(R.string.slide_my_app) + " |";
        tvDetailCategoryName.setText(label);
        tvDetailCategorySecondName.setText(R.string.menu_installed);

        FontUtils fontUtils = FontUtils.getInstance(getApplicationContext());
        fontUtils.setBoldFont(tvDetailCategoryName);
        fontUtils.setLightFont(tvDetailCategorySecondName);
        fontUtils.setRegularFont(mTvDetailInfo);

    }

    private void initHeaderTips() {
        Drawable drawable = getDrawable(R.drawable.ic_info);
        assert drawable != null;
        int size = ImageUtils.dp2Px(getApplicationContext(), 23);
        drawable.setBounds(0, 0, size, size);
        mTvDetailInfo.setVisibility(View.VISIBLE);
        mTvDetailInfo.setCompoundDrawables(drawable, null, null, null);
    }


    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 6,
                LinearLayoutManager.VERTICAL, false));

        mAppAdapter = new DetailAppAdapter(this, mAppDetailList);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int left = ImageUtils.dp2Px(getApplicationContext(), 11);
                int right = ImageUtils.dp2Px(getApplicationContext(), 11);
//                RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
//                if (view.equals(parent.getFocusedChild()) && holder instanceof HomeInstalledHolder) {
//                    int menuState = ((HomeInstalledHolder) holder).getMenuState();
//                    if (menuState == HomeInstalledHolder.STATE_MOVED) {
//                        left = 0;
//                        right = 0;
//                    }
//                }
                int top = ImageUtils.dp2Px(getApplicationContext(), 20);
                int bottom = ImageUtils.dp2Px(getApplicationContext(), 20);
                outRect.set(left, top, right, bottom);
            }
        });
        mAppAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                mAppsUtils.startApp(getBaseContext(),
                        mAppDetailList.get(position),
                        true);
            }
        });
        mAppAdapter.setOnItemDispatchKeyListener(new BaseAdapter.OnItemDispatchKeyListener() {
            @Override
            public boolean onDispatchKey(View view, KeyEvent event, int keycode) {
                if (mRecyclerView.getItemAnimator().isRunning()) {
                    return true;
                }
                RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
                if (viewHolder instanceof HomeInstalledHolder
                        && KeyEvent.ACTION_DOWN == event.getAction()) {
                    HomeInstalledHolder installedHolder = (HomeInstalledHolder) viewHolder;
                    return handleKeyAction(installedHolder, keycode, installedHolder.getAdapterPosition());
                }
                return false;
            }
        });
        mAppAdapter.setOnItemSelectListener(new BaseAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelected(View view, int position) {
                if (mRecyclerView.getChildViewHolder(view) instanceof AbstractHomeHolder) {
                    ((AbstractHomeHolder) mRecyclerView.getChildViewHolder(view)).focused();
                }

            }

            @Override
            public void onItemUnselected(View view, int position) {
                if (mRecyclerView.getChildViewHolder(view) instanceof AbstractHomeHolder) {
                    ((AbstractHomeHolder) mRecyclerView.getChildViewHolder(view)).unfocused();
                }

            }
        });

        mRecyclerView.setAdapter(mAppAdapter);
    }

    private boolean handleKeyAction(final HomeInstalledHolder homeInstalledHolder, int keycode, int pos) {
        int state = homeInstalledHolder.getMenuState();
        if (keycode == KeyEvent.KEYCODE_MENU
                && state == HomeInstalledHolder.STATE_NORMAL) {
            mMovedItemPosition = mOriginalItemPosition = homeInstalledHolder.getAdapterPosition();
            homeInstalledHolder.switchState(HomeInstalledHolder.STATE_MENU);
            mTvDetailInfo.setVisibility(View.GONE);
            return true;
        }

        boolean isBackValid = state != HomeInstalledHolder.STATE_NORMAL
                && keycode == KeyEvent.KEYCODE_BACK;
        if (isBackValid) {
            homeInstalledHolder.switchState(HomeInstalledHolder.STATE_NORMAL);
            restoreItemPosition();
            mTvDetailInfo.setVisibility(View.VISIBLE);
            mTvDetailInfo.setText(R.string.tips_menu);
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
                    mTvDetailInfo.setVisibility(View.VISIBLE);
                    mTvDetailInfo.setText(R.string.tips_move);
                    mAppAdapter.notifyItemRangeChanged(0, getAppDetailList().size(),
                            new Integer[]{BaseAppHolder.SHADOW_STATE_DARK, mOriginalItemPosition});
                } else {
                    AppDeleteDialog deleteDialog = new AppDeleteDialog(DetailAppActivity.this,
                            getAppDetailList().get(pos));
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
                    mAppsUtils.writeOrder(getAppDetailList());
                }
                homeInstalledHolder.switchState(HomeInstalledHolder.STATE_NORMAL);
                mAppAdapter.notifyItemRangeChanged(0, getAppDetailList().size(),
                        new Integer[]{BaseAppHolder.SHADOW_STATE_LIGHT, mMovedItemPosition});
                mTvDetailInfo.setVisibility(View.VISIBLE);
                mTvDetailInfo.setText(R.string.tips_menu);
            }
            return true;
        }
        return false;
    }

    private int handleItemMoved(int pos, int keycode) {
        final List<AppDetailBean> list = mAppDetailList;
        int nextPos = keycode == KeyEvent.KEYCODE_DPAD_LEFT ? pos - 1 : pos + 1;
        if (nextPos < 0 || nextPos >= list.size()) {
            return pos;
        }
        Collections.swap(list, pos, nextPos);
        mAppAdapter.notifyItemMoved(pos, nextPos);
        return nextPos;
    }

    private void restoreItemPosition() {
        if (mMovedItemPosition == mOriginalItemPosition) {
            mAppAdapter.notifyItemRangeChanged(0, getAppDetailList().size(),
                    new Integer[]{BaseAppHolder.SHADOW_STATE_LIGHT, mOriginalItemPosition});
            mMovedItemPosition = -1;
            mOriginalItemPosition = -1;
            return;
        }
        final List<AppDetailBean> list = mAppDetailList;
        AppDetailBean bean = list.get(mMovedItemPosition);
        list.remove(mMovedItemPosition);
        list.add(mOriginalItemPosition, bean);
        mAppAdapter.notifyItemMoved(mMovedItemPosition, mOriginalItemPosition);
        mAppAdapter.notifyItemRangeChanged(0, getAppDetailList().size(),
                new Integer[]{BaseAppHolder.SHADOW_STATE_LIGHT, mOriginalItemPosition});
        mMovedItemPosition = -1;
        mOriginalItemPosition = -1;
    }


    private void requestData() {
        if (mTask != null) {
            mTask.cancel(true);
        }
        mTask = new RequestInstalledTask();
        mTask.execute();
    }


    private void cancelRequest() {
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    private List<AppDetailBean> getAppDetailList() {
        return mAppDetailList;
    }

    @SuppressLint("StaticFieldLeak")
    private class RequestInstalledTask extends AsyncTask<Void, Void, List<AppDetailBean>> {

        @Override
        protected List<AppDetailBean> doInBackground(Void... voids) {
            List<AppDetailBean> data;
            while (true) {
                data = mAppsUtils.getInstalledApps(AppsUtils.NO_LIMIT);
                if (data == null || data.isEmpty()) {
                    continue;
                }
                return data;
            }

        }

        @Override
        protected void onPostExecute(List<AppDetailBean> appBeans) {
            if (mAppDetailList.containsAll(appBeans)
                    && mAppDetailList.size() == appBeans.size()) {
                return;
            }
            mAppDetailList.clear();
            mAppDetailList.addAll(appBeans);
            mAppAdapter.notifyDataSetChanged();
            mRecyclerView.setFocusedByUser(true);
        }
    }

    private class AppChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String url = intent.getStringExtra(AppsUtils.CHANGED_URL);
            int pos = intent.getIntExtra(AppsUtils.CHANGED_POS, -1);
            if (action == null || url == null) {
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
            final List<AppDetailBean> list = getAppDetailList();
            AppDetailBean bean = mAppsUtils.getAppInfo(url);
            if (bean != null) {
                list.add(pos, bean);
                mAppAdapter.notifyItemInserted(pos);
            }
        }

        void remove(String url) {
            final List<AppDetailBean> list = getAppDetailList();
            int pos = getAppIndex(url);
            if (pos != -1) {
                list.remove(list.size() - 1);
                mAppAdapter.notifyItemRemoved(pos);
            }
        }

        void change(String url) {
            final List<AppDetailBean> list = getAppDetailList();
            int pos = getAppIndex(url);
            if (pos != -1) {
                AppDetailBean bean = mAppsUtils.getAppInfo(mAppsUtils.getOrderItem(pos));
                if (bean != null) {
                    list.set(pos, bean);
                    mAppAdapter.notifyItemChanged(pos);
                }
            }
        }

        int getAppIndex(String url) {
            int index = 0;
            for (AppDetailBean bean : getAppDetailList()) {
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
