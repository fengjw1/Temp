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
import com.ktc.ecuador.data.protocal.CategoryDetail;

import java.util.List;

public class VideoRecommendAdapter extends RecyclerView.Adapter<VideoRecommendAdapter.ViewHolder> {
    private List<CategoryDetail.CategoryItem> mCategoryDetail;
    private Context mContext;
    private OnItemClickListener listener;
    private OnItemSelectListener selectedListener;
    private OnFocusChangeListener focusChangeListener;
    private OnKeyDownListener onKeyDownListener;
    private RequestOptions requestOptions;

    public VideoRecommendAdapter(Context context, List<CategoryDetail.CategoryItem> categoryDetail) {
        this.mContext = context;
        this.mCategoryDetail = categoryDetail;
        this.setHasStableIds(true);
        requestOptions = new RequestOptions().placeholder(R.drawable.placeholder_video_item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFocusChangeListener(OnFocusChangeListener listener) {
        this.focusChangeListener = listener;
    }

    public void setOnItemSelectListener(OnItemSelectListener listener) {
        this.selectedListener = listener;
    }

    public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recommend_video, null);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (mCategoryDetail.size() > 0 && mCategoryDetail.get(position) != null) {
            Glide.with(mContext).load(mCategoryDetail.get(position).getImage()).apply(requestOptions).into(holder.recommend_iv_video);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(position);
            }
        });
        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
                return onKeyDownListener.onKey(position, keyCode);
            }

        });
        holder.itemView.setAlpha(0.6f);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
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
                focusChangeListener.onFocusChange(hasFocus);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mCategoryDetail.isEmpty() ? 10 : mCategoryDetail.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnFocusChangeListener {
        void onFocusChange(boolean hasFocus);
    }


    public interface OnItemSelectListener {
        void onItemSelect(int position);
    }
    public interface OnKeyDownListener {
        boolean onKey(int position, int keyCode);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView recommend_iv_video;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recommend_iv_video = itemView.findViewById(R.id.recommend_iv_video);
        }
    }
}
