package com.mp3.launcher4.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.holders.CategoryOtherHolder;
import com.mp3.launcher4.holders.HomeBannerHolder;
import com.mp3.launcher4.holders.HomeVideoHolder;
import com.mp3.launcher4.holders.VideoHolder;

import java.util.List;

/**
 * @author longzj
 */
public class TrendingAdapter extends BaseTitleRecyclerAdapter<CategoryDetailBean> {

    private static final int TYPE_BANNER = 0;
    private static final int TYPE_MOVIE = 1;
    private static final int TYPE_OTHER = 4;

    public TrendingAdapter(Context context, List<CategoryDetailBean> data) {
        super(context, data);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = getInflater();
        if (viewType == TYPE_BANNER) {
            view = inflater.inflate(R.layout.item_home_banner, parent, false);
            return new HomeBannerHolder(view);
        } else if (viewType ==TYPE_OTHER){
            view = inflater.inflate(R.layout.item_all_other, parent, false);
            return new CategoryOtherHolder(view);

        }else {
            view = inflater.inflate(R.layout.item_video, parent, false);
            return new HomeVideoHolder(view);
        }
    }

    @Override
    public void updateView(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoHolder) {
            VideoHolder categoryVideoHolder = (VideoHolder) holder;
            categoryVideoHolder.bindData(getData().get(position));
        }else if (holder instanceof CategoryOtherHolder){
            CategoryOtherHolder categoryOtherHolder = (CategoryOtherHolder) holder;
            categoryOtherHolder.bindData(getData().get(position));
        }else {
            HomeBannerHolder categoryVideoHolder = (HomeBannerHolder) holder;
            categoryVideoHolder.bindData(getData().get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_BANNER;
        } else {
            if ("1".equals(getData().get(position).getType_id())){
                return TYPE_MOVIE;
            }else {
                return TYPE_OTHER;
            }
        }
    }
}
