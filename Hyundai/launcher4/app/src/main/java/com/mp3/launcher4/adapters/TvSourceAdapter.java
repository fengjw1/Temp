package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.TvSourceBean;
import com.mp3.launcher4.holders.TvSourceHolder;

import java.util.List;

/**
 * @author longzj
 */
public class TvSourceAdapter extends BaseTitleRecyclerAdapter<TvSourceBean> {

    public TvSourceAdapter(Context context, List<TvSourceBean> data) {
        super(context, data);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TvSourceHolder(getInflater().inflate(R.layout.item_home_tv_source, null, false));
    }
}
