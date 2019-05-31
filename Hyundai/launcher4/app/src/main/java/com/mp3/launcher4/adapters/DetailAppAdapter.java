package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mp3.launcher4.beans.AppDetailBean;

import java.util.List;

/**
 * @author zhouxw
 */
public class DetailAppAdapter extends InstalledAdapter {

    public DetailAppAdapter(@NonNull Context context, @NonNull List<AppDetailBean> objects) {
        super(context, objects);
        setHasStableIds(false);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_NORMAL;
    }
}
