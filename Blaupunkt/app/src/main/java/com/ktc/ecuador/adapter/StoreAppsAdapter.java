package com.ktc.ecuador.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ktc.ecuador.R;
import com.ktc.ecuador.data.protocal.StoreApp;

import java.util.ArrayList;

public class StoreAppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_POSTER = 0x114;
    private static final int VIEW_APP = 0x115;
    private ArrayList<StoreApp.ListBean> myAppBeans;
    private Context mContext;
    private OnItemSelectListener selectedListener;
    private OnKeyDownListener onKeyDownListener;
    private OnFocusChangeListener focusChangeListener;
    private  OnItemClickListener mOnItemClickListener;

    public StoreAppsAdapter(Context context, ArrayList<StoreApp.ListBean> myAppBeans) {
        this.mContext = context;
        this.myAppBeans = myAppBeans;
        this.setHasStableIds(true);
    }

    public void setOnItemSelectListener(OnItemSelectListener listener) {
        this.selectedListener = listener;
    }

    public void setFocusChangeListener(OnFocusChangeListener listener) {
        this.focusChangeListener = listener;
    }

    public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener;
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_POSTER:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_store_poster, null);
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                viewHolder = new PosterViewHolder(view);
                break;
            case VIEW_APP:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_store_apps, null);
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                viewHolder = new AppViewHolder(view);
                break;
            default:
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        holder.itemView.setAlpha(0.6f);
        if (getItemViewType(position) == VIEW_POSTER) {
            PosterViewHolder posterViewHolder = (PosterViewHolder) holder;
            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.placeholder_banner);
            if (position == 0) {
                //define
                posterViewHolder.item_iv_store_poster.setImageResource(R.drawable.poster_app_store);
            } else {
                Glide.with(mContext).load(myAppBeans.get(position).getImage()).apply(requestOptions).into(posterViewHolder.item_iv_store_poster);
            }
        } else {
            AppViewHolder appViewHolder = (AppViewHolder) holder;
            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.placeholder_app_item);
            Glide.with(mContext).load(myAppBeans.get(position).getImage()).apply(requestOptions).into(appViewHolder.item_iv_store_app);
        }
        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP)
                    return true;
                return onKeyDownListener.onKey(position, keyCode);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(position);
            }
        });
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusChangeListener.onFocusChange(hasFocus);
                if (hasFocus) {
                    holder.itemView.setAlpha(1.0f);
                    selectedListener.onItemSelect(position);
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.view_anim_big);
                    holder.itemView.startAnimation(animation);
                    holder.itemView.setTranslationZ(10f);
                } else {
                    holder.itemView.setAlpha(0.6f);
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.view_anim_small);
                    holder.itemView.startAnimation(animation);
                    holder.itemView.setTranslationZ(0f);
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || myAppBeans.get(position).getId().equals("noId")) {
            return VIEW_POSTER;
        } else {
            return VIEW_APP;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return myAppBeans.size();
    }


    public interface OnItemSelectListener {
        void onItemSelect(int position);
    }

    public interface OnKeyDownListener {
        boolean onKey(int position, int keyCode);
    }

    public interface OnFocusChangeListener {
        void onFocusChange(boolean hasFocus);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class PosterViewHolder extends RecyclerView.ViewHolder {
        private ImageView item_iv_store_poster;

        public PosterViewHolder(@NonNull View itemView) {
            super(itemView);
            item_iv_store_poster = itemView.findViewById(R.id.item_iv_store_poster);
        }
    }

    class AppViewHolder extends RecyclerView.ViewHolder {
        private ImageView item_iv_store_app;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            item_iv_store_app = itemView.findViewById(R.id.item_iv_store_app);
        }
    }
}
