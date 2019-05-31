package com.ktc.ecuador.adapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.ktc.ecuador.R;
import com.ktc.ecuador.data.protocal.TvSourceBean;
import com.ktc.ecuador.utils.AnimatorUtils;
import com.ktc.ecuador.view.holders.TvHolder;

import java.util.List;

public class HomeSourceAdapter extends RecyclerView.Adapter {
    public TvHolder mHolder;
    private Context mContext;
    private List<TvSourceBean> mData;
    private OnItemClickListener mListener;
    private OnItemSelectListener mOnItemSelectListener;
    private OnKeyDownListener onKeyDownListener;

    public HomeSourceAdapter(Context mContext, List<TvSourceBean> mData) {
        this.mContext = mContext;
        this.mData = mData;
        this.setHasStableIds(true);

    }

    public OnKeyDownListener getOnKeyDownListener() {
        return onKeyDownListener;
    }

    public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener;
    }

    public OnItemClickListener getListener() {
        return mListener;
    }

    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public OnItemSelectListener getOnItemSelectListener() {
        return mOnItemSelectListener;
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        mOnItemSelectListener = onItemSelectListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_home_source_focus, parent, false);
        view.setFocusableInTouchMode(true);
        view.setFocusable(true);
        return new TvHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof TvHolder) {
            ((TvHolder) holder).getSourceName().setText(mContext.getResources().getString(mData.get(position).getTvNameId()));
            ((TvHolder) holder).getSourceNamehl().setText(mContext.getResources().getString(mData.get(position).getTvNameId()));


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemClick(position);
                }
            });
            holder.itemView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        return true;
                    }
                    return onKeyDownListener.onKey(position, i);
                }
            });

            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (mOnItemSelectListener != null) {
                        if (b) {
                            mOnItemSelectListener.onItemSelect(view, position);
                            ((TvHolder) holder).setVisibility(true);
                            onSelectedAnimation(view);
                        } else {
                            mOnItemSelectListener.omItemUnSelect(view, position);
                            ((TvHolder) holder).setVisibility(false);
                            onUnSelectedAnimation(view);
                        }
                    }
                }
            });
        }

    }

    private void onSelectedAnimation(View view) {
        AnimatorSet set = new AnimatorSet();
        Animator transZAnim = AnimatorUtils.createElevationAnim(view, AnimatorUtils.BASE_Z);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(view, AnimatorUtils.BASE_BANNER_SCALE);
        set.playTogether(transZAnim, scaleAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    private void onUnSelectedAnimation(View view) {
        AnimatorSet set = new AnimatorSet();
        Animator transZAnim = AnimatorUtils.createElevationAnim(view, 0);
        Animator scaleAnim = AnimatorUtils.createScaleAnim(view, 1.0f);
        set.playTogether(transZAnim, scaleAnim);
        set.setDuration(AnimatorUtils.BASE_TIME);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public interface OnKeyDownListener {
        boolean onKey(int position, int keyCode);
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemSelectListener {
        void onItemSelect(View view, int position);

        void omItemUnSelect(View view, int position);
    }
}
