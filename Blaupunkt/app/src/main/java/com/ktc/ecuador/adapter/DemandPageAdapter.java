package com.ktc.ecuador.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class DemandPageAdapter extends PagerAdapter {
    private Context mContext;
    private List<View> views;

    public DemandPageAdapter(Context context, List<View> views) {
        this.mContext = context;
        this.views = views;
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view;
        if (position == 0) {
            view = views.get(0);
            container.addView(view);
        } else {
            view = views.get(1);
            container.addView(view);
        }
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
