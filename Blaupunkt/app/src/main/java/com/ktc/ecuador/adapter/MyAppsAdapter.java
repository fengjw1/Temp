package com.ktc.ecuador.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ktc.ecuador.R;
import com.ktc.ecuador.data.protocal.MyAppBean;
import com.ktc.ecuador.utils.CommonUtils;
import com.ktc.ecuador.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

public class MyAppsAdapter extends RecyclerView.Adapter<MyAppsAdapter.ViewHolder> {
    public boolean menuMode = false;
    public boolean isMoving = false;
    public int originPosition = -1;
    private static int MAX_COUNT=10;
    private ArrayList<MyAppBean> myAppBeans;
    private Context mContext;
    private OnItemSelectListener selectedListener;
    private OnKeyDownListener onKeyDownListener;
    private OnFocusChangeListener focusChangeListener;
    private OnItemClickListener mOnItemClickListener;
    private boolean isHomeApp;
    private RequestOptions requestOptions;

    public MyAppsAdapter(Context context, ArrayList<MyAppBean> myAppBeans, boolean isHomeApp) {
        this.mContext = context;
        this.myAppBeans = myAppBeans;
        this.isHomeApp = isHomeApp;
        requestOptions = new RequestOptions().placeholder(R.drawable.placeholder_app_item);
    }

    public void exitMovingMode() {
        isMoving = false;
        originPosition = -1;
        selectedListener.onItemExitMoveState();
    }

    public void setFocusChangeListener(OnFocusChangeListener listener) {
        this.focusChangeListener = listener;
    }

