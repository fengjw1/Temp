package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.holders.HomeCardHolder;
import com.mp3.launcher4.holders.HomeVideoHolder;
import com.mp3.launcher4.utils.AnimatorUtils;

import java.util.List;

/**
 * @author longzj
 */
public class HomeVideoAdapter extends BaseTitleRecyclerAdapter<CategoryDetailBean> {

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER = 1;

    public HomeVideoAdapter(Context context, List<CategoryDetailBean> data) {
        super(context, data);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_FOOTER) {
            view = getInflater().inflate(R.layout.item_home_video_footer, null, false);
            return new HomeCardHolder(view, AnimatorUtils.BASE_RECTANGLE_SCALE);
        } else {
            view = getInflater().inflate(R.layout.item_video, null, false);
            return new HomeVideoHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == getData().size() - 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_NORMAL;
        }
    }
}
