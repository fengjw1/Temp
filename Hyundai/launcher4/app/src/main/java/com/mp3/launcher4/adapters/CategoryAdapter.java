package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.CategoryBean;
import com.mp3.launcher4.holders.CategoryHolder;

import java.util.List;

/**
 * @author longzj
 */
public class CategoryAdapter extends BaseTitleRecyclerAdapter<CategoryBean> {

    public CategoryAdapter(Context context, List<CategoryBean> data) {
        super(context, data);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = getInflater().inflate(R.layout.item_home_categories, null, false);
        return new CategoryHolder(view);
    }
}
