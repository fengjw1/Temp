package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.holders.HomeCardHolder;
import com.mp3.launcher4.holders.HomeInstalledHolder;
import com.mp3.launcher4.utils.AnimatorUtils;

import java.util.List;

import static android.support.v7.widget.RecyclerView.NO_ID;

/**
 * @author longzj
 */
public class InstalledAdapter extends BaseAppAdapter {

    static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER = 1;


    private int mState;

    public InstalledAdapter(Context context, List<AppDetailBean> data) {
        super(context, data);
        setHasStableIds(false);
        mState = HomeInstalledHolder.SHADOW_STATE_LIGHT;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_FOOTER) {
            view = getInflater().inflate(R.layout.item_app_footer, null, false);
            return new HomeCardHolder(view, AnimatorUtils.BASE_SQUARE_SCALE);
        } else {
            view = getInflater().inflate(R.layout.item_home_installed, null, false);
            return new HomeInstalledHolder(view);
        }

    }

    @Override
    public void updateView(RecyclerView.ViewHolder holder, int position) {
        super.updateView(holder, position);
        if (holder instanceof HomeInstalledHolder) {
            ((HomeInstalledHolder) holder).setShadow(mState);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }


        Object object = payloads.get(0);
        if (object instanceof String[] && holder instanceof HomeInstalledHolder) {
            String[] reloadCmd = (String[]) object;
            ((HomeInstalledHolder) holder).bindData(getItemData(position));
            int state = position == Integer.valueOf(reloadCmd[1]) ? HomeInstalledHolder.SHADOW_STATE_NULL : mState;
            ((HomeInstalledHolder) holder).setShadow(state);
        }

        if (object instanceof Integer[]) {
            Integer[] values = (Integer[]) object;
            final int minSize = 2;
            if (values.length < minSize) {
                return;
            }

            int pos = values[1];
            if (pos == position) {
                return;
            }
            mState = values[0];
            if (holder instanceof HomeInstalledHolder) {
                ((HomeInstalledHolder) holder).setShadow(mState);
            }
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

    @Override
    public long getItemId(int position) {
        return NO_ID;
    }
}
