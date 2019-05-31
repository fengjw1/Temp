package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.holders.AbstractHomeHolder;

import java.util.List;

/**
 * @author longzj
 */
public abstract class BaseTitleRecyclerAdapter<T> extends BaseAdapter<T> {

    public BaseTitleRecyclerAdapter(Context context, List<T> data) {
        super(context, data);
    }

    @Override
    public void updateView(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AbstractHomeHolder) {
            ((AbstractHomeHolder) holder).bindData(getItemData(position));
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof AbstractHomeHolder) {
            ((AbstractHomeHolder) holder).bindData(getItemData(holder.getAdapterPosition()));
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof AbstractHomeHolder) {
            ((AbstractHomeHolder) holder).onDetached();
        }
    }
}
