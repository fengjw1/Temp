package com.mp3.launcher4.proxys;

import android.content.Context;
import android.view.View;

import com.mp3.launcher4.adapters.ExploreAdapter;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.utils.CommonUtils;

import java.util.List;

/**
 * @author longzj
 */
public class ExploreProxy extends BaseAppStoreProxy {

    public ExploreProxy(Context context, int titleId) {
        super(context, titleId);
    }

    @Override
    protected BaseAdapter<AppDetailBean> initAdapter(List<AppDetailBean> data) {
        return new ExploreAdapter(getContext(), data);
    }

    @Override
    public void onItemClicked(View view, int position) {
        super.onItemClicked(view, position);
        if (position == 0) {
            CommonUtils.startMP3Downloader(getContext());
        } else {
            getAppsUtils().startApp(getContext(),
                    getData().get(position),
                    true);
        }

    }

    @Override
    protected int getId() {
        return 1;
    }
}
