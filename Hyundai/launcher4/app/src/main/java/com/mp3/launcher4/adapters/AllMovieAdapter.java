package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.holders.CategoryOtherHolder;
import com.mp3.launcher4.holders.VideoHolder;

import java.util.List;

/**
 * @author longzj
 */
public class AllMovieAdapter extends BaseAdapter<CategoryDetailBean> {
    private static final int MOVIE =1;
    private static final int OTHER =4;

    public AllMovieAdapter(@NonNull Context context, @NonNull List<CategoryDetailBean> objects) {
        super(context, objects);
    }


    @Override
    public void updateView(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoHolder) {
            VideoHolder categoryVideoHolder = (VideoHolder) holder;
            categoryVideoHolder.bindData(getData().get(position));
        }else {
            CategoryOtherHolder categoryOtherHolder = (CategoryOtherHolder) holder;
            categoryOtherHolder.bindData(getData().get(position));
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==MOVIE){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_video, parent, false);
            return new VideoHolder(view);
        }else {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_all_other, parent, false);
            return new CategoryOtherHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if ("1".equals(getData().get(position).getType_id())){
            return MOVIE;
        }else {
            return OTHER;
        }
    }
}