    public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener;
    }

    public void setOnItemSelectListener(OnItemSelectListener listener) {
        this.selectedListener = listener;
    }

    public void setData(List<MyAppBean> data) {
        this.myAppBeans = (ArrayList<MyAppBean>) data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_my_apps, null);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.item_ll_fun.setVisibility(View.GONE);
        holder.item_iv_app_name.setText(myAppBeans.get(position).getAppName());

        if (myAppBeans.size() < MAX_COUNT){
            MAX_COUNT = myAppBeans.size();
        }

        if (isHomeApp && position == MAX_COUNT - 1) {
            holder.item_iv_app_icon.setVisibility(View.GONE);
            holder.item_iv_app_banner.setImageDrawable(mContext.getResources().
                    getDrawable(R.drawable.ic_home_app_viewall, null));
        } else {
            String url = myAppBeans.get(position).getIntentUrl();
            String pkg = myAppBeans.get(position).getAppPackageName();
            if (CommonUtils.isLocalApp(mContext, url)) {
                if (whiteList(pkg)){
                    setLocalImage(pkg, holder);
                }else {
                    PackageManager manager = mContext.getApplicationContext().getPackageManager();
                    try {
                        if (manager.getApplicationBanner(url) == null) {
                            holder.item_iv_app_icon.setVisibility(View.VISIBLE);
                            holder.item_iv_app_banner.setImageDrawable(mContext.getResources()
                                    .getDrawable(R.drawable.placeholder_app_item_gray, null));
                            holder.item_iv_app_icon.setImageDrawable(getIconFromPackageName(url, mContext));
                        } else {
                            holder.item_iv_app_icon.setVisibility(View.GONE);
                            holder.item_iv_app_banner.setImageDrawable(manager.getApplicationBanner(url));
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                holder.item_iv_app_icon.setVisibility(View.GONE);
                Glide.with(mContext).load(myAppBeans.get(position).getIconRes()).apply(requestOptions).into(holder
                        .item_iv_app_banner);
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(position);
            }
        });

        /**
         * 事件分发处理流程：
         * 如果是menu状态，首先将事件分发给menu菜单去进行处理
         * 接着是item的按键处理，比如back屏蔽，确认事件，进入/退出 menu状态
         */
        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP)
                    return true;
                if (menuMode) {
                    return dispatchKeyEventByMenuMode(holder, keyCode, holder.getAdapterPosition());
                }
                if (isMoving) {
                    return onKeyDownListener.onMoveEvent(keyCode, holder.getAdapterPosition());
                }
                if (onKeyDownListener.onKey(holder.getAdapterPosition(), keyCode)) {
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    if (isHomeApp&&position ==MAX_COUNT - 1){
                        return true;
                    }
                    enterMenuMode(holder);
                    return true;
                }
                return false;
            }
        });
        holder.itemView.setAlpha(0.6f);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusChangeListener.onFocusChange(hasFocus);
                if (hasFocus) {
                    selectedListener.onItemSelect(holder.getAdapterPosition(), v);
                    holder.itemView.setAlpha(1.0f);
                    if (!isHomeApp) {
                        holder.item_iv_app_name.setVisibility(View.VISIBLE);
                        holder.item_iv_app_name_bg.setVisibility(View.VISIBLE);
                    }
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.view_anim_big);
                    holder.itemView.startAnimation(animation);
                    holder.itemView.setTranslationZ(10f);
                } else {
                    selectedListener.onItemUnSelect(holder.getAdapterPosition(), v);
                    holder.itemView.setAlpha(0.6f);
                    if (!isHomeApp) {
                        holder.item_iv_app_name.setVisibility(View.GONE);
                        holder.item_iv_app_name_bg.setVisibility(View.GONE);
                    }
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.view_anim_small);
                    holder.itemView.startAnimation(animation);
                    holder.itemView.setTranslationZ(0f);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (isHomeApp){
            return myAppBeans.size()>MAX_COUNT?MAX_COUNT:myAppBeans.size();
        }else {
            return myAppBeans.size();
        }
    }

    /**
     * 获取大图标
     *
     * @param packageName 包名
     * @param context     上下文
     * @return 高分辨率的icon
     */
    private synchronized static Drawable getIconFromPackageName(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            try {
                PackageInfo pi = pm.getPackageInfo(packageName, 0);
                Context otherAppCtx = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
                int defDisplayMetrics = context.getResources().getDisplayMetrics().densityDpi;
                if (defDisplayMetrics == DisplayMetrics.DENSITY_MEDIUM) {
                    defDisplayMetrics = DisplayMetrics.DENSITY_HIGH;
                } else {
                    defDisplayMetrics = DisplayMetrics.DENSITY_XXXHIGH;
                }
                int[] displayMetrics = {DisplayMetrics.DENSITY_XXXHIGH, DisplayMetrics.DENSITY_XXHIGH, DisplayMetrics.DENSITY_XHIGH, DisplayMetrics.DENSITY_HIGH, DisplayMetrics.DENSITY_TV};
                for (int displayMetric : displayMetrics) {
                    try {
                        Drawable d = otherAppCtx.getResources().getDrawableForDensity(pi.applicationInfo.icon, displayMetric);
                        if (d != null && displayMetric == defDisplayMetrics) {
                            return d;
                        }
                    } catch (Resources.NotFoundException e) {
                        continue;
                    }
                }
            } catch (Exception e) {
                // Handle Error here
            }
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return appInfo.loadIcon(pm);
    }

    private boolean dispatchKeyEventByMenuMode(ViewHolder holder, int keyCode, int position) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && holder.item_iv_fun_delete.isActivated()) {
            holder.item_iv_fun_move.setActivated(true);
            holder.item_iv_fun_delete.setActivated(false);
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && holder.item_iv_fun_move.isActivated()) {
            holder.item_iv_fun_move.setActivated(false);
            holder.item_iv_fun_delete.setActivated(true);
        } else if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK) {
            exitMenuMode(holder);
        } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (holder.item_iv_fun_delete.isActivated()) {
                onKeyDownListener.deleteEvent(position);
            } else if (holder.item_iv_fun_move.isActivated()) {
                exitMenuMode(holder);
                isMoving = true;
                originPosition = position;
                selectedListener.onItemEnterMoveState();
            }
        }
        return true;
    }

    private void enterMenuMode(ViewHolder holder) {
        holder.item_ll_fun.setVisibility(View.VISIBLE);
        holder.item_iv_fun_move.setActivated(true);
        holder.item_iv_fun_delete.setActivated(false);
        menuMode = true;
    }

    private void exitMenuMode(ViewHolder holder) {
        holder.item_ll_fun.setVisibility(View.GONE);
        holder.item_iv_fun_move.setActivated(false);
        holder.item_iv_fun_delete.setActivated(false);
        menuMode = false;
    }

    /**
     * 过滤 包名
     * @param pkg
     * @return
     */
    private Boolean whiteList(String pkg){
        switch (pkg){
            case "com.ktc.tvhelper":
            case "com.mstar.tv.tvplayer.ui":
            case "android.systemupdate.service":
            case "com.android.browser":
            case "com.jrm.localmm":
            case "com.ktc.miracast":
            case "com.android.tv.settings":
            case "com.android.apkinstaller":
            case "com.android.quicksearchbox":
            case "com.android.providers.downloads.ui":
            case "com.ktc.help":
                return true;
            default:
                return false;
        }
    }

    /**
     * 使用Launcher本地的Banner图片
     * @param pkg
     * @param holder
     */
    private void setLocalImage(String pkg, ViewHolder holder){
        holder.item_iv_app_icon.setVisibility(View.GONE);
        switch (pkg){
            case "com.ktc.tvhelper":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.tv_assistant_banner));
                break;
            case "com.mstar.tv.tvplayer.ui":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.tv_banner));
                break;
            case "android.systemupdate.service":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.system_update_banner));
                break;
            case "com.android.browser":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.browser_banner));
                break;
            case "com.jrm.localmm":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.multimedia_banner));
                break;
            case "com.ktc.miracast":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.miracast_banner));
                break;
            case "com.android.tv.settings":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.settings_banner));
                break;
            case "com.android.apkinstaller":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.apkinstaller_banner_banner));
                break;
            case "com.android.quicksearchbox":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.search_banner));
                break;
            case "com.android.providers.downloads.ui":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.download_banner));
                break;
            case "com.ktc.help":
                holder.item_iv_app_banner.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.user_manual_banner));
                break;
        }
    }

    public void exitMenuMode() {
        menuMode = false;
    }

    public interface OnItemSelectListener {
        void onItemSelect(int position, View view);

        void onItemUnSelect(int position, View view);

        void onItemEnterMoveState();

        void onItemExitMoveState();
    }

    public interface OnKeyDownListener {
        boolean onKey(int position, int keyCode);

        void deleteEvent(int position);

        boolean onMoveEvent(int keyCode, int position);
    }

    public interface OnFocusChangeListener {
        void onFocusChange(boolean hasFocus);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView item_iv_app_icon;
        private ImageView item_iv_app_banner;
        private TextView item_iv_app_name;

        private LinearLayout item_ll_fun;
        private ImageView item_iv_fun_move;
        private ImageView item_iv_fun_delete;

        //add fengjw
        private LinearLayout item_iv_app_name_bg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_iv_app_icon = itemView.findViewById(R.id.item_iv_app_icon);
            item_iv_app_banner = itemView.findViewById(R.id.item_iv_app_banner);
            item_iv_app_name = itemView.findViewById(R.id.item_iv_app_name);
            item_iv_app_name_bg = itemView.findViewById(R.id.item_iv_app_name_bg);
            item_ll_fun = itemView.findViewById(R.id.item_ll_fun);
            item_iv_fun_move = itemView.findViewById(R.id.item_iv_fun_move);
            item_iv_fun_delete = itemView.findViewById(R.id.item_iv_fun_delete);
            FontUtils.getInstance(mContext).setRegularFont(item_iv_app_name);
        }
    }
}
