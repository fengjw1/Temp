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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ktc.ecuador.R;
import com.ktc.ecuador.data.protocal.CategoryDetail;

import java.util.List;

public class MovieRecommendAdapter extends RecyclerView.Adapter<MovieRecommendAdapter.ViewHolder> {
    private List<CategoryDetail.CategoryItem> mCategoryDetail;
    private Context mContext;
    private OnItemSelectListener selectedListener;
    private OnFocusChangeListener focusChangeListener;
    private OnKeyDownListener onKeyDownListener;
    private OnItemClickListener mOnItemClickListener;
    private RequestOptions requestOptions;

    public MovieRecommendAdapter(Context context, List<CategoryDetail.CategoryItem> categoryDetail) {
        this.mContext = context;
        this.mCategoryDetail = categoryDetail;
        this.setHasStableIds(true);
        requestOptions = new RequestOptions().placeholder(R.drawable.placeholder_movie_item);
    }

    public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener;
    }

    public void setOnItemSelectListener(OnItemSelectListener listener) {
        this.selectedListener = listener;
    }

    public void setFocusChangeListener(OnFocusChangeListener listener) {
        this.focusChangeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recommend_movie, null);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (position == getItemCount() - 1) {
            holder.recommend_tv_movie_view_all.setVisibility(View.VISIBLE);
            holder.recommend_iv_movie.setVisibility(View.GONE);
        } else {
            holder.recommend_tv_movie_view_all.setVisibility(View.GONE);
            holder.recommend_iv_movie.setVisibility(View.VISIBLE);
            if (position >= mCategoryDetail.size() || mCategoryDetail.get(position) == null) {
                //加载空数据
            } else {
                Glide.with(mContext).load(mCategoryDetail.get(position).getImage()).apply(requestOptions).into(holder.recommend_iv_movie);
            }
        }
        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
                return onKeyDownListener.onKey(position, keyCode);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(position);
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
        return 10;
    }

    public interface OnFocusChangeListener {
        void onFocusChange(boolean hasFocus);
    }


    public interface OnKeyDownListener {
        boolean onKey(int position, int keyCode);
    }

    public interface OnItemSelectListener {
        void onItemSelect(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView recommend_iv_movie;
        private TextView recommend_tv_movie_view_all;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recommend_iv_movie = itemView.findViewById(R.id.recommend_iv_movie);
            recommend_tv_movie_view_all = itemView.findViewById(R.id.recommend_tv_movie_view_all);
        }
    }
}
