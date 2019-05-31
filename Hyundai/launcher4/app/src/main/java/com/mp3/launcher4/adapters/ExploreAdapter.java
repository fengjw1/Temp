package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.holders.HomeCardHolder;
import com.mp3.launcher4.utils.AnimatorUtils;

import java.util.List;

/**
 * @author longzj
 */
public class ExploreAdapter extends BaseAppAdapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NORMAL = 1;

    public ExploreAdapter(Context context, List<AppDetailBean> data) {
        super(context, data);
    }

    @Override
    public void updateView(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            return;
        }
        super.updateView(holder, position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = getInflater().inflate(R.layout.item_home_store_banner, null, false);
            return new HomeCardHolder(view, AnimatorUtils.BASE_BANNER_SCALE);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_NORMAL;
        }
    }
}
