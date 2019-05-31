package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.holders.AppHolder;

import java.util.List;

/**
 * @author longzj
 */
public class AllAppAdapter extends BaseAdapter<AppDetailBean> {

    public AllAppAdapter(@NonNull Context context, @NonNull List<AppDetailBean> objects) {
        super(context, objects);
    }

    @Override
    public void updateView(RecyclerView.ViewHolder holder, int position) {
        AppHolder appHolder = (AppHolder) holder;
        appHolder.bindData(getData().get(position));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = getInflater().inflate(R.layout.item_base_app, parent, false);
        return new AppHolder(view);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof AppHolder) {
            ((AppHolder) holder).getIcon().setImageDrawable(null);
        }
    }
}
