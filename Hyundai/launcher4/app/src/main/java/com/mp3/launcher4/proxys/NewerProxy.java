package com.mp3.launcher4.proxys;

import android.content.Context;
import android.view.View;

import com.mp3.launcher4.adapters.BaseAppAdapter;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;

import java.util.List;

/**
 * @author longzj
 */
public class NewerProxy extends BaseAppStoreProxy {

    public NewerProxy(Context context, int titleId) {
        super(context, titleId);
    }

    @Override
    public boolean isGroupAbove() {
        return true;
    }

    @Override
    protected BaseAdapter<AppDetailBean> initAdapter(List<AppDetailBean> data) {
        return new BaseAppAdapter(getContext(), data);
    }

    @Override
    public void onItemClicked(View view, int position) {
        super.onItemClicked(view, position);
        getAppsUtils().startApp(getContext(),
                getData().get(position),
                true);
    }

    @Override
    protected int getId() {
        return 13;
    }
}
