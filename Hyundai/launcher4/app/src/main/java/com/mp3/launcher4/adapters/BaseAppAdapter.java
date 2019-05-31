package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.holders.BaseAppHolder;

import java.util.List;

/**
 * @author longzj
 */
public class BaseAppAdapter extends BaseTitleRecyclerAdapter<AppDetailBean> {


    public BaseAppAdapter(Context context, List<AppDetailBean> data) {
        super(context, data);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = getInflater().inflate(R.layout.item_base_app, null, false);
        return new BaseAppHolder(view);
    }
}
