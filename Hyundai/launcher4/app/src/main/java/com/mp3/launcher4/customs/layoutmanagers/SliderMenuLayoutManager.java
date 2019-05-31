package com.mp3.launcher4.customs.layoutmanagers;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.mp3.launcher4.utils.ImageUtils;

/**
 * @author longzj
 */
public class SliderMenuLayoutManager extends LinearLayoutManager {
    private Context mContext;

    public SliderMenuLayoutManager(Context context) {
        super(context, VERTICAL, false);
        this.mContext = context;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ImageUtils.dp2Px(mContext, 30));
    }
}
