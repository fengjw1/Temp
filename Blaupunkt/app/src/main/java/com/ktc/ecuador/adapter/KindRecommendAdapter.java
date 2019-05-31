package com.ktc.ecuador.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.ktc.ecuador.R;
import com.ktc.ecuador.data.protocal.CategoryBean;

import java.util.List;

public class KindRecommendAdapter extends RecyclerView.Adapter<KindRecommendAdapter.ViewHolder> {
    OnItemSelectListener selectedListener;
    private List<CategoryBean.CategoryItem> mCategories;
    private Context mContext;
    private OnItemClickListener listener;
    private OnFocusChangeListener focusChangeListener;

    public KindRecommendAdapter(Context context, List<CategoryBean.CategoryItem> mCategories) {
        this.mContext = context;
        this.mCategories = mCategories;
        this.setHasStableIds(true);
    }

    public void setFocusChangeListener(OnFocusChangeListener listener) {
        this.focusChangeListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemSelectListener(OnItemSelectListener listener) {
        this.selectedListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recommend_kind, null);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (position < mCategories.size() && mCategories.get(position) != null)
            holder.recommend_tv_kind_label.setText(mCategories.get(position).getDisplay());
        else
            holder.recommend_tv_kind_label.setText("");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(position);
            }
        });
        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });
        holder.itemView.setAlpha(0.6f);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusChangeListener.onFocusChange(hasFocus);
                if (hasFocus) {
                    holder.itemView.setAlpha(1.0f);
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.view_anim_big);
                    holder.itemView.startAnimation(animation);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.itemView.setTranslationZ(10f);
                    }
                } else {
                    holder.itemView.setAlpha(0.6f);
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.view_anim_small);
                    holder.itemView.startAnimation(animation);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.itemView.setTranslationZ(0f);
                    }
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
        return 5;
    }

    public interface OnFocusChangeListener {
        void onFocusChange(boolean hasFocus);
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemSelectListener {
        void onItemSelect(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView recommend_tv_kind_label;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recommend_tv_kind_label = itemView.findViewById(R.id.recommend_tv_kind_label);
        }
    }
}
