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
import android.widget.AdapterView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ktc.ecuador.R;
import com.ktc.ecuador.data.protocal.CategoryDetail;

import java.util.List;

public class CategoryDetailAdapter extends RecyclerView.Adapter<CategoryDetailAdapter.ViewHolder> {
    private List<CategoryDetail.CategoryItem> mCategories;
    private Context mContext;
    private OnItemSelectListener selectedListener;
    private OnKeyDownListener onKeyDownListener;


    private  OnItemClickListener mOnItemClickListener;
    private boolean isMovie;

    public CategoryDetailAdapter(Context context, List<CategoryDetail.CategoryItem> mCategories, boolean isMovie) {
        this.mContext = context;
        this.mCategories = mCategories;
        this.isMovie = isMovie;
        this.setHasStableIds(true);
    }

    public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener;
    }

    public void setOnItemSelectListener(OnItemSelectListener listener) {
        this.selectedListener = listener;
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_recommend_movie, null);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_recommend_video, null);
        }
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        RequestOptions requestOptions;
        if (getItemViewType(position) == 1) {
            requestOptions = new RequestOptions().placeholder(R.drawable.placeholder_movie_item);
            Glide.with(mContext).load(mCategories.get(position).getImage()).apply(requestOptions).into(holder.recommend_iv_movie);
        } else {
            requestOptions = new RequestOptions().placeholder(R.drawable.placeholder_video_item);
            Glide.with(mContext).load(mCategories.get(position).getImage()).apply(requestOptions).into(holder.recommend_iv_video);
        }
        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP)
                    return true;
                return onKeyDownListener.onKey(position, keyCode);
            }
        });
        holder.itemView.setAlpha(0.6f);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(position);
            }
        });
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
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return isMovie ? 1 : 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public interface OnKeyDownListener {
        boolean onKey(int position, int keyCode);
    }

    public interface OnItemSelectListener {
        void onItemSelect(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView recommend_iv_movie;
        private ImageView recommend_iv_video;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recommend_iv_movie = itemView.findViewById(R.id.recommend_iv_movie);
            recommend_iv_video = itemView.findViewById(R.id.recommend_iv_video);
        }
    }
}
